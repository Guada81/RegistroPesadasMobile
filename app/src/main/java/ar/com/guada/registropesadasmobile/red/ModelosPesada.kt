package ar.com.guada.registropesadasmobile.red

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PesadaCrearRequest(
    val animal: Int?,
    val caravana_desconocida: String?,
    val peso: Double,
    val unidad_medida: String,
    val uuid_cliente: String,
    val fecha_hora: String
)

@JsonClass(generateAdapter = true)
data class PesadaResponse(
    val id: Int,
    val animal: Int?,
    val caravana_desconocida: String?,
    val peso: String,
    val unidad_medida: String,
    val peso_kg: Double,
    val valida: Boolean,
    val fecha_hora: String,
    val uuid_cliente: String,
    val usuario: String
)