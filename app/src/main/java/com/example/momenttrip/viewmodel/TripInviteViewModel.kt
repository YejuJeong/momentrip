package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.TripInviteEntry
import com.example.momenttrip.repository.TripInviteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripInviteViewModel : ViewModel() {

    // 받은 초대 목록 상태
    private val _invites = MutableStateFlow<List<TripInviteEntry>>(emptyList())
    val invites: StateFlow<List<TripInviteEntry>> = _invites

    // 특정 유저가 받은 초대 목록 로드
    fun loadInvitesForUser(uid: String) {
        viewModelScope.launch {
            val result = TripInviteRepository.getInvitesForUser(uid)
            if (result.isSuccess) {
                _invites.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    // 초대 전송
    fun sendInvite(
        entry: TripInviteEntry,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripInviteRepository.sendInvite(entry)
            onResult(result.isSuccess, result.exceptionOrNull()?.message)
        }
    }

    // 초대 수락
    fun acceptInvite(
        invite: TripInviteEntry,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripInviteRepository.acceptInvite(invite)
            if (result.isSuccess) {
                loadInvitesForUser(invite.to_uid) // 목록 갱신
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    // 초대 거절
    fun declineInvite(
        inviteId: String,
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripInviteRepository.declineInvite(inviteId)
            if (result.isSuccess) {
                loadInvitesForUser(userId) // 목록 갱신
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }
}