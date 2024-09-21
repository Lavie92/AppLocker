package com.lavie.applocker.repository

import com.lavie.applocker.model.App
import com.lavie.applocker.model.AppDao
import java.util.concurrent.Flow

class AppRepository(private  val appDao: AppDao) {
    val allApps = appDao.getApps()
    val lockedApp = appDao.getLockedApp()

    suspend fun  insertApp(app: App) {
        appDao.insertApp(app)
    }

    suspend fun updateAppLockStatus(packageName: String, isLocked: Boolean) {
        appDao.updateAppLockStatus(packageName, isLocked)
    }

    suspend fun getAppByPackage(packageName: String): App? {
        return appDao.getAppByPackage(packageName)
    }

}