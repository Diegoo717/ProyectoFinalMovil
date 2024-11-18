package com.example.proyectofinalmovil.nota

class NoteRepository(private val noteDao: NoteDao) {

    // Obtiene todas las notas
    val allNotes = noteDao.getAllNotes()

    // Obtiene una nota por su ID
    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    // Inserta una nueva nota
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    // Actualiza una nota existente
    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    // Elimina una nota
    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
}
