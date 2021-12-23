package com.aortiz.android.thermosmart.thermostat.detail

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatDetailFragmentBinding
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class ThermostatDetailFragment : Fragment() {

    private val viewModel: ThermostatDetailViewModel by viewModel { parametersOf(thermostatId) }
    private lateinit var binding: ThermostatDetailFragmentBinding
    private lateinit var thermostatId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        thermostatId = ThermostatDetailFragmentArgs.fromBundle(requireArguments()).thermostatId
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_detail_fragment, container, false
            )
        binding.viewModel = viewModel
        viewModel.thermostat.observe(viewLifecycleOwner, { thermostat ->
            if (thermostat.latitude != null && thermostat.longitude != null) {
                viewModel.loadWeatherData(thermostat.latitude!!, thermostat.longitude!!)
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_config -> {
                viewModel.thermostat.value?.let {
                    findNavController().navigate(
                        ThermostatDetailFragmentDirections.actionThermostatDetailFragmentToThermostatConfigFragment(
                            it
                        )
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unloadWeatherData()
    }
}