package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(name: String, email: String, password: String) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.registerUser(name, email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = when (exception) {
                    is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu. Vui lòng sử dụng ít nhất 6 ký tự."
                    is FirebaseAuthInvalidCredentialsException -> "Địa chỉ email không hợp lệ."
                    is FirebaseAuthUserCollisionException -> "Email này đã được đăng ký bởi một tài khoản khác."
                    else -> "Lỗi: ${exception?.localizedMessage ?: "Đăng ký thất bại"}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Vui lòng nhập Email và Mật khẩu")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginUser(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Email hoặc mật khẩu không chính xác")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
