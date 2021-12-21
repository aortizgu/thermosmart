package com.aortiz.android.thermosmart.thermostat.config.selectlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.SelectLocationFragmentBinding
import com.aortiz.android.thermosmart.utils.setDisplayHomeAsUpEnabled
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import timber.log.Timber
import java.util.*


class SelectLocationFragment : OnMapReadyCallback, Fragment() {

    private lateinit var binding: SelectLocationFragmentBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private var marker: Marker? = null
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var selectedPOI: PointOfInterest? = null
    private var selectedLatLng: LatLng? = null
    private var selectedLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.select_location_fragment, container, false)

//        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
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
//        _viewModel.latitude.value = selectedLatLng?.latitude
//        _viewModel.longitude.value = selectedLatLng?.longitude
//        _viewModel.reminderSelectedLocationStr.value = selectedLocation
//        _viewModel.selectedPOI.value = selectedPOI
//        _viewModel.navigationCommand.value = NavigationCommand.Back
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
        //setPoiClick()
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
            getDeviceLocation()
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
//                _viewModel.showErrorMessage.value =
//                    getString(R.string.permission_denied_explanation)
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

//    private fun setPoiClick() {
//        Timber.d("setPoiClick")
//        map.setOnPoiClickListener { poi ->
//            selectedPOI = poi
//            selectedLatLng = poi.latLng
//            selectedLocation = poi.name
//            marker?.remove()
//            map.addMarker(
//                MarkerOptions()
//                    .position(poi.latLng)
//                    .title(poi.name)
//            ).let {
//                it.showInfoWindow()
//                marker = it
//            }
//
//            binding.selectLocationButtonSave.visibility = View.VISIBLE
//        }
//    }

    private fun setMapOnClick() {
        map.setOnMapClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            selectedPOI = null
            selectedLatLng = latLng
            selectedLocation = snippet

            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )

            binding.selectLocationButtonSave.visibility = View.VISIBLE
        }
    }
}
