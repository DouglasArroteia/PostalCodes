package com.douglas.postalcodes.view.states

import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel

sealed class ModelStates {
    object Idle : ModelStates()
    object Loading : ModelStates()
    class Success(val data: ArrayList<PostalCodeModel>) : ModelStates()
}
