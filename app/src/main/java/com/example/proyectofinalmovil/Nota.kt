package com.example.proyectofinalmovil.nota

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NotesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pantalla de Notas",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(top = 50.dp),
            textAlign = TextAlign.Center
        )
    }
}
