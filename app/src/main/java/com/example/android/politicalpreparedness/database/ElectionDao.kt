package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(election: Election)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllElections(vararg elections: Election)

    @Query("SELECT * FROM election_table")
    fun getSavedElections(): LiveData<List<Election>>

    @Query("SELECT * FROM election_table WHERE id in (SELECT id FROM followed_election_table) ORDER BY electionDay DESC")
    fun getFollowedElections(): LiveData<List<Election>>

    @Query("SELECT * FROM election_table WHERE id =:electionId")
    suspend fun getElectionById(electionId: Int): Election

    @Query("SELECT CASE id WHEN NULL THEN 0 ELSE 1 END FROM followed_election_table WHERE id = :idElection")
    fun isElectionFollowed(idElection: Int): LiveData<Int>


    @Query("INSERT INTO followed_election_table (id) VALUES(:idElection)")
    suspend fun followElection(idElection: Int)
    suspend fun followElection(election: Election) {
        followElection(election.id)
    }

    @Query("DELETE FROM election_table WHERE id =:electionId")
    suspend fun deleteElection(electionId: Int)
    suspend fun deleteElection(election: Election){
        deleteElection(election.id)
    }

    @Query("DELETE FROM followed_election_table WHERE id = :idElection")
    suspend fun unfollowElection(idElection: Int)
    suspend fun unfollowElection(election: Election) {
        unfollowElection(election.id)
    }

    @Query("SELECT * FROM election_table")
    suspend fun getAll() : List<Election>

    @Query("DELETE FROM election_table")
    fun clearAllElections()

}