package ar.com.guada.registropesadasmobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class EstadoSync {
    PENDIENTE,
    SINCRONIZADO,
    ERROR
}

@Entity(tableName = "pesadas")
data class PesadaLocal(
    @PrimaryKey
    val uuidCliente: String = UUID.randomUUID().toString(),
    val animalId: Int?,              // null si la caravana no fue reconocida
    val caravanaDesconocida: String?, // se completa cuando animalId es null
    val pesoKg: Double,
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE,
    val fechaHoraCaptura: Long = System.currentTimeMillis()
)

