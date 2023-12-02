package com.aortiz.android.thermosmart.thermostat.detail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatWateringConfigDialogFragmentBinding
import com.aortiz.android.thermosmart.domain.Thermostat


class ThermostatWateringConfigDialog(
    private var wateringConfig: Thermostat.Configuration.Watering,
    private val changeWateringConfigCallback: (Thermostat.Configuration.Watering) -> Unit
) :
    DialogFragment(R.layout.thermostat_watering_config_dialog_fragment) {

    private var _binding: ThermostatWateringConfigDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding =
            ThermostatWateringConfigDialogFragmentBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.watering_config_title_dialog))
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchWateringAutomaticActivation.isChecked = wateringConfig.automaticActivationEnabled == true
        binding.switchWateringAutomaticActivation.setOnCheckedChangeListener { _, checked ->
            wateringConfig.automaticActivationEnabled = checked
        }
        binding.buttonCancel.setOnClickListener {
            dismissNow()
        }
        binding.buttonOk.setOnClickListener {
            changeWateringConfigCallback(wateringConfig)
            dismiss()
        }
        wateringConfig.frequencyDay?.let {
            val index = it - 1
            val valuesSize = resources.getStringArray(R.array.wateringFreq).size
            if (index in 1..valuesSize) binding.wateringFreqSpinner.setSelection(index)
        }
        binding.wateringFreqSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    wateringConfig.frequencyDay = position + 1
                }
            }
        wateringConfig.durationMinute?.let {
            val index = it - 1
            val valuesSize = resources.getStringArray(R.array.wateringDuration).size
            if (index in 1..valuesSize) binding.wateringDurationSpinner.setSelection(index)
        }
        binding.wateringDurationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    wateringConfig.durationMinute = position + 1
                }
            }
        wateringConfig.activationHour?.let {
            val valuesSize = resources.getStringArray(R.array.wateringTime).size
            if (it in 1..valuesSize) binding.wateringTimeSpinner.setSelection(it)
        }
        binding.wateringTimeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    wateringConfig.activationHour = position
                }
            }
    }
}
