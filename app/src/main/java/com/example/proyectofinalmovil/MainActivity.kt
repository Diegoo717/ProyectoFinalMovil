package com.example.proyectofinalmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalmovil.nota.NotesScreen
import com.example.proyectofinalmovil.nota.VerNotaScreen
import com.example.proyectofinalmovil.tarea.TasksScreen
import com.example.proyectofinalmovil.tarea.VerTareaScreen
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
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(navController, scope, drawerState)
                    }
                ) {
                    Scaffold(
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
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "main_screen",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("main_screen") { MainScreen(navController) }
                            composable("tasks_screen/{taskId}") { backStackEntry ->
                                val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                                TasksScreen(navController, taskId)
                            }
                            composable("notas_screen/{noteId}") { backStackEntry ->
                                val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                                NotesScreen(navController, noteId)
                            }
                            // Composables para ver notas y tareas
                            composable("ver_nota_screen/{title}/{content}") { backStackEntry ->
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val content = backStackEntry.arguments?.getString("content") ?: ""
                                VerNotaScreen(navController, title, content)
                            }
                            composable("ver_tarea_screen/{title}/{content}/{date}/{time}") { backStackEntry ->
                                val title = backStackEntry.arguments?.getString("title") ?: ""
                                val content = backStackEntry.arguments?.getString("content") ?: ""
                                val date = backStackEntry.arguments?.getString("date") ?: ""
                                val time = backStackEntry.arguments?.getString("time") ?: ""
                                VerTareaScreen(navController, title, content, date, time)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Menú", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("main_screen")
            scope.launch { drawerState.close() }
        }) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "Home", modifier = Modifier.padding(end = 8.dp))
            Text("Home")
        }

        TextButton(onClick = {
            navController.navigate("notas_screen/0")
            scope.launch { drawerState.close() }
        }) {
            Icon(imageVector = Icons.Default.Note, contentDescription = "Notas", modifier = Modifier.padding(end = 8.dp))
            Text("Notas")
        }

        TextButton(onClick = {
            navController.navigate("tasks_screen/0")
            scope.launch { drawerState.close() }
        }) {
            Icon(imageVector = Icons.Default.Task, contentDescription = "Tareas", modifier = Modifier.padding(end = 8.dp))
            Text("Tareas")
        }
    }
}
