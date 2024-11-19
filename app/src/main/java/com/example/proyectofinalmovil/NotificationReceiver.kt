package com.example.proyectofinalmovil

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.core.app.ActivityCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Tarea pendiente"

        // Crear el canal de notificación (solo en Android O o superior)
        createNotificationChannel(context)

        // Verificar si el permiso para notificaciones ha sido concedido (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showNotification(context, taskTitle)
            } else {
                // El permiso no está concedido, podemos manejar el caso
                // En este caso, simplemente no mostramos la notificación, pero podrías informar al usuario
            }
        } else {
            // En versiones anteriores a Android 13, no es necesario verificar el permiso
            showNotification(context, taskTitle)
        }
    }

    private fun showNotification(context: Context, taskTitle: String) {
        val builder = NotificationCompat.Builder(context, "TASKS_CHANNEL_ID")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio de Tarea")
            .setContentText("Tienes una tarea pendiente: $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        // Mostrar la notificación
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(0, builder.build())
    }

    // Función para crear el canal de notificación (solo Android O o superior)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tareas Pendientes"
            val descriptionText = "Canal de notificaciones para tareas pendientes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TASKS_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
