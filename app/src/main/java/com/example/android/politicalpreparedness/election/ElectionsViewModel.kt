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
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ElectionsViewModel(private val dataSource: ElectionDao) : ViewModel() {

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>> = _upcomingElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>> = _savedElections

    init {
        loadData()
    }

    private fun loadData() {
        getUpcomingElectionsFromNetwork()
        getSavedElectionsFromDatabase()
    }

    private fun getUpcomingElectionsFromNetwork() {
        viewModelScope.launch {
            val electionResponse = CivicsApi.retrofitService.getElections()
            _upcomingElections.value = electionResponse.elections
        }
    }

    fun getSavedElectionsFromDatabase() {
        viewModelScope.launch {
            _savedElections.value = dataSource.getAll()
        }
    }
}