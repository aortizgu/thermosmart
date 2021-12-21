package com.aortiz.android.thermosmart.thermostat.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatSaveFragmentBinding
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ThermostatSaveFragment : Fragment() {

    private val viewModel: ThermostatSaveViewModel by viewModel()
    private lateinit var binding: ThermostatSaveFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_save_fragment, container, false
            )
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = this

        binding.saveThermostat.setOnClickListener {
            viewModel.save()
            findNavController().popBackStack()
        }
    }

}