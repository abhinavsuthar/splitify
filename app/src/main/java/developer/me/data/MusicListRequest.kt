package developer.me.data

import android.preference.PreferenceManager
import android.provider.MediaStore
import com.google.gson.Gson
import developer.me.App


class MusicListRequest {

    fun execute(): MusicList {


        val uriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID, MediaStore.Images.Media.DATA)
        val cur = App.instance.contentResolver.query(uriExternal, projection, null, null, null)


        val songList: ArrayList<Song> = ArrayList()

        if (cur != null) {
            while (cur.moveToNext()) {
                val artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val filePathIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                val song = Song(cur.getString(titleColumn), cur.getString(artistColumn), cur.getString(albumColumn), cur.getString(durationColumn), cur.getString(filePathIndex))

                songList.add(song)
            }
            cur.close()
        }
        val result = MusicList(songList)
        saveDataTOStorage(Gson().toJson(result))
        return result
    }

    private fun saveDataTOStorage(data: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(App.instance)
        val editor = sp.edit()
        editor.putString("songList", data)
        editor.apply()
    }
}