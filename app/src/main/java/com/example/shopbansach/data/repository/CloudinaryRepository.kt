package com.example.shopbansach.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryRepository(private val context: Context) {

    companion object {
        private const val CLOUD_NAME = "dl7tugwf8"

        private const val UPLOAD_PRESET_AVATAR = "anh_avatar"
        private const val UPLOAD_PRESET_SHOP = "anh_shop"
        private const val UPLOAD_PRESET_BOOK = "anh_book"

        // Các folder nằm trong thư mục cha book_app
        const val FOLDER_AVATAR    = "book_app/avatar"   // Ảnh đại diện người dùng
        const val FOLDER_SHOP      = "book_app/shop"     // Ảnh đại diện shop
        const val FOLDER_BOOKS     = "book_app/books"    // Ảnh bìa sách
    }

    init {
        try {
            val config = mapOf("cloud_name" to CLOUD_NAME)
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // MediaManager đã được khởi tạo trước đó, bỏ qua
        }
    }

    /**
     * Upload ảnh đại diện người dùng -> folder: book_app/avatar
     */
    suspend fun uploadAvatar(uri: Uri): Result<String> =
        upload(uri, FOLDER_AVATAR, UPLOAD_PRESET_AVATAR)

    /**
     * Upload ảnh đại diện shop -> folder: book_app/shop
     */
    suspend fun uploadShopAvatar(uri: Uri): Result<String> =
        upload(uri, FOLDER_SHOP, UPLOAD_PRESET_SHOP)

    /**
     * Upload ảnh bìa sách -> folder: book_app/books
     */
    suspend fun uploadBookCover(uri: Uri): Result<String> =
        upload(uri, FOLDER_BOOKS, UPLOAD_PRESET_BOOK)

    /**
     * Upload tổng quát
     */
    suspend fun uploadImage(
        uri: Uri,
        uploadPreset: String = UPLOAD_PRESET_AVATAR,
        folder: String = FOLDER_AVATAR
    ): Result<String> = upload(uri, folder, uploadPreset)

    // ── Private helper ────────────────────────────────────────────────────────

    private suspend fun upload(
        uri: Uri,
        folder: String,
        preset: String
    ): Result<String> = suspendCancellableCoroutine { continuation ->

        MediaManager.get()
            .upload(uri)
            .unsigned(preset)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    if (continuation.isActive) {
                        continuation.resume(Result.success(url))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.failure(Exception(error?.description ?: "Upload thất bại"))
                        )
                    }
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}
