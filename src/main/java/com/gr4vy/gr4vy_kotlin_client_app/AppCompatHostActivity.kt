package com.gr4vy.gr4vy_kotlin_client_app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.gr4vy.gr4vy_kotlin_client_app.ui.theme.Gr4vyKotlinClientAppTheme

/**
 * The same app as [MainActivity], hosted in an AppCompatActivity for QA.
 *
 * The 3DS SDK shows its challenge as AndroidX dialog fragments in an AppCompatActivity, and as platform
 * dialog fragments in MainActivity (a ComponentActivity). QA runs the challenge in both.
 */
class AppCompatHostActivity : AppCompatActivity() {
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
