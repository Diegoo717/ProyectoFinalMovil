package com.example.proyectofinalmovil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.example.proyectofinalmovil.nota.Note
import com.example.proyectofinalmovil.nota.NoteDatabase
import com.example.proyectofinalmovil.nota.NoteViewModel
import com.example.proyectofinalmovil.nota.NoteViewModelFactory
import com.example.proyectofinalmovil.nota.NoteItem
import com.example.proyectofinalmovil.tarea.Task
import com.example.proyectofinalmovil.tarea.TaskDatabase
import com.example.proyectofinalmovil.tarea.TaskItem
import com.example.proyectofinalmovil.tarea.TaskViewModel
import com.example.proyectofinalmovil.tarea.TaskViewModelFactory
import com.example.proyectofinalmovil.nota.NoteRepository
import com.example.proyectofinalmovil.tarea.TaskRepository

@Composable
fun MainScreen(
    navController: NavHostController,
    noteViewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(NoteRepository(NoteDatabase.getDatabase(LocalContext.current).noteDao()))),
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(TaskRepository(TaskDatabase.getDatabase(LocalContext.current).taskDao())))
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val notes by noteViewModel.notes.collectAsState(initial = emptyList())
    val tasks by taskViewModel.tasks.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate("tasks_screen/0")
                                isFabExpanded = false
                            },
                            containerColor = Color(0xFFBBDEFB),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("Tarea", color = Color.Black)
                        }

                        FloatingActionButton(
                            onClick = {
                                navController.navigate("notas_screen/0")
                                isFabExpanded = false
                            },
                            containerColor = Color(0xFFFFF59D),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("Nota", color = Color.Black)
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = Color.Gray
                ) {
                    Text("+", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Notas") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tareas") }
                )
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn {
                        items(notes) { note ->
                            NoteItem(
                                note = note,
                                onDeleteClick = { noteViewModel.delete(note) },
                                onEditClick = {
                                    navController.navigate("notas_screen/${note.id}")
                                }
                            )
                        }
                    }
                }
                1 -> {
                    LazyColumn {
                        items(tasks) { task ->
                            TaskItem(
                                task = task,
                                onDeleteClick = { taskViewModel.delete(task) },
                                onEditClick = {
                                    navController.navigate("tasks_screen/${task.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
