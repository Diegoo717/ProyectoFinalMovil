package com.example.proyectofinalmovil.tarea

import android.provider.Settings
import com.example.proyectofinalmovil.NotificationReceiver
import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.TakeVideo
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.IOException
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavHostController,
    taskId: Int? = null,
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

    // Variables de MediaRecorder y MediaPlayer
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    // Ruta para guardar el archivo de audio
    val audioFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/nota_audio.3gp"

    // Solicitador de permisos
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Estado para saber si estamos grabando
    var isRecording by remember { mutableStateOf(false) }

    // Estado para saber si estamos reproduciendo
    var isPlaying by remember { mutableStateOf(false) }

    var mediaUri by remember { mutableStateOf<String?>(null) }
    var isVideo by remember { mutableStateOf(false) }

    // Estado para el archivo temporal de la captura
    var tempMediaUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para tomar una foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = TakePicture(),
        onResult = { success ->
            if (success) {
                tempMediaUri?.let { uri ->
                    mediaUri = uri.toString()
                    isVideo = false
                    Toast.makeText(context, "Imagen capturada correctamente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No se pudo capturar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Lanzador para grabar un video
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = TakeVideo(),
        onResult = { uri ->
            if (uri != null) {
                mediaUri = uri.toString()
                isVideo = true
                Toast.makeText(context, "Video capturado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se pudo grabar el video", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Estado para mostrar el diálogo de selección de cámara
    var showDialog by remember { mutableStateOf(false) }

    // Solicitar el permiso de la cámara
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showDialog = true
            } else {
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Función para verificar y solicitar el permiso de cámara
    fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(permission)
        } else {
            showDialog = true
        }
    }

    // Mostrar el cuadro de diálogo para elegir entre foto o video
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar acción") },
            text = { Text("¿Qué deseas hacer?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    // Crear archivo para la foto y lanzar la cámara
                    val photoFile = File(context.filesDir, "temp_photo.jpg")
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "com.example.proyectofinalmovil.fileprovider",
                        photoFile
                    )
                    tempMediaUri = photoUri
                    takePictureLauncher.launch(photoUri)
                }) {
                    Text("Tomar foto")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    // Crear archivo para el video y lanzar la grabación
                    val videoFile = File(context.filesDir, "temp_video.mp4")
                    val videoUri = FileProvider.getUriForFile(
                        context,
                        "com.example.proyectofinalmovil.fileprovider",
                        videoFile
                    )
                    tempMediaUri = videoUri
                    takeVideoLauncher.launch(videoUri)
                }) {
                    Text("Grabar video")
                }
            }
        )
    }

    LaunchedEffect(taskId) {
        if (taskId != null && taskId != 0) {
            val note = viewModel.getTaskById(taskId)
            note?.let {
                taskTitle = it.title
                taskContent = it.content
                mediaUri = it.imageUri
                if (!mediaUri.isNullOrEmpty()) {
                    val type = context.contentResolver.getType(Uri.parse(mediaUri))
                    isVideo = type?.startsWith("video") == true
                }
            }
        }
    }

    // Lanzador para seleccionar imagen o video
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Mostrar el URI del archivo seleccionado (para depuración)
            Toast.makeText(context, "Video URI: $uri", Toast.LENGTH_LONG).show()

            val type = context.contentResolver.getType(uri)

            // Verifica si el archivo es un video
            isVideo = type?.startsWith("video") == true

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val extension = if (isVideo) ".mp4" else ".jpg" // Extensión según el tipo de archivo
                val file = File(context.filesDir, "media_${System.currentTimeMillis()}$extension")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                mediaUri = file.absolutePath // Guarda la ruta del archivo (imagen o video)
                Toast.makeText(
                    context,
                    if (isVideo) "Video guardado correctamente" else "Imagen guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lanzador para manejar permisos de galería
    val requestGalleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                pickMediaLauncher.launch("*/*")
            } else {
                Toast.makeText(context, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Función para verificar el permiso de la galería
    fun checkGalleryPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES // Android 13 y superiores
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE // Android 12 y anteriores
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            requestGalleryPermissionLauncher.launch(permission)
        } else {
            pickMediaLauncher.launch("*/*")
        }
    }

    fun startRecording() {
        // Verificamos si el permiso ya ha sido otorgado
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setOutputFile(audioFilePath)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    prepare()
                    start()
                }
                isRecording = true // Marcamos que estamos grabando
                Toast.makeText(context, "Grabando...", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si no se tiene el permiso, lo solicitamos
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Función para detener la grabación
    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
    }

    // Función para reproducir el audio
    fun startPlaying() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
            isPlaying = true
            Toast.makeText(context, "Reproduciendo...", Toast.LENGTH_SHORT).show()
            mediaPlayer?.setOnCompletionListener {
                it.release()
                isPlaying = false
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Error al reproducir el audio", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para detener la reproducción
    fun stopPlaying() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
        Toast.makeText(context, "Reproducción detenida", Toast.LENGTH_SHORT).show()
    }

    // Variable para almacenar la URI de la imagen seleccionada
    var imageUri = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(taskId) {
        if (!isNewTask) {
            taskId?.let { id ->
                val task = viewModel.getTaskById(id)
                task?.let {
                    taskTitle = it.title
                    taskContent = it.content
                    taskDate = it.date
                    taskTime = it.time
                    mediaUri = it.imageUri // Usar mediaUri en lugar de imageUri.value
                }
            }
        }
    }

    //Lanzador para seleccionar la imagen
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "imagen_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                imageUri.value = file.absolutePath // Guarda la ruta absoluta del archivo
                Toast.makeText(context, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
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
                    // Opción de activar notificación con checkbox
                    DropdownMenuItem(
                        onClick = {
                            isNotificationEnabled = !isNotificationEnabled
                            showMenu = false
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Activar Notificación")
                                Spacer(modifier = Modifier.width(8.dp))
                                Checkbox(
                                    checked = isNotificationEnabled,
                                    onCheckedChange = null
                                )
                            }
                        }
                    )
                }
            }

            IconButton(onClick = {
                // Validar que todos los campos necesarios no estén vacíos
                if (taskTitle.isNotEmpty() && taskContent.isNotEmpty() && taskDate.isNotEmpty() && taskTime.isNotEmpty()) {
                    // Crear o actualizar la tarea con todos los campos necesarios
                    val task = Task(
                        id = taskId ?: 0,
                        title = taskTitle,
                        content = taskContent,
                        date = taskDate,
                        time = taskTime,
                        imageUri = mediaUri // Usar la variable que almacena la URI de imagen/video
                    )

                    if (!isNewTask) {
                        // Actualizar la tarea existente
                        viewModel.update(task)
                        Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        // Insertar una nueva tarea
                        viewModel.insert(task)
                        Toast.makeText(context, "Tarea guardada", Toast.LENGTH_SHORT).show()
                    }

                    // Programar la notificación si está habilitada
                    if (isNotificationEnabled) {
                        scheduleNotification(context, taskTitle, taskDate, taskTime)
                    }

                    // Navegar hacia atrás después de guardar
                    navController.popBackStack()
                } else {
                    // Mostrar mensaje de error si algún campo está vacío
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

        // Mostrar la imagen o video seleccionado
        mediaUri?.let {
            if (isVideo) {
                // Usar AndroidView para integrar el VideoView
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            try {
                                // Establecer la URI del video
                                setVideoURI(Uri.parse(it))  // Verifica que la URI sea válida
                                start()
                                setOnErrorListener { mp, what, extra ->
                                    // Log de error si ocurre algún problema con el video
                                    Log.e("VideoViewError", "Error en VideoView: $what, $extra")
                                    false
                                }
                            } catch (e: Exception) {
                                // Log de error si hay un problema al configurar el VideoView
                                Log.e("VideoViewError", "Error al configurar el VideoView: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa todo el ancho disponible
                        .heightIn(max = 250.dp) // Limita la altura máxima del video
                        .widthIn(max = 350.dp)  // Limita el ancho máximo del video
                        .aspectRatio(16 / 9f)  // Ajusta la relación de aspecto
                )
            } else {
                // Mostrar imagen
                Image(painter = rememberAsyncImagePainter(it), contentDescription = "Imagen seleccionada", modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)  // Limita la altura máxima de la imagen
                    .widthIn(max = 350.dp)   // Limita el ancho máximo de la imagen
                )
            }
        }

        // Botones para añadir imagen y audio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            IconButton(
                onClick = { checkCameraPermission() },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Abrir cámara")
                    Text(text = "Cámara", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Botón para añadir imagen
            IconButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        checkGalleryPermission()
                    } else {
                        pickImageLauncher.launch("image/*")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp), // Ajusta el tamaño del botón para la galería
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFBBDEFB))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "Añadir Imagen")
                }
            }

            // Botón para añadir audio (con cambio de color según grabación)
            IconButton(
                onClick = {
                    if (mediaRecorder == null) {
                        startRecording()
                    } else {
                        stopRecording()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp), // Ajusta el tamaño del botón de grabación
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isRecording) Color.Red else Color(0xFFFFF59D) // Rojo si estamos grabando
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Añadir Audio")
                }
            }

            // Botón para reproducir audio con animación de color
            val buttonColor by animateColorAsState(
                targetValue = if (isPlaying) Color.Green else Color(0xFFFFF59D) // Verde si está reproduciendo
            )

            IconButton(
                onClick = {
                    if (isPlaying) {
                        stopPlaying()
                    } else {
                        startPlaying()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp), // Ajusta el tamaño del botón de reproducción
                colors = IconButtonDefaults.iconButtonColors(containerColor = buttonColor)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Reproducir Audio")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showDatePicker(context) { selectedDate -> taskDate = selectedDate } },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(if (taskDate.isNotEmpty()) taskDate else "Seleccionar fecha")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showTimePicker(context) { selectedTime -> taskTime = selectedTime } },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(if (taskTime.isNotEmpty()) taskTime else "Seleccionar hora")
        }
    }
}

fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = "$dayOfMonth/${month + 1}/$year"
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}

fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = "${if (hourOfDay < 10) "0" else ""}$hourOfDay:${if (minute < 10) "0" else ""}$minute"
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
    timePickerDialog.show()
}


// Función para programar la notificación
fun scheduleNotification(context: Context, taskTitle: String, taskDate: String, taskTime: String) {
    // Verificar si se tiene el permiso para programar alarmas exactas
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
            return
        }
    }

    val calendar = Calendar.getInstance()
    val dateParts = taskDate.split("/")
    val timeParts = taskTime.split(":")

    // Establecer la fecha y hora de la notificación
    calendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
    calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
    calendar.set(Calendar.YEAR, dateParts[2].toInt())
    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
    calendar.set(Calendar.SECOND, 0)

    // Verificar que la fecha y hora no sean en el pasado
    if (calendar.timeInMillis < System.currentTimeMillis()) {
        Toast.makeText(context, "La hora seleccionada ya pasó", Toast.LENGTH_SHORT).show()
        return
    }

    // Crear el Intent para disparar la notificación
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("taskTitle", taskTitle)
    }

    // Crear un PendingIntent para que se ejecute cuando el AlarmManager dispare la notificación
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Obtener una instancia de AlarmManager
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Programar la notificación para que se dispare a la hora seleccionada
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

}
