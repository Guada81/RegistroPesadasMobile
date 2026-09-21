package ar.com.guada.registropesadasmobile.hardware

import kotlinx.coroutines.flow.Flow

interface LectorHardware {
    suspend fun conectar(direccionMac: String)
    fun desconectar()
    val datosRecibidos: Flow<String>
}