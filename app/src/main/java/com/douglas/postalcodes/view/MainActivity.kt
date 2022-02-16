package com.douglas.postalcodes.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.douglas.postalcodes.R
import com.douglas.postalcodes.databinding.ActivityMainBinding
import com.douglas.postalcodes.util.collectLatestLifecycleFlow
import com.douglas.postalcodes.util.gone
import com.douglas.postalcodes.util.showSnackBar
import com.douglas.postalcodes.util.visible
import com.douglas.postalcodes.view.states.DownloadStates
import com.douglas.postalcodes.view.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showLoading()
                viewModel.scheduleFetch(this)
            } else {
                hideLoading()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(findViewById(R.id.toolbar))
        setContentView(binding.root)
        binding.permissionButton.setOnClickListener {
            onClickRequestPermission(it)
        }
        configureObserver()
    }

    private fun configureObserver() {
        collectLatestLifecycleFlow(viewModel.downloadState) { response ->
            when (response) {
                is DownloadStates.Idle -> Unit
                is DownloadStates.Loading -> {
                    showLoading()
                }
                is DownloadStates.Failure -> {
                    hideLoading()
                    Snackbar.make(
                        binding.root, getString(R.string.fail_to_download), Snackbar.LENGTH_LONG
                    ).show()
                }
                is DownloadStates.Success -> {
                    hideLoading()
                    binding.permissionButton.gone()
                    openFragment()
                }
                is DownloadStates.Downloaded -> {
                    hideLoading()
                    binding.permissionButton.gone()
                    openFragment()
                }
            }
        }
    }

    private fun openFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainerView, PostalCodesFragment(), "PostalCodeFragment")
            .commitAllowingStateLoss()
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

    private fun onClickRequestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                binding.root.showSnackBar(
                    view,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.open)
                ) {
                    binding.permissionButton.gone()
                    showLoading()
                    viewModel.scheduleFetch(this)
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                binding.root.showSnackBar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
}
