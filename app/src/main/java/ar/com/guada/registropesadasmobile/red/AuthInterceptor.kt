package ar.com.guada.registropesadasmobile.red

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sesionRepository: SesionRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sesionRepository.obtenerTokenActual() }

        val requestOriginal = chain.request()
        val requestConToken = if (token != null) {
            requestOriginal.newBuilder()
                .addHeader("Authorization", "Token $token")
                .build()
        } else {
            requestOriginal
        }

        return chain.proceed(requestConToken)
    }
}