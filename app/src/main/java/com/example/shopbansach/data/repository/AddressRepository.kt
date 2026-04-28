package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AddressRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getAddressCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("addresses")
    }

    suspend fun getAddresses(): List<Address> {
        return try {
            val collection = getAddressCollection() ?: return emptyList()
            val snapshot = collection.get().await()
            snapshot.toObjects(Address::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAddress(address: Address): Result<Unit> {
        return try {
            val collection = getAddressCollection() ?: throw Exception("User not logged in")
            val batch = firestore.batch()

            // Nếu là địa chỉ mặc định, bỏ mặc định tất cả các cái khác TRONG CÙNG BATCH
            if (address.isDefault) {
                val snapshot = collection.whereEqualTo("isDefault", true).get().await()
                for (doc in snapshot.documents) {
                    if (doc.id != address.id) {
                        batch.update(doc.reference, "isDefault", false)
                    }
                }
            }

            // Thêm lệnh lưu địa chỉ mới vào batch
            val docRef = collection.document(address.id)
            batch.set(docRef, address)

            // Thực thi toàn bộ thay đổi một lần duy nhất
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAddress(addressId: String): Result<Unit> {
        return try {
            val collection = getAddressCollection() ?: throw Exception("User not logged in")
            collection.document(addressId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAddressById(addressId: String): Address? {
        return try {
            val collection = getAddressCollection() ?: return null
            val doc = collection.document(addressId).get().await()
            doc.toObject(Address::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
