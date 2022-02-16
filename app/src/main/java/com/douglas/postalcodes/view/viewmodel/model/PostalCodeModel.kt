package com.douglas.postalcodes.view.viewmodel.model

data class PostalCodeModel(
    val postalCode: String = "",
    val extPostalCode: String = "",
    val localeName: String = "",
    val fullPostalCode: String = "$postalCode-$extPostalCode"
)
