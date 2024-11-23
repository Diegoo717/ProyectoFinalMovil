package com.example.proyectofinalmovil.nota

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    // Obtenemos todas las notas desde el repositorio
    val notes: StateFlow<List<Note>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Funciones para manipular las notas
    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    fun insert(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    // Función para agregar una imagen o video a una nota
    fun addMediaToNote(noteId: Int, mediaUri: String) {
        viewModelScope.launch {
            val note = getNoteById(noteId)
            if (note != null) {
                val updatedMediaUris = note.mediaUris.toMutableList().apply {
                    add(mediaUri) // Añadir el nuevo URI a la lista
                }
                val updatedNote = note.copy(mediaUris = updatedMediaUris)
                update(updatedNote)
            }
        }
    }

    // Función para eliminar un archivo (imagen o video) de una nota
    fun removeMediaFromNote(noteId: Int, mediaUri: String) {
        viewModelScope.launch {
            val note = getNoteById(noteId)
            if (note != null) {
                val updatedMediaUris = note.mediaUris.toMutableList().apply {
                    remove(mediaUri) // Eliminar el URI de la lista
                }
                val updatedNote = note.copy(mediaUris = updatedMediaUris)
                update(updatedNote)
            }
        }
    }
}
