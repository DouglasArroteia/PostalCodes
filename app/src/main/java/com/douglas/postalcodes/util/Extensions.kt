package com.douglas.postalcodes.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel
import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.Normalizer

fun InputStream.toFile(path: String) {
    use { input ->
        File(path).outputStream().use { input.copyTo(it) }
    }
}

fun CsvFileReader.toPostalCodeList(): ArrayList<PostalCodeModel> {
    val postalCodeList = arrayListOf<PostalCodeModel>()
    readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
        postalCodeList.add(
            PostalCodeModel(
                postalCode = row["num_cod_postal"] ?: "",
                localeName = row["nome_localidade"] ?: "",
                extPostalCode = row["ext_cod_postal"] ?: ""
            )
        )
    }
    return postalCodeList
}

fun String.normalizeString() =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(Regex("[^\\p{ASCII}]"), "").lowercase()

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun <T> Fragment.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun <T> AppCompatActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun View.showSnackBar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackBar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackBar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackBar.show()
    }
}
