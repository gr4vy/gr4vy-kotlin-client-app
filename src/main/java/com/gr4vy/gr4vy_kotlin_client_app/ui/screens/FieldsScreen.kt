package com.gr4vy.gr4vy_kotlin_client_app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gr4vy.gr4vy_kotlin_client_app.data.PreferencesRepository
import com.gr4vy.gr4vy_kotlin_client_app.ui.theme.Gr4vyKotlinClientAppTheme
import com.gr4vy.sdk.Gr4vy
import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import com.gr4vy.sdk.requests.Gr4vyCheckoutSessionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

enum class PaymentMethodType(val value: String, val displayName: String) {
    CARD("card", "Card"),
    CLICK_TO_PAY("click_to_pay", "Click to Pay"),
    ID("id", "ID");
    
    companion object {
        fun fromValue(value: String): PaymentMethodType {
            return values().find { it.value == value } ?: CARD
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(
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
    
    // Fields screen data
    val savedCheckoutSessionId by preferencesRepository.fieldsCheckoutSessionId.collectAsState(initial = "")
    val savedPaymentMethodType by preferencesRepository.fieldsPaymentMethodType.collectAsState(initial = "")
    val savedCardNumber by preferencesRepository.fieldsCardNumber.collectAsState(initial = "")
    val savedExpirationDate by preferencesRepository.fieldsExpirationDate.collectAsState(initial = "")
    val savedSecurityCode by preferencesRepository.fieldsSecurityCode.collectAsState(initial = "")
    val savedMerchantTransactionId by preferencesRepository.fieldsMerchantTransactionId.collectAsState(initial = "")
    val savedSrcCorrelationId by preferencesRepository.fieldsSrcCorrelationId.collectAsState(initial = "")
    val savedPaymentMethodId by preferencesRepository.fieldsPaymentMethodId.collectAsState(initial = "")
    val savedIdSecurityCode by preferencesRepository.fieldsIdSecurityCode.collectAsState(initial = "")
    
    // Local state
    var checkoutSessionId by remember { mutableStateOf("") }
    var selectedPaymentMethodType by remember { mutableStateOf(PaymentMethodType.CARD) }
    var cardNumber by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var securityCode by remember { mutableStateOf("") }
    var merchantTransactionId by remember { mutableStateOf("") }
    var srcCorrelationId by remember { mutableStateOf("") }
    var paymentMethodId by remember { mutableStateOf("") }
    var idSecurityCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Update local state when preferences change
    LaunchedEffect(
        savedCheckoutSessionId, savedPaymentMethodType, savedCardNumber, savedExpirationDate, 
        savedSecurityCode, savedMerchantTransactionId, savedSrcCorrelationId, 
        savedPaymentMethodId, savedIdSecurityCode
    ) {
        checkoutSessionId = savedCheckoutSessionId
        selectedPaymentMethodType = if (savedPaymentMethodType.isNotEmpty()) {
            PaymentMethodType.fromValue(savedPaymentMethodType)
        } else {
            PaymentMethodType.CARD
        }
        cardNumber = savedCardNumber
        expirationDate = savedExpirationDate
        securityCode = savedSecurityCode
        merchantTransactionId = savedMerchantTransactionId
        srcCorrelationId = savedSrcCorrelationId
        paymentMethodId = savedPaymentMethodId
        idSecurityCode = savedIdSecurityCode
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Fields") },
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
            // Session Section
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
                        text = "Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = checkoutSessionId,
                        onValueChange = { 
                            checkoutSessionId = it
                            coroutineScope.launch {
                                preferencesRepository.saveFieldsCheckoutSessionId(it)
                            }
                        },
                        label = { Text("checkout_session_id") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Payment Method Type Section
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
                        text = "Payment Method Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Segmented control-like picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PaymentMethodType.values().forEach { type ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .selectable(
                                        selected = selectedPaymentMethodType == type,
                                        onClick = {
                                            selectedPaymentMethodType = type
                                            coroutineScope.launch {
                                                preferencesRepository.saveFieldsPaymentMethodType(type.value)
                                            }
                                        }
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedPaymentMethodType == type) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Text(
                                    text = type.displayName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (selectedPaymentMethodType == type) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedPaymentMethodType == type) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Dynamic form fields based on selected payment method type
            when (selectedPaymentMethodType) {
                PaymentMethodType.CARD -> {
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
                                value = cardNumber,
                                onValueChange = { 
                                    cardNumber = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsCardNumber(it)
                                    }
                                },
                                label = { Text("number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            OutlinedTextField(
                                value = expirationDate,
                                onValueChange = { 
                                    expirationDate = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsExpirationDate(it)
                                    }
                                },
                                label = { Text("expiration_date") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            OutlinedTextField(
                                value = securityCode,
                                onValueChange = { 
                                    securityCode = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsSecurityCode(it)
                                    }
                                },
                                label = { Text("security_code") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
                
                PaymentMethodType.CLICK_TO_PAY -> {
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
                                text = "Click to Pay Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            OutlinedTextField(
                                value = merchantTransactionId,
                                onValueChange = { 
                                    merchantTransactionId = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsMerchantTransactionId(it)
                                    }
                                },
                                label = { Text("merchant_transaction_id") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = srcCorrelationId,
                                onValueChange = { 
                                    srcCorrelationId = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsSrcCorrelationId(it)
                                    }
                                },
                                label = { Text("src_correlation_id") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
                
                PaymentMethodType.ID -> {
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
                                text = "ID Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            OutlinedTextField(
                                value = paymentMethodId,
                                onValueChange = { 
                                    paymentMethodId = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsPaymentMethodId(it)
                                    }
                                },
                                label = { Text("id") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = idSecurityCode,
                                onValueChange = { 
                                    idSecurityCode = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsIdSecurityCode(it)
                                    }
                                },
                                label = { Text("security_code") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
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
                        sendTokenizeRequest(
                            gr4vyId = gr4vyId,
                            apiToken = apiToken,
                            serverEnvironment = serverEnvironment,
                            timeout = timeout,
                            checkoutSessionId = checkoutSessionId,
                            paymentMethodType = selectedPaymentMethodType,
                            cardNumber = cardNumber,
                            expirationDate = expirationDate,
                            securityCode = securityCode,
                            merchantTransactionId = merchantTransactionId,
                            srcCorrelationId = srcCorrelationId,
                            paymentMethodId = paymentMethodId,
                            idSecurityCode = idSecurityCode,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onSuccess = { response ->
                                onNavigateToResponse("Fields Response", response)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("PUT")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private suspend fun sendTokenizeRequest(
    gr4vyId: String,
    apiToken: String,
    serverEnvironment: String,
    timeout: String,
    checkoutSessionId: String,
    paymentMethodType: PaymentMethodType,
    cardNumber: String,
    expirationDate: String,
    securityCode: String,
    merchantTransactionId: String,
    srcCorrelationId: String,
    paymentMethodId: String,
    idSecurityCode: String,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit
) {
    onLoading(true)
    
    try {
            // Validate required admin settings
            val trimmedGr4vyId = gr4vyId.trim()
            val trimmedToken = apiToken.trim()
            
            if (trimmedGr4vyId.isEmpty()) {
                onError("Please configure Gr4vy ID in Admin settings")
                return
            }
            
            if (trimmedToken.isEmpty()) {
                onError("Please configure API Token in Admin settings")
                return
            }
            
            if (checkoutSessionId.trim().isEmpty()) {
                onError("Please enter checkout_session_id")
                return
            }
            
            // Configure Gr4vy SDK
            val server = if (serverEnvironment == "production") {
                Gr4vyServer.PRODUCTION
            } else {
                Gr4vyServer.SANDBOX
            }
            
            val gr4vy = try {
                if (timeout.trim().isNotEmpty()) {
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
            } catch (e: Exception) {
                onError("Failed to configure Gr4vy SDK: ${e.message}")
                return
            }
            
            // Create the appropriate payment method data based on selection
            val paymentMethod = when (paymentMethodType) {
                PaymentMethodType.CARD -> {
                    Gr4vyPaymentMethod.Card(
                        number = cardNumber.trim(),
                        expirationDate = expirationDate.trim(),
                        securityCode = if (securityCode.trim().isNotEmpty()) securityCode.trim() else null
                    )
                }
                
                PaymentMethodType.CLICK_TO_PAY -> {
                    Gr4vyPaymentMethod.ClickToPay(
                        merchantTransactionId = merchantTransactionId.trim(),
                        srcCorrelationId = srcCorrelationId.trim()
                    )
                }
                
                PaymentMethodType.ID -> {
                    Gr4vyPaymentMethod.Id(
                        id = paymentMethodId.trim(),
                        securityCode = if (idSecurityCode.trim().isNotEmpty()) idSecurityCode.trim() else null
                    )
                }
            }
            
            val request = Gr4vyCheckoutSessionRequest(paymentMethod = paymentMethod)


                  // Call Gr4vy SDK tokenize method
            try {
                val response = gr4vy.tokenize(checkoutSessionId.trim(), request)
                // Handle empty response (204 No Content) by providing meaningful response
                val displayResponse = if (response.rawResponse.isBlank()) {
                    """{"result": "OK"}"""
                } else {
                    response.rawResponse
                }
                onSuccess(displayResponse)
            } catch (e: Exception) {
                handleGr4vyError(e, trimmedGr4vyId, onError)
            }
            
    } catch (e: Exception) {
        onError("Unexpected error: ${e.message}")
    } finally {
        onLoading(false)
    }
}

private fun handleGr4vyError(error: Exception, gr4vyId: String, onError: (String) -> Unit) {
    when (error) {
        is Gr4vyError.InvalidGr4vyId -> {
            Log.e("Gr4vy", "Invalid Gr4vy ID: ${error.message}")
            onError("Invalid Gr4vy ID: ${error.message}")
        }
        is Gr4vyError.BadURL -> {
            Log.e("Gr4vy", "Bad URL: ${error.url}")
            onError("Bad URL: ${error.url}")
        }
        is Gr4vyError.HttpError -> {
            Log.e("Gr4vy", "HTTP Error: ${error.statusCode} - ${error.errorMessage}")
            onError("HTTP Error ${error.statusCode}: ${error.errorMessage ?: error.message}")
        }
        is Gr4vyError.NetworkError -> {
            val errorMsg = error.exception.message ?: error.message
            Log.e("Gr4vy", "Network Error: $errorMsg")
            when {
                errorMsg.contains("Cannot resolve host") || errorMsg.contains("Unable to resolve host") -> {
                    onError("Cannot find server. Please check your Gr4vy ID ($gr4vyId)")
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
            Log.e("Gr4vy", "Decoding Error: ${error.errorMessage}")
            onError("Decoding error: ${error.errorMessage}")
        }
        else -> {
            Log.e("Gr4vy", "Unknown Error: ${error.message}")
            onError("Failed to tokenize payment method: ${error.message ?: "Unknown error"}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FieldsScreenPreview() {
    Gr4vyKotlinClientAppTheme {
        FieldsScreen(
            onNavigateToResponse = { _, _ -> },
            onBackClick = {}
        )
    }
}