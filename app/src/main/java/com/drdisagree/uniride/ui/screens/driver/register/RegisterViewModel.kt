package com.drdisagree.uniride.ui.screens.driver.register

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.Driver
import com.drdisagree.uniride.data.utils.Constant.DRIVER_COLLECTION
import com.drdisagree.uniride.data.utils.Constant.DRIVER_DOCUMENT_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val application: Application,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: StorageReference
) : ViewModel() {

    private val _register = MutableStateFlow<Resource<Driver>>(Resource.Unspecified())
    val register: Flow<Resource<Driver>> = _register

    fun createUserWithEmailAndPassword(
        name: String,
        email: String,
        password: String,
        documents: List<Pair<ByteArray, String>>
    ) {
        runBlocking {
            _register.emit(Resource.Loading())
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        authResult.user?.let {
                            saveUserInfo(
                                it.uid,
                                name,
                                email,
                                documents
                            )
                        }
                    }
                    ?.addOnFailureListener {
                        _register.value = Resource.Error(it.message.toString())
                    }
            }
            .addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }

    private fun saveUserInfo(
        userUid: String,
        name: String,
        email: String,
        documents: List<Pair<ByteArray, String>>
    ) {
        viewModelScope.launch {
            val userStorageRef = storage.child(DRIVER_DOCUMENT_COLLECTION).child(userUid)

            try {
                val imagesUrlsDeferred = withContext(Dispatchers.IO) {
                    documents.map { (byteArray, documentName) ->
                        async {
                            val imageStorageRef = userStorageRef.child(documentName)
                            val uploadTask = imageStorageRef.putBytes(byteArray)
                            val result = uploadTask.await()

                            result.storage.downloadUrl.await().toString()
                        }
                    }
                }

                val imagesUrls = imagesUrlsDeferred.awaitAll()

                val driver = Driver(
                    id = userUid,
                    name = name,
                    email = email,
                    documents = imagesUrls
                )

                firestore.runTransaction { transaction ->
                    val documentRef = firestore.collection(DRIVER_COLLECTION).document(userUid)

                    transaction.set(documentRef, driver)
                }.addOnSuccessListener {
                    _register.value = Resource.Success(driver)
                }.addOnFailureListener {
                    userStorageRef.delete()
                    _register.value = Resource.Error(it.message.toString())
                }
            } catch (e: Exception) {
                userStorageRef.delete().await()
                _register.value = Resource.Error(e.message.toString())
            }
        }
    }

    fun getImageByteArray(pairs: List<Pair<Uri?, String>>): List<Pair<ByteArray, String>> {
        val imagesByteArray = mutableListOf<Pair<ByteArray, String>>()

        pairs.forEach { pair ->
            pair.first?.let { uri ->
                val stream = ByteArrayOutputStream()

                val imageBitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("deprecation") MediaStore.Images.Media.getBitmap(
                        application.contentResolver,
                        uri
                    )
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(
                        application.contentResolver,
                        uri
                    )
                    ImageDecoder.decodeBitmap(source)
                }

                if (imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                    imagesByteArray.add(Pair(stream.toByteArray(), pair.second))
                }
            }
        }

        return imagesByteArray
    }
}