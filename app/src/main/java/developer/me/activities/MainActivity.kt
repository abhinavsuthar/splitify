package developer.me.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import developer.me.R
import developer.me.adapters.MusicListAdapter
import developer.me.commands.MusicListResult
import developer.me.services.MusicService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Playlist"

        getMusicList()
        MusicService.stopAllMusicPlayer()

        fab.setOnClickListener({
            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
        })
    }

    private fun getMusicList() {
        ma_musicList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                ma_musicList.adapter = MusicListAdapter(MusicListResult().execute().songs)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 999)
            }
        else ma_musicList.adapter = MusicListAdapter(MusicListResult().execute().songs)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 999) {
            ma_musicList.adapter = MusicListAdapter(MusicListResult().execute().songs)
        }
    }
}
