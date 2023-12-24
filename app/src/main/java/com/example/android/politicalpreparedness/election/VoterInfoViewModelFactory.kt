package com.example.android.politicalpreparedness.election

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.models.Division
import java.lang.IllegalArgumentException

class VoterInfoViewModelFactory(private val dataSource: ElectionDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == VoterInfoViewModel::class.java) {
            "Unknown ViewModel class"
        }
        @Suppress("UNCHECKED_CAST")
        return VoterInfoViewModel(dataSource) as T
    }
}