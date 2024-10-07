package com.example.proyectofinalmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButtonsGroup(navController)
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = "main_screen") {
                        composable("main_screen") { MainScreen(navController) }
                        composable("notes_screen") { NotesScreen() }
                        composable("tasks_screen") { TasksScreen() }
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
                containerColor = Color.Gray,
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
                containerColor = Color.Gray,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Texto "Notas"
        Text(
            text = "Notas",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
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
            verticalAlignment = Alignment.CenterVertically, // Centra verticalmente
            horizontalArrangement = Arrangement.Center // Centra horizontalmente
        ) {
            // TextField para ingresar texto
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(text = "Buscar...") },
                singleLine = true, // Evita que sea multilinea
                modifier = Modifier
                    .weight(1f) // Hace que el TextField ocupe el espacio disponible
                    .padding(end = 8.dp) // Espacio entre el TextField y el botón
            )

            // Botón para realizar la búsqueda
            Button(onClick = {
                // Aquí puedes realizar la acción de búsqueda
                println("Búsqueda realizada: $searchText")
            }) {
                Text("Buscar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Espacio adicional si es necesario
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ProyectoFinalMovilTheme {
        MainScreen(rememberNavController())
    }
}
