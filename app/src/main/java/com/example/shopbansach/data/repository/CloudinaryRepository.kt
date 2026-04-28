package com.example.shopbansach.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryRepository(private val context: Context) {

    init {
        try {
            // Cấu hình Cloudinary Unsigned - Chỉ cần cloud_name
            val config = mapOf(
                "cloud_name" to "dl7tugwf8" // Cloud Name của bạn
            )
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // MediaManager đã được khởi tạo trước đó
        }
    }

    /**
     * Upload ảnh lên Cloudinary bằng chế độ Unsigned (không cần api_secret)
     * @param uri Uri của ảnh từ thư viện
     * @param uploadPreset Tên preset bạn tạo trên Cloudinary (phải để ở chế độ Unsigned)
     */
    suspend fun uploadImage(uri: Uri, uploadPreset: String = "accound"): Result<String> = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
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
                        continuation.resume(Result.failure(Exception(error?.description ?: "Upload failed")))
                    }
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}
