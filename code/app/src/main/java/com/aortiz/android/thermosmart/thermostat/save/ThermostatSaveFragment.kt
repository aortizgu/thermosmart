package com.aortiz.android.thermosmart.thermostat.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatSaveFragmentBinding
import com.aortiz.android.thermosmart.utils.ERROR_CODE
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
        viewModel.followState.observe(viewLifecycleOwner, {
            Timber.i("saveState: $it")
            when (it) {
                ThermostatSaveViewModel.SaveState.FOLLOWED -> {
                    findNavController().popBackStack()
                    viewModel.clearState()
                }
                ThermostatSaveViewModel.SaveState.ERROR -> {
                    viewModel.clearState()
                }
            }
        })
        viewModel.errorCode.observe(viewLifecycleOwner, {
            it?.let {
                Timber.i("errorCode: $it")
                var message = when (it) {
                    ERROR_CODE.ALREADY_FOLLOWING -> R.string.already_following
                    ERROR_CODE.INVALID_DEVICE -> R.string.invalid_device
                    ERROR_CODE.INVALID_USER -> R.string.invlid_user
                    ERROR_CODE.NOT_FOLLOWING -> R.string.not_following
                    else -> R.string.error_adding_thermostat
                }
                Toast.makeText(
                    context,
                    getString(message),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearError()
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner

        binding.saveThermostat.setOnClickListener {
            viewModel.followThermostat(binding.thermostatId.text.toString())
        }
    }

}