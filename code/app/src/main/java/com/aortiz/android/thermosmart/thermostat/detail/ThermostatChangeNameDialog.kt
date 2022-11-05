package com.aortiz.android.thermosmart.thermostat.detail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatChangeNameDialogFragmentBinding


class ThermostatChangeNameDialog(
    private val title: String,
    private val changeDeviceNameCallback: (String) -> Unit
) :
    DialogFragment(R.layout.thermostat_change_name_dialog_fragment) {

    private var _binding: ThermostatChangeNameDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ThermostatChangeNameDialogFragmentBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireActivity())
            .setTitle(title)
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
            changeDeviceNameCallback(binding.editTextTextDeviceName.text.toString())
            dismiss()
        }
    }
}