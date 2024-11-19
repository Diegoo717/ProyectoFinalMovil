package com.example.proyectofinalmovil

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.proyectofinalmovil.nota.NotaScreen
import com.example.proyectofinalmovil.nota.VerNotaScreen
import com.example.proyectofinalmovil.tarea.TasksScreen
import com.example.proyectofinalmovil.tarea.VerTareaScreen
import com.example.proyectofinalmovil.ui.theme.ProyectoFinalMovilTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.proyectofinalmovil.NotificationReceiver
import java.util.*

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permiso concedido, puedes mostrar notificaciones
        } else {
            // El permiso fue denegado
        }
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permiso concedido, puedes acceder a la galería
        } else {
            // El permiso fue denegado
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar el permiso de notificaciones en Android 13 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Solicitar el permiso para acceder a la galería
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Para versiones anteriores a Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Crear el canal de notificación si el sistema operativo es Oreo o superior
        createNotificationChannel()

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
                                NotaScreen(navController, noteId)
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

    // Esta función es la que maneja la creación del canal de notificaciones
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TASKS_CHANNEL_ID",
                "Tareas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de tareas"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Función para programar una notificación con AlarmManager
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTaskReminder(context: Context, taskTitle: String, taskDate: String, taskTime: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("taskTitle", taskTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Convertir la fecha y hora a un formato que AlarmManager pueda usar
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, taskTime.split(":")[0].toInt())
            set(Calendar.MINUTE, taskTime.split(":")[1].toInt())
            set(Calendar.DAY_OF_MONTH, taskDate.split("/")[0].toInt())
            set(Calendar.MONTH, taskDate.split("/")[1].toInt() - 1) // Meses en Calendar son 0-based
            set(Calendar.YEAR, taskDate.split("/")[2].toInt())
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
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
