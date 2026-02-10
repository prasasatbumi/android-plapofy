package com.finprov.plapofy.presentation.common

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.json.JSONObject

object ErrorMessageMapper {
    fun parse(t: Throwable?): String {
        android.util.Log.e("ErrorMessageMapper", "Error class: ${t?.javaClass?.name}, Message: ${t?.message}")
        
        return when (t) {
            is HttpException -> {
                try {
                    val errorBody = t.response()?.errorBody()?.string()
                    android.util.Log.e("ErrorMessageMapper", "Error Body: $errorBody")
                    
                    if (errorBody != null) {
                        val json = JSONObject(errorBody)
                        val message = json.optString("message", "")
                        if (message.isNotEmpty()) return message
                    }
                    getErrorByCode(t.code())
                } catch (e: Exception) {
                    android.util.Log.e("ErrorMessageMapper", "Json Parse Error: ${e.message}")
                    getErrorByCode(t.code())
                }
            }
            is UnknownHostException, is ConnectException, is SocketTimeoutException -> "Koneksi terputus. Cek internet kamu dulu ya."
            else -> {
                val msg = t?.message ?: "Terjadi kesalahan tidak terduga."
                if (msg.contains("HTTP 400")) {
                     "Email atau kata sandi salah. Coba dicek lagi." // Most likely login error
                } else if (msg.contains("HTTP")) {
                     "Terjadi kesalahan pada sistem."
                } else {
                    msg
                }
            }
        }
    }

    private fun getErrorByCode(code: Int): String {
        return when (code) {
            400 -> "Permintaan tidak valid. Cek data inputan ya."
            401 -> "Sesi habis. Login ulang yuk."
            403 -> "Maaf, Anda tidak punya akses ke fitur ini."
            404 -> "Data tidak ditemukan."
            500, 502, 503 -> "Layanan sedang sibuk. Coba sesaat lagi ya."
            else -> "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}
