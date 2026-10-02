package ar.com.guada.registropesadasmobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [AnimalLocal::class, PesadaLocal::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun pesadaDao(): PesadaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración nueva, dentro del companion object
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE animales ADD COLUMN activo INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE animales ADD COLUMN sexo TEXT")
            }
        }

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "registro_pesadas_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}