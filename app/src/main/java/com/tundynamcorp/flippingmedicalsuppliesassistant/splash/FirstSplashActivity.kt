package com.tundynamcorp.flippingmedicalsuppliesassistant.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.tundynamcorp.flippingmedicalsuppliesassistant.R

class FirstSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_splash)

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
                // explicitly hide the view so it never reappears
                splashImage.visibility = View.GONE

                // then launch next screen
                startActivity(Intent(this@FirstSplashActivity, SecondSplashActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            override fun onAnimationStart(a: Animation?){ }
            override fun onAnimationRepeat(a: Animation?){ }
        })

    }
}
