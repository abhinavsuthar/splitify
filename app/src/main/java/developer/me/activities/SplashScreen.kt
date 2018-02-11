package developer.me.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import developer.me.R

class SplashScreen : AppCompatActivity() {

    companion object {
        var flag = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_splash_screen)

        if (flag){
            Handler().postDelayed({
                flag = false
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 3000)
        }else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
