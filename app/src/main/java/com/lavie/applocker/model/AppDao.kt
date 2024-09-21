package com.lavie.applocker.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT *  FROM apps")
    fun getApps(): Flow<List<App>>

    @Query("SELECT * FROM apps WHERE isLocked = 1")
    fun getLockedApp(): Flow<List<App>>

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    suspend fun insertApp(newApp: App)

    @Update
    suspend fun updateApp(app: App)

    @Query("UPDATE apps SET isLocked = :isLocked WHERE appPackage = :packageName")
    suspend fun updateAppLockStatus(packageName: String, isLocked: Boolean)

    @Query("SELECT * FROM apps WHERE appPackage = :packageName")
    suspend fun getAppByPackage(packageName: String): App?
}