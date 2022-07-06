package com.smorodov.showcaseview

import android.app.Activity
import android.view.View
import android.view.ViewGroup

class ShowcaseViews(private val activity: Activity, showcaseTemplateLayout: Int) {
    private val views: MutableList<ShowcaseView?> = ArrayList()
    private lateinit var showcaseAcknowledgedListener: ShowcaseView.ShowcaseAcknowledgeListener


    constructor(
        activity: Activity,
        showcaseTemplateLayout: Int,
        acknowledgedListener: ShowcaseView.ShowcaseAcknowledgeListener
    ) : this(activity, showcaseTemplateLayout) {
        showcaseAcknowledgedListener = acknowledgedListener
    }

    fun addView(properties: ItemViewProperties) {
        val builder = ShowcaseViewBuilder(activity)
            .setText(properties.titleResId, properties.messageResId)
        if (showcaseActionBar(properties)) {
            builder.setShowcaseItem(properties.itemType, properties.id, activity)
        } else if (properties.id == ItemViewProperties.ID_NO_SHOWCASE) {
            builder.setShowcaseNoView()
        } else {
            builder.setShowcaseView(activity.findViewById(properties.id))
        }
        val showcaseView = builder.build()
        showcaseView.overrideButtonClick(createShowcaseViewDismissListener(showcaseView))
        views.add(showcaseView)
    }

    private fun showcaseActionBar(properties: ItemViewProperties): Boolean {
        return properties.itemType > ItemViewProperties.ID_NOT_IN_ACTIONBAR
    }

    private fun createShowcaseViewDismissListener(showcaseView: ShowcaseView?): View.OnClickListener {
        return View.OnClickListener {
            showcaseView!!.hide()
            if (views.isEmpty()) {
                showcaseAcknowledgedListener.onShowCaseAcknowledged(showcaseView)
            } else {
                show()
            }
        }
    }

    fun show() {
        if (views.isEmpty()) {
            return
        }
        val view = views[0]
        (activity.window.decorView as ViewGroup).addView(view)
        views.removeAt(0)
    }

    fun hasViews(): Boolean {
        return !views.isEmpty()
    }

    interface OnShowcaseAcknowledged {
        fun onShowCaseAcknowledged(showcaseView: ShowcaseView?)
    }

    class ItemViewProperties @JvmOverloads constructor(
        val id: Int,
        val titleResId: Int,
        val messageResId: Int,
        val itemType: Int = ID_NOT_IN_ACTIONBAR,
        protected val scale: Float = DEFAULT_SCALE
    ) {
        constructor(titleResId: Int, messageResId: Int) : this(
            ID_NO_SHOWCASE,
            titleResId,
            messageResId,
            ID_NOT_IN_ACTIONBAR,
            DEFAULT_SCALE
        )

        constructor(id: Int, titleResId: Int, messageResId: Int, scale: Float) : this(
            id,
            titleResId,
            messageResId,
            ID_NOT_IN_ACTIONBAR,
            scale
        )

        companion object {
            const val ID_NO_SHOWCASE = -2202
            const val ID_NOT_IN_ACTIONBAR = -1
            const val ID_SPINNER = 0
            const val ID_TITLE = 1
            const val ID_OVERFLOW = 2
            private const val DEFAULT_SCALE = 1f
        }
    }
}