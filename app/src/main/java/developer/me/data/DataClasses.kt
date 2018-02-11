package developer.me.data


data class MusicList(val songs: ArrayList<Song>)
data class Song(val title: String, val artist: String = "<unknown>", val album: String = "<Unknown>", val duration: String, val path: String)



