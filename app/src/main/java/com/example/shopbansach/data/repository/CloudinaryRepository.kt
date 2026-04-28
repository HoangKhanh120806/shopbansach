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
     * Upload ảnh lên Cloudinary bằng chế độ Unsigned
     * @return Result chứa Pair(secure_url, public_id)
     */
    suspend fun uploadImage(uri: Uri, uploadPreset: String = "accound"): Result<Pair<String, String>> = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    val publicId = resultData["public_id"] as? String ?: ""
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Pair(url, publicId)))
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

    /**
     * LƯU Ý: Việc xóa ảnh trực tiếp từ Android yêu cầu API Secret và Signature (Signed request).
     * Để bảo mật, API Secret KHÔNG nên để trong code App.
     * Thông thường, việc dọn dẹp ảnh cũ sẽ được thực hiện qua một Cloud Function hoặc Backend.
     */
    suspend fun deleteImage(publicId: String): Result<Unit> {
        // Đây là nơi bạn sẽ gọi API xóa nếu có Backend hỗ trợ
        // Hiện tại chúng ta sẽ để placeholder vì lý do bảo mật đã nêu trên
        return Result.success(Unit)
    }
}
