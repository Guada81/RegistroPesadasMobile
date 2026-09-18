package ar.com.guada.registropesadasmobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AnimalLocal::class, PesadaLocal::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun pesadaDao(): PesadaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "registro_pesadas_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        // onCreate se dispara UNA SOLA VEZ: la primera vez
                        // que el archivo de la base de datos se crea en el
                        // celular. Si ya existe (por ejemplo, cerraste y
                        // volviste a abrir la app), no se vuelve a llamar.
                        // Por eso es un buen lugar para cargar datos
                        // iniciales, sin riesgo de duplicarlos.
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.animalDao()?.insertarTodos(animalesDePrueba())
                            }
                        }
                    })
                    .build()
                INSTANCE = instancia
                instancia
            }
        }

        private fun animalesDePrueba(): List<AnimalLocal> = listOf(
            AnimalLocal(id = 1, caravana = "AR001234"),
            AnimalLocal(id = 2, caravana = "AR001235"),
            AnimalLocal(id = 3, caravana = "AR001236"),
            AnimalLocal(id = 4, caravana = "AR001237"),
            AnimalLocal(id = 5, caravana = "AR001238")
        )
    }
}