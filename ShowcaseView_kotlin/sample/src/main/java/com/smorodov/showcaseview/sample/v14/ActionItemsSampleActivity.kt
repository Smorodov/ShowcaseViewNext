package com.smorodov.showcaseview.sample.v14

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.smorodov.showcaseview.ShowcaseView
import com.smorodov.showcaseview.ShowcaseView.Companion.insertShowcaseViewWithType
import com.smorodov.showcaseview.ShowcaseView.ConfigOptions
import com.smorodov.showcaseview.sample.R
import com.smorodov.showcaseview.sample.R.id

class ActionItemsSampleActivity : Activity() {
    var sv: ShowcaseView? = null
    var mOptions = ConfigOptions()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        mOptions.block = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        sv = insertShowcaseViewWithType(
            ShowcaseView.ITEM_ACTION_OVERFLOW, id.menu_item1, this,
            R.string.showcase_simple_title, R.string.showcase_simple_message, mOptions
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) sv!!.setShowcaseItem(
            ShowcaseView.ITEM_ACTION_HOME,
            0,
            this
        ) else if (itemId == id.menu_item1) sv!!.setShowcaseItem(
            ShowcaseView.ITEM_ACTION_ITEM,
            id.menu_item1,
            this
        ) else if (itemId == id.menu_item2) sv!!.setShowcaseItem(ShowcaseView.ITEM_TITLE, 0, this)
        return true
    }
}