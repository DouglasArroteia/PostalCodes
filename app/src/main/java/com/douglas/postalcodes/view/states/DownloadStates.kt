package com.douglas.postalcodes.view.states

import java.io.File

sealed class DownloadStates {
    object Idle : DownloadStates()
    object Loading : DownloadStates()
    class Success(val data: File) : DownloadStates()
    object Failure : DownloadStates()
    class Downloaded(val data: File) : DownloadStates()
}
