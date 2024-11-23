package com.example.proyectofinalmovil

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromMediaUris(mediaUris: List<String>?): String {
        return mediaUris?.joinToString(",") ?: "" // Manejo de listas nulas
    }

    @TypeConverter
    fun toMediaUris(mediaUris: String?): List<String> {
        return mediaUris?.split(",")?.filter { it.isNotEmpty() } ?: emptyList() // Manejo de cadenas nulas o vacías
    }
}
