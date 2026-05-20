package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Address
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.CartRepository
import com.example.shopbansach.data.repository.FirebaseBookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class CartActionState {
    object Idle : CartActionState()
    object Loading : CartActionState()
    object Success : CartActionState()
    data class Error(val message: String) : CartActionState()
}

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val actionState: CartActionState = CartActionState.Idle,
    val errorMessage: String? = null
)

class CartViewModel(
    private val repository: CartRepository = CartRepository(),
    private val bookRepository: FirebaseBookRepository = FirebaseBookRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) loadCartItems()
            else _uiState.update { CartUiState() }
        }
    }

    fun loadCartItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rawItems = repository.getCartItems()
                if (rawItems.isEmpty()) {
                    _uiState.update { it.copy(cartItems = emptyList(), isLoading = false) }
                    return@launch
                }

                val bookMap = bookRepository.getBooksByIds(rawItems.map { it.bookId })
                    .associateBy { it.id }

                val validatedItems = rawItems.map { item ->
                    bookMap[item.bookId]?.let { book ->
                        item.copy(
                            title = book.title,
                            price = book.price,
                            imageUrl = book.imageUrl,
                            author = book.author,
                            ownerId = book.ownerId,
                            shopName = book.shopName,
                            stock = book.stock
                        )
                    } ?: item.copy(stock = 0)
                }

                _uiState.update { it.copy(cartItems = validatedItems, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addToCart(book: Book, quantity: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = CartActionState.Loading) }
            val item = CartItem(
                bookId = book.id,
                title = book.title,
                price = book.price,
                imageUrl = book.imageUrl,
                author = book.author,
                ownerId = book.ownerId,
                shopName = book.shopName,
                quantity = quantity,
                stock = book.stock
            )
            val result = repository.addToCart(item)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionState = CartActionState.Success) }
                loadCartItems()
            } else {
                _uiState.update { it.copy(actionState = CartActionState.Error("Lỗi thêm vào giỏ")) }
            }
        }
    }

    fun toggleSelectAll(isSelected: Boolean) {
        val previousState = _uiState.value.cartItems
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.map { 
                if (it.stock > 0) it.copy(isSelected = isSelected) else it.copy(isSelected = false)
            })
        }
        viewModelScope.launch {
            if (repository.toggleAllSelection(isSelected).isFailure) {
                _uiState.update { it.copy(cartItems = previousState) }
            }
        }
    }

    fun updateQuantity(bookId: String, newQuantity: Int) {
        val currentItems = _uiState.value.cartItems
        val item = currentItems.find { it.bookId == bookId } ?: return
        val finalQty = newQuantity.coerceIn(1, item.stock.coerceAtLeast(1))

        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.map { 
                if (it.bookId == bookId) it.copy(quantity = finalQty) else it 
            })
        }
        viewModelScope.launch {
            if (repository.updateQuantity(bookId, finalQty).isFailure) {
                _uiState.update { it.copy(cartItems = currentItems) }
            }
        }
    }

    fun processCheckout(
        checkoutItems: List<CartItem>,
        isBuyNow: Boolean,
        address: Address,
        paymentMethod: String,
        totalPrice: Long,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentUserId = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")

                val orderId = firestore.runTransaction { transaction ->
                    val snapshots = checkoutItems.associateWith { item ->
                        transaction.get(firestore.collection("books").document(item.bookId))
                    }

                    val finalItems = mutableListOf<CartItem>()
                    val sellerIds = mutableSetOf<String>()

                    checkoutItems.forEach { item ->
                        val snapshot = snapshots[item] ?: throw Exception("Lỗi sản phẩm")
                        if (!snapshot.exists()) throw Exception("Sản phẩm không tồn tại")

                        val stock = snapshot.getLong("stock") ?: 0L
                        if (stock < item.quantity.toLong()) throw Exception("Hết hàng")

                        val ownerId = snapshot.getString("ownerId") ?: ""
                        finalItems.add(item.copy(ownerId = ownerId, stock = stock.toInt()))
                        if (ownerId.isNotEmpty()) sellerIds.add(ownerId)
                    }

                    checkoutItems.forEach { item ->
                        val snapshot = snapshots[item]!!
                        val currentStock = snapshot.getLong("stock") ?: 0L
                        transaction.update(snapshot.reference, "stock", currentStock - item.quantity.toLong())
                    }

                    val orderRef = firestore.collection("orders").document()
                    val order = Order(
                        id = orderRef.id,
                        userId = currentUserId,
                        items = finalItems,
                        totalPrice = totalPrice,
                        shippingAddress = address,
                        paymentMethod = paymentMethod,
                        sellerIds = sellerIds.toList(),
                        createdAt = System.currentTimeMillis()
                    )
                    transaction.set(orderRef, order)

                    if (!isBuyNow) {
                        val cartColl = firestore.collection("users").document(currentUserId).collection("cart")
                        checkoutItems.forEach { transaction.delete(cartColl.document(it.bookId)) }
                    }
                    orderRef.id
                }.await()

                onComplete(orderId)
                loadCartItems()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleSelection(bookId: String, isSelected: Boolean) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.map { if (it.bookId == bookId) it.copy(isSelected = isSelected) else it })
        }
        viewModelScope.launch { repository.toggleSelection(bookId, isSelected) }
    }

    fun removeFromCart(bookId: String) {
        viewModelScope.launch { if (repository.removeFromCart(bookId).isSuccess) loadCartItems() }
    }

    fun clearCart() {
        viewModelScope.launch { if (repository.clearCart().isSuccess) _uiState.update { it.copy(cartItems = emptyList()) } }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = CartActionState.Idle, errorMessage = null) }
    }
}
