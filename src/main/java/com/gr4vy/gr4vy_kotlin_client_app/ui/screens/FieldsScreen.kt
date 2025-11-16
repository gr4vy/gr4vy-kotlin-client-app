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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
    ID("id", "ID");
    
    companion object {
        fun fromValue(value: String): PaymentMethodType {
            return values().find { it.value == value } ?: CARD
        }
    }
}

enum class ThemeOption(val rawValue: String, val displayName: String) {
    NONE("none", "No Theme"),
    RED_BLUE("redBlue", "Red / Blue"),
    ORANGE_PURPLE("orangePurple", "Orange / Purple"),
    GREEN_YELLOW("greenYellow", "Green / Yellow");
    
    companion object {
        fun fromRawValue(value: String): ThemeOption {
            return values().find { it.rawValue == value } ?: NONE
        }
    }
}

enum class TestCard(
    val rawValue: String,
    val displayName: String,
    val cardNumber: String,
    val expirationDate: String,
    val cvv: String
) {
    CUSTOM("custom", "Custom", "", "", ""),
    
    // Frictionless (AUTHENTICATED_APPLICATION_FRICTIONLESS)
    VISA_FRICTIONLESS("visaFrictionless", "Visa Frictionless Test Card", "4556557955726624", "01/30", "123"),
    MASTERCARD_FRICTIONLESS("mastercardFrictionless", "Mastercard Frictionless Test Card", "5333259155643223", "01/30", "123"),
    AMEX_FRICTIONLESS("amexFrictionless", "Amex Frictionless Test Card", "341502098634895", "01/30", "123"),
    DINERS_FRICTIONLESS("dinersFrictionless", "Diners Frictionless Test Card", "36000000000008", "01/30", "123"),
    JCB_FRICTIONLESS("jcbFrictionless", "JCB Frictionless Test Card", "3528000000000056", "01/30", "123"),
    
    // Challenge (APPLICATION_CHALLENGE)
    VISA_CHALLENGE("visaChallenge", "Visa Challenge Test Card", "4024007189449340", "01/30", "456"),
    MASTERCARD_CHALLENGE("mastercardChallenge", "Mastercard Challenge Test Card", "5267648608924299", "01/30", "456"),
    AMEX_CHALLENGE("amexChallenge", "Amex Challenge Test Card", "349531373081938", "01/30", "456"),
    DINERS_CHALLENGE("dinersChallenge", "Diners Challenge Test Card", "36000002000048", "01/30", "456"),
    JCB_CHALLENGE("jcbChallenge", "JCB Challenge Test Card", "3528000000000148", "01/30", "456");
    
    companion object {
        fun fromRawValue(value: String): TestCard {
            return values().find { it.rawValue == value } ?: CUSTOM
        }
    }
}

// Theme Builders
private fun buildRedBlueTheme(): com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap {
    val light = com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomization(
        label = com.gr4vy.sdk.models.Gr4vyThreeDSLabelCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#1c1c1e",
            headingTextFontName = "sans-serif-medium",
            headingTextFontSize = 24,
            headingTextColorHex = "#0a0a0a"
        ),
        toolbar = com.gr4vy.sdk.models.Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 17,
            textColorHex = "#ffffff",
            backgroundColorHex = "#007aff",
            headerText = "Secure Checkout",
            buttonText = "Cancel"
        ),
        textBox = com.gr4vy.sdk.models.Gr4vyThreeDSTextBoxCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#000000",
            borderWidth = 2,
            borderColorHex = "#e4e4e4",
            cornerRadius = 12
        ),
        view = com.gr4vy.sdk.models.Gr4vyThreeDSViewCustomization(
            challengeViewBackgroundColorHex = "#ffffff",
            progressViewBackgroundColorHex = "#ffffff"
        ),
        buttons = mapOf(
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.SUBMIT to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontName = "sans-serif-medium",
                textFontSize = 16,
                textColorHex = "#ffffff",
                backgroundColorHex = "#ff3b30",
                cornerRadius = 18
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CONTINUE to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#ffffff",
                backgroundColorHex = "#007aff",
                cornerRadius = 14
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CANCEL to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 15,
                textColorHex = "#007aff",
                backgroundColorHex = "#e5e5ea",
                cornerRadius = 12
            )
        )
    )
    
    val dark = com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomization(
        label = com.gr4vy.sdk.models.Gr4vyThreeDSLabelCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#ffffff",
            headingTextFontName = "sans-serif-medium",
            headingTextFontSize = 24,
            headingTextColorHex = "#ffffff"
        ),
        toolbar = com.gr4vy.sdk.models.Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 17,
            textColorHex = "#ffffff",
            backgroundColorHex = "#0a84ff",
            headerText = "SECURE CHECKOUT",
            buttonText = "Close"
        ),
        textBox = com.gr4vy.sdk.models.Gr4vyThreeDSTextBoxCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#ffffff",
            borderWidth = 2,
            borderColorHex = "#48484a",
            cornerRadius = 12
        ),
        view = com.gr4vy.sdk.models.Gr4vyThreeDSViewCustomization(
            challengeViewBackgroundColorHex = "#000000",
            progressViewBackgroundColorHex = "#1c1c1e"
        ),
        buttons = mapOf(
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.SUBMIT to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontName = "sans-serif-medium",
                textFontSize = 16,
                textColorHex = "#ffffff",
                backgroundColorHex = "#ff453a",
                cornerRadius = 18
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CONTINUE to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#ffffff",
                backgroundColorHex = "#0a84ff",
                cornerRadius = 14
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CANCEL to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 15,
                textColorHex = "#0a84ff",
                backgroundColorHex = "#2c2c2e",
                cornerRadius = 12
            )
        )
    )
    
    return com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap(default = light, dark = dark)
}

private fun buildOrangePurpleTheme(): com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap {
    val light = com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomization(
        label = com.gr4vy.sdk.models.Gr4vyThreeDSLabelCustomization(
            textFontName = "serif",
            textFontSize = 18,
            textColorHex = "#1c1c1e",
            headingTextFontName = "sans-serif-black",
            headingTextFontSize = 26,
            headingTextColorHex = "#af52de"
        ),
        toolbar = com.gr4vy.sdk.models.Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-black",
            textFontSize = 18,
            textColorHex = "#ffffff",
            backgroundColorHex = "#af52de",
            headerText = "Secure Checkout",
            buttonText = "Cancel"
        ),
        textBox = com.gr4vy.sdk.models.Gr4vyThreeDSTextBoxCustomization(
            textFontName = "serif",
            textFontSize = 16,
            textColorHex = "#000000",
            borderWidth = 3,
            borderColorHex = "#ff9500",
            cornerRadius = 8
        ),
        view = com.gr4vy.sdk.models.Gr4vyThreeDSViewCustomization(
            challengeViewBackgroundColorHex = "#ffffff",
            progressViewBackgroundColorHex = "#f9f9f9"
        ),
        buttons = mapOf(
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.SUBMIT to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontName = "sans-serif-black",
                textFontSize = 18,
                textColorHex = "#ffffff",
                backgroundColorHex = "#ff9500",
                cornerRadius = 24
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CONTINUE to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#ffffff",
                backgroundColorHex = "#af52de",
                cornerRadius = 20
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CANCEL to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#af52de",
                backgroundColorHex = "#e5e5ea",
                cornerRadius = 16
            )
        )
    )
    
    val dark = com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomization(
        label = com.gr4vy.sdk.models.Gr4vyThreeDSLabelCustomization(
            textFontName = "serif",
            textFontSize = 18,
            textColorHex = "#ffffff",
            headingTextFontName = "sans-serif-black",
            headingTextFontSize = 26,
            headingTextColorHex = "#bf5af2"
        ),
        toolbar = com.gr4vy.sdk.models.Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-black",
            textFontSize = 18,
            textColorHex = "#ffffff",
            backgroundColorHex = "#bf5af2",
            headerText = "SECURE CHECKOUT",
            buttonText = "Close"
        ),
        textBox = com.gr4vy.sdk.models.Gr4vyThreeDSTextBoxCustomization(
            textFontName = "serif",
            textFontSize = 16,
            textColorHex = "#ffffff",
            borderWidth = 3,
            borderColorHex = "#ff9f0a",
            cornerRadius = 8
        ),
        view = com.gr4vy.sdk.models.Gr4vyThreeDSViewCustomization(
            challengeViewBackgroundColorHex = "#000000",
            progressViewBackgroundColorHex = "#1c1c1e"
        ),
        buttons = mapOf(
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.SUBMIT to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontName = "sans-serif-black",
                textFontSize = 18,
                textColorHex = "#000000",
                backgroundColorHex = "#ff9f0a",
                cornerRadius = 24
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CONTINUE to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#ffffff",
                backgroundColorHex = "#bf5af2",
                cornerRadius = 20
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CANCEL to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#bf5af2",
                backgroundColorHex = "#2c2c2e",
                cornerRadius = 16
            )
        )
    )
    
    return com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap(default = light, dark = dark)
}

private fun buildGreenYellowTheme(): com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap {
    val light = com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomization(
        label = com.gr4vy.sdk.models.Gr4vyThreeDSLabelCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 17,
            textColorHex = "#000000",
            headingTextFontName = "sans-serif-medium",
            headingTextFontSize = 24,
            headingTextColorHex = "#000000"
        ),
        toolbar = com.gr4vy.sdk.models.Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 18,
            textColorHex = "#000000",
            backgroundColorHex = "#ffcc00",
            headerText = "Secure Checkout",
            buttonText = "Cancel"
        ),
        textBox = com.gr4vy.sdk.models.Gr4vyThreeDSTextBoxCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#000000",
            borderWidth = 2,
            borderColorHex = "#34c759",
            cornerRadius = 8
        ),
        view = com.gr4vy.sdk.models.Gr4vyThreeDSViewCustomization(
            challengeViewBackgroundColorHex = "#ffffff",
            progressViewBackgroundColorHex = "#fffacd"
        ),
        buttons = mapOf(
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.SUBMIT to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontName = "sans-serif-medium",
                textFontSize = 17,
                textColorHex = "#ffffff",
                backgroundColorHex = "#34c759",
                cornerRadius = 16
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CONTINUE to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#000000",
                backgroundColorHex = "#ffcc00",
                cornerRadius = 14
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CANCEL to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 15,
                textColorHex = "#000000",
                backgroundColorHex = "#f0f0f0",
                cornerRadius = 12
            )
        )
    )
    
    val dark = com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomization(
        label = com.gr4vy.sdk.models.Gr4vyThreeDSLabelCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 17,
            textColorHex = "#ffffff",
            headingTextFontName = "sans-serif-medium",
            headingTextFontSize = 24,
            headingTextColorHex = "#ffffff"
        ),
        toolbar = com.gr4vy.sdk.models.Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 18,
            textColorHex = "#000000",
            backgroundColorHex = "#ffd60a",
            headerText = "SECURE CHECKOUT",
            buttonText = "Close"
        ),
        textBox = com.gr4vy.sdk.models.Gr4vyThreeDSTextBoxCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#ffffff",
            borderWidth = 2,
            borderColorHex = "#30d158",
            cornerRadius = 8
        ),
        view = com.gr4vy.sdk.models.Gr4vyThreeDSViewCustomization(
            challengeViewBackgroundColorHex = "#000000",
            progressViewBackgroundColorHex = "#1c1c1e"
        ),
        buttons = mapOf(
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.SUBMIT to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontName = "sans-serif-medium",
                textFontSize = 17,
                textColorHex = "#000000",
                backgroundColorHex = "#30d158",
                cornerRadius = 16
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CONTINUE to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 16,
                textColorHex = "#000000",
                backgroundColorHex = "#ffd60a",
                cornerRadius = 14
            ),
            com.gr4vy.sdk.models.Gr4vyThreeDSButtonType.CANCEL to com.gr4vy.sdk.models.Gr4vyThreeDSButtonCustomization(
                textFontSize = 15,
                textColorHex = "#ffd60a",
                backgroundColorHex = "#3a3a3c",
                cornerRadius = 12
            )
        )
    )
    
    return com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap(default = light, dark = dark)
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
    val savedPaymentMethodId by preferencesRepository.fieldsPaymentMethodId.collectAsState(initial = "")
    val savedIdSecurityCode by preferencesRepository.fieldsIdSecurityCode.collectAsState(initial = "")
    
    // 3DS Settings
    val savedAuthenticate by preferencesRepository.fieldsAuthenticate.collectAsState(initial = true)
    val savedTestCard by preferencesRepository.fieldsTestCard.collectAsState(initial = "custom")
    val savedTheme by preferencesRepository.fieldsTheme.collectAsState(initial = "none")
    val savedSdkMaxTimeout by preferencesRepository.fieldsSdkMaxTimeout.collectAsState(initial = "5")
    
    // Local state
    var checkoutSessionId by remember { mutableStateOf("") }
    var selectedPaymentMethodType by remember { mutableStateOf(PaymentMethodType.CARD) }
    var cardNumber by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var securityCode by remember { mutableStateOf("") }
    var paymentMethodId by remember { mutableStateOf("") }
    var idSecurityCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // 3DS state
    var authenticate by remember { mutableStateOf(true) }
    var selectedTestCard by remember { mutableStateOf(TestCard.CUSTOM) }
    var selectedTheme by remember { mutableStateOf(ThemeOption.NONE) }
    var sdkMaxTimeout by remember { mutableStateOf("5") }
    
    // Update local state when preferences change
    LaunchedEffect(
        savedCheckoutSessionId, savedPaymentMethodType, savedCardNumber, savedExpirationDate, 
        savedSecurityCode, savedPaymentMethodId, savedIdSecurityCode,
        savedAuthenticate, savedTestCard, savedTheme, savedSdkMaxTimeout
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
        paymentMethodId = savedPaymentMethodId
        idSecurityCode = savedIdSecurityCode
        authenticate = savedAuthenticate
        selectedTestCard = TestCard.fromRawValue(savedTestCard)
        selectedTheme = ThemeOption.fromRawValue(savedTheme)
        sdkMaxTimeout = savedSdkMaxTimeout
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
            
            // 3DS Theme Section
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
                        text = "3DS Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    var themeExpanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = !themeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTheme.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Theme") },
                            trailingIcon = { 
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = themeExpanded
                                )
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false }
                        ) {
                            ThemeOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        selectedTheme = option
                                        coroutineScope.launch {
                                            preferencesRepository.saveFieldsTheme(option.rawValue)
                                        }
                                        themeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // SDK Settings Section
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
                        text = "SDK Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = sdkMaxTimeout,
                        onValueChange = { value ->
                            // Only allow numeric characters, max 2 digits (5-99 minutes)
                            val filtered = value.filter { it.isDigit() }.take(2)
                            sdkMaxTimeout = filtered
                            coroutineScope.launch {
                                preferencesRepository.saveFieldsSdkMaxTimeout(filtered)
                            }
                        },
                        label = { Text("SDK Max Timeout (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
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
            
            // Authentication Toggle (Card only)
            if (selectedPaymentMethodType == PaymentMethodType.CARD) {
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
                            text = "Authentication",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Authenticate")
                            Switch(
                                checked = authenticate,
                                onCheckedChange = { 
                                    authenticate = it
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsAuthenticate(it)
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Test Cards Section
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
                            text = "Test Cards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        var testCardExpanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = testCardExpanded,
                            onExpandedChange = { testCardExpanded = !testCardExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTestCard.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Test Card") },
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = testCardExpanded
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = testCardExpanded,
                                onDismissRequest = { testCardExpanded = false }
                            ) {
                                TestCard.values().forEach { testCard ->
                                    DropdownMenuItem(
                                        text = { Text(testCard.displayName) },
                                        onClick = {
                                            selectedTestCard = testCard
                                            
                                            // Populate test card data and save to preferences
                                            if (testCard != TestCard.CUSTOM) {
                                                cardNumber = testCard.cardNumber
                                                expirationDate = testCard.expirationDate
                                                securityCode = testCard.cvv
                                                
                                                coroutineScope.launch {
                                                    preferencesRepository.saveFieldsTestCard(testCard.rawValue)
                                                    preferencesRepository.saveFieldsCardNumber(testCard.cardNumber)
                                                    preferencesRepository.saveFieldsExpirationDate(testCard.expirationDate)
                                                    preferencesRepository.saveFieldsSecurityCode(testCard.cvv)
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    preferencesRepository.saveFieldsTestCard(testCard.rawValue)
                                                }
                                            }
                                            
                                            testCardExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Clear Form Button (only show if not custom)
                        if (selectedTestCard != TestCard.CUSTOM) {
                            OutlinedButton(
                                onClick = {
                                    selectedTestCard = TestCard.CUSTOM
                                    cardNumber = ""
                                    expirationDate = ""
                                    securityCode = ""
                                    coroutineScope.launch {
                                        preferencesRepository.saveFieldsTestCard(TestCard.CUSTOM.rawValue)
                                        preferencesRepository.saveFieldsCardNumber("")
                                        preferencesRepository.saveFieldsExpirationDate("")
                                        preferencesRepository.saveFieldsSecurityCode("")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear Form")
                            }
                        }
                    }
                }
                
                // Card Details Section (after test cards)
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
            
            // ID Details Section (for ID payment method type)
            if (selectedPaymentMethodType == PaymentMethodType.ID) {
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
            
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Get the Activity from context
                        val activity = context as? android.app.Activity
                        if (activity == null) {
                            // Show error in full-page screen matching iOS behavior
                            val errorJson = """
                                {
                                  "error": "UI Context Error",
                                  "description": "Unable to get Activity context for 3DS authentication"
                                }
                            """.trimIndent()
                            onNavigateToResponse("Error", errorJson)
                            return@launch
                        }
                        
                        sendTokenizeRequest(
                            activity = activity,
                            gr4vyId = gr4vyId,
                            apiToken = apiToken,
                            serverEnvironment = serverEnvironment,
                            timeout = timeout,
                            checkoutSessionId = checkoutSessionId,
                            paymentMethodType = selectedPaymentMethodType,
                            cardNumber = cardNumber,
                            expirationDate = expirationDate,
                            securityCode = securityCode,
                            paymentMethodId = paymentMethodId,
                            idSecurityCode = idSecurityCode,
                            authenticate = authenticate,
                            selectedTheme = selectedTheme,
                            sdkMaxTimeout = sdkMaxTimeout,
                            onLoading = { isLoading = it },
                            onError = { title, errorJson ->
                                // Navigate to full-page error response screen
                                onNavigateToResponse(title, errorJson)
                            },
                            onSuccess = { response ->
                                onNavigateToResponse("Complete", response)
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
    activity: android.app.Activity,
    gr4vyId: String,
    apiToken: String,
    serverEnvironment: String,
    timeout: String,
    checkoutSessionId: String,
    paymentMethodType: PaymentMethodType,
    cardNumber: String,
    expirationDate: String,
    securityCode: String,
    paymentMethodId: String,
    idSecurityCode: String,
    authenticate: Boolean,
    selectedTheme: ThemeOption,
    sdkMaxTimeout: String,
    onLoading: (Boolean) -> Unit,
    onError: (String, String) -> Unit,
    onSuccess: (String) -> Unit
) {
    onLoading(true)
    
    try {
            // Validate required admin settings
            val trimmedGr4vyId = gr4vyId.trim()
            val trimmedToken = apiToken.trim()
            
            if (trimmedGr4vyId.isEmpty()) {
                showErrorResponse("Configuration Error", "Please configure Gr4vy ID in Admin settings", onError)
                return
            }
            
            if (trimmedToken.isEmpty()) {
                showErrorResponse("Configuration Error", "Please configure API Token in Admin settings", onError)
                return
            }
            
            if (checkoutSessionId.trim().isEmpty()) {
                showErrorResponse("Validation Error", "Please enter checkout_session_id", onError)
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
                showErrorResponse("SDK Configuration Error", "Failed to configure Gr4vy SDK: ${e.message}", onError)
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
                // For Card payments with authenticate enabled, use 3DS flow
                if (paymentMethodType == PaymentMethodType.CARD && authenticate) {
                    // Get UI customization based on theme selection
                    val uiCustomization = when (selectedTheme) {
                        ThemeOption.NONE -> null
                        ThemeOption.RED_BLUE -> buildRedBlueTheme()
                        ThemeOption.ORANGE_PURPLE -> buildOrangePurpleTheme()
                        ThemeOption.GREEN_YELLOW -> buildGreenYellowTheme()
                    }
                    
                    // Parse SDK timeout in minutes (user inputs minutes directly)
                    val timeoutMinutes = try {
                        val minutes = sdkMaxTimeout.trim().toIntOrNull() ?: 5
                        minutes.coerceIn(5, 99) // SDK expects 5-99 minutes
                    } catch (e: Exception) {
                        5 // Default to 5 minutes
                    }
                    
                    // Call 3DS tokenize method
                    val result = gr4vy.tokenize(
                        checkoutSessionId = checkoutSessionId.trim(),
                        cardData = request,
                        activity = activity,
                        sdkMaxTimeoutMinutes = timeoutMinutes,
                        authenticate = authenticate,
                        uiCustomization = uiCustomization
                    )
                    
                    // Build response with authentication details
                    val responseJson = buildString {
                        append("{")
                        append("\"authentication\": {")
                        result.authentication?.let { auth ->
                            append("\"transaction_status\": ${auth.transactionStatus?.let { "\"$it\"" } ?: "null"},")
                            append("\"attempted\": ${auth.attempted},")
                            append("\"timed_out\": ${auth.hasTimedOut},")
                            append("\"user_cancelled\": ${auth.hasCancelled},")
                            append("\"type\": ${auth.type?.let { "\"$it\"" } ?: "null"}")
                        }
                        append("},")
                        append("\"tokenized\": ${result.tokenized}")
                        append("}")
                    }
                    
                    onSuccess(responseJson)
                } else {
                    // For non-3DS flows (ID or Card without authenticate), use original tokenize
                    val response = gr4vy.tokenize(checkoutSessionId.trim(), request)
                    // Handle empty response (204 No Content) by providing meaningful response
                    val displayResponse = if (response.rawResponse.isBlank()) {
                        """{"result": "OK"}"""
                    } else {
                        response.rawResponse
                    }
                    onSuccess(displayResponse)
                }
            } catch (e: Exception) {
                handleGr4vyError(e, trimmedGr4vyId, onError)
            }
            
    } catch (e: Exception) {
        showErrorResponse("Unexpected Error", e.message ?: "An unknown error occurred", onError)
    } finally {
        onLoading(false)
    }
}

/**
 * Helper function to format and display error responses as JSON, matching iOS behavior
 */
private fun showErrorResponse(
    errorTitle: String,
    errorDescription: String,
    onError: (String, String) -> Unit,
    additionalDetails: Map<String, String>? = null
) {
    val errorJson = buildString {
        append("{\n")
        append("  \"error\": \"$errorTitle\",\n")
        append("  \"description\": \"$errorDescription\"")
        
        additionalDetails?.forEach { (key, value) ->
            append(",\n  \"$key\": \"$value\"")
        }
        
        append("\n}")
    }
    
    onError("Error", errorJson)
}

/**
 * Handles Gr4vy errors and displays them in a full-page error screen, matching iOS behavior
 */
private fun handleGr4vyError(error: Exception, gr4vyId: String, onError: (String, String) -> Unit) {
    when (error) {
        is Gr4vyError.InvalidGr4vyId -> {
            Log.e("Gr4vy", "Invalid Gr4vy ID: ${error.message}")
            showErrorResponse(
                "Invalid Gr4vy ID",
                error.message ?: "The provided Gr4vy ID is invalid",
                onError
            )
        }
        is Gr4vyError.BadURL -> {
            Log.e("Gr4vy", "Bad URL: ${error.url}")
            showErrorResponse(
                "Bad URL",
                "The URL is malformed",
                onError,
                mapOf("url" to error.url)
            )
        }
        is Gr4vyError.HttpError -> {
            Log.e("Gr4vy", "HTTP Error: ${error.statusCode} - ${error.errorMessage}")
            
            // For HTTP errors, display the raw response data if available
            val responseData = error.responseData
            if (responseData != null && responseData.isNotEmpty()) {
                val responseString = try {
                    responseData.toString(Charsets.UTF_8)
                } catch (e: Exception) {
                    responseData.toString()
                }
                
                val title = if (error.statusCode == 400) {
                    "Error Response (Status: 400)"
                } else {
                    "Error Response (Status: ${error.statusCode})"
                }
                
                onError(title, responseString)
            } else {
                showErrorResponse(
                    "HTTP Error ${error.statusCode}",
                    error.errorMessage ?: error.message ?: "An HTTP error occurred",
                    onError
                )
            }
        }
        is Gr4vyError.NetworkError -> {
            val errorMsg = error.exception.message ?: error.message
            Log.e("Gr4vy", "Network Error: $errorMsg")
            
            when {
                errorMsg.contains("Cannot resolve host") || errorMsg.contains("Unable to resolve host") -> {
                    showErrorResponse(
                        "Cannot find server",
                        "Please check your Merchant ID ($gr4vyId)",
                        onError,
                        mapOf(
                            "url" to "https://api.$gr4vyId.gr4vy.app",
                            "error_code" to "cannotFindHost"
                        )
                    )
                }
                errorMsg.contains("timeout") -> {
                    showErrorResponse(
                        "Request timed out",
                        "Please try again",
                        onError,
                        mapOf("error_code" to "timedOut")
                    )
                }
                errorMsg.contains("No address associated with hostname") -> {
                    showErrorResponse(
                        "Cannot find server",
                        "Please check your Merchant ID ($gr4vyId)",
                        onError,
                        mapOf(
                            "url" to "https://api.$gr4vyId.gr4vy.app",
                            "error_code" to "cannotFindHost"
                        )
                    )
                }
                else -> {
                    showErrorResponse(
                        "Network error",
                        errorMsg,
                        onError,
                        mapOf("error_code" to "networkError")
                    )
                }
            }
        }
        is Gr4vyError.DecodingError -> {
            Log.e("Gr4vy", "Decoding Error: ${error.errorMessage}")
            showErrorResponse(
                "Decoding error",
                error.errorMessage,
                onError
            )
        }
        is Gr4vyError.ThreeDSError -> {
            Log.e("Gr4vy", "3D Secure Error: ${error.message}")
            showErrorResponse(
                "3DS error",
                error.message ?: "3D Secure authentication failed",
                onError
            )
        }
        is Gr4vyError.UiContextError -> {
            Log.e("Gr4vy", "UI Context Error: ${error.message}")
            showErrorResponse(
                "UI error",
                error.message ?: "UI context error occurred",
                onError
            )
        }
        else -> {
            Log.e("Gr4vy", "Unknown Error: ${error.message}")
            showErrorResponse(
                "Failed to tokenize payment method",
                error.message ?: "An unknown error occurred",
                onError
            )
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