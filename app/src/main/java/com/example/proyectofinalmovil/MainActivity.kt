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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalmovil.tarea.TasksScreen
import com.example.proyectofinalmovil.ui.theme.ProyectoFinalMovilTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProyectoFinalMovilTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                // Estado del Drawer
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Drawer
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(navController, scope, drawerState)
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(id = R.string.app_name)) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        },
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
                            composable("notas_guardadas") { NotasGuardadas(navController) }
                            composable("tareas_guardadas") { TareasGuardadas(navController) }
                            composable("tasks_screen") { TasksScreen(navController) }
                            composable("ajustes_screen") { AjustesActivity() } // Ruta para AjustesActivity
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavHostController, scope: CoroutineScope, drawerState: DrawerState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Menu", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Opción de Notas
        TextButton(onClick = {
            navController.navigate("notas_guardadas")
            scope.launch { drawerState.close() }
        }) {
            Icon(imageVector = Icons.Default.Note, contentDescription = "Notas", modifier = Modifier.padding(end = 8.dp))
            Text("Notas")
        }

        // Opción de Tareas
        TextButton(onClick = {
            navController.navigate("tareas_guardadas")
            scope.launch { drawerState.close() }
        }) {
            Icon(imageVector = Icons.Default.Task, contentDescription = "Tareas", modifier = Modifier.padding(end = 8.dp))
            Text("Tareas")
        }

        // Opción de Ajustes
        TextButton(onClick = {
            navController.navigate("ajustes_screen") // Navega a AjustesActivity
            scope.launch { drawerState.close() }
        }) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes", modifier = Modifier.padding(end = 8.dp))
            Text("Ajustes")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                onClick = { navController.navigate("notas_guardadas") },
                modifier = Modifier.padding(bottom = 60.dp),
                containerColor = Color(0xFFFFF59D), // Color amarillo
                shape = CircleShape
            ) {
                Text(stringResource(id = R.string.note), color = Color.Black)
            }
        }

        // Botón "Tarea"
        AnimatedVisibility(visible = expanded) {
            FloatingActionButton(
                onClick = { navController.navigate("tasks_screen") },
                modifier = Modifier.padding(bottom = 120.dp),
                containerColor = Color(0xFFBBDEFB), // Color azul
                shape = CircleShape
            ) {
                Text(stringResource(id = R.string.task), color = Color.Black)
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
                Text(stringResource(id = R.string.folder), color = Color.Black)
            }
        }

        // Botón principal "+"
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = Color.Gray
        ) {
            Text(stringResource(id = R.string.add_button), color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }

    val notesAndTasks = listOf(
        "Nota: Revisión de código",
        "Tarea: Comprar materiales",
        "Nota: Planificación semanal",
        "Tarea: Proyecto final"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Texto "Notas y Tareas"
        Text(
            text = stringResource(id = R.string.notes_tasks),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Barra de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

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
            containerColor = Color(0xFFBBDEFB)
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
fun DefaultPreview() {
    ProyectoFinalMovilTheme {
        MainScreen(rememberNavController())
    }
}