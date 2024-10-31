package com.example.proyectofinalmovil.tarea

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyectofinalmovil.R
import com.google.accompanist.insets.imePadding
import java.util.*

@Composable
fun TasksScreen(navController: NavHostController) {
    var taskTitle by remember { mutableStateOf("") }
    var taskContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var notificationEnabled by remember { mutableStateOf(false) }

    // Variables para manejar el estado de los diálogos
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                contentDescription = stringResource(id = R.string.back_button),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )

            // Título centrado
            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                placeholder = { Text(text = stringResource(id = R.string.task_title_placeholder)) },
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
                    contentDescription = stringResource(id = R.string.menu),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            showMenu = !showMenu
                        }
                )

                // DropdownMenu para guardar, borrar, compartir
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.save)) },
                        onClick = {
                            showMenu = false
                            println("Guardar tarea")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.share)) },
                        onClick = {
                            showMenu = false
                            println("Compartir tarea")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.delete)) },
                        onClick = {
                            showMenu = false
                            println("Borrar tarea")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row para los botones de seleccionar hora y fecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón para seleccionar la hora
            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f).padding(end = 8.dp) // Añadir margen derecho
            ) {
                Text(text = if (selectedTime.isEmpty()) stringResource(id = R.string.select_time) else selectedTime)
            }

            // Botón para seleccionar la fecha
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f).padding(start = 8.dp) // Añadir margen izquierdo
            ) {
                Text(text = if (selectedDate.isEmpty()) stringResource(id = R.string.select_date) else selectedDate)
            }
        }

        // Mostrar el diálogo de selección de hora
        if (showTimePicker) {
            val currentTime = Calendar.getInstance()
            TimePickerDialog(
                LocalContext.current,
                { _: TimePicker, hour: Int, minute: Int ->
                    selectedTime = String.format("%02d:%02d", hour, minute)
                    showTimePicker = false // Cierra el diálogo
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
            ).show()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el diálogo de selección de fecha
        if (showDatePicker) {
            val currentDate = Calendar.getInstance()
            DatePickerDialog(
                LocalContext.current,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    showDatePicker = false // Cierra el diálogo
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checkbox para activar notificaciones
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = notificationEnabled,
                onCheckedChange = { notificationEnabled = it }
            )
            Text(text = stringResource(id = R.string.enable_notifications)) // Asegúrate de definir este string en strings.xml
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Área de texto para los detalles de la tarea
        TextField(
            value = taskContent,
            onValueChange = { taskContent = it },
            placeholder = { Text(text = stringResource(id = R.string.task_content_placeholder)) },
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
                contentDescription = stringResource(id = R.string.add_image),
                modifier = Modifier.size(48.dp)
            )
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = stringResource(id = R.string.add_audio),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
