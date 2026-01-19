package com.talhaatif.financeapk.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.firebase.Variables
import java.io.ByteArrayOutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    // Inisialisasi variabel
    private val db = Variables.db
    private val auth = Variables.auth
    private val storageRef = Variables.storageRef
    private val utils = Util()

    // LiveData untuk UI
    private val _profileData = MutableLiveData<Map<String, String>>()
    val profileData: LiveData<Map<String, String>> get() = _profileData

    private val _profileImageUri = MutableLiveData<Uri>()
    val profileImageUri: LiveData<Uri> get() = _profileImageUri

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    fun logout() {
        utils.saveLocalData(getApplication(), "uid", "")
        utils.saveLocalData(getApplication(), "auth", "false")
        utils.saveLocalData(getApplication(), "currency", "")
        auth.signOut()
    }

    fun getCurrency(): String? {
        return utils.getLocalData(getApplication(), "currency")
    }

    // --- BAGIAN YANG DIPERBAIKI ---
    fun fetchUserProfile() {
        // 1. Prioritaskan ambil UID dari FirebaseAuth langsung agar akurat
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: utils.getLocalData(getApplication(), "uid")

        // 2. CEK PENTING: Mencegah crash "Invalid document reference"
        if (userId.isNullOrEmpty()) {
            Log.e("ProfileViewModel", "User ID kosong, tidak bisa ambil data.")
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // A. User Lama: Ambil data seperti biasa
                    val name = document.getString("name") ?: ""
                    val currency = document.getString("currency") ?: "Rp"
                    val image = document.getString("image") ?: ""

                    // Simpan currency ke lokal agar AddTransaction tidak bingung
                    utils.saveLocalData(getApplication(), "currency", currency)

                    val profileDataMap = hashMapOf(
                        "name" to name,
                        "currency" to currency,
                        "image" to image
                    )
                    _profileData.value = profileDataMap
                } else {
                    // B. User Baru (Google): Data belum ada, MAKA BUAT DULU
                    // Ini mencegah crash karena data kosong
                    if (currentUser != null) {
                        createNewUserDefaultData(currentUser)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Gagal ambil data: ${e.message}")
                _profileData.value = emptyMap()
            }
    }

    // Fungsi Tambahan: Membuat data default untuk User Google baru
    private fun createNewUserDefaultData(user: FirebaseUser) {
        val defaultData = hashMapOf(
            "name" to (user.displayName ?: "Pengguna Baru"),
            "email" to (user.email ?: ""),
            "currency" to "Rp",  // Default mata uang
            "balance" to 0,      // PENTING: Saldo awal 0 (Mencegah error NumberFormat)
            "income" to 0,
            "expense" to 0,
            "image" to (user.photoUrl?.toString() ?: "") // Ambil foto dari Google jika ada
        )

        db.collection("users").document(user.uid)
            .set(defaultData)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "User baru berhasil dibuat di database")
                // Panggil ulang fetch untuk update UI
                fetchUserProfile()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Gagal membuat user baru: ${e.message}")
            }
    }
    // -----------------------------

    fun updateUserProfile(name: String, currency: String, bitmap: Bitmap?, imgChange: Boolean) {
        val userId = auth.currentUser?.uid

        // Cek lagi untuk keamanan
        if (userId.isNullOrEmpty()) return

        val userUpdates = hashMapOf("name" to name, "currency" to currency)

        if (imgChange && bitmap != null) {
            val imageUri = getImageUri(bitmap)
            val fileRef = storageRef.child("users/$userId")

            fileRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    fileRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userUpdates["image"] = task.result.toString()
                        saveToFirestore(userId, userUpdates)
                        utils.saveLocalData(getApplication(), "currency", currency)
                    } else {
                        _updateSuccess.value = false
                    }
                }
        } else {
            saveToFirestore(userId, userUpdates)
            utils.saveLocalData(getApplication(), "currency", currency)
        }
    }

    private fun saveToFirestore(userId: String, userUpdates: Map<String, String>) {
        db.collection("users").document(userId)
            .set(userUpdates, SetOptions.merge())
            .addOnSuccessListener { _updateSuccess.value = true }
            .addOnFailureListener { _updateSuccess.value = false }
    }

    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        // Suppress deprecation warning karena Anda masih pakai cara lama (tidak apa-apa)
        @Suppress("DEPRECATION")
        val path = MediaStore.Images.Media.insertImage(
            getApplication<Application>().contentResolver, inImage, "Title", null
        )
        return Uri.parse(path)
    }
}