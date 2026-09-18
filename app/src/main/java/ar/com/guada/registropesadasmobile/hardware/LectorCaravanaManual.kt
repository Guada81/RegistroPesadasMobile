package ar.com.guada.registropesadasmobile.hardware

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class LectorCaravanaManual : LectorHardware {

    // MutableSharedFlow es como un "canal" al que nosotros mismos podemos
    // emitirle valores desde afuera (a diferencia del Flow de la balanza,
    // que emitía solo. Acá el "emisor" es el usuario tocando un botón).
    private val idsSeleccionados = MutableSharedFlow<String>()

    override fun conectar(direccionMac: String) {
        // No hay conexión real, no hacemos nada.
    }

    override fun desconectar() {
        // Nada que cerrar tampoco.
    }

    override val datosRecibidos: Flow<String> = idsSeleccionados

    // Método propio, fuera de la interfaz LectorHardware: lo llama la
    // pantalla cuando el usuario toca un animal de la lista.
    suspend fun seleccionarAnimal(idAnimal: Int) {
        idsSeleccionados.emit(idAnimal.toString())
    }
}