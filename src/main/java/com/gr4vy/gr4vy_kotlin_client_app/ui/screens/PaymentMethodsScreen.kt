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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gr4vy.gr4vy_kotlin_client_app.data.PreferencesRepository
import com.gr4vy.gr4vy_kotlin_client_app.ui.theme.Gr4vyKotlinClientAppTheme
import com.gr4vy.sdk.Gr4vy
import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.models.Gr4vyBuyersPaymentMethods
import com.gr4vy.sdk.models.Gr4vyOrderBy
import com.gr4vy.sdk.models.Gr4vySortBy
import com.gr4vy.sdk.requests.Gr4vyBuyersPaymentMethodsRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onNavigateToResponse: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val preferencesRepository = remember { PreferencesRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // Admin settings
    val merchantId by preferencesRepository.merchantId.collectAsState(initial = "")
    val gr4vyId by preferencesRepository.gr4vyId.collectAsState(initial = "")
    val apiToken by preferencesRepository.apiToken.collectAsState(initial = "")
    val serverEnvironment by preferencesRepository.serverEnvironment.collectAsState(initial = "sandbox")
    val timeout by preferencesRepository.timeout.collectAsState(initial = "")
    
    // Payment Methods data
    val savedBuyerId by preferencesRepository.paymentMethodsBuyerId.collectAsState(initial = "")
    val savedBuyerExternalIdentifier by preferencesRepository.paymentMethodsBuyerExternalIdentifier.collectAsState(initial = "")
    val savedSortBy by preferencesRepository.paymentMethodsSortBy.collectAsState(initial = "")
    val savedOrderBy by preferencesRepository.paymentMethodsOrderBy.collectAsState(initial = "desc")
    val savedCountry by preferencesRepository.paymentMethodsCountry.collectAsState(initial = "")
    val savedCurrency by preferencesRepository.paymentMethodsCurrency.collectAsState(initial = "")
    
    // Local state
    var buyerId by remember { mutableStateOf("") }
    var buyerExternalIdentifier by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf<Gr4vySortBy?>(null) }
    var orderBy by remember { mutableStateOf(Gr4vyOrderBy.DESC) }
    var country by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Dropdown states
    var sortByExpanded by remember { mutableStateOf(false) }
    var orderByExpanded by remember { mutableStateOf(false) }
    
    // Update local state when preferences change
    LaunchedEffect(savedBuyerId, savedBuyerExternalIdentifier, savedSortBy, savedOrderBy, savedCountry, savedCurrency) {
        buyerId = savedBuyerId
        buyerExternalIdentifier = savedBuyerExternalIdentifier
        sortBy = if (savedSortBy.isNotEmpty() && savedSortBy == "last_used_at") Gr4vySortBy.LAST_USED_AT else null
        orderBy = if (savedOrderBy == "asc") Gr4vyOrderBy.ASC else Gr4vyOrderBy.DESC
        country = savedCountry
        currency = savedCurrency
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Payment Methods") },
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
                        text = "Payment Methods",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = buyerId,
                        onValueChange = { 
                            buyerId = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentMethodsBuyerId(it)
                            }
                        },
                        label = { Text("buyer_id") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = buyerExternalIdentifier,
                        onValueChange = { 
                            buyerExternalIdentifier = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentMethodsBuyerExternalIdentifier(it)
                            }
                        },
                        label = { Text("buyer_external_identifier") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Sort By dropdown
                    ExposedDropdownMenuBox(
                        expanded = sortByExpanded,
                        onExpandedChange = { sortByExpanded = !sortByExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = when (sortBy) {
                                Gr4vySortBy.LAST_USED_AT -> "Last Used At"
                                null -> "None"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("sort_by") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortByExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = sortByExpanded,
                            onDismissRequest = { sortByExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    sortBy = null
                                    coroutineScope.launch {
                                        preferencesRepository.savePaymentMethodsSortBy("")
                                    }
                                    sortByExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Last Used At") },
                                onClick = {
                                    sortBy = Gr4vySortBy.LAST_USED_AT
                                    coroutineScope.launch {
                                        preferencesRepository.savePaymentMethodsSortBy(Gr4vySortBy.LAST_USED_AT.value)
                                    }
                                    sortByExpanded = false
                                }
                            )
                        }
                    }
                    
                    // Order By dropdown
                    ExposedDropdownMenuBox(
                        expanded = orderByExpanded,
                        onExpandedChange = { orderByExpanded = !orderByExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = when (orderBy) {
                                Gr4vyOrderBy.ASC -> "Ascending"
                                Gr4vyOrderBy.DESC -> "Descending"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("order_by") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orderByExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = orderByExpanded,
                            onDismissRequest = { orderByExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Descending") },
                                onClick = {
                                    orderBy = Gr4vyOrderBy.DESC
                                    coroutineScope.launch {
                                        preferencesRepository.savePaymentMethodsOrderBy(Gr4vyOrderBy.DESC.value)
                                    }
                                    orderByExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ascending") },
                                onClick = {
                                    orderBy = Gr4vyOrderBy.ASC
                                    coroutineScope.launch {
                                        preferencesRepository.savePaymentMethodsOrderBy(Gr4vyOrderBy.ASC.value)
                                    }
                                    orderByExpanded = false
                                }
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = country,
                        onValueChange = { 
                            country = it
                            coroutineScope.launch {
                                preferencesRepository.savePaymentMethodsCountry(it)
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
                                preferencesRepository.savePaymentMethodsCurrency(it)
                            }
                        },
                        label = { Text("currency") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
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
                        sendPaymentMethodsRequest(
                            gr4vyId = gr4vyId,
                            apiToken = apiToken,
                            serverEnvironment = serverEnvironment,
                            timeout = timeout,
                            buyerId = buyerId,
                            buyerExternalIdentifier = buyerExternalIdentifier,
                            sortBy = sortBy,
                            orderBy = orderBy,
                            country = country,
                            currency = currency,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onSuccess = { response ->
                                onNavigateToResponse("Payment Methods Response", response)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("GET")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private suspend fun sendPaymentMethodsRequest(
    gr4vyId: String,
    apiToken: String,
    serverEnvironment: String,
    timeout: String,
    buyerId: String,
    buyerExternalIdentifier: String,
    sortBy: Gr4vySortBy?,
    orderBy: Gr4vyOrderBy,
    country: String,
    currency: String,
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
                    timeout = timeoutValue
                )
            } else {
                Gr4vy(
                    gr4vyId = trimmedGr4vyId,
                    token = trimmedToken,
                    server = server
                )
            }
        } else {
            Gr4vy(
                gr4vyId = trimmedGr4vyId,
                token = trimmedToken,
                server = server
            )
        }
        
        // Create payment methods request - similar to SwiftUI version
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = if (buyerId.trim().isEmpty()) null else buyerId.trim(),
            buyerExternalIdentifier = if (buyerExternalIdentifier.trim().isEmpty()) null else buyerExternalIdentifier.trim(),
            sortBy = sortBy?.value,
            orderBy = orderBy.value,
            country = if (country.trim().isEmpty()) null else country.trim(),
            currency = if (currency.trim().isEmpty()) null else currency.trim()
        )
        
        val requestBody = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethods)
        
        // Call SDK directly - similar to SwiftUI version line 228
        gr4vy.paymentMethods.list(requestBody) { result ->
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
        onError("Failed to get payment methods: ${error?.message ?: "Unknown error"}")
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
                    onError("Cannot find server. Please check your Gr4vy ID ($gr4vyId). The URL being called is: https://api.$gr4vyId.gr4vy.app/payment-methods")
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
fun PaymentMethodsScreenPreview() {
    Gr4vyKotlinClientAppTheme {
        PaymentMethodsScreen(
            onNavigateToResponse = { _, _ -> },
            onBackClick = {}
        )
    }
} 