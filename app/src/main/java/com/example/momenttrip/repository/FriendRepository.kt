package com.example.momenttrip.repository

import com.example.momenttrip.data.FriendRequest
import com.example.momenttrip.data.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FriendRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getMyUidOrFail(): String {
        return auth.currentUser?.uid
            ?: throw Exception("로그인이 필요합니다.")
    }

    // 요청할 친구 uid 찾기
    suspend fun searchUserByKeyword(query: String): Result<User?> {
        val myUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .whereArrayContains("search_key", query)
                .get()
                .await()

            val user = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                .firstOrNull { it.uid != myUid }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //친구 요청 보내기
    suspend fun sendFriendRequest(toUid: String): Result<Unit> {
        val fromUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        if (fromUid == toUid) {
            return Result.failure(Exception("자기 자신에게는 친구 요청을 보낼 수 없습니다."))
        }

        try {
            // 1. 중복 요청 방지: 이미 요청이 존재하는지 확인
            val existing = db.collection("friend_requests")
                .whereEqualTo("from_uid", fromUid)
                .whereEqualTo("to_uid", toUid)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("이미 친구 요청을 보냈습니다."))
            }

            // 2. 요청 객체 생성
            val request = FriendRequest(
                from_uid = fromUid,
                to_uid = toUid,
                created_at = Timestamp.now()
            )

            // 3. friend_requests 컬렉션에 추가
            db.collection("friend_requests").add(request).await()

            // 4. 상대방 유저의 friend_request_ids 배열에 내 uid 추가
            db.collection("users")
                .document(toUid)
                .update("friend_request_ids", FieldValue.arrayUnion(fromUid))
                .await()

            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // 친구 요청 리스트 불러오기
    suspend fun getReceivedFriendRequests(): Result<List<FriendRequest>> {
        val myUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val snapshots = FirebaseFirestore.getInstance()
                .collection("friend_requests")
                .whereEqualTo("to_uid", myUid)
                .get()
                .await()

            val requests = snapshots.documents.mapNotNull { doc ->
                doc.toObject(FriendRequest::class.java)?.copy(request_id = doc.id)
            }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //친구 요청 수락
    suspend fun acceptFriendRequest(requestId: String, fromUid: String): Result<Unit> {
        val myUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val db = FirebaseFirestore.getInstance()

            // 1. 내 friends 필드에 상대 UID 추가
            db.collection("users").document(myUid)
                .update("friends", FieldValue.arrayUnion(fromUid)).await()

            // 2. 상대의 friends 필드에 내 UID 추가
            db.collection("users").document(fromUid)
                .update("friends", FieldValue.arrayUnion(myUid)).await()

            // 3. 내 friend_request_ids에서 해당 UID 제거
            db.collection("users").document(myUid)
                .update("friend_request_ids", FieldValue.arrayRemove(fromUid)).await()

            // 4. 요청 문서 삭제
            db.collection("friend_requests").document(requestId)
                .delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //친구 요청 거절
    suspend fun declineFriendRequest(requestId: String, fromUid: String): Result<Unit> {
        val myUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val db = FirebaseFirestore.getInstance()

            // 1. 내 요청 목록(friend_request_ids)에서 해당 UID 제거
            db.collection("users").document(myUid)
                .update("friend_request_ids", FieldValue.arrayRemove(fromUid)).await()

            // 2. 요청 문서 삭제
            db.collection("friend_requests").document(requestId)
                .delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //친구 목록
    suspend fun getFriendList(): Result<List<User>> {
        val myUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val db = FirebaseFirestore.getInstance()

            // 1. 내 user 문서에서 friends 리스트 가져오기
            val snapshot = db.collection("users").document(myUid).get().await()
            val user = snapshot.toObject(User::class.java)
            val friendUids = user?.friends ?: emptyList()

            // 2. 각 UID로 사용자 정보 가져오기
            val friendUsers = friendUids.mapNotNull { uid ->
                val friendSnap = db.collection("users").document(uid).get().await()
                friendSnap.toObject(User::class.java)
            }

            Result.success(friendUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //친구 삭제
    suspend fun removeFriend(targetUid: String): Result<Unit> {
        val myUid = try { getMyUidOrFail() } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val db = FirebaseFirestore.getInstance()

            // 1. 내 friends에서 target 제거
            db.collection("users").document(myUid)
                .update("friends", FieldValue.arrayRemove(targetUid)).await()

            // 2. 상대의 friends에서 내 UID 제거
            db.collection("users").document(targetUid)
                .update("friends", FieldValue.arrayRemove(myUid)).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}