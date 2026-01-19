package com.talhaatif.financeapk.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.talhaatif.financeapk.repositories.UserRepository

class AuthViewModel : ViewModel() {
    private val repository = UserRepository()

    val authState: LiveData<Boolean> get() = repository.authState
    val errorMessage: LiveData<String> get() = repository.errorMessage

    fun signUp(
        context: Context,
        email: String,
        password: String,
        name: String,
        currency: String,
        imageUri: Uri?,
        bitmap: Bitmap?
    ) {
        repository.signUp(context, email, password, name, currency, imageUri, bitmap)
    }

    fun login(email: String, password: String, context: Context) {
        repository.login(email, password, context)
    }
}
