package com.example.proyectofinalmovil.nota

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(entities = [Note::class], version = 2, exportSchema = false) // Versión 2
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                // Define la migración de la versión 1 a la versión 2
                val migration1To2 = object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        // Agrega las nuevas columnas 'imageUri' y 'audioUri' a la tabla 'notes'
                        database.execSQL("ALTER TABLE notes ADD COLUMN imageUri TEXT")
                        database.execSQL("ALTER TABLE notes ADD COLUMN audioUri TEXT")
                    }
                }

                // Construye la base de datos con la migración incluida
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(migration1To2) // Aplica la migración
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
