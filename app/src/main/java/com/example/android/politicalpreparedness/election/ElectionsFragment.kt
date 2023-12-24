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
    private lateinit var viewModelFactory: ElectionsViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this

        setupViewModel()
        setupAdapters(binding)
        refreshSavedElections()

        binding.viewModel = viewModel

        return binding.root
    }

    private fun setupViewModel() {
        val dataSource = ElectionDatabase.getInstance(requireActivity().applicationContext).electionDao
        viewModelFactory = ElectionsViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)[ElectionsViewModel::class.java]
    }

    private fun setupAdapters(binding: FragmentElectionBinding) {
        val electionListener = ElectionListener { election ->
            val action = ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id, election.division, election)
            findNavController().navigate(action)
        }

        binding.rvUpcomingElections.adapter = ElectionListAdapter(electionListener)
        binding.rvSavedElections.adapter = ElectionListAdapter(electionListener)
    }

    private fun refreshSavedElections() {
        viewModel.getSavedElectionsFromDatabase()
    }
}