package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Address
import com.example.shopbansach.data.repository.AddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddressUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val actionSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddressViewModel(private val repository: AddressRepository = AddressRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val list = repository.getAddresses()
                _uiState.update { it.copy(addresses = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.setDefaultAddress(addressId)
            if (result.isSuccess) {
                loadAddresses()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun saveAddress(
        id: String?,
        fullName: String,
        phoneNumber: String,
        addressDetail: String,
        city: String,
        isDefault: Boolean
    ) {
        if (fullName.isBlank() || phoneNumber.isBlank() || addressDetail.isBlank() || city.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng điền đầy đủ thông tin") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val address = Address(
                id = id ?: UUID.randomUUID().toString(),
                fullName = fullName,
                phoneNumber = phoneNumber,
                addressDetail = addressDetail,
                city = city,
                isDefault = isDefault
            )
            val result = repository.saveAddress(address)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, actionSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
            loadAddresses()
        }
    }
    
    fun resetState() {
        _uiState.update { it.copy(actionSuccess = false, errorMessage = null) }
    }
}
