package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment : Fragment() {

    private lateinit var viewModel: ElectionsViewModel

    private lateinit var adapterUpcomingElections: ElectionListAdapter
    private lateinit var adapterSavedElections: ElectionListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentElectionBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_election,
            container,
            false
        )
        binding.lifecycleOwner = this

        val database = ElectionDatabase.getInstance(requireContext())

        val viewModelFactory = ElectionsViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)
        binding.viewModel = viewModel

        val electionAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })
        binding.rvUpcomingElections.adapter = electionAdapter

        val savedElectionAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })
        binding.rvSavedElections.adapter = savedElectionAdapter

        viewModel.navigateToVoterInfoFragment.observe(viewLifecycleOwner) {
            it?.let { election ->
                this.findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        election.id,
                        election.division
                    )
                )
                viewModel.onNavigationComplete()
            }
        }
        return binding.root
    }
}