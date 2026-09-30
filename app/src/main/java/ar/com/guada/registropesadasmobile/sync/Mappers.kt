package ar.com.guada.registropesadasmobile.sync

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ar.com.guada.registropesadasmobile.data.PesadaLocal
import ar.com.guada.registropesadasmobile.red.PesadaCrearRequest


fun PesadaLocal.toRequest(): PesadaCrearRequest {
    val fechaIso = Instant.ofEpochMilli(this.fechaHoraCaptura)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    return PesadaCrearRequest(
        animal = this.animalId,
        caravana_desconocida = this.caravanaDesconocida,
        peso = this.pesoKg,
        unidad_medida = "kg",
        uuid_cliente = this.uuidCliente,
        fecha_hora = fechaIso
    )
}