package com.example.android.politicalpreparedness.election

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.google.android.gms.maps.model.LatLng
import java.util.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.location.Geocoder
import com.example.android.politicalpreparedness.R

class VoterInfoFragment : Fragment() {

    private lateinit var viewModelFactory: VoterInfoViewModelFactory
    private lateinit var viewModel: VoterInfoViewModel
    private lateinit var binding: FragmentVoterInfoBinding

    private lateinit var dataSource : ElectionDao
    private var votingInfoLocationsUrlString = ""
    private var ballotInformationUrlString = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentVoterInfoBinding.inflate(inflater)

        val args : VoterInfoFragmentArgs by navArgs()

        initViewModel()

        initUI(args)

        return binding.root
    }

    private fun initViewModel() {
        // Getting dataSource and Connecting ViewModel
        dataSource = ElectionDatabase.getInstance(requireContext()).electionDao
        viewModelFactory = VoterInfoViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]

        binding.viewModel = viewModel
    }

    private fun initUI(args: VoterInfoFragmentArgs) {
        // Changing location in viewModel
        val location  = LatLng(38.369685, -98.783669)
        val locationString = geoCodeLocation(location).toFormattedString()
        viewModel.setLocationAndElectionIdAndGetVoterResponse(locationString, args.argumentElectionId)
        viewModel.voterInfoResponse.observe(viewLifecycleOwner, Observer {
            setInfoForElection(it)
        })

        binding.btnVoterInfo.setOnClickListener { startIntentFromUrl(votingInfoLocationsUrlString) }
        binding.btnBallotInfo.setOnClickListener { startIntentFromUrl(ballotInformationUrlString) }

        viewModel.checkElectionByIdFromDatabase(args.argumentElectionId)
        viewModel.savedElection.observe(viewLifecycleOwner, Observer {
            setFollowButtonState(it, args.election)
        })

        binding.btnFollow.setOnClickListener {
            toggleFollowElection(args.election)
        }
    }

    private fun setInfoForElection(response: VoterInfoResponse) {
        votingInfoLocationsUrlString = response.state!![0].electionAdministrationBody.electionInfoUrl.toString()
        ballotInformationUrlString = response.state!![0].electionAdministrationBody.ballotInfoUrl.toString()
        binding.tbElectionName.title = response.election.name.toString()
        binding.tvElectionDate.text = response.election.electionDay.toString()
    }

    private fun startIntentFromUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }


    private fun geoCodeLocation(location: LatLng): Address {
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

    @SuppressLint("SetTextI18n")
    private fun setFollowButtonState(savedElection: Election?, currentElection: Election) =
        if (savedElection == currentElection) {
            binding.btnFollow.text = getString(R.string.unfollow_election)
            binding.tbElectionName.title = savedElection.name
            binding.tvElectionDate.text = savedElection.electionDay.toString()
        } else {
            binding.btnFollow.text = getString(R.string.follow_election)
        }

    private fun toggleFollowElection(election: Election) {
        if (viewModel.savedElection.value == election) {
            viewModel.deleteElection(election)
        } else {
            viewModel.saveElection(election)
        }
    }
}