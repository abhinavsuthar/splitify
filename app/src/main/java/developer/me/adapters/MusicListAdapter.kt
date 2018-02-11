package developer.me.adapters

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import developer.me.App
import developer.me.R
import developer.me.data.Song
import developer.me.services.MusicService

class MusicListAdapter(private val songs: List<Song>) : RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MusicListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.musiclist_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicListAdapter.ViewHolder, position: Int) {
        holder.title?.text = songs[position].title
        holder.artist?.text = songs[position].artist
        holder.duration?.text = songs[position].duration

        holder.left?.setOnClickListener({ playSongOnLeft(position); holder.fab?.visibility = View.VISIBLE })

        holder.right?.setOnClickListener({ playSongOnRight(position); holder.fab?.visibility = View.VISIBLE })

        holder.card?.setOnClickListener({ playSongOnCenter(position); holder.fab?.visibility = View.GONE })


    }

    override fun getItemCount(): Int = songs.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView? = view.findViewById(R.id.ml_cover)
        val title: TextView? = view.findViewById(R.id.ml_title)
        val artist: TextView? = view.findViewById(R.id.ml_artist)
        val duration: TextView? = view.findViewById(R.id.ml_album)
        val left: ImageView? = view.findViewById(R.id.ml_left)
        val right: ImageView? = view.findViewById(R.id.ml_right)
        val card: RelativeLayout? = view.findViewById(R.id.ml_baseRelativeLayout)

        val fab: FloatingActionButton? = view.findViewById(R.id.fab)
    }

    private fun playSongOnLeft(index: Int) {

        val left = Intent(App.instance, MusicService::class.java)
        left.putExtra("index", index)
        left.putExtra("stereo", 0)
        App.instance.startService(left)
    }

    private fun playSongOnRight(index: Int) {

        val right = Intent(App.instance, MusicService::class.java)
        right.putExtra("index", index)
        right.putExtra("stereo", 1)
        App.instance.startService(right)
    }

    private fun playSongOnCenter(index: Int) {

        val center = Intent(App.instance, MusicService::class.java)
        center.putExtra("index", index)
        center.putExtra("stereo", 2)
        App.instance.startService(center)
    }
}