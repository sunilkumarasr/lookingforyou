package com.stranger_sparks.fcm

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.stranger_sparks.utils.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallService : Service() {

    override fun onCreate() {
        super.onCreate()
        // Must be called within 5 seconds
        startForeground(101, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPrefs = SharedPreferenceManager(applicationContext)
            val profileId = sharedPrefs.getCallUserIdValue("profile_id")
            val title = sharedPrefs.getCallUserIdValue("title")

            if (!profileId.isNullOrEmpty() && !title.isNullOrEmpty()) {
                if (isNetworkAvailable(applicationContext)) {
                    ZegoCallManager.initialize(application, profileId, title)
                    stopSelf()
                } else {
                    registerNetworkCallback(sharedPrefs)
                }
            } else {
                Log.e("CallService", "Missing profile_id or title")
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "Stranger Sparks"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Stranger Sparks",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.stranger_sparks.R.drawable.ic_call)
            .setContentTitle("Incoming Call...")
            .setContentText("Setting up call environment...")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .build()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun registerNetworkCallback(sharedPrefs: SharedPreferenceManager) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val userID = sharedPrefs.getSavedLoginResponseUser()?.data?.id.toString()
                val userName = sharedPrefs.getSavedLoginResponseUser()?.data?.name.toString()
                ZegoCallManager.initialize(application, userID, userName)

                Log.e("CallService", "Zego initialized after network became available.")
                connectivityManager.unregisterNetworkCallback(this)
                stopSelf()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}