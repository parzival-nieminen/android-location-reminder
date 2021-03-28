package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.permission.PermissionProvider
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geoClient: GeofencingClient
    private lateinit var permissionProvider: PermissionProvider

    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEO_LOCATION_ENTER
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onStart() {
        super.onStart()
        permissionProvider = PermissionProvider(requireContext())
        checkGeoPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.lifecycleOwner = this
        geoClient = LocationServices.getGeofencingClient(requireContext())
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val reminderData = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                _viewModel.latitude.value,
                _viewModel.longitude.value
            )
            _viewModel.validateAndSaveReminder(reminderData)
            _viewModel.isValid.value.let {
                if (it == true) {
                    checkGeoPermissions(reminderData = reminderData)
                    _viewModel.onClear()
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (permissionProvider.backgroundLocation
                .isApproved(requestCode, permissions, grantResults)
                .not()
        ) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    //check app settings
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettings()
        }
    }

    private fun checkGeoPermissions(reminderData: ReminderDataItem? = null) {
        if (permissionProvider.hasPermissions()) {
            checkDeviceLocationSettings(reminderData = reminderData)
        } else {
            requestFineLocationPermission()
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (permissionProvider.backgroundLocation.hasPermission()) {
            return
        }
        if (permissionProvider.fineLocation.hasPermission()) {
            permissionProvider.backgroundLocation.requestPermission(::requestPermissions)
        }
    }

    private fun requestFineLocationPermission() {
        if (permissionProvider.fineLocation.hasPermission()) {
            return
        }
        permissionProvider.fineLocation.requestPermission(::requestPermissions)
    }

    private fun checkDeviceLocationSettings(
        resolve: Boolean = true,
        reminderData: ReminderDataItem? = null
    ) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {

                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (error: IntentSender.SendIntentException) {
                    error.message?.let {
                        Timber.d("Error: $it")
                    }
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_required_error, Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                reminderData?.let { reminderDataItem ->
                    addGeoTracking(reminderDataItem)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeoTracking(reminderData: ReminderDataItem) {
        val geoLocation = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geoLocation)
            .build()
        //remove pending or active geo intent first
        geoClient.removeGeofences(geoPendingIntent)?.run {
            addOnCompleteListener {
                geoClient.addGeofences(request, geoPendingIntent)?.run {
                    addOnSuccessListener {
                        Timber.i("Added geo location: ${reminderData.title}")
                    }
                    addOnFailureListener {
                        _viewModel.showErrorMessage.value =
                            getString(R.string.error_adding_geofence)
                        it.message?.let { msg ->
                            Timber.e("error: $msg")
                        }
                    }
                }
            }
        }
    }
}

const val ACTION_GEO_LOCATION_ENTER = "ACTION_GEO_LOCATION_ENTER"
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
