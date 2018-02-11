package developer.me.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import developer.me.R
import developer.me.activities.MainActivity
import developer.me.utils.MusicPlayer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask


class MusicService : Service() {

    companion object {
        val mpLeft by lazy { MusicPlayer('L') }
        val mpRight by lazy { MusicPlayer('R') }
        val mpCenter by lazy { MusicPlayer('C') }

        fun stopAllMusicPlayer() {
            mpLeft.stop()
            mpRight.stop()
            mpCenter.stop()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val index = intent.getIntExtra("index", 0)
        val stereo = intent.getIntExtra("stereo", 2)
        registerBroadcastReceiver()

        when (stereo) {
            0 -> {
                mpLeft.playSong(index)
                mpCenter.pause()
                if (!isHeadsetConnected()) mpRight.stop()
                updateNotificationTime = false
                stopForeground(true)
            }
            1 -> {
                mpRight.playSong(index)
                mpCenter.pause()
                if (!isHeadsetConnected()) mpLeft.stop()
                updateNotificationTime = false
                stopForeground(true)
            }
            2 -> {
                mpCenter.playSong(index)
                mpLeft.pause()
                mpRight.pause()
                updateNotificationTime = true
                showNotification(index)
                mpCenter.setOnCompletionListener({ mpCenter.playNext(); showNotification(mpCenter.getCurrentIndex()) })
            }
            else -> Toast.makeText(applicationContext, "Error occurred", Toast.LENGTH_SHORT).show()
        }

        return START_NOT_STICKY
    }

    private fun isHeadsetConnected(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.isWiredHeadsetOn
    }


    //--------------------------------------------------------------------------------------------//
    //All about showing notification
    private val contentView: RemoteViews by lazy { RemoteViews(packageName, R.layout.notification_card) }
    private var notification: Notification? = null
    private var vdNotiTimeTimer: Timer? = null
    private var updateNotificationTime = true

    private fun showNotification(index: Int) {

        Log.d("Suthar", "Notification ")

        musicAlbumArt(index)

        contentView.setImageViewResource(R.id.noti_vd_prev, R.drawable.noti_previous_track)
        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.noti_pause)
        contentView.setImageViewResource(R.id.noti_vd_next, R.drawable.noti_next_track)
        contentView.setTextViewText(R.id.noti_title, MusicPlayer.list[index].title)

        val intent = Intent(this, MainActivity::class.java)
        val pIntent: PendingIntent? = PendingIntent.getActivity(this, 500, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder = Notification.Builder(applicationContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(contentView)
                .setContentIntent(pIntent)

        val prevIntent = Intent()
        prevIntent.action = "prev"
        val pPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent()
        playIntent.action = "play"
        val pPlayIntent = PendingIntent.getBroadcast(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent()
        nextIntent.action = "next"
        val pNextIntent = PendingIntent.getBroadcast(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val deleteIntent = Intent()
        deleteIntent.action = "stopService"
        val pDeleteIntent = PendingIntent.getBroadcast(this, 3, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        contentView.setOnClickPendingIntent(R.id.noti_vd_prev, pPrevIntent)
        contentView.setOnClickPendingIntent(R.id.noti_vd_play, pPlayIntent)
        contentView.setOnClickPendingIntent(R.id.noti_vd_next, pNextIntent)
        mBuilder.setDeleteIntent(pDeleteIntent)

        notification = mBuilder.build()
        notification?.flags = notification!!.flags or Notification.DEFAULT_LIGHTS

        startForeground(999, notification)
        notificationTime(mpCenter, 999)
        registerBroadcastReceiver()
    }

    private fun notificationTime(mp: MediaPlayer, id: Int) {
        try {
            vdNotiTimeTimer?.cancel()
        } catch (ignored: Exception) {
        }

        vdNotiTimeTimer = Timer()
        var duration = mp.duration
        val d2 = Date(mp.duration.toLong())
        duration /= 1000
        val df: SimpleDateFormat
        if (duration < 3600)
            df = SimpleDateFormat("mm:ss")
        else
            df = SimpleDateFormat("HH:mm:ss")
        df.setTimeZone(TimeZone.getTimeZone("UTC"))

        vdNotiTimeTimer?.scheduleAtFixedRate(timerTask {
            val position = mp.currentPosition
            val d = Date(position.toLong())
            contentView.setTextViewText(R.id.noti_vd_time, df.format(d) + "/" + df.format(d2))
            if (updateNotificationTime) startForeground(id, notification)
        }, 0, 1000)
    }

    private fun musicAlbumArt(index: Int) {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(MusicPlayer.list[index].path)
            val data = mmr.embeddedPicture
            if (data == null)
                contentView.setImageViewResource(R.id.noti_album_art, R.drawable.song_cover)
            else {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                contentView.setImageViewBitmap(R.id.noti_album_art, bitmap)
            }
        } catch (e: IllegalArgumentException) {
            contentView.setImageViewResource(R.id.noti_album_art, R.drawable.song_cover)
        }
    }

    private fun pauseMediaPlayer() {
        mpCenter.pause()
        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.noti_play)
        startForeground(999, notification)
        updateNotificationTime = false
        stopForeground(false)
    }

    private fun resumeMediaPlayer() {
        mpCenter.start()
        contentView.setImageViewResource(R.id.noti_vd_play, R.drawable.noti_pause)
        startForeground(999, notification)
        updateNotificationTime = true
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    //Notification click event handler
    private var handler = NotificationActionHandler()

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter()
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        filter.addAction("prev")
        filter.addAction("play")
        filter.addAction("next")
        filter.addAction("stopService")
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        this.registerReceiver(handler, filter)
    }

    private inner class NotificationActionHandler : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == null) return

            when (intent.action) {
                "prev" -> { mpCenter.playPrevious(); showNotification(mpCenter.getCurrentIndex()) }
                "play" -> if (mpCenter.isPlaying) pauseMediaPlayer() else resumeMediaPlayer()
                "next" -> { mpCenter.playNext(); showNotification(mpCenter.getCurrentIndex()) }
                "stopService" -> stopSelf()
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    try {
                        pauseMediaPlayer()
                        mpLeft.pause()
                        mpRight.pause()
                    } catch (e: Exception) {
                        mpRight.stop()
                        mpLeft.stop()
                        mpCenter.stop()
                    }
                }
                else -> Toast.makeText(applicationContext, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
