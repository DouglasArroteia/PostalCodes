package com.douglas.postalcodes.data

import com.douglas.postalcodes.util.Constants.URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder().baseUrl(
                URL
            ).addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}
