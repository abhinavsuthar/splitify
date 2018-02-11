package developer.me.commands

import developer.me.data.MusicList
import developer.me.data.MusicListRequest

class MusicListResult : Command<MusicList> {
    override fun execute(): MusicList {
        return MusicListRequest().execute()
    }
}