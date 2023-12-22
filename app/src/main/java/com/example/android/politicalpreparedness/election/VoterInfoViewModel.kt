package com.example.android.politicalpreparedness.election

import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoterInfoViewModel(
    private val dataSource: ElectionDao,
    private val electionId: Int,
    private val division: Division
) : ViewModel() {

    var electionFromDatabase: Election? = null

    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    init {
        getVoterInfo()
    }

    private fun getVoterInfo() {
        viewModelScope.launch {
            var address = "country:${division.country}"
            if (!division.state.isBlank() && !division.state.isEmpty()) {
                address += "/state:${division.state}"
            } else {
                address += "/state:ca"
            }
            _voterInfo.value = CivicsApi.retrofitService.getVoterInfo(
                address, electionId
            )
        }
    }

    private val _votingLocationsUrl = MutableLiveData<String?>()
    val votingLocationUrl: LiveData<String?>
        get() = _votingLocationsUrl

    fun votingLocationsClick() {
        _votingLocationsUrl.value =
            _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl
    }

    fun votingLocationsNavigated() {
        _votingLocationsUrl.value = null
    }

    // Ballot Information
    private val _ballotInformationUrl = MutableLiveData<String?>()
    val ballotInformationUrl: LiveData<String?>
        get() = _ballotInformationUrl

    fun ballotInformationClick() {
        _votingLocationsUrl.value =
            _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
    }

    fun ballotInformationNavigated() {
        _ballotInformationUrl.value = null
    }

    private val _isElectionFollowed: LiveData<Int>
        get() = dataSource.isElectionFollowed(electionId)

    val isElectionFollowed =
        Transformations.map(_isElectionFollowed) { followValue ->
            followValue?.let {
                followValue == 1
            }
        }

    fun followUnfollowButton() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (isElectionFollowed.value == true) {
                    dataSource.unfollowElection(electionId)
                } else {
                    dataSource.followElection(electionId)
                }
            }
        }
    }
}