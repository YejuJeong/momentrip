package com.example.momenttrip.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.Trip
import com.example.momenttrip.data.User
import com.example.momenttrip.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class UserViewModel:ViewModel(){
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    //현재 사용자
    fun loadCurrentUser() {
        viewModelScope.launch {
            _user.value = UserRepository.getCurrentUser()
        }
    }

    //회원가입
    fun registerUser(
        email: String,
        password: String,
        name: String,
        nickname: String,
        phone: String,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = UserRepository.registerUser(email, password, name, nickname, phone)
            if (result.isSuccess) {
                callback(true, null) // 성공
            } else {
                callback(false, result.exceptionOrNull()?.message) // 실패 + 에러 메시지 전달
            }
        }
    }

    //로그인
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = UserRepository.login(email, password)
            if (result.isSuccess) {
                callback(true, null) // 성공
            } else {
                callback(false, result.exceptionOrNull()?.message) // 실패 + 에러 메시지 전달
            }
        }
    }

    //로그인 유지
    fun isLoggedIn(): Boolean {
        return UserRepository.isLoggedIn()
    }

    //로그아웃
    fun logout() {
        UserRepository.logout()
        _user.value = null
    }

    // 이메일 중복 확인
    fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = UserRepository.isEmailTaken(email)
            callback(exists) //중복
        }
    }

    // 닉네임 중복 확인: 회원가입 시
    fun checkNicknameExistsForSignup(nickname: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = UserRepository.isNicknameTakenForSignup(nickname)
            callback(exists)
        }
    }

    // 닉네임 중복 확인: 프로필 수정 시
    fun checkNicknameExistsForUpdate(nickname: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = UserRepository.isNicknameTakenForUpdate(nickname)
            callback(exists)
        }
    }

    //전화번호 중복 확인
    fun checkPhoneExists(phone: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = UserRepository.isPhoneNumberTaken(phone)
            callback(exists)
        }
    }

    //닉네임 변경
    fun updateNickname(nickname: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = UserRepository.updateNickname(nickname)
            if (result.isSuccess) {
                _user.value = _user.value?.copy(nickname = nickname)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    // 프로필 이미지 업로드(이전 이미지 삭제 -> 새 이미지 업로드 -> URL 얻기 -> URL DB에 저장)
    fun replaceProfileImage(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return callback(false, "로그인 필요")

        viewModelScope.launch {
            // 1. 이전 이미지 삭제
            UserRepository.deleteOldProfileImage(uid)

            // 2. 새 이미지 업로드
            val uploadResult = UserRepository.uploadProfileImageFile(uid, imageUri)

            if (uploadResult.isSuccess) {
                val url = uploadResult.getOrNull()

                if (!url.isNullOrBlank()) {
                    // 3. URL을 Firestore에 저장
                    val saveResult = UserRepository.updateProfileImage(url)

                    if (saveResult.isSuccess) {
                        // 4. ViewModel 내 상태도 업데이트하여 UI에 바로 반영
                        _user.value = _user.value?.copy(profile_url = url)

                        callback(true, null)
                    } else {
                        callback(false, saveResult.exceptionOrNull()?.message)
                    }
                } else {
                    callback(false, "URL 생성 실패")
                }
            } else {
                callback(false, uploadResult.exceptionOrNull()?.message)
            }
        }
    }
    
    //현재 여행 업데이트(여행 시작날짜와 끝나는 날짜 중간에 있으면 trip_id가 current_trip_id로)
    fun updateCurrentTripIdBasedOnToday(trips: List<Trip>) {
        val today = LocalDate.now()

        val currentTrip = trips.find { trip ->
            val start = trip.start_date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val end = trip.end_date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            today in start..end
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            val tripId = currentTrip?.trip_id
            FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .update("current_trip_id", tripId) // tripId가 null이면 제거됨
        }
    }

    fun fetchCurrentTripStatus(onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onResult(false)

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val currentTripId = doc.getString("current_trip_id")
                onResult(!currentTripId.isNullOrBlank())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

}