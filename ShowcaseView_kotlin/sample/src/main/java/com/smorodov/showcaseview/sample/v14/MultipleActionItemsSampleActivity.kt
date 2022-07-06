package com.smorodov.showcaseview.sample.v14

import android.app.ActionBar
import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import com.smorodov.showcaseview.ShowcaseView
import com.smorodov.showcaseview.ShowcaseView.ConfigOptions
import com.smorodov.showcaseview.ShowcaseViews
import com.smorodov.showcaseview.ShowcaseViews.ItemViewProperties
import com.smorodov.showcaseview.sample.R

class MultipleActionItemsSampleActivity : Activity(), ActionBar.OnNavigationListener {
    var mOptions = ConfigOptions()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_LIST
        actionBar!!.setListNavigationCallbacks(
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Item1", "Item2", "Item3")
            ), this
        )
        mOptions.block = false
    }

    override fun onResume() {
        super.onResume()

        val ackListener = ShowcaseView.ShowcaseAcknowledgeListener {
            Toast.makeText(
                applicationContext,
                R.string.dismissed_message,
                Toast.LENGTH_SHORT
            ).show()
        }
        val views = ShowcaseViews(this, R.layout.showcase_view_template, ackListener)
        views.addView(
            ItemViewProperties(
                ItemViewProperties.ID_SPINNER,
                R.string.showcase_spinner_title,
                R.string.showcase_spinner_message,
                ShowcaseView.ITEM_SPINNER
            )
        )
        views.addView(
            ItemViewProperties(
                ItemViewProperties.ID_TITLE,
                R.string.showcase_simple_title,
                R.string.showcase_simple_message,
                ShowcaseView.ITEM_TITLE
            )
        )
        views.addView(
            ItemViewProperties(
                ItemViewProperties.ID_OVERFLOW,
                R.string.showcase_overflow_title,
                R.string.showcase_overflow_message,
                ShowcaseView.ITEM_ACTION_OVERFLOW
            )
        )
        views.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }

    override fun onNavigationItemSelected(i: Int, l: Long): Boolean {
        return false
    }

}