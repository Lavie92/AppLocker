package com.lavie.applocker.view

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lavie.applocker.service.ForegroundService
import com.lavie.applocker.databinding.ActivityMainBinding
import com.lavie.applocker.model.App
import com.lavie.applocker.model.AppDatabase
import com.lavie.applocker.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppAdapter
    private lateinit var appRepository: AppRepository
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        appRepository = AppRepository(database.appDao())
        adapter = AppAdapter { app, isLocked ->
            lifecycleScope.launch(Dispatchers.IO) {
                appRepository.updateAppLockStatus(app.appPackage, isLocked)
            }
        }
        binding.rcApp.layoutManager = LinearLayoutManager(this)
        binding.rcApp.adapter = adapter

        if (!hasUsageStatsPermission()) {
            requestUsageAccessPermission()
        } else {
            startForegroundService()
        }

        val apps = getInstalledApps()
        adapter.submitList(apps)

        scope.launch {
            for (app in apps) {
                appRepository.insertApp(app)
            }
        }


        if (!checkOverlayPermission(this)) {
            requestOverlayPermission()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun checkOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivity(intent)
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startService(serviceIntent)
    }

    private fun getInstalledApps(): List<App> {
        val packageManager: PackageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return installedApps
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { appInfo ->
                App(
                    appPackage = appInfo.packageName,
                    name = appInfo.loadLabel(packageManager).toString(),
                    icon = appInfo.packageName,
                    isLocked = false
                )
            }
            .sortedBy { it.name }
    }
}
