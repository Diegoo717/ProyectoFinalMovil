package com.example.proyectofinalmovil.tarea

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavHostController,
    taskId: Int?,
    viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(TaskRepository(TaskDatabase.getDatabase(LocalContext.current).taskDao())))
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskContent by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf("") }
    var taskTime by remember { mutableStateOf("") }
    var isNotificationEnabled by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Verificar si es una nueva tarea
    val isNewTask = taskId == null || taskId == 0

    LaunchedEffect(taskId) {
        if (!isNewTask) {
            taskId?.let { id ->
                val task = viewModel.getTaskById(id)
                task?.let {
                    taskTitle = it.title
                    taskContent = it.content
                    taskDate = it.date
                    taskTime = it.time
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
                            Toast.makeText(context, "Compartiendo la tarea...", Toast.LENGTH_SHORT).show()
                        },
                        text = { Text("Compartir") }
                    )
                    // Opción de activar notificación con checkbox
                    DropdownMenuItem(
                        onClick = {
                            isNotificationEnabled = !isNotificationEnabled
                            showMenu = false
                            Toast.makeText(context, if (isNotificationEnabled) "Notificación activada" else "Notificación desactivada", Toast.LENGTH_SHORT).show()
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Activar Notificación")
                                Spacer(modifier = Modifier.width(8.dp))
                                Checkbox(
                                    checked = isNotificationEnabled,
                                    onCheckedChange = null // Evitar que el Checkbox sea clickeable por separado
                                )
                            }
                        }
                    )
                }
            }

            // Botón de guardar
            IconButton(onClick = {
                if (taskTitle.isNotEmpty() && taskContent.isNotEmpty() && taskDate.isNotEmpty() && taskTime.isNotEmpty()) {
                    if (!isNewTask) {
                        viewModel.update(Task(
                            id = taskId ?: 0,
                            title = taskTitle,
                            content = taskContent,
                            date = taskDate,
                            time = taskTime
                        ))
                        Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.insert(Task(
                            title = taskTitle,
                            content = taskContent,
                            date = taskDate,
                            time = taskTime
                        ))
                        Toast.makeText(context, "Tarea guardada", Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Guardar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            placeholder = { Text(text = "Título de la tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = taskContent,
            onValueChange = { taskContent = it },
            placeholder = { Text(text = "Contenido de la tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        // Mejora del diseño de los botones de selección de fecha y hora
        Button(
            onClick = { showDatePicker(context) { selectedDate -> taskDate = selectedDate } },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)
        ) {
            Text(text = if (taskDate.isEmpty()) "Seleccionar Fecha" else taskDate)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showTimePicker(context) { selectedTime -> taskTime = selectedTime } },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)
        ) {
            Text(text = if (taskTime.isEmpty()) "Seleccionar Hora" else taskTime)
        }
    }
}

private fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        onDateSelected("$selectedDay/${selectedMonth + 1}/$selectedYear")
    }, year, month, day).show()
}

private fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
        onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
    }, hour, minute, true).show()
}
