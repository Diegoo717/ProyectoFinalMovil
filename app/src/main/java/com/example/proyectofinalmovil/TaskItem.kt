package com.example.proyectofinalmovil.tarea

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TaskItem(
    task: Task,
    onDeleteClick: (Task) -> Unit,
    onEditClick: (Task) -> Unit, // Callback para editar
    modifier: Modifier = Modifier // Añadir el parámetro modifier para flexibilidad
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFBBDEFB)) // Fondo azul
            .clickable { onEditClick(task) } // Navegar al hacer clic en la tarjeta
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Text(text = task.content, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Fecha: ${task.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Hora: ${task.time}", style = MaterialTheme.typography.bodyMedium)

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                IconButton(onClick = { onEditClick(task) }) { // Navegar a editar tarea
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta tarea?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick(task)
                    showDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
