    package com.example.proyectofinalmovil.nota

    import android.Manifest
    import android.content.pm.PackageManager
    import android.media.MediaPlayer
    import android.media.MediaRecorder
    import android.os.Environment
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
        var showMenu by remember { mutableStateOf(false) } // Estado para el menú desplegable
        val context = LocalContext.current

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

        // Función para comenzar la grabación
        fun startRecording() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                return
            }
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
        }

        // Función para detener la grabación
        fun stopRecording() {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false // Marcamos que ya no estamos grabando
        }

        // Función para reproducir el audio
        fun startPlaying() {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFilePath)
                    prepare()
                    start()
                }
                isPlaying = true // Marcamos que estamos reproduciendo
                Toast.makeText(context, "Reproduciendo...", Toast.LENGTH_SHORT).show()
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    isPlaying = false // Restauramos el estado de reproducción al finalizar
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
            isPlaying = false // Restauramos el estado de reproducción al detenerse
            Toast.makeText(context, "Reproducción detenida", Toast.LENGTH_SHORT).show()
        }

        // Revisar si noteId es null o -1 (u otro indicador) para nueva nota
        val isNewNote = noteId == null || noteId == 0

        // Estado para la URI de la imagen seleccionada
        var imageUri by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(noteId) {
            if (!isNewNote) {
                noteId?.let { id ->
                    val note = viewModel.getNoteById(id)
                    note?.let {
                        noteTitle = it.title
                        noteContent = it.content
                        imageUri = it.imageUri // Cargamos la URI de la imagen guardada
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
                    imageUri = file.absolutePath // Guarda la ruta absoluta del archivo
                    Toast.makeText(context, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Lanzador para manejar permisos de galería
        val requestGalleryPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    pickImageLauncher.launch("image/*")
                } else {
                    Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
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
                pickImageLauncher.launch("image/*")
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

                // Menú de tres puntos
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
                            if (!isNewNote) {
                                // Actualizar una nota existente
                                viewModel.update(
                                    Note(
                                        id = noteId ?: 0,
                                        title = noteTitle,
                                        content = noteContent,
                                        imageUri = imageUri // Guardamos la URI de la imagen
                                    )
                                )
                                Toast.makeText(context, "Nota actualizada", Toast.LENGTH_SHORT).show()
                            } else {
                                // Insertar una nueva nota
                                viewModel.insert(
                                    Note(
                                        title = noteTitle,
                                        content = noteContent,
                                        imageUri = imageUri // Guardamos la URI de la imagen
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

            // Mostrar la imagen seleccionada, si existe
            imageUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(File(uri)),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botones para añadir imagen y audio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Botón para añadir imagen
                IconButton(
                    onClick = { checkGalleryPermission() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFBBDEFB))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Seleccionar Imagen")
                    }
                }

                // Botón para añadir audio (con cambio de color según grabación)
                IconButton(
                    onClick = {
                        if (isRecording) stopRecording() else startRecording()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isRecording) Color.Red else Color(0xFFFFF59D)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Grabar Audio")
                    }
                }

                // Botón para reproducir audio con animación de color
                val buttonColor by animateColorAsState(
                    targetValue = if (isPlaying) Color.Green else Color(0xFFFFF59D)
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
                        .padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = buttonColor)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Reproducir Audio")
                    }
                }
            }
        }
    }
