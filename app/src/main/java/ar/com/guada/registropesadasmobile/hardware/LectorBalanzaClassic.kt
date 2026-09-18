package ar.com.guada.registropesadasmobile.hardware

import android.content.Context
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

class LectorBalanzaClassic(
    private val scope: CoroutineScope,
    private val context: Context
) : LectorHardware {

    companion object {
        // UUID estándar de SPP (Serial Port Profile). Es un valor fijo,
        // no algo que definas vos: todos los dispositivos Bluetooth Classic
        // que actúan como "puerto serie" usan este mismo identificador.
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var socket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    override fun conectar(direccionMac: String) {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
            ?: throw IllegalStateException("Este dispositivo no tiene Bluetooth")

        val device: BluetoothDevice = adapter.getRemoteDevice(direccionMac)
        adapter.cancelDiscovery()

        socket = device.createRfcommSocketToServiceRecord(SPP_UUID).apply {
            connect()
        }
    }

    override fun desconectar() {
        try {
            socket?.close()
        } catch (e: IOException) {
            // Si ya estaba desconectado o hubo un error de bajo nivel al
            // cerrar, no es un caso que deba tirar la app abajo.
        }
        socket = null
    }

    override val datosRecibidos: Flow<String> = callbackFlow {
        val activeSocket = socket
            ?: run {
                close()
                return@callbackFlow
            }

        val reader = BufferedReader(InputStreamReader(activeSocket.inputStream))

        // Lanzamos un loop de lectura en un hilo aparte (Dispatchers.IO,
        // pensado para operaciones de entrada/salida que bloquean el hilo).
        val job = scope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val linea = reader.readLine() ?: break  // null = el socket se cerró
                    trySend(linea)
                }
            } catch (e: IOException) {
                close(e)  // propaga el error al Flow, para que quien lo consuma se entere
            }
        }

        // awaitClose se ejecuta cuando quien está escuchando este Flow deja
        // de escuchar (por ejemplo, si cambia de pantalla). Ahí cancelamos
        // el loop de lectura para no dejar hilos corriendo de más.
        awaitClose { job.cancel() }
    }
}