package com.aortiz.android.thermosmart.thermostat.config.selectlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.SelectLocationFragmentBinding
import com.aortiz.android.thermosmart.thermostat.config.ThermostatConfigViewModel
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
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*


class SelectLocationFragment : OnMapReadyCallback, Fragment() {

    private lateinit var binding: SelectLocationFragmentBinding
    private val viewModel: ThermostatConfigViewModel by inject()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private var marker: Marker? = null
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var selectedLatLng: LatLng? = null
    private var selectedLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.select_location_fragment, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.selectLocationButtonSave.setOnClickListener {
            onLocationSelected()
        }
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.selectLocationMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onLocationSelected() {
        viewModel.latitude.value = selectedLatLng?.latitude
        viewModel.longitude.value = selectedLatLng?.longitude
        viewModel.location.value = selectedLocation
        findNavController().popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Timber.d("onMapReady")
        map = googleMap!!
        setMapOnClick()
        enableMyLocation()
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        Timber.d("enableMyLocation")
        if (isPermissionGranted()) {
            Timber.d("enableMyLocation: permission granted")
            map.isMyLocationEnabled = true
            if (!viewModel.location.value.isNullOrEmpty() && !viewModel.location.value.contentEquals(
                    "null"
                )
            ) {
                val latLng = LatLng(
                    viewModel.latitude.value ?: defaultLocation.latitude,
                    viewModel.longitude.value ?: defaultLocation.longitude,
                )
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng, 15f
                    )
                )
                setMarker(latLng, viewModel.location.value)
            } else {
                getDeviceLocation()
            }
        } else {
            Timber.d("enableMyLocation: permission not granted")
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("onRequestPermissionsResult: requestCode? $requestCode permissions? $permissions grantResults? $grantResults")
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Timber.d("onRequestPermissionsResult: granted, try to enable my location")
                enableMyLocation()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.permission_denied_explanation),
                    Toast.LENGTH_SHORT
                ).show()
                Timber.d("onRequestPermissionsResult: not granted")
            }
        } else {
            Timber.d("onRequestPermissionsResult: not managed code")
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
                        map?.moveCamera(
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
                    map?.moveCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, 15f)
                    )
                    map?.uiSettings?.isMyLocationButtonEnabled = false
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
