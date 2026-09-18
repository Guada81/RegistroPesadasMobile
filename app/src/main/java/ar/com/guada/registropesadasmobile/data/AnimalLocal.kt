package ar.com.guada.registropesadasmobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class AnimalLocal(
    @PrimaryKey
    val id: Int,               // mismo id que usa el backend Django
    val caravana: String,      // número de caravana, para mostrarlo en la lista
    val categoria: String? = null,      // pendiente de backend, por ahora siempre null
    val ultimoPesoKg: Double? = null    // pendiente de backend, por ahora siempre null
)

