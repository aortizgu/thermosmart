package com.aortiz.android.thermosmart.thermostat.list

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.authentication.AuthenticationActivity
import com.aortiz.android.thermosmart.databinding.ThermostatListFragmentBinding
import com.aortiz.android.thermosmart.thermostat.MainActivity
import com.aortiz.android.thermosmart.utils.ERROR
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
    ): View {
        Timber.i("onCreateView")
        setDisplayHomeAsUpEnabled(false)
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.thermostat_list_fragment, container, false
            )
        binding.viewModel = viewModel
        binding.thermostatRecyclerView.adapter =
            ThermostatAdapter(ThermostatClickListener({ thermostat ->
                findNavController().navigate(
                    ThermostatListFragmentDirections.actionThermostatListFragmentToThermostatDetailFragment(
                        thermostat.id!!
                    )
                )
            }) { thermostat ->
                AlertDialog.Builder(activity)
                    .setTitle(
                        getString(
                            R.string.delete_fragment_dialog_title,
                            thermostat.configuration.name
                        )
                    )
                    .setPositiveButton(
                        getString(com.aortiz.android.thermosmart.R.string.option_yes)
                    ) { _, _ -> thermostat.id?.let { viewModel.unfollowThermostat(it) } }
                    .setNegativeButton(
                        getString(com.aortiz.android.thermosmart.R.string.option_no)
                    ) { _, _ -> }
                    .create()
                    .show()
                true
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
                            val intent =
                                Intent(requireContext(), AuthenticationActivity::class.java)
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
            ThermostatAddDialog { deviceId ->
                viewModel.followThermostat(deviceId)
            }.show(
                childFragmentManager,
                ThermostatListFragment::class.toString()
            )
        }
        viewModel.followState.observe(viewLifecycleOwner) {
            Timber.i("saveState: $it")
            when (it) {
                ThermostatListViewModel.FollowState.FOLLOWED -> {
                    //findNavController().popBackStack()
                    viewModel.clearFollowState()
                }
                ThermostatListViewModel.FollowState.ERROR -> {
                    viewModel.clearFollowState()
                }
                ThermostatListViewModel.FollowState.IDLE -> {
                }
                null -> {
                }
            }
        }
        viewModel.unfollowState.observe(viewLifecycleOwner) {
            Timber.i("unfollowState: $it")
            when (it) {
                ThermostatListViewModel.UnfollowState.UNFOLLOWED -> {
                    viewModel.clearUnfollowState()
                }
                ThermostatListViewModel.UnfollowState.ERROR -> {
                    viewModel.clearUnfollowState()
                }
                ThermostatListViewModel.UnfollowState.IDLE -> {
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
        viewModel.thermostatList.observe(viewLifecycleOwner) { list ->
            try {
                if ((activity as MainActivity).isFirstNavigation() && list.size == 1) {
                    findNavController().navigate(
                        ThermostatListFragmentDirections.actionThermostatListFragmentToThermostatDetailFragment(
                            list.first().id!!
                        )
                    )
                }
            } catch (e: java.lang.Exception) {
                Timber.e("error $e")
            }
        }
    }
}