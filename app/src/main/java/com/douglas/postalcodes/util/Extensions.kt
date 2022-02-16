package com.douglas.postalcodes.util

import android.view.View
import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel
import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
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
