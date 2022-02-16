package com.douglas.postalcodes.view

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.douglas.postalcodes.R
import com.douglas.postalcodes.view.viewmodel.model.PostalCodeModel
import com.douglas.postalcodes.databinding.HomeFragmentBinding
import com.douglas.postalcodes.util.gone
import com.douglas.postalcodes.util.normalizeString
import com.douglas.postalcodes.util.visible
import com.douglas.postalcodes.view.adapter.PostalCodeAdapter
import com.douglas.postalcodes.view.states.DownloadStates
import com.douglas.postalcodes.view.states.ModelStates
import com.douglas.postalcodes.view.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.home_fragment) {

    private lateinit var binding: HomeFragmentBinding

    private val viewModel by viewModels<HomeViewModel>()
    private val postalAdapter: PostalCodeAdapter by lazy { PostalCodeAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.permissionButton.setOnClickListener {
            requestForPermission()
        }
    }

    override fun onStart() {
        super.onStart()
        requestForPermission()
        configureObservers(requireView())
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

    private fun requestForPermission() {
        val isStoragePermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!isStoragePermissionGranted) {
            hideLoading()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(WRITE_EXTERNAL_STORAGE), 0)
        } else {
            binding.permissionButton.gone()
            showLoading()
            lifecycleScope.launch {
                viewModel.scheduleFetch(requireContext())
            }
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

    private fun configureObservers(view: View) {
        lifecycleScope.launchWhenStarted {
            viewModel.downloadState.collect { response ->
                when (response) {
                    is DownloadStates.Idle -> Unit
                    is DownloadStates.Loading -> {
                        showLoading()
                    }
                    is DownloadStates.Failure -> {
                        hideLoading()
                        Snackbar.make(
                            view, getString(R.string.fail_to_download), Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is DownloadStates.Success -> {
                        hideLoading()
                        observePostalCodes()
                        viewModel.parseCsv(response.data)
                    }
                    is DownloadStates.Downloaded -> {
                        hideLoading()
                        observePostalCodes()
                        Snackbar.make(
                            view, getString(R.string.file_downloaded), Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.parseCsv(response.data)
                    }
                }
            }
        }
    }

    private fun observePostalCodes() {
        lifecycleScope.launchWhenStarted {
            viewModel.postalCodeListState.collect { response ->
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
                                binding.postalCodeRecycler.visibility = View.VISIBLE
                                binding.notFound.visibility = View.GONE
                            } else {
                                binding.postalCodeRecycler.visibility = View.GONE
                                binding.notFound.visibility = View.VISIBLE
                            }
                        } else {
                            binding.postalCodeRecycler.visibility = View.VISIBLE
                            binding.notFound.visibility = View.GONE
                            postalAdapter.list = originalList
                        }
                    }
                    return false
                }
            })
        }
    }
}
