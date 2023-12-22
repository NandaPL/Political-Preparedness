package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionsViewModel(application: Application) : ViewModel() {
    private val database = ElectionDatabase.getInstance(application)
    private val electionRepository = ElectionRepository(database)

    val upcomingElections: LiveData<List<Election>>
        get() = electionRepository.allElections

    val followedElections: LiveData<List<Election>>
        get() = electionRepository.allFollowedElections

    init {
        viewModelScope.launch {
            electionRepository.refreshElections()
        }
    }

    private val _navigateToDetailElection = MutableLiveData<Election?>()
    private val _navigateToVoterInfoFragment = MutableLiveData<Election?>()
    val navigateToVoterInfoFragment: MutableLiveData<Election?>
        get() = _navigateToVoterInfoFragment

    fun onElectionClicked(election: Election) {
        _navigateToVoterInfoFragment.value = election
    }

    fun onNavigationComplete() {
        _navigateToVoterInfoFragment.value = null
    }

    fun onElectionNavigated() {
        _navigateToDetailElection.value = null
    }
}

class ElectionRepository(private val database: ElectionDatabase) {
    val allElections: LiveData<List<Election>> = database.electionDao.getSavedElections()

    val allFollowedElections: LiveData<List<Election>> = database.electionDao.getFollowedElections()

    val voterInfo = MutableLiveData<VoterInfoResponse>()

    suspend fun refreshElections() {
        withContext(Dispatchers.IO) {
            try {
                val electionsResponse = CivicsApi.retrofitService.getElections()
                val result = electionsResponse.elections

                database.electionDao.insertAllElections(*result.toTypedArray())

                Log.d(TAG, result.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}