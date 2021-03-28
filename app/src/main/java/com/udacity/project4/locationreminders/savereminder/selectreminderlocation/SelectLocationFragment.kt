package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.permission.PermissionProvider
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var poiMarker: Marker? = null
    private lateinit var permissionProvider: PermissionProvider

    companion object {
        private const val DEFAULT_ZOOM = 15f
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        binding.saveButton.isEnabled = false
        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        permissionProvider = PermissionProvider(requireContext())
        if (permissionProvider.fineLocation.hasPermission().not()) {
            permissionProvider.fineLocation.requestPermission(::requestPermissions)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation(map)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissionProvider.fineLocation.isApproved(requestCode, permissions, grantResults)) {
            enableMyLocation(map)
        }
    }

    private fun onLocationSelected() {
        findNavController().navigateUp()
    }

    private fun getDeviceLocation() {
        try {
            if (permissionProvider.fineLocation.hasPermission()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnSuccessListener { location ->
                    location?.let {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude),
                                DEFAULT_ZOOM
                            )
                        )
                    }
                }
            }
        } catch (error: SecurityException) {
            error.message?.let {
                Timber.e("Error: $it")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(map: GoogleMap) {
        if (permissionProvider.fineLocation.hasPermission()) {
            getDeviceLocation()
            map.isMyLocationEnabled = true
        } else {
            permissionProvider.fineLocation.requestPermission(::requestPermissions)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
        } catch (error: Resources.NotFoundException) {
            error.message?.let {
                Timber.e("Error Style: $it")
            }
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            createMarker(poi)
            _viewModel.selectedPOI.value = poi
            _viewModel.latitude.value = poi?.latLng?.latitude
            _viewModel.longitude.value = poi?.latLng?.longitude
            _viewModel.reminderSelectedLocationStr.value = poi?.name
            binding.saveButton.isEnabled = true
        }
    }

    private fun createMarker(poi: PointOfInterest) {
        poiMarker?.remove()
        poiMarker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )
        poiMarker?.showInfoWindow()
    }
}
