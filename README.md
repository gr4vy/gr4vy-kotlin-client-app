# Kotlin Client App for Gr4vy Kotlin SDK

<div align="left">
    <img alt="Platforms" src="https://img.shields.io/badge/Platforms-Android-yellowgreen?style=for-the-badge">
    <img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/gr4vy/gr4vy-kotlin-client-app/android.yml?branch=main&style=for-the-badge">
</div>

## Summary

A Kotlin Android sample application demonstrating integration with the [Gr4vy Kotlin Android SDK](https://github.com/gr4vy/gr4vy-kotlin). This app provides a testing interface for the SDK endpoints with persistent configuration management using Jetpack Compose, including comprehensive 3DS authentication support with customizable UI themes.

- [Kotlin Client App for Gr4vy Kotlin SDK](#kotlin-client-app-for-gr4vy-kotlin-sdk)
  - [Summary](#summary)
  - [Architecture](#architecture)
  - [App Structure](#app-structure)
    - [Bottom Navigation](#bottom-navigation)
    - [API Screens (4 Endpoints)](#api-screens-4-endpoints)
  - [Admin Panel](#admin-panel)
    - [Core Configuration](#core-configuration)
    - [How Configuration Works](#how-configuration-works)
  - [Key Features](#key-features)
    - [3DS Authentication Support](#3ds-authentication-support)
    - [Coroutines Implementation](#coroutines-implementation)
    - [Error Handling](#error-handling)
    - [Response Handling](#response-handling)
    - [Data Persistence](#data-persistence)
  - [Setup Instructions](#setup-instructions)
    - [1. Configure Admin Settings](#1-configure-admin-settings)
    - [2. Test API Endpoints](#2-test-api-endpoints)
    - [3. Development Usage](#3-development-usage)
  - [Customization](#customization)
    - [Adding New Endpoints](#adding-new-endpoints)
    - [Modifying UI](#modifying-ui)
    - [3DS UI Customization](#3ds-ui-customization)
    - [SDK Integration](#sdk-integration)
  - [Dependencies](#dependencies)
  - [Requirements](#requirements)
  - [Build \& Run](#build--run)
    - [Using Android Studio](#using-android-studio)
    - [Using Command Line](#using-command-line)
  - [CI/CD](#cicd)
  - [License](#license)

## Architecture

The app uses modern Android patterns with Kotlin Coroutines for async API calls, calling the Gr4vy Android SDK directly and DataStore for persistent configuration across app sessions.

## App Structure

### Bottom Navigation

- **Home Tab**: Main navigation to API endpoint screens
- **Admin Tab**: Configuration management panel

### API Screens (4 Endpoints)

1. **Payment Options** - `POST /payment-options`
   - Configure metadata, country, currency, amount, locale, and cart items
   - Dynamic metadata key-value pairs
   - Cart items with detailed product information

2. **Card Details** - `GET /card-details`
   - Test card BIN lookup and payment method validation
   - Supports intent, subsequent payments, and merchant-initiated transactions

3. **Payment Methods** - `GET /buyers/{buyer_id}/payment-methods`
   - Retrieve stored payment methods for buyers
   - Sorting and filtering options
   - Buyer identification by ID or external identifier

4. **Tokenize** - `PUT /tokenize`
   - Tokenize payment methods (card or stored payment method ID)
   - 3DS authentication support
   - Test card selection for frictionless and challenge flows
   - Customizable 3DS UI themes (light/dark mode support)
   - SDK timeout configuration
   - Secure payment method storage

## Admin Panel

The Admin tab provides centralized configuration for all API calls:

### Core Configuration

- **gr4vyId** - Your Gr4vy merchant identifier 
- **token** - API authentication token 
- **server** - Environment selection (sandbox/production)
- **timeout** - Request timeout in seconds (optional)
- **merchantId** - Used in payment options requests

### How Configuration Works

- All settings persist across app restarts using DataStore Preferences
- Empty timeout field uses SDK default timeout
- Configuration is shared across all API screens
- Switch between sandbox and production environments instantly

## Key Features

### 3DS Authentication Support

The Tokenize screen includes comprehensive 3DS authentication features:
- **Authentication Toggle**: Enable/disable 3DS authentication
- **Test Cards**: Pre-configured test cards for both flows:
  - **Frictionless Flow**: Cards that complete authentication without challenge (4242 4242 4242 4242)
  - **Challenge Flow**: Cards that trigger authentication challenge screens (4916 9940 6425 2017, 5100 0000 0000 0010)
- **UI Customization**: Three built-in themes for 3DS challenge screens:
  - Red/Blue theme
  - Orange/Purple theme
  - Green/Yellow theme
  - Each theme supports both light and dark modes
- **Timeout Configuration**: Configurable SDK max timeout (in minutes)
- **Response Data**: Returns authentication details including:
  - `tokenized`: Whether the payment method was successfully tokenized
  - `authentication.attempted`: Whether 3DS authentication was attempted
  - `authentication.user_cancelled`: Whether the user cancelled the authentication
  - `authentication.timed_out`: Whether the authentication timed out
  - `authentication.type`: The type of authentication performed
  - `authentication.transaction_status`: The final status of the 3DS transaction

### Coroutines Implementation

All API calls use Kotlin Coroutines:

```kotlin
Button(onClick = {
    scope.launch {
        sendRequest()
    }
}) {
    Text("GET")
}
```

### Error Handling

- SDK error type handling including 3DS-specific errors:
  - `threeDSError`: 3DS authentication failures
  - `uiContextError`: UI context-related issues
- Network error detection and visual messages
- HTTP status code display with detailed error responses
- Expandable error messages show full JSON error details

### Response Handling

- Pretty-printed JSON responses
- Copy/share functionality for debugging
- Separate navigation for success and error responses

### Data Persistence

- Form data persists between app launches using DataStore
- Admin settings stored securely in encrypted preferences
- Complex data structures (metadata, cart items) serialized with Kotlinx Serialization

## Setup Instructions

### 1. Configure Admin Settings

- Open the **Admin** tab
- Enter your `gr4vyId` and optional `token`
- Select environment
- Optionally set custom timeout

### 2. Test API Endpoints

- Navigate through the **Home** tab to each API screen
- Fill in required fields (marked with validation)
- **For Tokenize**: Select test cards for 3DS testing (frictionless or challenge flows), choose a theme, and configure authentication settings
- Tap the action button (GET/POST/PUT) to make requests
- View responses with authentication details (for 3DS-enabled requests)

### 3. Development Usage

- Use as reference implementation for SDK integration
- Test various parameter combinations
- Debug API responses with detailed error information

## Customization

### Adding New Endpoints

1. Create new Composable following existing patterns
2. Add admin settings storage with DataStore
3. Implement suspend request function with error handling
4. Add navigation route in `MainActivity.kt`

### Modifying UI

- All screens use Jetpack Compose with Material 3 design
- Consistent styling with custom theme
- Error states handled with Material error colors
- Loading states with CircularProgressIndicator

### 3DS UI Customization

The app demonstrates comprehensive 3DS UI theming capabilities:

```kotlin
private fun buildRedBlueTheme(): Gr4vyThreeDSUiCustomizationMap {
    val light = Gr4vyThreeDSUiCustomization(
        label = Gr4vyThreeDSLabelCustomization(
            textFontName = "sans-serif",
            textFontSize = 16,
            textColorHex = "#1c1c1e",
            headingTextFontName = "sans-serif-medium",
            headingTextFontSize = 24,
            headingTextColorHex = "#0a0a0a"
        ),
        toolbar = Gr4vyThreeDSToolbarCustomization(
            textFontName = "sans-serif-medium",
            textFontSize = 17,
            textColorHex = "#ffffff",
            backgroundColorHex = "#007aff",
            headerText = "Secure Checkout",
            buttonText = "Cancel"
        ),
        // ... additional customizations
    )
    // Separate dark mode customization
    val dark = Gr4vyThreeDSUiCustomization(...)
    
    return Gr4vyThreeDSUiCustomizationMap(default = light, dark = dark)
}
```

Customizable elements include:
- Labels (text font, size, color for both body and heading)
- Toolbar (font, colors, button text, header text)
- Text boxes (font, colors, border width, corner radius)
- View backgrounds (challenge and progress views)
- Buttons (submit, continue, next, resend, etc. - each with individual styling)

### SDK Integration

Basic SDK initialization:

```kotlin
val server: Gr4vyServer = if (serverEnvironment == "production") {
    Gr4vyServer.Production
} else {
    Gr4vyServer.Sandbox
}

val gr4vy = try {
    Gr4vy(
        gr4vyId = gr4vyID,
        token = trimmedToken,
        server = server,
        timeout = timeoutInterval
    )
} catch (e: Exception) {
    errorMessage = "Failed to configure Gr4vy SDK: ${e.message}"
    return
}
```

Tokenize with 3DS authentication:

```kotlin
gr4vy.tokenize(
    checkoutSessionId = checkoutSessionId,
    cardData = cardRequest,
    activity = activity, // Activity context required
    sdkMaxTimeoutMinutes = timeoutMinutes,
    authenticate = true,
    uiCustomization = buildRedBlueTheme() // Optional theme
) { result ->
    when (result) {
        is Gr4vyResult.Success -> {
            val tokenizeResult = result.data
            // Access tokenizeResult.tokenized and tokenizeResult.authentication
        }
        is Gr4vyResult.Error -> {
            // Handle error
        }
    }
}
```

## Dependencies

- **Jetpack Compose** - Modern Android UI toolkit
- **Kotlin Coroutines** - Asynchronous programming
- **DataStore Preferences** - Settings persistence
- **Navigation Compose** - Screen navigation
- **Kotlinx Serialization** - JSON handling
- **OkHttp** - HTTP client for networking
- **Gr4vy Android SDK** - Payment processing

## Requirements

- Android 8.0+ (API level 26)
- Kotlin 2.0+
- Android Studio Koala+ (2024.1.1+)
- Gradle 8.0+
- Gr4vy Android SDK

## Build & Run

### Using Android Studio

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on device or emulator

### Using Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## CI/CD

The project includes a simplified GitHub Actions workflow (`.github/workflows/android.yml`) that:
- Validates Gradle wrapper integrity
- Builds the sample app with `./gradlew assemble`
- Runs Android Lint for code quality checks

## License

This sample app is provided as-is for demonstration purposes.
