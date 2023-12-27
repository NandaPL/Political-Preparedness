package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.setNewValue
import com.google.android.gms.location.LocationServices
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var dataBinding: FragmentRepresentativeBinding

    private val viewModel: RepresentativeViewModel by lazy {
        ViewModelProvider(this)[RepresentativeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dataBinding = FragmentRepresentativeBinding.inflate(inflater)
        dataBinding.lifecycleOwner = this
        dataBinding.viewModel = viewModel

        val representativeAdapter = RepresentativeListAdapter()
        dataBinding.rvRepresentatives.adapter = representativeAdapter

        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            it?.let {
                representativeAdapter.submitList(it)
            }
        })

        viewModel.address.observe(viewLifecycleOwner, Observer {
            it?.let {
                dataBinding.spState.setNewValue(it.state)
            }
        })

        dataBinding.btnSearch.setOnClickListener {
            if(getAddressFromUser() != null){
                viewModel.setAddressOfUser(getAddressFromUser()!!)
                viewModel.getRepresentativeResponse()
                hideKeyboard()

            }else{
                Toast.makeText(requireContext(), "Fill Out Fields!!", Toast.LENGTH_LONG).show()
            }
            hideKeyboard()
        }

        dataBinding.btnLocation.setOnClickListener {
            getLocation()
        }

        if (savedInstanceState != null) {
            val position = savedInstanceState.getInt("scroll_position", 0)
            (dataBinding.rvRepresentatives.layoutManager as LinearLayoutManager).scrollToPosition(
                position
            )
        }

        viewModel.restoreState(savedInstanceState)

        return dataBinding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        dataBinding.let { binding ->
            val layoutManager = binding.rvRepresentatives.layoutManager as? LinearLayoutManager
            val position = layoutManager?.findFirstVisibleItemPosition()
            if (position != null) {
                outState.putInt("scroll_position", position)
            }
        }
        viewModel.saveState(outState)
    }

    private fun getAddressFromUser(): Address?{
        val line1 = dataBinding.etAddressLine1.text.toString()
        val line2 = dataBinding.etAddressLine2.text.toString()
        val city = dataBinding.etCity.text.toString()
        val state = dataBinding.spState.selectedItem
        val zip = dataBinding.etZip.text.toString()

        return if(line1.isNotEmpty() && city.isNotEmpty() && state!= null && zip.isNotEmpty()){
            Address(line1, line2, city, state.toString(), zip)
        }else{
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkLocationPermissions()) {
            LocationServices.getFusedLocationProviderClient(requireContext())
                .lastLocation.addOnSuccessListener { location ->
                    viewModel.getAddressFromLocation(geoCodeLocation(location))
                    viewModel.getRepresentatives(viewModel.address.value.toString())
                }
        } else {
            // Request location permission here instead of in checkLocationPermissions
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            ?.map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }!!.first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}