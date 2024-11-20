package com.example.proyectofinalmovil.tarea

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(entities = [Task::class], version = 2, exportSchema = false) // Versión 2
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                // Define la migración de la versión 1 a la versión 2
                val migration1To2 = object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        // Agrega las nuevas columnas 'imageUri' y 'audioUri' a la tabla 'tasks'
                        database.execSQL("ALTER TABLE tasks ADD COLUMN imageUri TEXT")
                        database.execSQL("ALTER TABLE tasks ADD COLUMN audioUri TEXT")
                    }
                }

                // Construye la base de datos con la migración incluida
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .addMigrations(migration1To2) // Aplica la migración
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
