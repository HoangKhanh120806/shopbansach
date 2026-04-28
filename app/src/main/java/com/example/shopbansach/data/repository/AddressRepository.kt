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
            
            // Nếu là địa chỉ mặc định, bỏ mặc định các cái khác
            if (address.isDefault) {
                val snapshot = collection.whereEqualTo("isDefault", true).get().await()
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isDefault", false)
                }
                batch.commit().await()
            }

            collection.document(address.id).set(address).await()
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
