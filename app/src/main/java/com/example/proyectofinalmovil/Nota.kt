package com.example.proyectofinalmovil.nota

import androidx.core.content.FileProvider
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.TakeVideo
import androidx.compose.ui.viewinterop.AndroidView
import android.Manifest
import android.content.Context
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
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
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

    // Estado para manejar los permisos
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var isRecording = false
    var isPlaying = false
    var audioFilePath: String? = null
    val audioFilesState = remember { mutableStateListOf<String>() }

    // Estado para almacenar URIs de medios (imágenes y videos)
    var mediaUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var isVideo by remember { mutableStateOf(false) }

    // Estado para el archivo de la captura
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
                mediaUris = it.mediaUris.distinct() // Elimina duplicados si ya existen
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
    // Función para generar una ruta de archivo única para cada grabación
    fun generateAudioFilePath(context: Context): String {
        // Generar un nombre único usando el timestamp actual
        return "${context.externalCacheDir?.absolutePath}/audio_${System.currentTimeMillis()}.3gp"
    }

    // Funciones de grabación
    fun startRecording(context: Context) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Generar la ruta del archivo para esta grabación
            audioFilePath = generateAudioFilePath(context)
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

    // Función para detener la grabación y agregar el nuevo audio a la lista
    fun stopRecording(context: Context, audioFilesState: MutableList<String>) {
        mediaRecorder?.apply {
            try {
                stop()
                reset()
                release()
                isRecording = false
                Toast.makeText(context, "Grabación finalizada", Toast.LENGTH_SHORT).show()

                // Agregar la ruta del archivo de audio a la lista de audios
                if (audioFilePath != null) {
                    audioFilesState.add(audioFilePath!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
    }

    // Funciones de reproducción (se actualizan para permitir múltiples audios)
    fun startPlaying(context: Context, audioPath: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioPath)
                prepare()
                start()
                isPlaying = true
                setOnCompletionListener {
                    isPlaying = false
                }
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
            IconButton(onClick = { /* Acción para regresar */ }) {
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
            IconButton(onClick = {
                if (noteTitle.isNotEmpty() && noteContent.isNotEmpty()) {
                    // Combina mediaUris y audioFilesState evitando duplicados
                    val combinedMediaUris = (mediaUris + audioFilesState).distinct()

                    if (noteId != null && noteId != 0) {
                        // Actualizar nota existente
                        viewModel.update(
                            Note(
                                id = noteId,
                                title = noteTitle,
                                content = noteContent,
                                mediaUris = combinedMediaUris // Guarda solo URIs únicas
                            )
                        )
                        Toast.makeText(context, "Nota actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        // Insertar nueva nota
                        viewModel.insert(
                            Note(
                                title = noteTitle,
                                content = noteContent,
                                mediaUris = combinedMediaUris // Guarda solo URIs únicas
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

        //Mostrar los audios grabados
        LaunchedEffect(mediaUris) {
            if (!mediaUris.isNullOrEmpty()) {
                // Filtrar solo archivos con extensiones .mp3 o .3gp y eliminar duplicados
                val filteredAudioUris = mediaUris
                    .filter { it.endsWith(".mp3", ignoreCase = true) || it.endsWith(".3gp", ignoreCase = true) } // .mp3 y .3gp
                    .distinct() // Asegurarse de que no haya duplicados

                audioFilesState.clear() // Limpiar el estado antes de agregar nuevos valores
                audioFilesState.addAll(filteredAudioUris)
            } else {
                // Limpiar si no hay mediaUris
                audioFilesState.clear()
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(audioFilesState) { audioPath ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Mostrar el nombre del archivo de audio (sin la ruta completa)
                    Text(
                        text = "Audio: ${audioPath.substringAfterLast('/')}",
                        modifier = Modifier.weight(1f)
                    )

                    // Botón para reproducir el audio
                    // Crear un estado para manejar la reproducción del audio
                    var isAudioPlaying by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            if (isAudioPlaying) {
                                // Si está reproduciendo, pausamos el audio
                                stopPlaying()  // Detenemos la reproducción
                            } else {
                                // Si está pausado, reproducimos el audio
                                startPlaying(context, audioPath)  // Iniciamos la reproducción
                            }
                            isAudioPlaying = !isAudioPlaying  // Cambiar el estado de reproducción
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAudioPlaying) "Pausar Audio" else "Reproducir Audio"
                        )
                    }

                    // Icono para eliminar el audio
                    IconButton(
                        onClick = {
                            Log.d("AudioFiles", "Eliminando audio: $audioPath")
                            audioFilesState.remove(audioPath) // Eliminar de la lista
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Audio")
                    }
                }
            }
        }

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
                val uniqueMediaUris = mediaUris.distinct() // Asegúrate de eliminar duplicados
                if (mediaUrisState != uniqueMediaUris) { // Compara los contenidos de las listas
                    mediaUrisState.clear() // Limpiar el estado actual
                    mediaUrisState.addAll(uniqueMediaUris) // Agregar URIs únicas
                }
            } else {
                // Si mediaUris está vacío o es nulo, limpia el estado
                mediaUrisState.clear()
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mediaUrisState) { mediaUri ->
                Log.d("MediaUris", "Procesando mediaUri: $mediaUri")

                // Omitir archivos de audio
                if (mediaUri.endsWith(".3gp", ignoreCase = true) || mediaUri.endsWith(".mp3", ignoreCase = true)) {
                    Log.d("MediaUris", "Archivo de audio detectado, no se mostrará: $mediaUri")
                    return@items // Salir de la iteración para este elemento
                }

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
                                    .fillMaxWidth(0.8f) // Ajustar ancho
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

                            IconButton(
                                onClick = {
                                    isPlaying = !isPlaying
                                    Log.d("MediaUris", if (isPlaying) "Reproduciendo video: $mediaUri" else "Pausando video: $mediaUri")
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar Video" else "Reproducir Video"
                                )
                            }
                        }

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

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        var isExpanded by remember { mutableStateOf(false) }

                        if (isExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .clickable { isExpanded = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(mediaUri),
                                    contentDescription = "Imagen expandida",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 600.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(mediaUri),
                                    contentDescription = "Imagen seleccionada",
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .heightIn(max = 250.dp)
                                        .clickable { isExpanded = true },
                                    contentScale = ContentScale.Crop
                                )

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
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Seleccionar imagen o video")
                            }
                        }

                        val recordButtonColor by animateColorAsState(
                            targetValue = if (isRecording) Color.Red else Color(0xFFFFF59D)
                        )

                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    stopRecording(context, audioFilesState)
                                } else {
                                    startRecording(context)
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
                    }
                }
            }
        }
    }
}



