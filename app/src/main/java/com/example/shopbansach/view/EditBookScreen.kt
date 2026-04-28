package com.example.shopbansach.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.viewmodel.BookActionState
import com.example.shopbansach.viewmodel.BookDetailViewModel
import com.example.shopbansach.viewmodel.BookViewModel
import com.example.shopbansach.viewmodel.factory.BookViewModelFactory
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    navController: NavController,
    bookId: String,
    detailViewModel: BookDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val editViewModel: BookViewModel = viewModel(
        factory = BookViewModelFactory(CloudinaryRepository(context))
    )
    
    val detailState by detailViewModel.uiState.collectAsState()
    val actionState by editViewModel.actionState.collectAsState()

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableStateOf(0.0) }
    
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Văn học", "Kinh tế", "Tâm lý", "Kỹ năng sống", "Thiếu nhi", "Ngoại ngữ", "Khác")

    // Lấy dữ liệu sách hiện tại
    LaunchedEffect(bookId) {
        detailViewModel.getBookDetail(bookId)
    }

    // Điền dữ liệu vào form khi tải xong
    LaunchedEffect(detailState.book) {
        detailState.book?.let { book ->
            title = book.title
            author = book.author
            price = book.price.toString()
            pages = book.pages.toString()
            stock = book.stock.toString()
            synopsis = book.synopsis
            category = book.category
            existingImageUrl = book.imageUrl
            rating = book.rating
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(actionState) {
        if (actionState is BookActionState.Success) {
            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            editViewModel.resetState()
            navController.popBackStack()
        } else if (actionState is BookActionState.Error) {
            Toast.makeText(context, (actionState as BookActionState.Error).message, Toast.LENGTH_SHORT).show()
            editViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chỉnh sửa sách", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (detailState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (!existingImageUrl.isNullOrEmpty()) {
                        AsyncImage(model = existingImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên sách") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Dropdown
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Thể loại") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Tác giả") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Giá") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Số trang") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Tồn kho") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = synopsis, onValueChange = { synopsis = it }, label = { Text("Tóm tắt") }, modifier = Modifier.fillMaxWidth().height(120.dp))

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        editViewModel.saveBook(
                            bookId = bookId,
                            title = title,
                            author = author,
                            price = price,
                            pages = pages,
                            synopsis = synopsis,
                            category = category,
                            stock = stock,
                            imageUri = selectedImageUri,
                            existingImageUrl = existingImageUrl,
                            rating = rating
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = actionState !is BookActionState.Loading
                ) {
                    if (actionState is BookActionState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu thay đổi")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
