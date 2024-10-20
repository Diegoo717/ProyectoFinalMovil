package com.example.proyectofinalmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalmovil.ui.theme.ProyectoFinalMovilTheme
import com.example.proyectofinalmovil.nota.NotesScreen
import com.example.proyectofinalmovil.tarea.TasksScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProyectoFinalMovilTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        if (currentRoute == "main_screen") {
                            FloatingActionButtonsGroup(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main_screen") { MainScreen(navController) }
                        composable("notes_screen") { NotesScreen(navController) }
                        composable("tasks_screen") { TasksScreen(navController) }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingActionButtonsGroup(navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp, end = 30.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Botón "Nota"
        AnimatedVisibility(visible = expanded) {
            FloatingActionButton(
                onClick = { navController.navigate("notes_screen") },
                modifier = Modifier.padding(bottom = 60.dp),
                containerColor = Color(0xFFFFF59D), // Color amarillo
                shape = CircleShape
            ) {
                Text("Nota", color = Color.Black) // Cambiado el color del texto a negro
            }
        }

        // Botón "Tarea"
        AnimatedVisibility(visible = expanded) {
            FloatingActionButton(
                onClick = { navController.navigate("tasks_screen") },
                modifier = Modifier.padding(bottom = 120.dp),
                containerColor = Color(0xFFBBDEFB), // Color azul (el mismo que el de las notas)
                shape = CircleShape
            ) {
                Text("Tarea", color = Color.Black) // Cambiado el color del texto a negro
            }
        }

        // Botón "Nueva Carpeta"
        AnimatedVisibility(visible = expanded) {
            FloatingActionButton(
                onClick = { /* Acción para nueva carpeta */ },
                modifier = Modifier.padding(bottom = 180.dp),
                containerColor = Color(0xFFFFC107), // Color naranja claro
                shape = CircleShape
            ) {
                Text("Carpeta", color = Color.Black) // Cambiado el color del texto a negro
            }
        }

        // Botón principal "+"
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = Color.Gray // Color gris
        ) {
            Text("+", color = Color.White) // Cambiado el color del texto a blanco
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Asegúrate de tener esta anotación
@Composable
fun MainScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }

    val notesAndTasks = listOf(
        "Nota: Revisión de código",
        "Tarea: Comprar materiales",
        "Nota: Planificación semanal",
        "Tarea: Proyecto final"
    )

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Texto "Notas y Tareas"
        Text(
            text = "Notas y Tareas",
            style = MaterialTheme.typography.headlineMedium,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Barra de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text(text = "Buscar...") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de notas y tareas
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(notesAndTasks) { item ->
                if (item.startsWith("Nota:")) {
                    NoteItem(item)
                } else {
                    TaskItem(item)
                }
            }
        }
    }
}

// Composable para mostrar una nota
@Composable
fun NoteItem(note: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF59D)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = note,
            modifier = Modifier.padding(16.dp),
            color = Color.Black
        )
    }
}

// Composable para mostrar una tarea
@Composable
fun TaskItem(task: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBBDEFB) // Color azul
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = task,
            modifier = Modifier.padding(16.dp),
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProyectoFinalMovilTheme {
        MainScreen(rememberNavController())
    }
}
