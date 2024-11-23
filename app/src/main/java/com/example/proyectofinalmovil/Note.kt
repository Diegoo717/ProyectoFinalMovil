package com.example.proyectofinalmovil.nota

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val mediaUris: List<String> = emptyList(), // Almacenar múltiples URIs (imágenes y videos)
    val audioUri: String? = null // Mantener el URI del audio
)
