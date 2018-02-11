package developer.me.utils

import android.media.MediaPlayer
import android.preference.PreferenceManager
import android.widget.Toast
import com.google.gson.Gson
import developer.me.App
import developer.me.data.MusicList
import developer.me.data.Song
import java.util.ArrayList

class MusicPlayerException(error: String = "Player Error") : RuntimeException()

class MusicPlayer(channel: Char = 'C') : MediaPlayer() {
    companion object {

        val list: ArrayList<Song> by lazy { getSongsDataClass().songs }

        private fun getSongsDataClass(): MusicList {
            val sp = PreferenceManager.getDefaultSharedPreferences(App.instance)
            val json = sp.getString("songList", "")
            return Gson().fromJson(json, MusicList::class.java)
        }
    }

    private val mChannel = channel
    private var mIndex = 0

    fun playNext() {
        if (mIndex == list.size - 1) Toast.makeText(App.instance, "End of playlist reached", Toast.LENGTH_SHORT).show()//throw MusicPlayerException("Media list is out of bound")
        else playSong(++mIndex)
    }

    fun playPrevious() {
        if (mIndex == 0) Toast.makeText(App.instance, "Already at start of playlist", Toast.LENGTH_SHORT).show()// throw MusicPlayerException("Media list is out of bound")
        else playSong(--mIndex)
    }

    fun playSong(index: Int) {
        setIndex(index)
        prepareAndStart()
    }

    private fun setIndex(index: Int = 0) {
        mIndex = index
    }

    private fun setSource(){
        reset()
        setDataSource(list[mIndex].path)
    }

    fun prepareAndWait() {
        setChannelVolume()
        setSource()
        prepareAsync()
    }

    private fun prepareAndStart() {
        setChannelVolume()
        setSource()
        prepare()
        start()
    }

    private fun setChannelVolume() {
        when (mChannel) {
            'C' -> setVolume(1F, 1F)
            'L' -> setVolume(1F, 0F)
            'R' -> setVolume(0F, 1F)
        }
    }

    fun getTitle() = list[mIndex].title
    fun getAlbum() = list[mIndex].album
    fun getArtist() = list[mIndex].artist
    fun getCurrentPath() = list[mIndex].path
    fun getCurrentIndex() = mIndex
    fun getChannel() = mChannel
}