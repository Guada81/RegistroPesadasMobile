package ar.com.guada.registropesadasmobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PesadaDao {

    @Query("SELECT * FROM pesadas ORDER BY fechaHoraCaptura DESC")
    fun obtenerTodas(): Flow<List<PesadaLocal>>

    @Query("SELECT * FROM pesadas WHERE estadoSync = 'PENDIENTE'")
    suspend fun obtenerPendientes(): List<PesadaLocal>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(pesada: PesadaLocal)

    @Update
    suspend fun actualizar(pesada: PesadaLocal)
}