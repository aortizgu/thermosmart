package com.aortiz.android.thermosmart.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.AppConfigFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AppConfigFragment : Fragment() {

    private val viewModel: AppConfigViewModel by viewModel()
    private lateinit var binding: AppConfigFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView")
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.app_config_fragment, container, false
            )
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.saveAppSettingsButton.setOnClickListener {
            viewModel.showInFahrenheit = binding.farRadioButton.isChecked
            findNavController().popBackStack()
        }
    }
}