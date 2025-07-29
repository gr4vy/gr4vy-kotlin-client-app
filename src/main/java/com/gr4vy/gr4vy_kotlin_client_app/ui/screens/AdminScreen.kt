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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gr4vy.gr4vy_kotlin_client_app.data.PreferencesRepository
import com.gr4vy.gr4vy_kotlin_client_app.ui.theme.Gr4vyKotlinClientAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val context = LocalContext.current
    val preferencesRepository = remember { PreferencesRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // State for form fields
    val merchantId by preferencesRepository.merchantId.collectAsState(initial = "")
    val gr4vyId by preferencesRepository.gr4vyId.collectAsState(initial = "")
    val apiToken by preferencesRepository.apiToken.collectAsState(initial = "")
    val serverEnvironment by preferencesRepository.serverEnvironment.collectAsState(initial = "sandbox")
    val timeout by preferencesRepository.timeout.collectAsState(initial = "")
    
    // Local state for input fields
    var merchantIdInput by remember { mutableStateOf("") }
    var gr4vyIdInput by remember { mutableStateOf("") }
    var apiTokenInput by remember { mutableStateOf("") }
    var serverEnvironmentInput by remember { mutableStateOf("sandbox") }
    var timeoutInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    var showSaveDialog by remember { mutableStateOf(false) }
    
    // Update input fields when preferences change
    LaunchedEffect(merchantId, gr4vyId, apiToken, serverEnvironment, timeout) {
        merchantIdInput = merchantId
        gr4vyIdInput = gr4vyId
        apiTokenInput = apiToken
        serverEnvironmentInput = serverEnvironment
        timeoutInput = timeout
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Admin") },
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
                        text = "API Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = merchantIdInput,
                        onValueChange = { merchantIdInput = it },
                        label = { Text("merchantId") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = gr4vyIdInput,
                        onValueChange = { gr4vyIdInput = it },
                        label = { Text("gr4vyId") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = apiTokenInput,
                        onValueChange = { apiTokenInput = it },
                        label = { Text("token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = serverEnvironmentInput,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("server") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("sandbox") },
                                onClick = {
                                    serverEnvironmentInput = "sandbox"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("production") },
                                onClick = {
                                    serverEnvironmentInput = "production"
                                    expanded = false
                                }
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = timeoutInput,
                        onValueChange = { timeoutInput = it },
                        label = { Text("timeout") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        preferencesRepository.saveMerchantId(merchantIdInput)
                        preferencesRepository.saveGr4vyId(gr4vyIdInput)
                        preferencesRepository.saveApiToken(apiTokenInput)
                        preferencesRepository.saveServerEnvironment(serverEnvironmentInput)
                        preferencesRepository.saveTimeout(timeoutInput)
                        showSaveDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Settings Saved") },
            confirmButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    Gr4vyKotlinClientAppTheme {
        AdminScreen()
    }
} 