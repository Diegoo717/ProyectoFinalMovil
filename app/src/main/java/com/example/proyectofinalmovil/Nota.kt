package com.example.proyectofinalmovil.nota

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavHostController,
    noteId: Int?,
    isReadOnly: Boolean = false,
    viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(NoteRepository(NoteDatabase.getDatabase(LocalContext.current).noteDao())))
) {
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) } // Estado para el menú desplegable
    val context = LocalContext.current

    // Revisar si noteId es null o -1 (u otro indicador) para nueva nota
    val isNewNote = noteId == null || noteId == 0

    LaunchedEffect(noteId) {
        if (!isNewNote) {
            noteId?.let { id ->
                val note = viewModel.getNoteById(id)
                note?.let {
                    noteTitle = it.title
                    noteContent = it.content
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
            }

            // Menú de tres puntos
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Opción de compartir
                    DropdownMenuItem(
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Compartiendo la nota...", Toast.LENGTH_SHORT).show()
                        },
                        text = { Text("Compartir") }
                    )
                }
            }

            // Botón de guardar
            if (!isReadOnly) {
                IconButton(onClick = {
                    if (noteTitle.isNotEmpty() && noteContent.isNotEmpty()) {
                        if (!isNewNote) {
                            // Actualizar una nota existente
                            viewModel.update(Note(
                                id = noteId ?: 0,
                                title = noteTitle,
                                content = noteContent
                            ))
                            Toast.makeText(context, "Nota actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            // Insertar una nueva nota
                            viewModel.insert(Note(
                                title = noteTitle,
                                content = noteContent
                            ))
                            Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                        }
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Guardar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = noteTitle,
            onValueChange = { if (!isReadOnly) noteTitle = it },
            placeholder = { Text(text = "Título de la nota") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = isReadOnly
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = noteContent,
            onValueChange = { if (!isReadOnly) noteContent = it },
            placeholder = { Text(text = "Contenido de la nota") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = isReadOnly
        )

        // Botones para añadir imagen y audio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Botón para añadir imagen
            IconButton(onClick = { /* Acción para añadir imagen */ },
                modifier = Modifier.weight(1f).padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFBBDEFB))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "Añadir Imagen")
                    Text(text = "Añadir Imagen", color = Color.Black)
                }
            }

            // Botón para añadir audio
            IconButton(onClick = { /* Acción para añadir audio */ },
                modifier = Modifier.weight(1f).padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFFF59D))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Añadir Audio")
                    Text(text = "Añadir Audio", color = Color.Black)
                }
            }
        }
    }
}
