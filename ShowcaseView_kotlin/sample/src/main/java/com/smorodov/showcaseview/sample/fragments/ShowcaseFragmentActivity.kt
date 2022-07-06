package com.smorodov.showcaseview.sample.fragments

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.smorodov.showcaseview.sample.R

class ShowcaseFragmentActivity : FragmentActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_activity_layout)
    }
}