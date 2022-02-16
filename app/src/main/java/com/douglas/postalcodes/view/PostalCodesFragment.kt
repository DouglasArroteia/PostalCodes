package com.douglas.postalcodes.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.douglas.postalcodes.R
import com.douglas.postalcodes.databinding.PostalCodeFragmentBinding
import com.douglas.postalcodes.util.collectLatestLifecycleFlow
import com.douglas.postalcodes.util.gone
import com.douglas.postalcodes.util.normalizeString
import com.douglas.postalcodes.util.visible
import com.douglas.postalcodes.view.adapter.PostalCodeAdapter
import com.douglas.postalcodes.view.states.ModelStates
import com.douglas.postalcodes.view.viewmodel.HomeViewModel
import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel

class PostalCodesFragment : Fragment() {

    private lateinit var binding: PostalCodeFragmentBinding

    private val viewModel : HomeViewModel by activityViewModels()
    private val postalAdapter: PostalCodeAdapter by lazy { PostalCodeAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostalCodeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePostalCodes()
        viewModel.parseCsv()
    }

    override fun onStart() {
        super.onStart()
        configureRecycler()
    }

    private fun showLoading() {
        with(binding) {
            loadingProgress.visible()
            loadingProgressText.visible()
        }
    }

    private fun hideLoading() {
        with(binding) {
            loadingProgress.gone()
            loadingProgressText.gone()
        }
    }

    private fun configureRecycler() {
        with(binding.postalCodeRecycler) {
            val linearLayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            layoutManager = linearLayoutManager
            adapter = postalAdapter
        }
    }

    private fun observePostalCodes() {
        collectLatestLifecycleFlow(viewModel.postalCodeListState) { response ->
            when (response) {
                is ModelStates.Success -> {
                    hideLoading()
                    postalAdapter.list = response.data
                    updateView(response.data)
                }
                is ModelStates.Loading -> {
                    showLoading()
                }
                is ModelStates.Idle -> Unit
            }
        }
    }

    private fun updateView(originalList: MutableList<PostalCodeModel>) {
        binding.apply {
            postalCodeRecycler.visible()
            postalSearch.visible()
            postalSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        val finalText = newText.normalizeString()
                        val newList: MutableList<PostalCodeModel>
                        if (finalText.length > 3) {
                            newList = postalAdapter.list.filter {
                                it.fullPostalCode.contains(
                                    finalText,
                                    ignoreCase = true
                                ) || it.localeName.contains(finalText)
                            }.toMutableList()
                            if (newList.size > 0) {
                                postalAdapter.list = newList
                                binding.postalCodeRecycler.visible()
                                binding.notFound.gone()
                            } else {
                                binding.postalCodeRecycler.gone()
                                binding.notFound.visible()
                            }
                        } else {
                            binding.postalCodeRecycler.visible()
                            binding.notFound.gone()
                            postalAdapter.list = originalList
                        }
                    }
                    return false
                }
            })
        }
    }
}
