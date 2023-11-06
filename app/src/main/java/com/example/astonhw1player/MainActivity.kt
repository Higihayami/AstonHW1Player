package com.example.astonhw1player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.SeekBar
import com.example.astonhw1player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var playerService: PlayerService.PlayerServiceInterface? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder

            playerService?.init()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val seekBar = binding.seekbar

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentPosition = playerService?.getCurrentPosition() ?: 0
                val duration = playerService?.getDuration() ?: 1

                val progress = (currentPosition * 100) / duration
                seekBar.progress = progress

                handler.postDelayed(this, 100)
            }
        }, 0)

        //Foreground notification
        val serviceIntent = Intent(this@MainActivity, PlayerService::class.java)
        startService(serviceIntent)

        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        binding.btnPlay.setImageResource(R.drawable.ic_play)

        binding.btnPlay.setOnClickListener {
            if (playerService?.state() == true) {
                binding.btnPlay.setImageResource(R.drawable.ic_play)
                playerService?.pause()
            } else {
                binding.btnPlay.setImageResource(R.drawable.ic_pause)
                playerService?.play()
            }
        }
        binding.btnNext.setOnClickListener {
            binding.btnPlay.setImageResource(R.drawable.ic_pause)
            playerService?.playNext()
        }

        binding.btnPrevious.setOnClickListener {
            binding.btnPlay.setImageResource(R.drawable.ic_pause)
            playerService?.playPrevious()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = playerService?.getDuration() ?: 1
                    val newPosition = (duration * progress) / 100
                    playerService?.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        stopService(intent)
        super.onDestroy()
    }
}