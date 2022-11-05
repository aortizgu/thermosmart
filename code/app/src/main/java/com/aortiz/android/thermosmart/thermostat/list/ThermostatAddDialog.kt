package com.aortiz.android.thermosmart.thermostat.list

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatAddDeviceFragmentDialogBinding

class ThermostatAddDialog(val deviceToAddCallback: (String) -> Unit) :
    DialogFragment(R.layout.thermostat_add_device_fragment_dialog) {

    private var _binding: ThermostatAddDeviceFragmentDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ThermostatAddDeviceFragmentDialogBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonCancel.setOnClickListener {
            dismissNow()
        }
        binding.buttonOk.setOnClickListener {
            deviceToAddCallback(binding.editTextTextDevoceId.text.toString())
            dismiss()
        }
    }
}