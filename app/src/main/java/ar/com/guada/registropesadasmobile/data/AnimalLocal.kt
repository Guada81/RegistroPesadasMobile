package ar.com.guada.registropesadasmobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class AnimalLocal(
    @PrimaryKey
    val id: Int,               // mismo id que usa el backend Django
    val caravana: String,      // número de caravana, para mostrarlo en la lista
    val categoria: String? = null,      // pendiente de backend, por ahora siempre null
    val ultimoPesoKg: Double? = null, // pendiente de backend, por ahora siempre null
    @ColumnInfo(defaultValue = "1")
    val activo: Boolean = true,         // false = dado de baja en el backend
    val sexo: String? = null            // "M" o "H", tal como lo manda el backend
)

