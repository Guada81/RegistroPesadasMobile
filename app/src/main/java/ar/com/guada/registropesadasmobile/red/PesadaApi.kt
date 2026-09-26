package ar.com.guada.registropesadasmobile.red

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PesadaApi {
    @POST("api/v1/pesadas/")
    suspend fun crearPesada(@Body pesada: PesadaCrearRequest): Response<PesadaResponse>
}