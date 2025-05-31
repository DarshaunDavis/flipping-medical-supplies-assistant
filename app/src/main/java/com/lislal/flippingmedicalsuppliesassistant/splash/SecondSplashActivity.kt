package com.lislal.flippingmedicalsuppliesassistant.splash

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.lislal.flippingmedicalsuppliesassistant.MainActivity
import com.lislal.flippingmedicalsuppliesassistant.R

@SuppressLint("CustomSplashScreen")
class SecondSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_splash)

        // FirstSplashActivity.kt (and similarly in SecondSplashActivity)
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        val hold   = AlphaAnimation(1f, 1f).apply {
            duration = 3000
            fillAfter = true
        }
        val fadeOut= AlphaAnimation(1f, 0f).apply {
            duration = 1000
            fillAfter = true          // <-- persist alpha=0 after anim ends
        }

        val splashImage = findViewById<ImageView>(R.id.logoImageView)
        splashImage.startAnimation(fadeIn)

        fadeIn.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationEnd(a: Animation?) {
                splashImage.startAnimation(hold)
            }
            override fun onAnimationStart(a: Animation?){ }
            override fun onAnimationRepeat(a: Animation?){ }
        })

        hold.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationEnd(a: Animation?) {
                splashImage.startAnimation(fadeOut)
            }
            override fun onAnimationStart(a: Animation?){ }
            override fun onAnimationRepeat(a: Animation?){ }
        })

        fadeOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationEnd(a: Animation?) {
                val intent = Intent(this@SecondSplashActivity, MainActivity::class.java)
                val opts = ActivityOptions.makeCustomAnimation(
                    this@SecondSplashActivity,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                startActivity(intent, opts.toBundle())
                finish()
            }
            override fun onAnimationStart(a: Animation?){ }
            override fun onAnimationRepeat(a: Animation?){ }
        })

    }
}
