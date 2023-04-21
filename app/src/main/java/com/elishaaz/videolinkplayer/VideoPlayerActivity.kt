package com.elishaaz.videolinkplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.elishaaz.videolinkplayer.databinding.ActivityVideoPlayerBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding

    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var videoId: String

    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        youTubePlayerView = binding.youtubePlayerView

        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (!verifyIntent()) return

        videoId = intent.data?.getQueryParameter("v") ?: return


        initYouTubePlayerView()
    }

    private fun verifyIntent(): Boolean {
        val intentData = intent.data ?: return false
        val intentScheme = intentData.scheme ?: return false

        if (intentScheme != "https" && intentScheme != "http") return false
        if (intentData.host?.contains("youtube", ignoreCase = true) != true) return false
        return true
    }

    private fun initYouTubePlayerView() {
        lifecycle.addObserver(youTubePlayerView)
        val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                // using pre-made custom ui
                val defaultPlayerUiController =
                    DefaultPlayerUiController(youTubePlayerView, youTubePlayer)

                defaultPlayerUiController.showYouTubeButton(false)
                    .setFullscreenButtonClickListener { toggleFullscreen() }
                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.rootView)

                youTubePlayer.loadOrCueVideo(
                    lifecycle,
                    videoId,
                    0f
                )
            }
        }

        // disable web ui
        val options: IFramePlayerOptions = IFramePlayerOptions.Builder().controls(0).build()
        youTubePlayerView.initialize(listener, options)
    }

    fun toggleFullscreen() {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d("OrientationChanged", newConfig.orientation.toString())
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            youTubePlayerView.matchParent()
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            youTubePlayerView.wrapContent()
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}