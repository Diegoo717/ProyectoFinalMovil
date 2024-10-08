package com.example.proyectofinalmovil.nota

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsWithImePadding

@Composable
fun NotesScreen(navController: NavHostController) {
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) } // Para controlar la visibilidad del menú desplegable

    // Color de fondo y texto del tema actual
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Permitir desplazamiento en toda la pantalla
            .navigationBarsWithImePadding() // Ajustar la interfaz cuando el teclado esté visible
    ) {
        // Barra superior: botón de regreso, título y menú de tres puntos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón de regreso
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Regresar",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        navController.popBackStack() // Navegar de regreso
                    }
            )

            // Título centrado
            TextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                placeholder = { Text(text = "Título") },
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            // Menú de tres puntos
            Box {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menú",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            showMenu = !showMenu // Mostrar u ocultar el menú
                        }
                )

                // DropdownMenu para guardar, borrar, compartir
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false } // Cerrar el menú cuando se hace clic fuera
                ) {
                    DropdownMenuItem(
                        text = { Text("Guardar") },
                        onClick = {
                            // Acción para guardar la nota
                            showMenu = false
                            println("Guardar nota")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir") },
                        onClick = {
                            // Acción para compartir la nota
                            showMenu = false
                            println("Compartir nota")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Borrar") },
                        onClick = {
                            // Acción para borrar la nota
                            showMenu = false
                            println("Borrar nota")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }

        // Área de texto para la nota
        TextField(
            value = noteContent,
            onValueChange = { noteContent = it },
            placeholder = { Text(text = "Escribe tu nota aquí...") },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  // Permitir que el área de texto ocupe el espacio restante
                .heightIn(min = 200.dp) // Mantener altura mínima
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Íconos para agregar imagen y audio
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = "Agregar imagen",
                modifier = Modifier.size(48.dp)
            )
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Agregar audio",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
