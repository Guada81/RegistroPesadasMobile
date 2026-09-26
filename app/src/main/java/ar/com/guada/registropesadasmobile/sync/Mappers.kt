package ar.com.guada.registropesadasmobile.sync

import ar.com.guada.registropesadasmobile.data.PesadaLocal
import ar.com.guada.registropesadasmobile.red.PesadaCrearRequest

fun PesadaLocal.toRequest(): PesadaCrearRequest {
    return PesadaCrearRequest(
        animal = this.animalId,
        caravana_desconocida = this.caravanaDesconocida,
        peso = this.pesoKg,
        unidad_medida = "kg",
        uuid_cliente = this.uuidCliente
    )
}