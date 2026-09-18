package ar.com.guada.registropesadasmobile

const val PESO_MINIMO_KG = 50.0
const val PESO_MAXIMO_KG = 1800.0

fun pesoFueraDeRango(pesoKg: Double): Boolean =
    pesoKg < PESO_MINIMO_KG || pesoKg > PESO_MAXIMO_KG