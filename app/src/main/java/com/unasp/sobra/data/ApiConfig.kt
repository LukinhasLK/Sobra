package com.unasp.sobra.data

/**
 * Endereço da API.
 * No emulador do Android Studio, `10.0.2.2` é o computador onde o emulador roda
 * (o `localhost` do emulador seria ele mesmo). A porta 8084 é a do back end (`backend/`).
 * TODO: para celular de verdade, trocar pelo IP do computador na rede ou pelo endereço hospedado.
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8084/api/v1"
}
