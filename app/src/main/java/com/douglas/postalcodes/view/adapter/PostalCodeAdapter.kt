package com.douglas.postalcodes.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel
import com.douglas.postalcodes.databinding.PostalItemLayoutBinding

class PostalCodeAdapter :
    RecyclerView.Adapter<PostalCodeAdapter.PostalCodeViewHolder>() {

    var list: MutableList<PostalCodeModel> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostalCodeViewHolder(
        PostalItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: PostalCodeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class PostalCodeViewHolder(private val binding: PostalItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PostalCodeModel) {
            binding.apply {
                postalCodeText.text = data.fullPostalCode
                localeText.text = data.localeName
            }
        }
    }
}
