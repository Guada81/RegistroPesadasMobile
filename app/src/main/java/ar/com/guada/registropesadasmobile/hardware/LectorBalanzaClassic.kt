package ar.com.guada.registropesadasmobile.hardware

import android.content.Context
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

class LectorBalanzaClassic(
    private val scope: CoroutineScope,
    private val context: Context
) : LectorHardware {

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var socket: BluetoothSocket? = null
    private var lecturaJob: Job? = null

    private val _datosRecibidos = MutableSharedFlow<String>(replay = 1)
    override val datosRecibidos: Flow<String> = _datosRecibidos.asSharedFlow()

    private val _estadoConexion = MutableStateFlow(EstadoConexion.DESCONECTADO)
    val estadoConexion: StateFlow<EstadoConexion> = _estadoConexion.asStateFlow()

    @SuppressLint("MissingPermission")
    override suspend fun conectar(direccionMac: String) = withContext(Dispatchers.IO) {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
            ?: throw IllegalStateException("Este dispositivo no tiene Bluetooth")

        val device: BluetoothDevice = adapter.getRemoteDevice(direccionMac)
        adapter.cancelDiscovery()

        socket = device.createRfcommSocketToServiceRecord(SPP_UUID).apply {
            connect()
        }

        _estadoConexion.value = EstadoConexion.CONECTADO
        iniciarLecturaContinua()
    }

    private fun iniciarLecturaContinua() {
        lecturaJob?.cancel()

        val activeSocket = socket ?: return
        val reader = BufferedReader(InputStreamReader(activeSocket.inputStream))

        lecturaJob = scope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val linea = reader.readLine() ?: break
                    _datosRecibidos.tryEmit(linea)
                }
                // readLine() devolvió null: el socket se cerró del otro lado
                // (ej. la balanza se apagó o se fue de rango).
                _estadoConexion.value = EstadoConexion.ERROR
            } catch (e: IOException) {
                // Corte imprevisto de la conexión mientras se leía.
                _estadoConexion.value = EstadoConexion.ERROR
            }
        }
    }

    override fun desconectar() {
        lecturaJob?.cancel()
        try {
            socket?.close()
        } catch (e: IOException) {
            // Ya estaba desconectado o hubo un error de bajo nivel al cerrar.
        }
        socket = null
        _estadoConexion.value = EstadoConexion.DESCONECTADO
    }
}