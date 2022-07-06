package com.smorodov.showcaseview.sample.legacy

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.smorodov.showcaseview.ShowcaseView
import com.smorodov.showcaseview.ShowcaseView.ConfigOptions
import com.smorodov.showcaseview.ShowcaseViews
import com.smorodov.showcaseview.ShowcaseViews.ItemViewProperties
import com.smorodov.showcaseview.sample.R
import com.smorodov.showcaseview.sample.R.id

class MultipleShowcaseSampleActivity : Activity() {
    var mOptions = ConfigOptions()
    var mViews: ShowcaseViews? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_legacy)
        findViewById<View>(id.buttonLike).setOnClickListener {
            Toast.makeText(
                applicationContext, R.string.like_message, Toast.LENGTH_SHORT
            ).show()
        }
        mOptions.block = false
        mOptions.hideOnClickOutside = false

        val ackListener = ShowcaseView.ShowcaseAcknowledgeListener {
            Toast.makeText(
                this@MultipleShowcaseSampleActivity,
                R.string.dismissed_message,
                Toast.LENGTH_SHORT
            ).show()
        }

        mViews = ShowcaseViews(
            this,
            R.layout.showcase_view_template, ackListener
        )
        mViews!!.addView(
            ItemViewProperties(
                id.image,
                R.string.showcase_image_title,
                R.string.showcase_image_message,
                SHOWCASE_KITTEN_SCALE
            )
        )
        mViews!!.addView(
            ItemViewProperties(
                id.buttonLike,
                R.string.showcase_like_title,
                R.string.showcase_like_message,
                SHOWCASE_LIKE_SCALE
            )
        )
        mViews!!.show()
    }

    companion object {
        private const val SHOWCASE_KITTEN_SCALE = 1.2f
        private const val SHOWCASE_LIKE_SCALE = 0.5f
    }
}