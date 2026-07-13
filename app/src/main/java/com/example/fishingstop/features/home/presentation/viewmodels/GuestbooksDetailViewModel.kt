package com.example.fishingstop.features.home.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newBie.new_bie.core.utils.Constants.TAG
import com.newBie.new_bie.features.profile.domain.entities.GuestbooksEntity
import com.newBie.new_bie.features.profile.domain.usecase.guestbooksUseCase.DeleteGuestbookUseCase
import com.newBie.new_bie.features.profile.domain.usecase.guestbooksUseCase.GetGuestbookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuestbooksDetailViewModel @Inject constructor(
    private val saveStateHandle: SavedStateHandle, // Hilt가 알아서 넣어준다.
    private val getGuestbookUseCase: GetGuestbookUseCase,
    private val deleteGuestbookUseCase: DeleteGuestbookUseCase
): ViewModel() {

    // 상세 페이지의 postId, guestbookId처럼 "이 값 없으면 화면을 그리는 것 자체가 불가능한 필수 고정값"을 다룰 때
    private val guestbookId: Int = checkNotNull(saveStateHandle["guestbookId"])

    // 방명록 정보
    private val _guestbooks = MutableStateFlow<GuestbooksEntity?>(null)
    val guestbooks = _guestbooks.asStateFlow()

    init {
        fetchGuestbooks()
    }

    fun fetchGuestbooks(){
        viewModelScope.launch {
            val result = getGuestbookUseCase(guestbookId = guestbookId)

            result.onSuccess {
                _guestbooks.value = it
            }.onFailure {e ->
                Log.e(TAG, "fetchGuestbooks: ${e.message}", )
                _guestbooks.value = null
            }
        }
    }
    fun deleteGuestbook(onSuccess: () -> Unit){
        viewModelScope.launch {
            val result = deleteGuestbookUseCase(guestbookId = guestbookId)

            result.onSuccess {
                fetchGuestbooks()
                onSuccess()
            }.onFailure {
                Log.e(TAG, "deleteGuestbook: ${it.message}", )
            }
        }
    }
}