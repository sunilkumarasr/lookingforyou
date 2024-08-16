package com.stranger_sparks.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.stranger_sparks.R
import com.stranger_sparks.agora.media.AudioCall.IncomingAudioCallActivity
import com.stranger_sparks.agora.media.VideoCall.IncomingVideoCallsActivity
import com.stranger_sparks.view.activities.ui.activities.notifications.Notifications


import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class MyFirebaseMessagingServiceOLD : FirebaseMessagingService() {

    companion object {
        private val TAG = "MyFirebaseToken"
        private var title: String = ""
        var token: String? = null
        var body: String? = ""
        var type: String? = ""
        var receiver_id: String? = ""
        var id: String? = null
        lateinit var context: Context
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.e("onMessageReceived: ", p0.data.toString())

        p0.data.isNotEmpty().let {
            val data = p0.data
            val message = data["body"] ?: return
            val obj = JSONObject(message)
            val notification = obj.getJSONObject("notification")
            val title = notification.getString("title")
            val body = notification.getString("body")
            val type = notification.getString("type")
            val email = notification.getString("email")
            //val soundUrl = notification.getString("sound")

            // Download the custom ringtone
            //val soundUri = downloadCustomRingtone(soundUrl)

            val intent: Intent
            if(type.equals("Audio")){
                 intent = Intent(this, IncomingAudioCallActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                     putExtra("title", title)
                     putExtra("email", email)
                }
            }else if(type.equals("Video")){
                 intent = Intent(this, IncomingVideoCallsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                     putExtra("title", title)
                     putExtra("email", email)
                }
            }else{
                 intent = Intent(this, Notifications::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }
            val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val channelId = "1"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkNotificationChannel(channelId)
            }

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle("Stranger Sparks")
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //.setSound(soundUri)
                .setStyle(
                    NotificationCompat.MessagingStyle(title)
                        .setGroupConversation(false)
                        .addMessage(body, System.currentTimeMillis(), title)
                )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notificationBuilder.build())
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkNotificationChannel(CHANNEL_ID: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stranger Sparks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = body
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun downloadCustomRingtone(soundUrl: String): Uri? {
        try {
            // Create a URL object
            val url = URL(soundUrl)

            // Open a connection to the URL
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            // Create a temporary file to store the downloaded ringtone
            val file = File.createTempFile("ringtone", ".mp3", applicationContext.cacheDir)

            // Create an output stream to write the downloaded data to the file
            val outputStream = FileOutputStream(file)

            // Read the data from the connection's input stream and write it to the file
            val inputStream = connection.inputStream
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // Close the streams
            outputStream.close()
            inputStream.close()

            // Return the URI of the downloaded file
            return Uri.fromFile(file)
        } catch (e: Exception) {
            // Handle any errors that occur during the download process
            Log.e("DownloadRingtone", "Error downloading ringtone", e)
        }
        return null
    }

    override fun onNewToken(p0: String) {
        token = p0
        super.onNewToken(p0)
    }


}