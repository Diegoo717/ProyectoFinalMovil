package com.example.proyectofinalmovil.tarea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    // Obtenemos todas las tareas desde el repositorio
    val tasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Funciones para manipular las tareas
    suspend fun getTaskById(id: Int): Task? {
        return repository.getTaskById(id)
    }

    fun insert(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    fun update(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    // Función para agregar un archivo de audio a una nota
    fun addAudioToNote(taskId: Int, audioUri: String) {
        viewModelScope.launch {
            val task = getTaskById(taskId)
            if (task != null) {
                val updatedNote = task.copy(audioUri = audioUri)
                update(updatedNote)
            }
        }
    }
}
