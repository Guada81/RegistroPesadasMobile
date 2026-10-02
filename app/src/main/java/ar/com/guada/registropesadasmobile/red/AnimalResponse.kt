package ar.com.guada.registropesadasmobile.red

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnimalResponse(
    val id: Int,
    val nro_identificacion: String,
    val activo: Boolean,
    val sexo: String?
)