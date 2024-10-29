package com.example.proyectofinalmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier


class AjustesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AjustesScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen() {
    // Contenedor de la pantalla
    Scaffold(
        topBar = {
            // Barra superior
            TopAppBar(
                title = { Text("Ajustes") }
            )
        }
    ) { innerPadding ->
        // Contenido de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // Padding alrededor del texto
            verticalArrangement = Arrangement.Center, // Centrar verticalmente
            horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente
        ) {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAjustesScreen() {
    AjustesScreen()
}
