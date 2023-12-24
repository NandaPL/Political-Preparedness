package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {

    private val _voterInfoResponse = MutableLiveData<VoterInfoResponse>()
    val voterInfoResponse : LiveData<VoterInfoResponse>
        get() = _voterInfoResponse

    private val _locationString = MutableLiveData<String>()
    val locationString : LiveData<String>
        get() = _locationString

    private var id = 0

    private val _savedElection = MutableLiveData<Election>()
    val savedElection : LiveData<Election>
        get() = _savedElection

    fun setLocationAndElectionIdAndGetVoterResponse(location: String, Id : Int){
        _locationString.value = location
        id = Id
        getVoterInfoResponse()
    }

    private fun getVoterInfoResponse(){
        try{
            viewModelScope.launch {
                val response = CivicsApi.retrofitService.getVoterInfo(_locationString.value!!,id)
                _voterInfoResponse.value = response
            }
        }catch (e: Exception){
            Timber.tag("Error").i(" Trying to Get Data From Retrofit")
        }

    }

    fun checkElectionByIdFromDatabase(electionId: Int) {
        viewModelScope.launch {
            _savedElection.value = dataSource.getElectionById(id)
        }
    }


    fun saveElection(election : Election){
        viewModelScope.launch {
            dataSource.insert(election)
            checkElectionByIdFromDatabase(election.id)
        }
    }
    fun deleteElection(election: Election){
        viewModelScope.launch {
            dataSource.deleteElection(election)
            checkElectionByIdFromDatabase(election.id)
        }
    }

}