package com.aortiz.android.thermosmart.thermostat.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatDetailFragmentBinding
import com.aortiz.android.thermosmart.notifications.DetailNotificationActivity
import com.aortiz.android.thermosmart.notifications.NotificationActivity
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class ThermostatDetailFragment : Fragment() {

    private val viewModel: ThermostatDetailViewModel by inject { parametersOf(thermostatId) }
    private lateinit var binding: ThermostatDetailFragmentBinding
    private lateinit var thermostatId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        val args = ThermostatDetailFragmentArgs.fromBundle(requireArguments())
        setDisplayHomeAsUpEnabled(args.navHost)
        thermostatId = args.thermostatId
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_detail_fragment, container, false
            )
        binding.viewModel = viewModel
        viewModel.thermostat.observe(viewLifecycleOwner) { thermostat ->
            val location = thermostat.configuration.location
            if (location.latitude != null && location.longitude != null) {
                viewModel.loadWeatherData(location.latitude!!, location.longitude!!)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.deviceNameCardView.setOnClickListener {
            val title = getString(R.string.change_name_fragment_dialog_title, thermostatId)
            ThermostatChangeNameDialog(title) {
                viewModel.changeDeviceName(it)
            }.show(
                childFragmentManager,
                ThermostatDetailFragment::class.toString()
            )
        }
        binding.noExteriorDataCardView.setOnClickListener {
            viewModel.thermostat.value?.let {
                findNavController().navigate(
                    ThermostatDetailFragmentDirections.actionThermostatDetailFragmentToSelectLocationFragment(
                        it
                    )
                )
            }
        }
        binding.exteriorDataCardView.setOnClickListener {
            viewModel.thermostat.value?.let {
                findNavController().navigate(
                    ThermostatDetailFragmentDirections.actionThermostatDetailFragmentToSelectLocationFragment(
                        it
                    )
                )
            }
        }
        binding.switchBoilerAutomaticActivation.setOnCheckedChangeListener { _, checked ->
            viewModel.setControllerBoilerAutomaticActivation(checked)
        }
        binding.switchWateringAutomaticActivation.setOnCheckedChangeListener { _, checked ->
            viewModel.setControllerWateringAutomaticActivation(checked)
        }
        binding.thermostatThresholdImageView.setOnClickListener {
            viewModel.thermostat.value?.configuration?.boiler?.threshold?.let { threshold ->
                ThermostatChangeThresholdDialog(threshold) { thresholdOut ->
                    viewModel.setControllerBoilerThreshold(thresholdOut)
                }.show(
                    childFragmentManager,
                    ThermostatDetailFragment::class.toString()
                )
            }
        }
        binding.wateringSettingsConstraintLayout.setOnClickListener { _ ->
            viewModel.thermostat.value?.configuration?.watering?.let { it ->
                ThermostatWateringConfigDialog(it) {
                    viewModel.setControllerWateringConfig(it)
                }.show(
                    childFragmentManager,
                    ThermostatDetailFragment::class.toString()
                )
            }
        }
        binding.wateringImageViewTopLeftIcon.setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle(R.string.start_watering_dialog_title)
                .setPositiveButton(
                    getString(R.string.option_yes)
                ) { _, _ ->
                    Timber.i("start watering")
                    viewModel.startWatering()
                }
                .setNegativeButton(
                    getString(R.string.option_no)
                ) { _, _ -> }
                .create()
                .show()
        }

        viewModel.updateState.observe(viewLifecycleOwner) {
            Timber.i("updateState: $it")
            when (it) {
                ThermostatDetailViewModel.UpdateState.UPDATED -> {
                    viewModel.clearUpdateState()
                }
                ThermostatDetailViewModel.UpdateState.ERROR -> {
                    viewModel.clearUpdateState()
                }
                ThermostatDetailViewModel.UpdateState.IDLE -> {
                }
                null -> {
                }
            }
        }
        viewModel.errorCode.observe(viewLifecycleOwner) {
            it?.let {
                Timber.i("errorCode: $it")
                val message = when (it) {
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
        viewModel.unloadWeatherData()
    }
}