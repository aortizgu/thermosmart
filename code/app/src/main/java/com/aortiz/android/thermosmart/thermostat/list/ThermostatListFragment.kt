package com.aortiz.android.thermosmart.thermostat.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.authentication.AuthenticationActivity
import com.aortiz.android.thermosmart.databinding.ThermostatListFragmentBinding
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ThermostatListFragment : Fragment() {

    private val viewModel: ThermostatListViewModel by viewModel()
    private lateinit var binding: ThermostatListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("onCreateView")
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_list_fragment, container, false
            )
        binding.viewModel = viewModel
        binding.thermostatRecyclerView.adapter =
            ThermostatAdapter(ThermostatClickListener { thermostat ->
                Timber.d("click on $thermostat")
                findNavController().navigate(
                    ThermostatListFragmentDirections.actionThermostatListFragmentToThermostatDetailFragment(
                        thermostat.id!!
                    )
                )
            })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated")
        binding.lifecycleOwner = this
        binding.addThermostatButton.setOnClickListener {
            findNavController().navigate(ThermostatListFragmentDirections.actionThermostatListFragmentToThermostatSaveFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                    val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            R.id.action_settings -> {
                findNavController().navigate(ThermostatListFragmentDirections.actionThermostatListFragmentToAppConfigFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }
}