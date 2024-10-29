package com.example.proyectofinalmovil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasGuardadas(navController: NavHostController, modifier: Modifier = Modifier) {
    val notesAndTasks = listOf(
        "Nota: Revisión de código",
        "Nota: Planificación semanal",
    )

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Texto "Notas Guardadas"
        Text(
            text = "Notas Guardadas",
            style = MaterialTheme.typography.headlineMedium,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Lista de notas y tareas
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(notesAndTasks) { item ->
                // Aquí puedes definir cómo se muestra cada ítem
                Text(text = item, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
