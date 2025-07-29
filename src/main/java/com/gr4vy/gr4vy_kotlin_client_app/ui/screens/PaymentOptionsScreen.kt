package com.gr4vy.gr4vy_kotlin_client_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gr4vy.gr4vy_kotlin_client_app.data.PreferencesRepository
import com.gr4vy.gr4vy_kotlin_client_app.ui.theme.Gr4vyKotlinClientAppTheme
import com.gr4vy.sdk.Gr4vy
import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.requests.Gr4vyPaymentOptionRequest
import com.gr4vy.sdk.requests.Gr4vyPaymentOptionCartItem
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class MetadataEntry(
    val id: String = UUID.randomUUID().toString(),
    var key: String = "",
    var value: String = ""
)

@Serializable
data class CartItemEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var quantity: String = "",
    var unitAmount: String = "",
    var discountAmount: String = "",
    var taxAmount: String = "",
    var externalIdentifier: String = "",
    var sku: String = "",
    var productUrl: String = "",
    var imageUrl: String = "",
    var categories: String = "",
    var productType: String = "",
    var sellerCountry: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentOptionsScreen(
    onNavigateToResponse: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val preferencesRepository = remember { PreferencesRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val json = remember { Json { ignoreUnknownKeys = true } }
    
    // Admin settings
    val merchantId by preferencesRepository.merchantId.collectAsState(initial = "")
    val gr4vyId by preferencesRepository.gr4vyId.collectAsState(initial = "")
    val apiToken by preferencesRepository.apiToken.collectAsState(initial = "")
    val serverEnvironment by preferencesRepository.serverEnvironment.collectAsState(initial = "sandbox")
    val timeout by preferencesRepository.timeout.collectAsState(initial = "")
    
    // Payment Options data
    val savedMetadataEntries by preferencesRepository.paymentOptionsMetadataEntries.collectAsState(initial = "")
    val savedCartItems by preferencesRepository.paymentOptionsCartItems.collectAsState(initial = "")
    val savedCountry by preferencesRepository.paymentOptionsCountry.collectAsState(initial = "")
    val savedCurrency by preferencesRepository.paymentOptionsCurrency.collectAsState(initial = "")
    val savedAmount by preferencesRepository.paymentOptionsAmount.collectAsState(initial = "")
    val savedLocale by preferencesRepository.paymentOptionsLocale.collectAsState(initial = "")
    
    // Local state
    var metadataEntries by remember { mutableStateOf(listOf<MetadataEntry>()) }
    var cartItems by remember { mutableStateOf(listOf<CartItemEntry>()) }
    var country by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var locale by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load data from preferences
    LaunchedEffect(savedMetadataEntries, savedCartItems, savedCountry, savedCurrency, savedAmount, savedLocale) {
        // Load metadata entries
        if (savedMetadataEntries.isNotEmpty()) {
            try {
                metadataEntries = json.decodeFromString(savedMetadataEntries)
            } catch (e: Exception) {
                metadataEntries = emptyList()
            }
        }
        
        // Load cart items
        if (savedCartItems.isNotEmpty()) {
            try {
                cartItems = json.decodeFromString(savedCartItems)
            } catch (e: Exception) {
                cartItems = emptyList()
            }
        }
        
        // Load basic fields
        country = savedCountry
        currency = savedCurrency
        amount = savedAmount
        locale = savedLocale
    }
    
    // Helper functions
    fun saveMetadataEntries() {
        coroutineScope.launch {
            val jsonString = json.encodeToString(kotlinx.serialization.serializer(), metadataEntries)
            preferencesRepository.savePaymentOptionsMetadataEntries(jsonString)
        }
    }
    
    fun saveCartItems() {
        coroutineScope.launch {
            val jsonString = json.encodeToString(kotlinx.serialization.serializer(), cartItems)
            preferencesRepository.savePaymentOptionsCartItems(jsonString)
        }
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Payment Options") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metadata Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Metadata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    metadataEntries.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = entry.key,
                                onValueChange = { newKey ->
                                    metadataEntries = metadataEntries.toMutableList().apply {
                                        this[index] = entry.copy(key = newKey)
                                    }
                                    saveMetadataEntries()
                                },
                                label = { Text("Key") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = entry.value,
                                onValueChange = { newValue ->
                                    metadataEntries = metadataEntries.toMutableList().apply {
                                        this[index] = entry.copy(value = newValue)
                                    }
                                    saveMetadataEntries()
                                },
                                label = { Text("Value") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            IconButton(
                                onClick = {
                                    metadataEntries = metadataEntries.toMutableList().apply {
                                        removeAt(index)
                                    }
                                    saveMetadataEntries()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            metadataEntries = metadataEntries + MetadataEntry()
                            saveMetadataEntries()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Add Metadata Entry")
                    }
                }
            }
            
            // Payment Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Payment Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = country,
                        onValueChange = { 
                            country = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentOptionsCountry(it)
                            }
                        },
                        label = { Text("country") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { 
                            currency = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentOptionsCurrency(it)
                            }
                        },
                        label = { Text("currency") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentOptionsAmount(it)
                            }
                        },
                        label = { Text("amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = locale,
                        onValueChange = { 
                            locale = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentOptionsLocale(it)
                            }
                        },
                        label = { Text("locale") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Cart Items Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cart Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    cartItems.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Item ${index + 1}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(
                                        onClick = {
                                            cartItems = cartItems.toMutableList().apply {
                                                removeAt(index)
                                            }
                                            saveCartItems()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                
                                // Cart item fields - all fields from iOS version
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { newName ->
                                        cartItems = cartItems.toMutableList().apply {
                                            this[index] = item.copy(name = newName)
                                        }
                                        saveCartItems()
                                    },
                                    label = { Text("name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.quantity,
                                        onValueChange = { newQuantity ->
                                            cartItems = cartItems.toMutableList().apply {
                                                this[index] = item.copy(quantity = newQuantity)
                                            }
                                            saveCartItems()
                                        },
                                        label = { Text("quantity") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    
                                    OutlinedTextField(
                                        value = item.unitAmount,
                                        onValueChange = { newAmount ->
                                            cartItems = cartItems.toMutableList().apply {
                                                this[index] = item.copy(unitAmount = newAmount)
                                            }
                                            saveCartItems()
                                        },
                                        label = { Text("unit_amount") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.discountAmount,
                                        onValueChange = { newDiscount ->
                                            cartItems = cartItems.toMutableList().apply {
                                                this[index] = item.copy(discountAmount = newDiscount)
                                            }
                                            saveCartItems()
                                        },
                                        label = { Text("discount_amount") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    
                                    OutlinedTextField(
                                        value = item.taxAmount,
                                        onValueChange = { newTax ->
                                            cartItems = cartItems.toMutableList().apply {
                                                this[index] = item.copy(taxAmount = newTax)
                                            }
                                            saveCartItems()
                                        },
                                        label = { Text("tax_amount") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = item.externalIdentifier,
                                    onValueChange = { newExternal ->
                                        cartItems = cartItems.toMutableList().apply {
                                            this[index] = item.copy(externalIdentifier = newExternal)
                                        }
                                        saveCartItems()
                                    },
                                    label = { Text("external_identifier") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = item.sku,
                                    onValueChange = { newSku ->
                                        cartItems = cartItems.toMutableList().apply {
                                            this[index] = item.copy(sku = newSku)
                                        }
                                        saveCartItems()
                                    },
                                    label = { Text("sku") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = item.productUrl,
                                    onValueChange = { newProductUrl ->
                                        cartItems = cartItems.toMutableList().apply {
                                            this[index] = item.copy(productUrl = newProductUrl)
                                        }
                                        saveCartItems()
                                    },
                                    label = { Text("product_url") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                                )
                                
                                OutlinedTextField(
                                    value = item.imageUrl,
                                    onValueChange = { newImageUrl ->
                                        cartItems = cartItems.toMutableList().apply {
                                            this[index] = item.copy(imageUrl = newImageUrl)
                                        }
                                        saveCartItems()
                                    },
                                    label = { Text("image_url") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                                )
                                
                                OutlinedTextField(
                                    value = item.categories,
                                    onValueChange = { newCategories ->
                                        cartItems = cartItems.toMutableList().apply {
                                            this[index] = item.copy(categories = newCategories)
                                        }
                                        saveCartItems()
                                    },
                                    label = { Text("categories (comma separated)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.productType,
                                        onValueChange = { newProductType ->
                                            cartItems = cartItems.toMutableList().apply {
                                                this[index] = item.copy(productType = newProductType)
                                            }
                                            saveCartItems()
                                        },
                                        label = { Text("product_type") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    
                                    OutlinedTextField(
                                        value = item.sellerCountry,
                                        onValueChange = { newSellerCountry ->
                                            cartItems = cartItems.toMutableList().apply {
                                                this[index] = item.copy(sellerCountry = newSellerCountry)
                                            }
                                            saveCartItems()
                                        },
                                        label = { Text("seller_country") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            cartItems = cartItems + CartItemEntry()
                            saveCartItems()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Add Cart Item")
                    }
                }
            }
            
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text("Sending request...")
                }
            }
            
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        sendPaymentOptionsRequest(
                            merchantId = merchantId,
                            gr4vyId = gr4vyId,
                            apiToken = apiToken,
                            serverEnvironment = serverEnvironment,
                            timeout = timeout,
                            country = country,
                            currency = currency,
                            amount = amount,
                            locale = locale,
                            metadataEntries = metadataEntries,
                            cartItems = cartItems,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onSuccess = { response ->
                                onNavigateToResponse("Payment Options Response", response)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("POST")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private suspend fun sendPaymentOptionsRequest(
    merchantId: String,
    gr4vyId: String,
    apiToken: String,
    serverEnvironment: String,
    timeout: String,
    country: String,
    currency: String,
    amount: String,
    locale: String,
    metadataEntries: List<MetadataEntry>,
    cartItems: List<CartItemEntry>,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit
) {
    onLoading(true)
    
    val trimmedGr4vyId = gr4vyId.trim()
    val trimmedToken = apiToken.trim()
    
    try {
        // Validate required fields
        if (trimmedGr4vyId.isEmpty()) {
            onError("Please configure Gr4vy ID in Admin settings")
            return
        }
        
        if (trimmedToken.isEmpty()) {
            onError("Please configure API Token in Admin settings")
            return
        }
        
        // Configure Gr4vy SDK - similar to SwiftUI version
        val server: Gr4vyServer = if (serverEnvironment == "production") Gr4vyServer.PRODUCTION else Gr4vyServer.SANDBOX
        
        val gr4vy: Gr4vy = if (timeout.trim().isNotEmpty()) {
            val timeoutValue = timeout.trim().toDoubleOrNull()
            if (timeoutValue != null && timeoutValue > 0) {
                Gr4vy(
                    gr4vyId = trimmedGr4vyId,
                    token = trimmedToken,
                    server = server,
                    timeout = timeoutValue,
                    debugMode = true
                )
            } else {
                Gr4vy(
                    gr4vyId = trimmedGr4vyId,
                    token = trimmedToken,
                    server = server,
                    debugMode = true
                )
            }
        } else {
            Gr4vy(
                gr4vyId = trimmedGr4vyId,
                token = trimmedToken,
                server = server,
                debugMode = true
            )
        }
        
        // Build metadata map - similar to SwiftUI version
        val metadata = mutableMapOf<String, String>()
        val validMetadataEntries = metadataEntries.filter { 
            it.key.trim().isNotEmpty() && it.value.trim().isNotEmpty() 
        }
        
        for (entry in validMetadataEntries) {
            metadata[entry.key.trim()] = entry.value.trim()
        }
        
        // Build cart items - similar to SwiftUI version
        val gr4vyCartItems = mutableListOf<Gr4vyPaymentOptionCartItem>()
        val validCartItems = cartItems.filter { item ->
            item.name.trim().isNotEmpty() && 
            item.quantity.trim().isNotEmpty() && 
            item.unitAmount.trim().isNotEmpty()
        }
        
        for (item in validCartItems) {
            val categories = if (item.categories.trim().isEmpty()) {
                null
            } else {
                item.categories.trim()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
            
            val cartItem = Gr4vyPaymentOptionCartItem(
                name = item.name.trim(),
                quantity = item.quantity.trim().toIntOrNull() ?: 1,
                unitAmount = item.unitAmount.trim().toIntOrNull() ?: 0,
                discountAmount = if (item.discountAmount.trim().isEmpty()) null else item.discountAmount.trim().toIntOrNull(),
                taxAmount = if (item.taxAmount.trim().isEmpty()) null else item.taxAmount.trim().toIntOrNull(),
                externalIdentifier = if (item.externalIdentifier.trim().isEmpty()) null else item.externalIdentifier.trim(),
                sku = if (item.sku.trim().isEmpty()) null else item.sku.trim(),
                productUrl = if (item.productUrl.trim().isEmpty()) null else item.productUrl.trim(),
                imageUrl = if (item.imageUrl.trim().isEmpty()) null else item.imageUrl.trim(),
                categories = categories,
                productType = if (item.productType.trim().isEmpty()) null else item.productType.trim(),
                sellerCountry = if (item.sellerCountry.trim().isEmpty()) null else item.sellerCountry.trim()
            )
            gr4vyCartItems.add(cartItem)
        }
        
        // Create payment option request - similar to SwiftUI version
        val requestBody = Gr4vyPaymentOptionRequest(
            merchantId = merchantId.trim().takeIf { it.isNotEmpty() },
            metadata = metadata,
            country = country.trim().takeIf { it.isNotEmpty() },
            currency = currency.trim().takeIf { it.isNotEmpty() },
            amount = amount.trim().takeIf { it.isNotEmpty() }?.toIntOrNull(),
            locale = locale.trim().takeIf { it.isNotEmpty() } ?: "en-GB",
            cartItems = gr4vyCartItems.takeIf { it.isNotEmpty() }
        )
        
        // Call SDK directly - similar to SwiftUI version line 472
        gr4vy.paymentOptions.list(requestBody) { result ->
            onLoading(false)
            
            when {
                result.isSuccess -> {
                    val response = result.getOrNull()
                    if (response != null) {
                        onSuccess(response.rawResponse)
                    } else {
                        onError("Empty response received")
                    }
                }
                result.isFailure -> {
                    val error = result.exceptionOrNull()
                    handleError(error, trimmedGr4vyId, onError)
                }
            }
        }
        
    } catch (e: Exception) {
        onLoading(false)
        if (e is Gr4vyError) {
            handleGr4vyError(e, trimmedGr4vyId, onError)
        } else {
            onError("Error: ${e.message}")
        }
    }
}

private fun handleError(error: Throwable?, gr4vyId: String, onError: (String) -> Unit) {
    if (error is Gr4vyError) {
        handleGr4vyError(error, gr4vyId, onError)
    } else {
        onError("Failed to get payment options: ${error?.message ?: "Unknown error"}")
    }
}

private fun handleGr4vyError(error: Gr4vyError, gr4vyId: String, onError: (String) -> Unit) {
    when (error) {
        is Gr4vyError.InvalidGr4vyId -> {
            onError("Invalid Gr4vy ID: ${error.message}")
        }
        is Gr4vyError.BadURL -> {
            onError("Bad URL: ${error.url}")
        }
        is Gr4vyError.HttpError -> {
            onError("HTTP Error ${error.statusCode}: ${error.errorMessage ?: error.message}")
        }
        is Gr4vyError.NetworkError -> {
            val errorMsg = error.exception.message ?: error.message
            when {
                errorMsg.contains("Cannot resolve host") || errorMsg.contains("Unable to resolve host") -> {
                    onError("Cannot find server. Please check your Gr4vy ID ($gr4vyId). The URL being called is: https://api.$gr4vyId.gr4vy.app/payment-options")
                }
                errorMsg.contains("timeout") -> {
                    onError("Request timed out. Please try again.")
                }
                errorMsg.contains("No address associated with hostname") -> {
                    onError("Cannot find server. Please check your Gr4vy ID ($gr4vyId)")
                }
                else -> {
                    onError("Network error: $errorMsg")
                }
            }
        }
        is Gr4vyError.DecodingError -> {
            onError("Decoding error: ${error.errorMessage}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentOptionsScreenPreview() {
    Gr4vyKotlinClientAppTheme {
        PaymentOptionsScreen(
            onNavigateToResponse = { _, _ -> },
            onBackClick = {}
        )
    }
} 