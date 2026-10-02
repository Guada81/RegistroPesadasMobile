package ar.com.guada.registropesadasmobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.com.guada.registropesadasmobile.Pantalla
import ar.com.guada.registropesadasmobile.data.AnimalRepository
import ar.com.guada.registropesadasmobile.data.AppDatabase
import ar.com.guada.registropesadasmobile.data.ResultadoSyncAnimales
import ar.com.guada.registropesadasmobile.red.RetrofitCliente
import ar.com.guada.registropesadasmobile.red.SesionInfo
import ar.com.guada.registropesadasmobile.hardware.LectorBalanzaClassic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Usamos la misma instancia de SesionRepository que ya usa el resto
    // de la capa de red (el interceptor y el repositorio de animales).
    private val sesionRepository = RetrofitCliente.obtenerSesionRepository()

    private val animalRepository = AnimalRepository(
        AppDatabase.obtenerInstancia(application).animalDao(),
        RetrofitCliente.animalApi,
        sesionRepository
    )

    // --- Sesión ---
    // sesion: null puede significar "todavía no sé" o "no hay sesión".
    // sesionCargada distingue los dos casos: false = DataStore todavía no contestó.
    private val _sesion = MutableStateFlow<SesionInfo?>(null)
    val sesion: StateFlow<SesionInfo?> = _sesion

    private val _sesionCargada = MutableStateFlow(false)
    val sesionCargada: StateFlow<Boolean> = _sesionCargada

    init {
        viewModelScope.launch {
            sesionRepository.sesion.collect {
                _sesion.value = it
                _sesionCargada.value = true
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { sesionRepository.cerrarSesion() }
    }

    // --- Navegación ---
    private val _pantallaActual = MutableStateFlow(Pantalla.PRINCIPAL)
    val pantallaActual: StateFlow<Pantalla> = _pantallaActual

    fun irAPantalla(pantalla: Pantalla) {
        _pantallaActual.value = pantalla
    }

    // --- Sincronización de animales ---
    private val _actualizandoAnimales = MutableStateFlow(false)
    val actualizandoAnimales: StateFlow<Boolean> = _actualizandoAnimales

    private val _resultadosSync = Channel<ResultadoSyncAnimales>(Channel.BUFFERED)
    val resultadosSync = _resultadosSync.receiveAsFlow()

    fun sincronizarAnimales() {
        if (_actualizandoAnimales.value) return
        _actualizandoAnimales.value = true
        viewModelScope.launch {
            val resultado = animalRepository.sincronizar()
            _actualizandoAnimales.value = false
            _resultadosSync.send(resultado)
        }
    }
    // --- Balanza Bluetooth ---
    // Vive en el ViewModel para que la conexión sobreviva a la rotación.
    // Recibe el scope del ViewModel y el contexto de la aplicación
    // (no el de la pantalla), así no depende de que la pantalla exista.
    val lectorBalanza = LectorBalanzaClassic(viewModelScope, application)

    // Se llama cuando el ViewModel ya no se necesita (por ejemplo, la app
    // se cerró de verdad). Ahí cerramos la conexión con la balanza.
    override fun onCleared() {
        lectorBalanza.desconectar()
        super.onCleared()
    }
}