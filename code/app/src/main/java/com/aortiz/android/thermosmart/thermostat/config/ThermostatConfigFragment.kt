package com.aortiz.android.thermosmart.thermostat.config

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatConfigFragmentBinding
import com.aortiz.android.thermosmart.thermostat.detail.ThermostatDetailFragmentArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ThermostatConfigFragment : Fragment() {

    private val viewModel: ThermostatConfigViewModel by viewModel()
    private lateinit var binding: ThermostatConfigFragmentBinding
    private lateinit var thermostatId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        setHasOptionsMenu(true)
        thermostatId = ThermostatDetailFragmentArgs.fromBundle(requireArguments()).thermostatId
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_config_fragment, container, false
            )
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.saveThermostatSettingsButton.setOnClickListener {
            viewModel.saveConfig()
            findNavController().popBackStack()
        }
        binding.configThresholdSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val scaled = (i * 20f / 100f) + 10f
                binding.configThresholdValTextView.text = "${scaled.toString()} ºC"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_remove -> {
                viewModel.removeThermostat()
                findNavController().navigate(ThermostatConfigFragmentDirections.actionThermostatConfigFragmentToThermostatListFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_config, menu)
    }
}

private fun SeekBar.setOnSeekBarChangeListener() {
    TODO("Not yet implemented")
}
