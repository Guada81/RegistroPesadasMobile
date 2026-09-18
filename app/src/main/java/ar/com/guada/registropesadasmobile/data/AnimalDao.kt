package ar.com.guada.registropesadasmobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animales ORDER BY caravana ASC")
    fun obtenerTodos(): Flow<List<AnimalLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(animales: List<AnimalLocal>)
}