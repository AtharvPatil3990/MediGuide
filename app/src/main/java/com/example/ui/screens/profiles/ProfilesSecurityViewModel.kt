package com.example.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserRole
import com.example.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesSecurityViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun createProfile(
        name: String,
        role: UserRole,
        age: Int,
        bloodGroup: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            userRepository.createProfile(
                name = name,
                role = role,
                age = age,
                bloodGroup = bloodGroup
            )
        }
    }
}
