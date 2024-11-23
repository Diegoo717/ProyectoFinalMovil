package com.example.proyectofinalmovil.nota

import androidx.core.content.FileProvider
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.TakeVideo
import androidx.compose.ui.viewinterop.AndroidView
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.io.File
import java.io.IOException
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaScreen(
    navController: NavHostController,
    noteId: Int?,
    isReadOnly: Boolean = false,
    viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(NoteRepository(NoteDatabase.getDatabase(LocalContext.current).noteDao())))
) {
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Variables de MediaRecorder y MediaPlayer
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    // Ruta para guardar el archivo de audio
    val audioFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/nota_audio.3gp"

    // Estado para manejar los permisos
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

    // Estado para almacenar URIs de medios (imágenes y videos)
    var mediaUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var isVideo by remember { mutableStateOf(false) }

    // Estado para el archivo temporal de la captura
    var tempMediaUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para tomar una foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = TakePicture(),
        onResult = { success ->
            if (success) {
                tempMediaUri?.let { uri ->
                    mediaUris = mediaUris + uri.toString()  // Agregar el URI a la lista
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
                mediaUris = mediaUris + uri.toString()  // Agregar el URI a la lista
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

    LaunchedEffect(noteId) {
        if (noteId != null && noteId != 0) {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                noteTitle = it.title
                noteContent = it.content
                mediaUris = it.mediaUris // Cargar las URIs almacenadas en la base de datos
            }
        }
    }

    // Lanzador para seleccionar imagen o video
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Mostrar el URI del archivo seleccionado
            Toast.makeText(context, "Archivo seleccionado: $uri", Toast.LENGTH_LONG).show()

            val type = context.contentResolver.getType(uri)

            // Verifica si el archivo es un video
            isVideo = type?.startsWith("video") == true

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val extension = if (isVideo) ".mp4" else ".jpg"
                val file = File(context.filesDir, "media_${System.currentTimeMillis()}$extension")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                mediaUris = mediaUris + file.absolutePath // Agregar el URI a la lista
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
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            requestGalleryPermissionLauncher.launch(permission)
        } else {
            pickMediaLauncher.launch("*/*")
        }
    }

    // Funciones de grabación
    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                reset()
                release()
                isRecording = false
                Toast.makeText(context, "Grabación finalizada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
    }

    // Funciones de reproducción
    fun startPlaying() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                isPlaying = true
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al reproducir el audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stopPlaying() {
        mediaPlayer?.apply {
            stop()
            release()
            isPlaying = false
        }
        mediaPlayer = null
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

            // Menú de tres puntos
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Compartiendo la nota...", Toast.LENGTH_SHORT).show()
                        },
                        text = { Text("Compartir") }
                    )
                }
            }

            // Botón de guardar
            if (!isReadOnly) {
                IconButton(onClick = {
                    if (noteTitle.isNotEmpty() && noteContent.isNotEmpty()) {
                        if (noteId != null && noteId != 0) {
                            viewModel.update(
                                Note(
                                    id = noteId,
                                    title = noteTitle,
                                    content = noteContent,
                                    mediaUris = mediaUris // Guardar la lista de URIs
                                )
                            )
                            Toast.makeText(context, "Nota actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.insert(
                                Note(
                                    title = noteTitle,
                                    content = noteContent,
                                    mediaUris = mediaUris // Guardar la lista de URIs
                                )
                            )
                            Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                        }
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Guardar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = noteTitle,
            onValueChange = { if (!isReadOnly) noteTitle = it },
            placeholder = { Text(text = "Título de la nota") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = isReadOnly
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = noteContent,
            onValueChange = { if (!isReadOnly) noteContent = it },
            placeholder = { Text(text = "Contenido de la nota") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mostrar las imágenes o videos seleccionados
        // Función auxiliar para determinar si la URI es un video
        fun isVideo(uri: String): Boolean {
            return uri.endsWith(".mp4", ignoreCase = true) ||
                    uri.endsWith(".avi", ignoreCase = true) ||
                    uri.endsWith(".mov", ignoreCase = true) ||
                    uri.endsWith(".mkv", ignoreCase = true)
        }

        //Mostrar el contenido multimedia
        // Lista mutable para almacenar las URIs multimedia y controlar el estado
        val mediaUrisState = remember { mutableStateListOf<String>() }

        // Inicializamos mediaUrisState con mediaUris solo si no está vacía
        LaunchedEffect(mediaUris) {
            if (!mediaUris.isNullOrEmpty()) {
                mediaUrisState.clear()
                mediaUrisState.addAll(mediaUris)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mediaUrisState) { mediaUri ->
                Log.d("MediaUris", "Procesando mediaUri: $mediaUri")

                if (isVideo(mediaUri)) {
                    Log.d("MediaUris", "El archivo es un video: $mediaUri")

                    var isPlaying by remember { mutableStateOf(false) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AndroidView(
                                factory = { context ->
                                    VideoView(context).apply {
                                        // Asegúrate de que la URI sea válida antes de cargarla
                                        try {
                                            Log.d("MediaUris", "Intentando cargar video desde URI: $mediaUri")
                                            setVideoURI(Uri.parse(mediaUri))

                                            setOnPreparedListener { mp ->
                                                Log.d("MediaUris", "Video preparado con éxito: $mediaUri")
                                                mp.setOnVideoSizeChangedListener { _, _, _ ->
                                                    requestLayout()
                                                }
                                                pause() // Pausar por defecto después de prepararse
                                            }

                                            setOnCompletionListener {
                                                Log.d("MediaUris", "Reproducción del video completada: $mediaUri")
                                                isPlaying = false
                                            }

                                            setOnErrorListener { _, what, extra ->
                                                Log.e("VideoViewError", "Error en VideoView para URI: $mediaUri, Error: $what, Extra: $extra")
                                                false
                                            }
                                        } catch (e: Exception) {
                                            Log.e("VideoLoadError", "Excepción al cargar el video desde URI: $mediaUri, Error: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f) // Ajustar ancho para dejar espacio para el botón de eliminar
                                    .heightIn(max = 250.dp)
                                    .widthIn(max = 350.dp)
                                    .aspectRatio(16 / 9f),
                                update = { videoView ->
                                    if (isPlaying) {
                                        Log.d("MediaUris", "Iniciando reproducción del video: $mediaUri")
                                        videoView.start()
                                    } else {
                                        Log.d("MediaUris", "Pausando reproducción del video: $mediaUri")
                                        videoView.pause()
                                    }
                                }
                            )

                            Button(
                                onClick = {
                                    isPlaying = !isPlaying
                                    Log.d("MediaUris", if (isPlaying) "Reproduciendo video: $mediaUri" else "Pausando video: $mediaUri")
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(if (isPlaying) "Pausar" else "Reproducir")
                            }
                        }

                        // Botón para eliminar el video
                        IconButton(
                            onClick = {
                                Log.d("MediaUris", "Eliminando video: $mediaUri")
                                mediaUrisState.remove(mediaUri)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Video")
                        }
                    }
                } else {
                    Log.d("MediaUris", "El archivo es una imagen: $mediaUri")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Mostrar imagen
                        Image(
                            painter = rememberAsyncImagePainter(mediaUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxWidth(0.8f) // Ajustar ancho para dejar espacio para el botón de eliminar
                                .heightIn(max = 250.dp)
                                .widthIn(max = 350.dp)
                        )

                        // Botón para eliminar la imagen
                        IconButton(
                            onClick = {
                                Log.d("MediaUris", "Eliminando imagen: $mediaUri")
                                mediaUrisState.remove(mediaUri)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Imagen")
                        }
                    }
                }
            }

            // Espaciador para separar elementos
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botones para grabar o reproducir audio y otras funcionalidades
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón de cámara
                    IconButton(
                        onClick = {
                            Log.d("Botones", "Clic en botón de cámara")
                            checkCameraPermission()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Abrir cámara")
                            Text(text = "Cámara", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Botón para seleccionar imagen o video
                    if (!isReadOnly) {
                        IconButton(
                            onClick = {
                                Log.d("Botones", "Clic en botón de galería")
                                checkGalleryPermission()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Seleccionar imagen o video"
                                )
                            }
                        }

                        // Botón de grabación
                        val recordButtonColor by animateColorAsState(
                            targetValue = if (isRecording) Color.Red else Color(0xFFFFF59D)
                        )

                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    Log.d("Botones", "Clic en botón para detener grabación")
                                    stopRecording()
                                } else {
                                    Log.d("Botones", "Clic en botón para iniciar grabación")
                                    startRecording()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = recordButtonColor)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Grabar Audio")
                            }
                        }

                        // Botón de reproducción de audio
                        val playButtonColor by animateColorAsState(
                            targetValue = if (isPlaying) Color.Green else Color(0xFFFFF59D)
                        )

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    Log.d("Botones", "Clic en botón para detener reproducción de audio")
                                    stopPlaying()
                                } else {
                                    Log.d("Botones", "Clic en botón para iniciar reproducción de audio")
                                    startPlaying()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = playButtonColor)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Reproducir Audio")
                            }
                        }
                    }
                }
            }
        }
    }
}