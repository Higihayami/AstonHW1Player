package com.example.astonhw1player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class PlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var tracks: List<Int>
    private var currentTrackIndex = -1
    private val NOTIFICATION_ID = 1000
    private var playerBinder: PlayerBinder? = null

    override fun onBind(intent: Intent): IBinder {
        playerBinder = PlayerBinder()
        return PlayerBinder()
    }

    override fun onCreate() {
        val rawResources: List<Int> =
            listOf(R.raw.music1, R.raw.music2, R.raw.music3, R.raw.music4, R.raw.music5)
        mediaPlayer = MediaPlayer()
        tracks = rawResources
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)

        intent?.let {
            val action = intent.action
            when (action) {
                "PLAY_ACTION" -> {
                    if (playerBinder?.state() == true) {
                        playerBinder?.pause()
                    } else {
                        playerBinder?.play()
                        notificationLayout.setImageViewResource(R.id.btn_play, R.drawable.ic_pause)
                    }
                    startForeground(NOTIFICATION_ID, createNotification())
                }
            }
        }

        intent?.let {
            val action = intent.action
            when (action) {
                "PLAY_NEXT_ACTION" -> {
                    playerBinder?.playNext()
                    startForeground(NOTIFICATION_ID, createNotification())
                }
            }
        }

        intent?.let {
            val action = intent.action
            when (action) {
                "PLAY_PREVIOUS_ACTION" -> {
                    playerBinder?.playPrevious()
                    startForeground(NOTIFICATION_ID, createNotification())
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val playIntent = Intent(this, PlayerService::class.java).apply {
            action = "PLAY_ACTION"
        }
        val playPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_MUTABLE)
        notificationLayout.setOnClickPendingIntent(R.id.btn_play, playPendingIntent)

        val playNextIntent = Intent(this, PlayerService::class.java).apply {
            action = "PLAY_NEXT_ACTION"
        }
        val playNextPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, playNextIntent, PendingIntent.FLAG_MUTABLE)
        notificationLayout.setOnClickPendingIntent(R.id.btn_next, playNextPendingIntent)

        val playPreviousIntent = Intent(this, PlayerService::class.java).apply {
            action = "PLAY_PREVIOUS_ACTION"
        }
        val playPreviousPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, playPreviousIntent, PendingIntent.FLAG_MUTABLE)
        notificationLayout.setOnClickPendingIntent(R.id.btn_previous, playPreviousPendingIntent)

        val notification = NotificationCompat.Builder(this, "10000")
            .setSmallIcon(R.drawable.ic_play)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setSilent(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("10000", "Music", NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(channel)
            notification.setChannelId("10000")
        }

        return notification.build()
    }

    interface PlayerServiceInterface {
        fun playNext()
        fun playPrevious()
        fun pause()
        fun state(): Boolean
        fun play()
        fun init()
        fun getCurrentPosition(): Int
        fun seekTo(position: Int)
        fun getDuration(): Int
    }

    inner class PlayerBinder : Binder(), PlayerServiceInterface {

        override fun playNext() {
            if (currentTrackIndex == tracks.size - 1) {
                currentTrackIndex = 0
            } else {
                currentTrackIndex++
            }
            playTrack(tracks[currentTrackIndex])
        }

        override fun init() {
            if (currentTrackIndex == tracks.size - 1) {
                currentTrackIndex = 0
            } else {
                currentTrackIndex++
            }
            initTrack(tracks[currentTrackIndex])
        }

        private fun initTrack(trackResId: Int) {
            mediaPlayer?.reset()
            mediaPlayer = MediaPlayer.create(this@PlayerService, trackResId)
            setOnCompletionListener()
        }

        private fun setOnCompletionListener() {
            mediaPlayer?.setOnCompletionListener {
                playNext()
            }
        }

        override fun playPrevious() {
            if (currentTrackIndex == 0) {
                currentTrackIndex = 4
            } else {
                currentTrackIndex--
            }
            playTrack(tracks[currentTrackIndex])
        }

        private fun playTrack(trackResId: Int) {
            mediaPlayer?.reset()
            mediaPlayer = MediaPlayer.create(this@PlayerService, trackResId)
            mediaPlayer?.start()
            setOnCompletionListener()
        }

        override fun pause() {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        }

        override fun play() {
            mediaPlayer?.start()
        }

        override fun state(): Boolean {
            return mediaPlayer?.isPlaying == true
        }

        override fun seekTo(position: Int) {
            mediaPlayer?.seekTo(position)
        }

        override fun getCurrentPosition(): Int {
            return mediaPlayer?.currentPosition ?: 0
        }

        override fun getDuration(): Int {
            return mediaPlayer?.duration ?: 0
        }
    }
}