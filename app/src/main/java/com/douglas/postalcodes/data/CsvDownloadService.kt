package com.douglas.postalcodes.data

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface CsvDownloadService {

    @GET
    fun downloadCsv(@Url csvUrl: String): Call<ResponseBody>
}
