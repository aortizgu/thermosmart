package com.aortiz.android.thermosmart.thermostat.config

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatConfigFragmentBinding
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.utils.BindingAdapters
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.math.roundToInt

class ThermostatConfigFragment : Fragment() {

    private val viewModel: ThermostatConfigViewModel by inject()
    private lateinit var binding: ThermostatConfigFragmentBinding
    private lateinit var thermostat: Thermostat

    companion object {
        const val MAX_THRESHOLD = 30.0
        const val MIN_THRESHOLD = 10.0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        setDisplayHomeAsUpEnabled(true)
        thermostat = ThermostatConfigFragmentArgs.fromBundle(requireArguments()).thermostat
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_config_fragment, container, false
            )
        binding.configThresholdSeekBar.progress =
            getProgressFromThreshold(thermostat.threshold ?: MIN_THRESHOLD)
        thermostat.threshold?.let {
            BindingAdapters.setTempText(binding.configThresholdValTextView, it)
        }
        binding.viewModel = viewModel
        viewModel.initData(thermostat)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_config, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_remove -> {
                        thermostat.id?.let {
                            viewModel.unfollowThermostat(it)
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.saveThermostatSettingsButton.setOnClickListener {
            viewModel.saveConfig(thermostat)
            findNavController().popBackStack()
        }
        binding.configThresholdSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val scaled = getThresholdFromProgress(i)
                BindingAdapters.setTempText(binding.configThresholdValTextView, scaled)
                thermostat.threshold = scaled
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.selectLocation.setOnClickListener {
            findNavController().navigate(ThermostatConfigFragmentDirections.actionThermostatConfigFragmentToSelectLocationFragment())
        }
        viewModel.unfollowState.observe(viewLifecycleOwner) {
            Timber.i("unfollowState: $it")
            when (it) {
                ThermostatConfigViewModel.UnfollowState.UNFOLLOWED -> {
                    findNavController().navigate(ThermostatConfigFragmentDirections.actionThermostatConfigFragmentToThermostatListFragment())
                    viewModel.clearState()
                }
                ThermostatConfigViewModel.UnfollowState.ERROR -> {
                    viewModel.clearState()
                }
                ThermostatConfigViewModel.UnfollowState.IDLE -> {
                }
                null -> {
                }
            }
        }
        viewModel.errorCode.observe(viewLifecycleOwner) {
            it?.let {
                Timber.i("errorCode: $it")
                var message = when (it) {
                    ERROR.ALREADY_FOLLOWING -> R.string.already_following
                    ERROR.INVALID_DEVICE -> R.string.invalid_device
                    ERROR.INVALID_USER -> R.string.invlid_user
                    ERROR.NOT_FOLLOWING -> R.string.not_following
                    else -> R.string.error_adding_thermostat
                }
                Toast.makeText(
                    context,
                    getString(message),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClear()
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