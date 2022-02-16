package com.douglas.postalcodes.view.viewmodel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.douglas.postalcodes.data.CsvDownloadService
import com.douglas.postalcodes.data.RetrofitInstance
import com.douglas.postalcodes.util.Constants.CSV_URL
import com.douglas.postalcodes.util.Constants.DOWNLOADS_DIRECTORY
import com.douglas.postalcodes.util.Constants.FILE_NAME
import com.douglas.postalcodes.util.toFile
import com.douglas.postalcodes.util.toPostalCodeList
import com.douglas.postalcodes.view.states.DownloadStates
import com.douglas.postalcodes.view.states.ModelStates
import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val service =
        RetrofitInstance.getRetrofitInstance().create(CsvDownloadService::class.java)

    private val _downloadState = MutableStateFlow<DownloadStates>(DownloadStates.Idle)
    val downloadState = _downloadState.asStateFlow()

    private val _postalCodeListState = MutableStateFlow<ModelStates>(ModelStates.Idle)
    val postalCodeListState = _postalCodeListState.asStateFlow()

    private lateinit var csvFile: File


    fun scheduleFetch(context: Context) {
        _downloadState.value =
            DownloadStates.Loading
        downloadFile(context)
    }

    private fun downloadFile(context: Context) {
        val file =
            File("${context.getExternalFilesDir(FILE_DIRECTORY)?.absolutePath}$DOWNLOADS_DIRECTORY")
        if (!file.exists()) {
            downloadCsvFile(file)
        } else {
            val finalFile = File("${file.absoluteFile}$FILE_NAME")
            _downloadState.value =
                DownloadStates.Downloaded(finalFile)
            csvFile = finalFile
        }
    }

    fun parseCsv() {
        val postalCodeList = arrayListOf<PostalCodeModel>()
        viewModelScope.launch {
            _postalCodeListState.value = ModelStates.Loading
            csvReader().open(csvFile) {
                postalCodeList.addAll(toPostalCodeList())
            }
        }.invokeOnCompletion {
            _postalCodeListState.value = ModelStates.Success(postalCodeList)
        }
    }

    private fun downloadCsvFile(file: File) {
        file.mkdirs()
        viewModelScope.launch {
            service.downloadCsv(CSV_URL).enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()?.byteStream()
                        if (result != null) {
                            createFile(result, file)
                        } else {
                            _downloadState.value = DownloadStates.Failure
                        }
                    } else {
                        _downloadState.value = DownloadStates.Failure
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _downloadState.value = DownloadStates.Failure
                }
            })
        }
    }

    private fun createFile(inputStream: InputStream, file: File) {
        val finalFile = File("${file.absoluteFile}$FILE_NAME")
        try {
            inputStream.toFile(finalFile.absolutePath)
            _downloadState.value = DownloadStates.Success(finalFile)
            csvFile = finalFile
        } catch (e: IOException) {
            _downloadState.value = DownloadStates.Failure
        }
    }

    companion object {
        val FILE_DIRECTORY: String = Environment.DIRECTORY_DOCUMENTS
    }
}

