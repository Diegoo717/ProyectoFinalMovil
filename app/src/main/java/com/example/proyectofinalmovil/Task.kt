package com.example.proyectofinalmovil.tarea

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String, // Campo para la fecha
    val time: String,  // Campo para la hora
    val imageUri: String? = null,
    val audioUri: String? = null
)
