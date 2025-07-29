package com.gr4vy.gr4vy_kotlin_client_app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gr4vy_settings")

class PreferencesRepository(private val context: Context) {
    
    companion object {
        private val MERCHANT_ID = stringPreferencesKey("merchant_id")
        private val GR4VY_ID = stringPreferencesKey("gr4vy_id")
        private val API_TOKEN = stringPreferencesKey("api_token")
        private val SERVER_ENVIRONMENT = stringPreferencesKey("server_environment")
        private val TIMEOUT = stringPreferencesKey("timeout")
        
        // Payment Options fields
        private val PAYMENT_OPTIONS_METADATA_ENTRIES = stringPreferencesKey("payment_options_metadata_entries")
        private val PAYMENT_OPTIONS_CART_ITEMS = stringPreferencesKey("payment_options_cart_items")
        private val PAYMENT_OPTIONS_COUNTRY = stringPreferencesKey("payment_options_country")
        private val PAYMENT_OPTIONS_CURRENCY = stringPreferencesKey("payment_options_currency")
        private val PAYMENT_OPTIONS_AMOUNT = stringPreferencesKey("payment_options_amount")
        private val PAYMENT_OPTIONS_LOCALE = stringPreferencesKey("payment_options_locale")
        
        // Fields screen
        private val FIELDS_CHECKOUT_SESSION_ID = stringPreferencesKey("fields_checkout_session_id")
        private val FIELDS_PAYMENT_METHOD_TYPE = stringPreferencesKey("fields_payment_method_type")
        private val FIELDS_CARD_NUMBER = stringPreferencesKey("fields_card_number")
        private val FIELDS_EXPIRATION_DATE = stringPreferencesKey("fields_expiration_date")
        private val FIELDS_SECURITY_CODE = stringPreferencesKey("fields_security_code")
        private val FIELDS_MERCHANT_TRANSACTION_ID = stringPreferencesKey("fields_merchant_transaction_id")
        private val FIELDS_SRC_CORRELATION_ID = stringPreferencesKey("fields_src_correlation_id")
        private val FIELDS_PAYMENT_METHOD_ID = stringPreferencesKey("fields_payment_method_id")
        private val FIELDS_ID_SECURITY_CODE = stringPreferencesKey("fields_id_security_code")
        
        // Card Details
        private val CARD_DETAILS_NUMBER = stringPreferencesKey("card_details_number")
        private val CARD_DETAILS_CURRENCY = stringPreferencesKey("card_details_currency")
        private val CARD_DETAILS_AMOUNT = stringPreferencesKey("card_details_amount")
        private val CARD_DETAILS_BIN = stringPreferencesKey("card_details_bin")
        private val CARD_DETAILS_COUNTRY = stringPreferencesKey("card_details_country")
        private val CARD_DETAILS_INTENT = stringPreferencesKey("card_details_intent")
        private val CARD_DETAILS_IS_SUBSEQUENT_PAYMENT = booleanPreferencesKey("card_details_is_subsequent_payment")
        private val CARD_DETAILS_MERCHANT_INITIATED = booleanPreferencesKey("card_details_merchant_initiated")
        private val CARD_DETAILS_METADATA = stringPreferencesKey("card_details_metadata")
        private val CARD_DETAILS_PAYMENT_METHOD_ID = stringPreferencesKey("card_details_payment_method_id")
        private val CARD_DETAILS_PAYMENT_SOURCE = stringPreferencesKey("card_details_payment_source")
        
        // Payment Methods
        private val PAYMENT_METHODS_ID = stringPreferencesKey("payment_methods_id")
        private val PAYMENT_METHODS_BUYER_ID = stringPreferencesKey("payment_methods_buyer_id")
        private val PAYMENT_METHODS_BUYER_EXTERNAL_IDENTIFIER = stringPreferencesKey("payment_methods_buyer_external_identifier")
        private val PAYMENT_METHODS_SORT_BY = stringPreferencesKey("payment_methods_sort_by")
        private val PAYMENT_METHODS_ORDER_BY = stringPreferencesKey("payment_methods_order_by")
        private val PAYMENT_METHODS_COUNTRY = stringPreferencesKey("payment_methods_country")
        private val PAYMENT_METHODS_CURRENCY = stringPreferencesKey("payment_methods_currency")
    }
    
    // Admin settings
    val merchantId: Flow<String> = context.dataStore.data.map { it[MERCHANT_ID] ?: "" }
    val gr4vyId: Flow<String> = context.dataStore.data.map { it[GR4VY_ID] ?: "" }
    val apiToken: Flow<String> = context.dataStore.data.map { it[API_TOKEN] ?: "" }
    val serverEnvironment: Flow<String> = context.dataStore.data.map { it[SERVER_ENVIRONMENT] ?: "sandbox" }
    val timeout: Flow<String> = context.dataStore.data.map { it[TIMEOUT] ?: "" }
    
    // Payment Options
    val paymentOptionsMetadataEntries: Flow<String> = context.dataStore.data.map { it[PAYMENT_OPTIONS_METADATA_ENTRIES] ?: "" }
    val paymentOptionsCartItems: Flow<String> = context.dataStore.data.map { it[PAYMENT_OPTIONS_CART_ITEMS] ?: "" }
    val paymentOptionsCountry: Flow<String> = context.dataStore.data.map { it[PAYMENT_OPTIONS_COUNTRY] ?: "" }
    val paymentOptionsCurrency: Flow<String> = context.dataStore.data.map { it[PAYMENT_OPTIONS_CURRENCY] ?: "" }
    val paymentOptionsAmount: Flow<String> = context.dataStore.data.map { it[PAYMENT_OPTIONS_AMOUNT] ?: "" }
    val paymentOptionsLocale: Flow<String> = context.dataStore.data.map { it[PAYMENT_OPTIONS_LOCALE] ?: "" }
    
    // Fields
    val fieldsCheckoutSessionId: Flow<String> = context.dataStore.data.map { it[FIELDS_CHECKOUT_SESSION_ID] ?: "" }
    val fieldsPaymentMethodType: Flow<String> = context.dataStore.data.map { it[FIELDS_PAYMENT_METHOD_TYPE] ?: "" }
    val fieldsCardNumber: Flow<String> = context.dataStore.data.map { it[FIELDS_CARD_NUMBER] ?: "" }
    val fieldsExpirationDate: Flow<String> = context.dataStore.data.map { it[FIELDS_EXPIRATION_DATE] ?: "" }
    val fieldsSecurityCode: Flow<String> = context.dataStore.data.map { it[FIELDS_SECURITY_CODE] ?: "" }
    val fieldsMerchantTransactionId: Flow<String> = context.dataStore.data.map { it[FIELDS_MERCHANT_TRANSACTION_ID] ?: "" }
    val fieldsSrcCorrelationId: Flow<String> = context.dataStore.data.map { it[FIELDS_SRC_CORRELATION_ID] ?: "" }
    val fieldsPaymentMethodId: Flow<String> = context.dataStore.data.map { it[FIELDS_PAYMENT_METHOD_ID] ?: "" }
    val fieldsIdSecurityCode: Flow<String> = context.dataStore.data.map { it[FIELDS_ID_SECURITY_CODE] ?: "" }
    
    // Card Details
    val cardDetailsNumber: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_NUMBER] ?: "" }
    val cardDetailsCurrency: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_CURRENCY] ?: "" }
    val cardDetailsAmount: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_AMOUNT] ?: "" }
    val cardDetailsBin: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_BIN] ?: "" }
    val cardDetailsCountry: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_COUNTRY] ?: "" }
    val cardDetailsIntent: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_INTENT] ?: "" }
    val cardDetailsIsSubsequentPayment: Flow<Boolean> = context.dataStore.data.map { it[CARD_DETAILS_IS_SUBSEQUENT_PAYMENT] ?: false }
    val cardDetailsMerchantInitiated: Flow<Boolean> = context.dataStore.data.map { it[CARD_DETAILS_MERCHANT_INITIATED] ?: false }
    val cardDetailsMetadata: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_METADATA] ?: "" }
    val cardDetailsPaymentMethodId: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_PAYMENT_METHOD_ID] ?: "" }
    val cardDetailsPaymentSource: Flow<String> = context.dataStore.data.map { it[CARD_DETAILS_PAYMENT_SOURCE] ?: "" }
    
    // Payment Methods
    val paymentMethodsId: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_ID] ?: "" }
    val paymentMethodsBuyerId: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_BUYER_ID] ?: "" }
    val paymentMethodsBuyerExternalIdentifier: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_BUYER_EXTERNAL_IDENTIFIER] ?: "" }
    val paymentMethodsSortBy: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_SORT_BY] ?: "" }
    val paymentMethodsOrderBy: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_ORDER_BY] ?: "desc" }
    val paymentMethodsCountry: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_COUNTRY] ?: "" }
    val paymentMethodsCurrency: Flow<String> = context.dataStore.data.map { it[PAYMENT_METHODS_CURRENCY] ?: "" }
    
    suspend fun saveMerchantId(value: String) {
        context.dataStore.edit { it[MERCHANT_ID] = value }
    }
    
    suspend fun saveGr4vyId(value: String) {
        context.dataStore.edit { it[GR4VY_ID] = value }
    }
    
    suspend fun saveApiToken(value: String) {
        context.dataStore.edit { it[API_TOKEN] = value }
    }
    
    suspend fun saveServerEnvironment(value: String) {
        context.dataStore.edit { it[SERVER_ENVIRONMENT] = value }
    }
    
    suspend fun saveTimeout(value: String) {
        context.dataStore.edit { it[TIMEOUT] = value }
    }
    
    suspend fun savePaymentOptionsMetadataEntries(value: String) {
        context.dataStore.edit { it[PAYMENT_OPTIONS_METADATA_ENTRIES] = value }
    }
    
    suspend fun savePaymentOptionsCartItems(value: String) {
        context.dataStore.edit { it[PAYMENT_OPTIONS_CART_ITEMS] = value }
    }
    
    suspend fun savePaymentOptionsCountry(value: String) {
        context.dataStore.edit { it[PAYMENT_OPTIONS_COUNTRY] = value }
    }
    
    suspend fun savePaymentOptionsCurrency(value: String) {
        context.dataStore.edit { it[PAYMENT_OPTIONS_CURRENCY] = value }
    }
    
    suspend fun savePaymentOptionsAmount(value: String) {
        context.dataStore.edit { it[PAYMENT_OPTIONS_AMOUNT] = value }
    }
    
    suspend fun savePaymentOptionsLocale(value: String) {
        context.dataStore.edit { it[PAYMENT_OPTIONS_LOCALE] = value }
    }
    
    suspend fun saveFieldsCheckoutSessionId(value: String) {
        context.dataStore.edit { it[FIELDS_CHECKOUT_SESSION_ID] = value }
    }
    
    suspend fun saveFieldsPaymentMethodType(value: String) {
        context.dataStore.edit { it[FIELDS_PAYMENT_METHOD_TYPE] = value }
    }
    
    suspend fun saveFieldsCardNumber(value: String) {
        context.dataStore.edit { it[FIELDS_CARD_NUMBER] = value }
    }
    
    suspend fun saveFieldsExpirationDate(value: String) {
        context.dataStore.edit { it[FIELDS_EXPIRATION_DATE] = value }
    }
    
    suspend fun saveFieldsSecurityCode(value: String) {
        context.dataStore.edit { it[FIELDS_SECURITY_CODE] = value }
    }
    
    suspend fun saveFieldsMerchantTransactionId(value: String) {
        context.dataStore.edit { it[FIELDS_MERCHANT_TRANSACTION_ID] = value }
    }
    
    suspend fun saveFieldsSrcCorrelationId(value: String) {
        context.dataStore.edit { it[FIELDS_SRC_CORRELATION_ID] = value }
    }
    
    suspend fun saveFieldsPaymentMethodId(value: String) {
        context.dataStore.edit { it[FIELDS_PAYMENT_METHOD_ID] = value }
    }
    
    suspend fun saveFieldsIdSecurityCode(value: String) {
        context.dataStore.edit { it[FIELDS_ID_SECURITY_CODE] = value }
    }
    
    suspend fun saveCardDetailsNumber(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_NUMBER] = value }
    }
    
    suspend fun saveCardDetailsCurrency(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_CURRENCY] = value }
    }
    
    suspend fun saveCardDetailsAmount(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_AMOUNT] = value }
    }
    
    suspend fun saveCardDetailsBin(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_BIN] = value }
    }
    
    suspend fun saveCardDetailsCountry(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_COUNTRY] = value }
    }
    
    suspend fun saveCardDetailsIntent(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_INTENT] = value }
    }
    
    suspend fun saveCardDetailsIsSubsequentPayment(value: Boolean) {
        context.dataStore.edit { it[CARD_DETAILS_IS_SUBSEQUENT_PAYMENT] = value }
    }
    
    suspend fun saveCardDetailsMerchantInitiated(value: Boolean) {
        context.dataStore.edit { it[CARD_DETAILS_MERCHANT_INITIATED] = value }
    }
    
    suspend fun saveCardDetailsMetadata(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_METADATA] = value }
    }
    
    suspend fun saveCardDetailsPaymentMethodId(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_PAYMENT_METHOD_ID] = value }
    }
    
    suspend fun saveCardDetailsPaymentSource(value: String) {
        context.dataStore.edit { it[CARD_DETAILS_PAYMENT_SOURCE] = value }
    }
    
    suspend fun savePaymentMethodsId(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_ID] = value }
    }

    suspend fun savePaymentMethodsBuyerId(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_BUYER_ID] = value }
    }

    suspend fun savePaymentMethodsBuyerExternalIdentifier(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_BUYER_EXTERNAL_IDENTIFIER] = value }
    }

    suspend fun savePaymentMethodsSortBy(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_SORT_BY] = value }
    }

    suspend fun savePaymentMethodsOrderBy(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_ORDER_BY] = value }
    }

    suspend fun savePaymentMethodsCountry(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_COUNTRY] = value }
    }

    suspend fun savePaymentMethodsCurrency(value: String) {
        context.dataStore.edit { it[PAYMENT_METHODS_CURRENCY] = value }
    }
} 