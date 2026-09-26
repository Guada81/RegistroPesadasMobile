package ar.com.guada.registropesadasmobile.red

import android.content.Context
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ar.com.guada.registropesadasmobile.BuildConfig

object RetrofitCliente {
    private val URL_BASE = BuildConfig.URL_BASE_API

    private lateinit var sesionRepository: SesionRepository

    // Se llama una sola vez, al arrancar la app (ver MainActivity).
    fun inicializar(context: Context) {
        sesionRepository = SesionRepository(context.applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sesionRepository))
            .build()
    }

    private val moshi = Moshi.Builder().build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val pesadaApi: PesadaApi by lazy { retrofit.create(PesadaApi::class.java) }
}