package com.example.proyectofinalmovil.tarea

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task? // Método para obtener una tarea por ID

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>
}
