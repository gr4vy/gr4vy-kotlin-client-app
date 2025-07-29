package com.gr4vy.gr4vy_kotlin_client_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.AdminScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.CardDetailsScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.FieldsScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.HomeScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.JsonResponseScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.PaymentMethodsScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.screens.PaymentOptionsScreen
import com.gr4vy.gr4vy_kotlin_client_app.ui.theme.Gr4vyKotlinClientAppTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Gr4vyKotlinClientAppTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Admin") },
                    label = { Text("Admin") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        navController.navigate("admin") {
                            popUpTo("admin") { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { 
                HomeScreen(navController) 
            }
            composable("admin") { 
                AdminScreen() 
            }
            composable("payment_options") {
                PaymentOptionsScreen(
                    onNavigateToResponse = { title, jsonResponse ->
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        val encodedResponse = URLEncoder.encode(jsonResponse, StandardCharsets.UTF_8.toString())
                        navController.navigate("json_response/$encodedTitle/$encodedResponse")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("fields") {
                FieldsScreen(
                    onNavigateToResponse = { title, jsonResponse ->
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        val encodedResponse = URLEncoder.encode(jsonResponse, StandardCharsets.UTF_8.toString())
                        navController.navigate("json_response/$encodedTitle/$encodedResponse")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("card_details") {
                CardDetailsScreen(
                    onNavigateToResponse = { title, jsonResponse ->
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        val encodedResponse = URLEncoder.encode(jsonResponse, StandardCharsets.UTF_8.toString())
                        navController.navigate("json_response/$encodedTitle/$encodedResponse")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("payment_methods") {
                PaymentMethodsScreen(
                    onNavigateToResponse = { title, jsonResponse ->
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        val encodedResponse = URLEncoder.encode(jsonResponse, StandardCharsets.UTF_8.toString())
                        navController.navigate("json_response/$encodedTitle/$encodedResponse")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("json_response/{title}/{jsonResponse}") { backStackEntry ->
                val encodedTitle = backStackEntry.arguments?.getString("title") ?: "Response"
                val encodedResponse = backStackEntry.arguments?.getString("jsonResponse") ?: "{}"
                
                val title = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
                val jsonResponse = URLDecoder.decode(encodedResponse, StandardCharsets.UTF_8.toString())
                
                JsonResponseScreen(
                    title = title,
                    jsonString = jsonResponse,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    Gr4vyKotlinClientAppTheme {
        MainApp()
    }
}