package developer.me.activities


import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SeekBar
import developer.me.R
import developer.me.services.MusicService
import developer.me.utils.MusicPlayer
import kotlinx.android.synthetic.main.activity_player.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask
import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import android.speech.RecognizerIntent
import android.util.Log
import org.jetbrains.anko.toast


class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicService.mpCenter.pause()
        setContentView(R.layout.activity_player)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Now Playing"
        initPlayers()
        Timer().scheduleAtFixedRate(timerTask { runOnUiThread({ updateTimer() }) }, 0, 1000)
    }

    private fun initPlayer(musicPlayer: MusicPlayer) {
        musicPlayer.setOnCompletionListener {
            musicPlayer.playNext()
            updatePlayerUI(musicPlayer)
        }
        when (musicPlayer.getChannel()) {
            'L' -> {
                updatePlayerUI(musicPlayer)
                leftPrevious.setOnClickListener({
                    neutralizePlayer(MusicService.mpRight)
                    musicPlayer.playPrevious()
                    updatePlayerUI(musicPlayer)
                })
                leftNext.setOnClickListener({
                    neutralizePlayer(MusicService.mpRight)
                    musicPlayer.playNext()
                    updatePlayerUI(musicPlayer)
                })
                leftToggle.setOnClickListener({
                    neutralizePlayer(MusicService.mpRight)
                    if (musicPlayer.isPlaying && musicPlayer.duration != 0) {
                        musicPlayer.pause()
                        updatePlayerUI(musicPlayer)
                    } else {
                        musicPlayer.start()
                        updatePlayerUI(musicPlayer)
                    }
                })
            }
            'R' -> {
                updatePlayerUI(musicPlayer)
                rightPrevious.setOnClickListener({
                    neutralizePlayer(MusicService.mpLeft)
                    musicPlayer.playPrevious()
                    updatePlayerUI(musicPlayer)
                })
                rightNext.setOnClickListener({
                    neutralizePlayer(MusicService.mpLeft)
                    musicPlayer.playNext()
                    updatePlayerUI(musicPlayer)
                })
                rightToggle.setOnClickListener({
                    neutralizePlayer(MusicService.mpLeft)
                    if (musicPlayer.isPlaying && musicPlayer.duration != 0) {
                        musicPlayer.pause()
                        updatePlayerUI(musicPlayer)
                    } else {
                        musicPlayer.start()
                        updatePlayerUI(musicPlayer)
                    }
                })
            }
        }
    }

    private fun neutralizePlayer(musicPlayer: MusicPlayer) {
        if (!isHeadsetConnected() && musicPlayer.isPlaying) {
            musicPlayer.pause()
            updateActivityUI()
        }
    }

    private fun updateTimer() {
        rightStartTime.text = getSeekTime(MusicService.mpRight.currentPosition)
        leftStartTime.text = getSeekTime(MusicService.mpLeft.currentPosition)
        rightSeekBar.post({ rightSeekBar.progress = ((MusicService.mpRight.currentPosition.toDouble() / MusicService.mpRight.duration.toDouble()) * 100).toInt() })
        leftSeekBar.post({ leftSeekBar.progress = ((MusicService.mpLeft.currentPosition.toDouble() / MusicService.mpLeft.duration.toDouble()) * 100).toInt() })
    }

    private fun initPlayers() {
        if (!MusicService.mpRight.isPlaying) MusicService.mpRight.prepareAndWait()
        if (!MusicService.mpLeft.isPlaying) MusicService.mpLeft.prepareAndWait()
        initPlayer(MusicService.mpRight)
        initPlayer(MusicService.mpLeft)
    }

    private fun updateActivityUI() {
        updatePlayerUI(MusicService.mpLeft)
        updatePlayerUI(MusicService.mpRight)
    }

    private fun updatePlayerUI(musicPlayer: MusicPlayer) {
        when (musicPlayer.getChannel()) {
            'L' -> {
                setAlbumArt(musicPlayer, leftAlbumArt)
                if (musicPlayer.isPlaying) leftToggle.setImageResource(R.drawable.noti_pause)
                else leftToggle.setImageResource(R.drawable.noti_play)
                leftTitle.text = musicPlayer.getTitle()
                leftArtist.text = musicPlayer.getArtist()
                leftAlbum.text = musicPlayer.getAlbum()
                leftEndTime.text = getSeekTime(musicPlayer.duration)
                leftSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        assert(progress in 0..100)
                        if (fromUser) {
                            musicPlayer.seekTo(progress * musicPlayer.duration / 100)
                            updateTimer()
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                    }
                })
            }
            'R' -> {
                setAlbumArt(musicPlayer, rightAlbumArt)
                if (musicPlayer.isPlaying) rightToggle.setImageResource(R.drawable.noti_pause)
                else rightToggle.setImageResource(R.drawable.noti_play)
                rightTitle.text = musicPlayer.getTitle()
                rightArtist.text = musicPlayer.getArtist()
                rightAlbum.text = musicPlayer.getAlbum()
                rightEndTime.text = getSeekTime(musicPlayer.duration)
                rightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        assert(progress in 0..100)
                        if (fromUser) {
                            musicPlayer.seekTo(progress * musicPlayer.duration / 100)
                            updateTimer()
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                    }
                })
            }
        }
    }

    private fun getSeekTime(currentPosition: Int): String {
        val seekTime: SimpleDateFormat = if ((currentPosition / 1000) < 3600)
            SimpleDateFormat("mm:ss", Locale.US)
        else
            SimpleDateFormat("HH:mm:ss", Locale.US)
        seekTime.timeZone = TimeZone.getTimeZone("UTC")
        return seekTime.format(currentPosition.toLong())
    }

    private fun setAlbumArt(musicPlayer: MusicPlayer, contentView: ImageView) {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(musicPlayer.getCurrentPath())
            val data = mmr.embeddedPicture
            if (data == null)
                contentView.setImageResource(R.drawable.song_cover)
            else {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                contentView.setImageBitmap(bitmap)
            }
        } catch (e: IllegalArgumentException) {
            contentView.setImageResource(R.drawable.song_cover)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.player_mic, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == 16908332) super.onBackPressed()
        else if (item?.itemId == R.id.mic) openMic()

        return super.onOptionsItemSelected(item)
    }


    private fun openMic() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 101)
            if (!isHeadsetConnected()) {
                MusicService.mpLeft.setVolume(0.05F, 0F)
                MusicService.mpRight.setVolume(0F, 0.05F)
            }
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMic2(){

    }

    private fun isHeadsetConnected(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.isWiredHeadsetOn
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                Log.d("Suthar", result[0])
                voiceCommands(result[0])
            }
            MusicService.mpLeft.setVolume(1F, 0F)
            MusicService.mpRight.setVolume(0F, 1F)
        }
    }

    private fun voiceCommands(command: String) {
        val action = when {
            command.contains("play") -> "play"
            command.contains("pause") || command.contains("was") -> "pause"
            command.contains("stop") -> "stop"
            else -> "#"
        }
        val channel = when {
            command.contains("left") || command.contains("online") -> 'L'
            command.contains("right") || command.contains("alright") -> 'R'
            else -> '#'
        }
        val mover = when {
            command.contains("next") -> 'N'
            command.contains("previous") -> 'P'
            else -> '!'
        }
        when {
            action == "play" && mover == 'N' && channel == 'L' -> MusicService.mpLeft.playNext()
            action == "play" && mover == 'N' && channel == 'R' -> MusicService.mpRight.playNext()
            action == "play" && mover == 'P' && channel == 'L' -> MusicService.mpLeft.playPrevious()
            action == "play" && mover == 'P' && channel == 'R' -> MusicService.mpRight.playPrevious()
            action == "pause" && channel == 'R' -> MusicService.mpRight.pause()
            action == "pause" && channel == 'L' -> MusicService.mpLeft.pause()
            action == "play" && channel == 'L' -> MusicService.mpLeft.start()
            action == "play" && channel == 'R' -> MusicService.mpRight.start()
            action == "stop" && channel == 'R' -> {
                MusicService.mpRight.pause(); MusicService.mpRight.seekTo(0)
            }
            action == "stop" && channel == 'L' -> {
                MusicService.mpLeft.pause(); MusicService.mpLeft.seekTo(0)
            }
            else -> toast("Command not recognised! Please try again...")
        }
        if (channel == 'L') neutralizePlayer(MusicService.mpRight)
        else neutralizePlayer(MusicService.mpLeft)
        updateTimer()
        updateActivityUI()
    }
}