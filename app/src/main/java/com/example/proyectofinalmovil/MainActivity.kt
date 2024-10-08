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
                        // Mostrar el botón flotante solo en la pantalla principal ("main_screen")
                        if (currentRoute == "main_screen") {
                            FloatingActionButtonsGroup(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = "main_screen") {
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
        AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.shrinkVertically()
        ) {
            FloatingActionButton(
                onClick = {
                    // Navegar a la pantalla de notas
                    navController.navigate("notes_screen")
                },
                modifier = Modifier.padding(bottom = 60.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Text("Nota")
            }
        }

        // Botón "Tarea"
        AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.shrinkVertically()
        ) {
            FloatingActionButton(
                onClick = {
                    // Navegar a la pantalla de tareas
                    navController.navigate("tasks_screen")
                },
                modifier = Modifier.padding(bottom = 120.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Text("Tarea")
            }
        }

        // Botón principal "+"
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text("+")
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }

    // Lista simulada de notas y tareas (puedes reemplazarla con tus datos reales)
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

        // Barra de búsqueda y botón
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // TextField para ingresar texto
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(text = "Buscar...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            // Botón para realizar la búsqueda
            Button(onClick = {
                println("Búsqueda realizada: $searchText")
            }) {
                Text("Buscar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de notas y tareas
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(notesAndTasks) { item ->
                // Distinción entre notas y tareas
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
            containerColor = Color(0xFFFFF59D) // Color para diferenciar las notas
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
            containerColor = Color(0xFFBBDEFB) // Color para diferenciar las tareas
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
