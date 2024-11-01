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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyectofinalmovil.R
import com.google.accompanist.insets.imePadding

@Composable
fun NotesScreen(navController: NavHostController) {
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // Color de fondo y texto del tema actual
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
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
                contentDescription = stringResource(id = R.string.back_button), // Descripción internacionalizada
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
                placeholder = { Text(text = stringResource(id = R.string.note_title_placeholder)) }, // Placeholder internacionalizado
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
                    contentDescription = stringResource(id = R.string.menu), // Descripción internacionalizada
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
                        text = { Text(stringResource(id = R.string.save)) }, // Texto internacionalizado
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
                        text = { Text(stringResource(id = R.string.share)) }, // Texto internacionalizado
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
                        text = { Text(stringResource(id = R.string.delete)) }, // Texto internacionalizado
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

        Spacer(modifier = Modifier.height(16.dp))

        // Área de texto para los detalles de la nota
        TextField(
            value = noteContent,
            onValueChange = { noteContent = it },
            placeholder = { Text(text = stringResource(id = R.string.note_content_placeholder)) }, // Placeholder internacionalizado
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Íconos para agregar imagen y audio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = stringResource(id = R.string.add_image), // Descripción internacionalizada
                modifier = Modifier.size(48.dp)
            )
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = stringResource(id = R.string.add_audio), // Descripción internacionalizada
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
