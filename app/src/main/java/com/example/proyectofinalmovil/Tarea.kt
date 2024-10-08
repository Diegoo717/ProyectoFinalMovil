package com.example.proyectofinalmovil.tarea

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsWithImePadding
import java.util.*

@Composable
fun TasksScreen(navController: NavHostController) {
    var taskTitle by remember { mutableStateOf("") }
    var taskContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(false) } // Controlar notificaciones
    val context = LocalContext.current

    // Fecha y hora seleccionadas
    var selectedDate by remember { mutableStateOf("Seleccionar fecha") }
    var selectedTime by remember { mutableStateOf("Seleccionar hora") }

    // Mostrar DatePicker y TimePicker
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTime = String.format("%02d:%02d", hour, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                value = taskTitle,
                onValueChange = { taskTitle = it },
                placeholder = { Text(text = "Título de la tarea") },
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
                            showMenu = !showMenu
                        }
                )

                // DropdownMenu para guardar, compartir, borrar y activar/desactivar notificaciones
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (notificationEnabled) "Desactivar notificación" else "Activar notificación") },
                        onClick = {
                            notificationEnabled = !notificationEnabled
                            showMenu = false
                            val message = if (notificationEnabled) {
                                "Notificación activada"
                            } else {
                                "Notificación desactivada"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = {
                            Icon(imageVector = if (notificationEnabled) Icons.Filled.NotificationsOff else Icons.Filled.Notifications, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Guardar") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Tarea guardada", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Compartir tarea", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Borrar") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Tarea borrada", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }

        // Seleccionar fecha
        Button(
            onClick = {
                datePickerDialog.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = selectedDate)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seleccionar hora
        Button(
            onClick = {
                timePickerDialog.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = selectedTime)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Área de texto para los detalles de la tarea
        TextField(
            value = taskContent,
            onValueChange = { taskContent = it },
            placeholder = { Text(text = "Detalles de la tarea") },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
