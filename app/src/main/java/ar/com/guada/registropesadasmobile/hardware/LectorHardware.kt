package ar.com.guada.registropesadasmobile.hardware

import kotlinx.coroutines.flow.Flow

interface LectorHardware {
    fun conectar(direccionMac: String)
    fun desconectar()
    val datosRecibidos: Flow<String>
}