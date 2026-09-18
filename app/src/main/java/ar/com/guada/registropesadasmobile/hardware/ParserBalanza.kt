package ar.com.guada.registropesadasmobile.hardware

// Resultado de interpretar una línea cruda del indicador de balanza.
data class PesoLeido(
    val pesoTexto: String,  // representación exacta que muestra el display, ej. "0.100"
    val pesoKg: Double,     // valor numérico, para validaciones y persistencia
    val estable: Boolean
)

// Convierte una línea cruda (ej. "  136.75KG ") en un PesoLeido, o null
// si la línea no tiene el formato esperado (trama cortada, ruido, etc.)
fun parsearTramaBalanza(lineaCruda: String): PesoLeido? {
    val linea = lineaCruda.removePrefix("\u0002")
    if (linea.length != 11) return null

    val indiceKG = linea.indexOf("KG")
    if (indiceKG == -1) return null  // trama sin "KG", la descartamos

    val campoPeso = linea.substring(0, indiceKG).trim()
    val ultimoCaracter = linea.last()

    val peso = campoPeso.toDoubleOrNull() ?: return null
    val estable = ultimoCaracter != 'M'

    return PesoLeido(pesoTexto = campoPeso, pesoKg = peso, estable = estable)
}