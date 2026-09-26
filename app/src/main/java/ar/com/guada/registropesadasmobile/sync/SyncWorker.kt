package ar.com.guada.registropesadasmobile.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ar.com.guada.registropesadasmobile.data.AppDatabase
import ar.com.guada.registropesadasmobile.data.EstadoSync
import ar.com.guada.registropesadasmobile.red.RetrofitCliente
import java.io.IOException

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pesadaDao = AppDatabase.obtenerInstancia(applicationContext).pesadaDao()
        val pesadaApi = RetrofitCliente.pesadaApi

        val pendientes = pesadaDao.obtenerPendientes()
        var huboErrorDeRed = false

        for (pesada in pendientes) {
            try {
                val response = pesadaApi.crearPesada(pesada.toRequest())

                when {
                    response.isSuccessful -> {
                        pesadaDao.actualizar(pesada.copy(estadoSync = EstadoSync.SINCRONIZADO))
                    }
                    response.code() == 401 -> {
                        return Result.failure()
                    }
                    response.code() in 400..499 -> {
                        pesadaDao.actualizar(pesada.copy(estadoSync = EstadoSync.ERROR))
                    }
                    else -> {
                        huboErrorDeRed = true
                    }
                }
            } catch (e: IOException) {
                huboErrorDeRed = true
            }
        }

        return if (huboErrorDeRed) Result.retry() else Result.success()
    }
}