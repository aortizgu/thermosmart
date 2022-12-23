package com.aortiz.android.thermosmart.thermostat.detail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatChangeThresholdDialogFragmentBinding
import com.aortiz.android.thermosmart.utils.BindingAdapters
import kotlin.math.roundToInt

class ThermostatChangeThresholdDialog(
    private var threshold: Double,
    private val changeThermostatThresholdCallback: (Double) -> Unit
) :
    DialogFragment(R.layout.thermostat_change_threshold_dialog_fragment) {

    private var _binding: ThermostatChangeThresholdDialogFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val MAX_THRESHOLD = 25.0
        const val MIN_THRESHOLD = 15.0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ThermostatChangeThresholdDialogFragmentBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.thermostat_change_threshold_dialog_fragment)
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
            changeThermostatThresholdCallback(threshold)
            dismiss()
        }
        binding.configThresholdSeekBar.progress = getProgressFromThreshold(threshold)
        BindingAdapters.setTempText(binding.thresholdValTextView, threshold)
        binding.configThresholdSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val scaled = getThresholdFromProgress(i)
                BindingAdapters.setTempText(binding.thresholdValTextView, scaled)
                threshold = scaled
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getThresholdFromProgress(progress: Int): Double {
        return (progress * (MAX_THRESHOLD - MIN_THRESHOLD) / 100.0) + MIN_THRESHOLD
    }

    private fun getProgressFromThreshold(t: Double): Int {
        var threshold = t
        if (threshold < MIN_THRESHOLD) {
            threshold = MIN_THRESHOLD
        } else if (threshold > MAX_THRESHOLD) {
            threshold = MAX_THRESHOLD
        }
        return ((threshold - MIN_THRESHOLD) * 100 / (MAX_THRESHOLD - MIN_THRESHOLD)).roundToInt()
    }

}