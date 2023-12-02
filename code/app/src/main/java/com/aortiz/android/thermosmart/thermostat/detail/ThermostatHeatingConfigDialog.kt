package com.aortiz.android.thermosmart.thermostat.detail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatHeaterConfigDialogFragmentBinding
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.utils.BindingAdapters

class ThermostatHeatingConfigDialog(
    private var heatingConfig: Thermostat.Configuration.Heating,
    private val changeHeaterConfigCallback: (Thermostat.Configuration.Heating) -> Unit
) :
    DialogFragment(R.layout.thermostat_heater_config_dialog_fragment) {

    private var _binding: ThermostatHeaterConfigDialogFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val STEP_THRESHOLD = 0.5
        const val MAX_THRESHOLD = 25.0
        const val MIN_THRESHOLD = 15.0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ThermostatHeaterConfigDialogFragmentBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.thermostat_change_heating_config_dialog_fragment)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchHeatingAutomaticActivation.isChecked = heatingConfig.automaticActivationEnabled == true
        binding.switchHeatingAutomaticActivation.setOnCheckedChangeListener { _, checked ->
            heatingConfig.automaticActivationEnabled = checked
        }

        BindingAdapters.setTempText(binding.thresholdValTextView, heatingConfig.threshold!!)

        binding.buttonCancel.setOnClickListener {
            dismissNow()
        }
        binding.buttonOk.setOnClickListener {
            changeHeaterConfigCallback(heatingConfig)
            dismiss()
        }
        binding.increaseThresholdButton.setOnClickListener {
            if (heatingConfig.threshold!! < MAX_THRESHOLD) {
                heatingConfig.threshold = heatingConfig.threshold!! + STEP_THRESHOLD
                BindingAdapters.setTempText(binding.thresholdValTextView, heatingConfig.threshold!!)
            }
        }
        binding.decreaseThresholdButton.setOnClickListener {
            if (heatingConfig.threshold!! > MIN_THRESHOLD) {
                heatingConfig.threshold = heatingConfig.threshold!! - STEP_THRESHOLD
                BindingAdapters.setTempText(binding.thresholdValTextView, heatingConfig.threshold!!)
            }
        }
    }
}