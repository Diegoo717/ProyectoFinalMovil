package com.example.proyectofinalmovil.tarea

class TaskRepository(private val taskDao: TaskDao) {

    // Obtiene todas las tareas
    val allTasks = taskDao.getAllTasks()

    // Obtiene una tarea por su ID
    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    // Inserta una nueva tarea
    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    // Actualiza una tarea existente
    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    // Elimina una tarea
    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }
}
