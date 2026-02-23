package org.kasumi321.ushio.phitracker.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kasumi321.ushio.phitracker.domain.model.Server
import org.kasumi321.ushio.phitracker.domain.repository.PhigrosRepository
import org.kasumi321.ushio.phitracker.domain.usecase.SyncSaveUseCase
import javax.inject.Inject

data class LoginUiState(
    val token: String = "",
    val server: Server = Server.CN,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isCheckingToken: Boolean = true
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: PhigrosRepository,
    private val syncSaveUseCase: SyncSaveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingToken()
    }

    private fun checkExistingToken() {
        viewModelScope.launch {
            val saved = repository.getSessionToken()
            if (saved != null) {
                _uiState.update {
                    it.copy(
                        token = saved.first,
                        server = saved.second,
                        isLoggedIn = true,
                        isCheckingToken = false
                    )
                }
            } else {
                _uiState.update { it.copy(isCheckingToken = false) }
            }
        }
    }

    fun updateToken(token: String) {
        _uiState.update { it.copy(token = token.trim(), error = null) }
    }

    fun updateServer(server: Server) {
        _uiState.update { it.copy(server = server, error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.token.isBlank()) {
            _uiState.update { it.copy(error = "请输入 Session Token") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. 验证 Token
            val validateResult = repository.validateToken(state.token, state.server)
            if (validateResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Token 验证失败: ${validateResult.exceptionOrNull()?.message}"
                    )
                }
                return@launch
            }

            // 2. 保存 Token
            repository.saveSessionToken(state.token, state.server)

            // 3. 同步存档
            val syncResult = syncSaveUseCase(state.token, state.server)
            if (syncResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "存档同步失败: ${syncResult.exceptionOrNull()?.message}"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
