package com.aortiz.android.thermosmart.thermostat.selectlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ThermostatSelectLocationFragmentBinding
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*


class ThermostatSelectLocationFragment : OnMapReadyCallback, Fragment() {

    private lateinit var binding: ThermostatSelectLocationFragmentBinding
    private lateinit var thermostat: Thermostat
    private val viewModel: ThermostatSelectLocationViewModel by viewModel()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var marker: Marker? = null
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var selectedLatLng: LatLng? = null
    private var selectedLocation: String? = null
    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            Timber.d("requestMultiplePermissions: granted, try to enable my location")
            enableMyLocation()
        } else {
            Toast.makeText(
                context,
                getString(R.string.permission_denied_explanation),
                Toast.LENGTH_SHORT
            ).show()
            Timber.d("requestMultiplePermissions: not granted")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        thermostat =
            ThermostatSelectLocationFragmentArgs.fromBundle(requireArguments()).thermostat
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.thermostat_select_location_fragment, container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.selectLocationButtonSave.setOnClickListener {
            onLocationSelected()
        }
        setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.normal_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }
                    R.id.hybrid_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }
                    R.id.satellite_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }
                    R.id.terrain_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.selectLocationMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        viewModel.saveState.observe(viewLifecycleOwner) {
            Timber.i("saveState: $it")
            when (it) {
                ThermostatSelectLocationViewModel.SaveState.SAVED -> {
                    viewModel.clearSavedState()
                    findNavController().popBackStack()
                }
                ThermostatSelectLocationViewModel.SaveState.ERROR -> {
                    viewModel.clearSavedState()
                }
                ThermostatSelectLocationViewModel.SaveState.IDLE -> {
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

    private fun onLocationSelected() {
        thermostat.id?.let { id ->
            selectedLatLng?.let { latLng ->
                selectedLocation?.let { location ->
                    viewModel.setControllerLocation(
                        id,
                        latLng.latitude,
                        latLng.longitude,
                        location
                    )
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Timber.d("onMapReady")
        map = googleMap
        setMapOnClick()
        enableMyLocation()
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        Timber.d("enableMyLocation")
        if (isPermissionGranted()) {
            Timber.d("enableMyLocation: permission granted")
            map.isMyLocationEnabled = true
            if (!thermostat.configuration.location.location.isNullOrEmpty()
                && !thermostat.configuration.location.location.contentEquals(
                    "null"
                )
            ) {
                val latLng = LatLng(
                    thermostat.configuration.location.latitude ?: defaultLocation.latitude,
                    thermostat.configuration.location.longitude ?: defaultLocation.longitude,
                )
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng, 15f
                    )
                )
                setMarker(latLng, thermostat.configuration.location.location)
            } else {
                getDeviceLocation()
            }
        } else {
            Timber.d("enableMyLocation: permission not granted")
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getDeviceLocation() {
        Timber.d("getDeviceLocation")
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Timber.d("getDeviceLocation: Task Successful, move map to current location")
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                ), 15f
                            )
                        )
                    }
                } else {
                    Timber.d("getDeviceLocation: Current location is null. Using defaults.")
                    Timber.e("Exception: %s", task.exception)
                    map.moveCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, 15f)
                    )
                    map.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            Timber.e("getDeviceLocation: Exception: $e.message, $e")
        }
    }

    private fun setMarker(latLng: LatLng, location: String?) {
        marker?.remove()
        marker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(location)
        )
    }

    private fun setMapOnClick() {
        map.setOnMapClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            selectedLatLng = latLng
            selectedLocation = snippet

            setMarker(latLng, snippet)

            binding.selectLocationButtonSave.visibility = View.VISIBLE
        }
    }
}
