# Gr4vy Kotlin Android Sample App

A Kotlin Android sample application demonstrating integration with the Gr4vy Android SDK. This app provides a testing interface for the SDK endpoints with persistent configuration management using Jetpack Compose.

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

4. **Fields (Tokenize)** - `PUT /tokenize`
   - Tokenize payment methods (card, click-to-pay, or stored payment method ID)
   - Secure payment method storage
   - Multiple payment method types supported

## Admin Panel

The Admin tab provides centralized configuration for all API calls:

### Core Configuration
- **gr4vyId** - Your Gr4vy merchant identifier (required)
- **token** - API authentication token (required)  
- **server** - Environment selection (sandbox/production)
- **timeout** - Request timeout in seconds (optional)
- **merchantId** - Used in payment options requests

### How Configuration Works
- All settings persist across app restarts using DataStore Preferences
- Empty timeout field uses SDK default timeout
- Configuration is shared across all API screens
- Switch between sandbox and production environments instantly

## Key Features

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
- SDK error type handling
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
- Tap the action button (GET/POST/PUT) to make requests
- View responses in the JSON Response screen

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

### SDK Integration

```kotlin
val server: Gr4vyServer = if (serverEnvironment == "production") {
    Gr4vyServer.Production
} else {
    Gr4vyServer.Sandbox
}
val timeoutInterval = timeout.toLongOrNull() ?: 30000L

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

## Project Structure

```
src/main/java/com/gr4vy/gr4vy_kotlin_client_app/
├── MainActivity.kt              # Main activity with navigation
├── data/
│   └── PreferencesRepository.kt # DataStore configuration persistence
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt        # Main navigation screen
│   │   ├── AdminScreen.kt       # Configuration management
│   │   ├── PaymentOptionsScreen.kt
│   │   ├── CardDetailsScreen.kt
│   │   ├── PaymentMethodsScreen.kt
│   │   ├── FieldsScreen.kt      # Tokenize endpoint
│   │   └── JsonResponseScreen.kt # Response display
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
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