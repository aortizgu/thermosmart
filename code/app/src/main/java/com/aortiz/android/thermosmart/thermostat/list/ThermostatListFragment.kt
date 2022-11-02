package com.aortiz.android.thermosmart.thermostat.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.authentication.AuthenticationActivity
import com.aortiz.android.thermosmart.databinding.ThermostatListFragmentBinding
import com.aortiz.android.thermosmart.thermostat.detail.ThermostatDetailFragmentDirections
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
        binding.lifecycleOwner = viewLifecycleOwner
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                            Firebase.auth.signOut()
                            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        true
                    }
                    R.id.action_settings -> {
                        findNavController().navigate(ThermostatListFragmentDirections.actionThermostatListFragmentToAppConfigFragment())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.addThermostatButton.setOnClickListener {
            findNavController().navigate(ThermostatListFragmentDirections.actionThermostatListFragmentToThermostatSaveFragment())
        }
    }
}