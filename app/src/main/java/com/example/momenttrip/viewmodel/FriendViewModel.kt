package com.example.momenttrip.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.FriendRequest
import com.example.momenttrip.data.User
import com.example.momenttrip.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendViewModel: ViewModel() {
    private val _friendList = MutableStateFlow<List<User>>(emptyList())
    val friendList: StateFlow<List<User>> = _friendList

    private val _receivedRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val receivedRequests: StateFlow<List<FriendRequest>> = _receivedRequests

    private val _searchedUser = MutableStateFlow<User?>(null)
    val searchedUser: StateFlow<User?> = _searchedUser

    // 친구 찾기
    fun searchUser(query: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = FriendRepository.searchUserByKeyword(query)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _searchedUser.value = user

                if (user == null) {
                    callback(false, "사용자를 찾을 수 없습니다.")
                } else {
                    callback(true, null)
                }
            } else {
                callback(false, result.exceptionOrNull()?.message ?: "검색 실패")
            }
        }
    }

    //친구 요청 보내기
    fun sendFriendRequest(toUid: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = FriendRepository.sendFriendRequest(toUid)
            if (result.isSuccess) {
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    // 친구 요청 리스트 불러오기
    fun loadReceivedFriendRequests() {
        viewModelScope.launch {
            val result = FriendRepository.getReceivedFriendRequests()
            if (result.isSuccess) {
                _receivedRequests.value = result.getOrNull() ?: emptyList()
            } else {
                // 에러 처리 (optional)
            }
        }
    }

    //친구 요청 수락
    fun acceptFriendRequest(request: FriendRequest, callback: (Boolean, String?) -> Unit) {
        val requestId = request.request_id ?: return callback(false, "요청 ID 없음")
        viewModelScope.launch {
            val result = FriendRepository.acceptFriendRequest(requestId, request.from_uid)
            if (result.isSuccess) {
                _receivedRequests.value = _receivedRequests.value.filterNot { it.request_id == requestId }
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    //친구 요청 거절
    fun declineFriendRequest(request: FriendRequest, callback: (Boolean, String?) -> Unit) {
        val requestId = request.request_id ?: return callback(false, "요청 ID 없음")
        viewModelScope.launch {
            val result = FriendRepository.declineFriendRequest(requestId, request.from_uid)
            if (result.isSuccess) {
                _receivedRequests.value = _receivedRequests.value.filterNot { it.request_id == requestId }
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    // 친구 목록
    fun loadFriendList() {
        viewModelScope.launch {
            val result = FriendRepository.getFriendList()
            if (result.isSuccess) {
                _friendList.value = result.getOrNull() ?: emptyList()
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "친구 목록을 불러오는 중 오류가 발생했습니다."
                Log.e("FriendViewModel", "loadFriendList 실패: $errorMessage")
            }
        }
    }

    //친구 삭제
    fun removeFriend(targetUid: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = FriendRepository.removeFriend(targetUid)
            if (result.isSuccess) {
                callback(true, null)
                loadFriendList() // 리스트 갱신
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }


}