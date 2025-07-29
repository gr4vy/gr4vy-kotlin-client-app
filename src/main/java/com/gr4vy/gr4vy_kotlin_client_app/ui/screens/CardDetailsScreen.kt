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
import androidx.compose.material3.Switch
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
import com.gr4vy.sdk.models.Gr4vyCardDetails
import com.gr4vy.sdk.requests.Gr4vyCardDetailsRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(
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
    
    // Card Details data
    val savedCurrency by preferencesRepository.cardDetailsCurrency.collectAsState(initial = "")
    val savedAmount by preferencesRepository.cardDetailsAmount.collectAsState(initial = "")
    val savedBin by preferencesRepository.cardDetailsBin.collectAsState(initial = "")
    val savedCountry by preferencesRepository.cardDetailsCountry.collectAsState(initial = "")
    val savedIntent by preferencesRepository.cardDetailsIntent.collectAsState(initial = "")
    val savedIsSubsequentPayment by preferencesRepository.cardDetailsIsSubsequentPayment.collectAsState(initial = false)
    val savedMerchantInitiated by preferencesRepository.cardDetailsMerchantInitiated.collectAsState(initial = false)
    val savedMetadata by preferencesRepository.cardDetailsMetadata.collectAsState(initial = "")
    val savedPaymentMethodId by preferencesRepository.cardDetailsPaymentMethodId.collectAsState(initial = "")
    val savedPaymentSource by preferencesRepository.cardDetailsPaymentSource.collectAsState(initial = "")
    
    // Local state
    var currency by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var bin by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var intent by remember { mutableStateOf("") }
    var isSubsequentPayment by remember { mutableStateOf(false) }
    var merchantInitiated by remember { mutableStateOf(false) }
    var metadata by remember { mutableStateOf("") }
    var paymentMethodId by remember { mutableStateOf("") }
    var paymentSource by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Dropdown states
    var intentExpanded by remember { mutableStateOf(false) }
    var paymentSourceExpanded by remember { mutableStateOf(false) }
    
    // Dropdown options
    val intentOptions = listOf("", "authorize", "capture")
    val paymentSourceOptions = listOf("", "ecommerce", "moto", "recurring", "installment", "card_on_file")
    
    // Update local state when preferences change
    LaunchedEffect(
        savedCurrency, savedAmount, savedBin, savedCountry, savedIntent, 
        savedIsSubsequentPayment, savedMerchantInitiated, savedMetadata, 
        savedPaymentMethodId, savedPaymentSource
    ) {
        currency = savedCurrency
        amount = savedAmount
        bin = savedBin
        country = savedCountry
        intent = savedIntent
        isSubsequentPayment = savedIsSubsequentPayment
        merchantInitiated = savedMerchantInitiated
        metadata = savedMetadata
        paymentMethodId = savedPaymentMethodId
        paymentSource = savedPaymentSource
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Card Details") },
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
                        text = "Card Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { 
                            currency = it
                            coroutineScope.launch {
                                preferencesRepository.saveCardDetailsCurrency(it)
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
                                preferencesRepository.saveCardDetailsAmount(it)
                            }
                        },
                        label = { Text("amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = bin,
                        onValueChange = { newValue ->
                            // Limit to 8 characters like iOS
                            val limitedValue = if (newValue.length > 8) newValue.take(8) else newValue
                            bin = limitedValue
                            coroutineScope.launch {
                                preferencesRepository.saveCardDetailsBin(limitedValue)
                            }
                        },
                        label = { Text("bin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = country,
                        onValueChange = { newValue ->
                            // Limit to 2 characters like iOS
                            val limitedValue = if (newValue.length > 2) newValue.take(2) else newValue
                            country = limitedValue
                            coroutineScope.launch {
                                preferencesRepository.saveCardDetailsCountry(limitedValue)
                            }
                        },
                        label = { Text("country") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Intent dropdown
                    ExposedDropdownMenuBox(
                        expanded = intentExpanded,
                        onExpandedChange = { intentExpanded = !intentExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = intent,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("intent") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intentExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = intentExpanded,
                            onDismissRequest = { intentExpanded = false }
                        ) {
                            intentOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(if (option.isEmpty()) "None" else option) },
                                    onClick = {
                                        intent = option
                                        coroutineScope.launch {
                                            preferencesRepository.saveCardDetailsIntent(option)
                                        }
                                        intentExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Toggle switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "is_subsequent_payment",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isSubsequentPayment,
                            onCheckedChange = { 
                                isSubsequentPayment = it
                                coroutineScope.launch {
                                    preferencesRepository.saveCardDetailsIsSubsequentPayment(it)
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "merchant_initiated",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = merchantInitiated,
                            onCheckedChange = { 
                                merchantInitiated = it
                                coroutineScope.launch {
                                    preferencesRepository.saveCardDetailsMerchantInitiated(it)
                                }
                            }
                        )
                    }
                    
                    OutlinedTextField(
                        value = metadata,
                        onValueChange = { 
                            metadata = it
                            coroutineScope.launch {
                                preferencesRepository.saveCardDetailsMetadata(it)
                            }
                        },
                        label = { Text("metadata") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = paymentMethodId,
                        onValueChange = { 
                            paymentMethodId = it
                            coroutineScope.launch {
                                preferencesRepository.saveCardDetailsPaymentMethodId(it)
                            }
                        },
                        label = { Text("payment_method_id") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Payment Source dropdown
                    ExposedDropdownMenuBox(
                        expanded = paymentSourceExpanded,
                        onExpandedChange = { paymentSourceExpanded = !paymentSourceExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = paymentSource,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("payment_source") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentSourceExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = paymentSourceExpanded,
                            onDismissRequest = { paymentSourceExpanded = false }
                        ) {
                            paymentSourceOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(if (option.isEmpty()) "None" else option) },
                                    onClick = {
                                        paymentSource = option
                                        coroutineScope.launch {
                                            preferencesRepository.saveCardDetailsPaymentSource(option)
                                        }
                                        paymentSourceExpanded = false
                                    }
                                )
                            }
                        }
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
                        sendCardDetailsRequest(
                            gr4vyId = gr4vyId,
                            apiToken = apiToken,
                            serverEnvironment = serverEnvironment,
                            timeout = timeout,
                            currency = currency,
                            amount = amount,
                            bin = bin,
                            country = country,
                            intent = intent,
                            isSubsequentPayment = isSubsequentPayment,
                            merchantInitiated = merchantInitiated,
                            metadata = metadata,
                            paymentMethodId = paymentMethodId,
                            paymentSource = paymentSource,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onSuccess = { response ->
                                onNavigateToResponse("Card Details Response", response)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && currency.trim().isNotEmpty()
            ) {
                Text("GET")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private suspend fun sendCardDetailsRequest(
    gr4vyId: String,
    apiToken: String,
    serverEnvironment: String,
    timeout: String,
    currency: String,
    amount: String,
    bin: String,
    country: String,
    intent: String,
    isSubsequentPayment: Boolean,
    merchantInitiated: Boolean,
    metadata: String,
    paymentMethodId: String,
    paymentSource: String,
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
        
        val trimmedCurrency = currency.trim()
        if (trimmedCurrency.isEmpty()) {
            onError("Please enter a currency")
            return
        }
        
        // Configure Gr4vy SDK - similar to SwiftUI version line 171-182
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
        
        // Create card details - similar to SwiftUI version lines 193-204
        val details = Gr4vyCardDetails(
            currency = trimmedCurrency,
            amount = if (amount.trim().isEmpty()) null else amount.trim(),
            bin = if (bin.trim().isEmpty()) null else bin.trim(),
            country = if (country.trim().isEmpty()) null else country.trim(),
            intent = if (intent.isEmpty()) null else intent,
            isSubsequentPayment = if (isSubsequentPayment) true else null,
            merchantInitiated = if (merchantInitiated) true else null,
            metadata = if (metadata.trim().isEmpty()) null else metadata.trim(),
            paymentMethodId = if (paymentMethodId.trim().isEmpty()) null else paymentMethodId.trim(),
            paymentSource = if (paymentSource.isEmpty()) null else paymentSource
        )
        
        val requestBody = Gr4vyCardDetailsRequest(cardDetails = details)
        
        // Call SDK directly - similar to SwiftUI version line 208
        gr4vy.cardDetails.get(requestBody) { result ->
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
        onError("Failed to get card details: ${error?.message ?: "Unknown error"}")
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
                    onError("Cannot find server. Please check your Gr4vy ID ($gr4vyId). The URL being called is: https://api.$gr4vyId.gr4vy.app/card-details")
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
fun CardDetailsScreenPreview() {
    Gr4vyKotlinClientAppTheme {
        CardDetailsScreen(
            onNavigateToResponse = { _, _ -> },
            onBackClick = {}
        )
    }
} 