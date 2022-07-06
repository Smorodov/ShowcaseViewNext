package com.smorodov.showcaseview

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.NinePatchDrawable
import androidx.window.layout.WindowMetricsCalculator

import android.os.Build
import android.text.*
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.RelativeLayout
import com.smorodov.showcaseview.anim.AnimationUtils
import com.smorodov.showcaseview.anim.AnimationUtils.AnimationEndListener

/**
 * A view which allows you to showcase areas of your app with an explanation.
 */
class ShowcaseView protected constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
    RelativeLayout(context, attrs, defStyle), View.OnClickListener, OnTouchListener {
    private val metricScale: Float
    private val backColor: Int
    private val sv_titleTextBgColor: Int
    private val sv_detailTextBgColor: Int
    private val mEndButton: Button?
    private val mDetailSpan: TextAppearanceSpan
    private val mTitleSpan: TextAppearanceSpan
    private val buttonText: String?
    private val mShowcaseColor: Int
    var mDisplayWidth = 0
    var mDisplayHeight = 0

    // 0 - circle
    // 1 - rectangle
    var targetHighligtShape = 1
    var yMargin = 0
    var va: ValueAnimator? = null
    private var showcaseX = -1f
    private var showcaseY = -1f
    private var showcaseW = -1f
    private var showcaseH = -1f
    private var legacyShowcaseX = -1f
    private var legacyShowcaseY = -1f
    private var isRedundant = false
    private var hasCustomClickListener = false
    private var mOptions: ConfigOptions? = null
    private var mEraser: Paint? = null
    private var mPaintDetail: TextPaint? = null
    private var mPaintTitle: TextPaint? = null
    private var circleShowcase: LayerDrawable? = null
    private var rectangleShowcase: NinePatchDrawable? = null
    private var animated_v = 1f
    private var mHandy: View? = null
    private var mEventListener: OnShowcaseEventListener? = null
    private var voidedArea: Rect? = null
    private var mTitleText: CharSequence? = null
    private var mSubText: CharSequence? = null
    private var mDynamicTitleLayout: DynamicLayout? = null
    private var mDynamicDetailLayout: DynamicLayout? = null
    private var mBestTextPosition = FloatArray(2)
    private var mAlteredText = false
    private var scaleMultiplier = 1f


    // ---------------------------
    //
    // ---------------------------
    constructor(context: Context) : this(
        context,
        null,
        R.styleable.CustomTheme_showcaseViewStyle
    )

    fun interface ShowcaseAcknowledgeListener {
        //fun OnShowcaseAcknowledged(showcaseView: ShowcaseView?)
        fun onShowCaseAcknowledged(showcaseView: ShowcaseView)
    }

    val navigationBarHeight: Int
        get() {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }
    val displaySize: Point
        get() {
            val size = Point()
            val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity!!)
            val currentBounds = windowMetrics.bounds // E.g. [0 0 1350 1800]
            size.x = currentBounds.width()
            size.y = currentBounds.height()
            return size
        }

    fun hasSoftNavigationBar(): Boolean {
        val context = activity?.baseContext
        val resources = context?.resources
        val id = resources?.getIdentifier("config_showNavigationBar", "bool", "android")
        return Build.FINGERPRINT.startsWith("generic") || id!! > 0 && resources.getBoolean(id)
    }

    private val activity: Activity?
        private get() {
            var context = context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }
    val statusBarHeight: Int
        get() {
            val r = Rect()
            val w = activity?.window
            w!!.decorView.getWindowVisibleDisplayFrame(r)
            return r.top
        }
    val titleBarHeight: Int
        get() {
            val viewTop =activity?.window?.findViewById<View>(Window.ID_ANDROID_CONTENT)?.top!!
            return viewTop - statusBarHeight
        }

    private fun init() {
        va = ValueAnimator.ofFloat(1.0f, 1.1f)
        va!!.duration = 500
        va!!.addUpdateListener { valueAnimator: ValueAnimator? ->
            animated_v = va!!.animatedValue as Float
            invalidate()
        }
        va!!.repeatCount = ValueAnimator.INFINITE
        va!!.repeatMode = ValueAnimator.REVERSE
        va!!.start()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val hasShot = context.getSharedPreferences(PREFS_SHOWCASE_INTERNAL, Context.MODE_PRIVATE)
            .getBoolean("hasShot" + configOptions.showcaseId, false)
        if (hasShot && mOptions!!.shotType == TYPE_ONE_SHOT) {
            // The showcase has already been shot once, so we don't need to do anything
            visibility = GONE
            isRedundant = true
            return
        }
        // load drawables
        if (targetHighligtShape == 0) {
            circleShowcase = context.getDrawable(R.drawable.drawing) as LayerDrawable?
        }
        if (targetHighligtShape == 1) {
            rectangleShowcase = context.getDrawable(R.drawable.shadow_rect) as NinePatchDrawable?
        }

        // Парамеры экрана
        val mDisplayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
        mDisplayWidth = mDisplayMetrics.widthPixels
        mDisplayHeight = mDisplayMetrics.heightPixels
        if (targetHighligtShape == 0) {
            assert(circleShowcase != null)
            circleShowcase!!.setColorFilter(
                mShowcaseColor,
                PorterDuff.Mode.MULTIPLY
            )
        }
        if (targetHighligtShape == 1) {
            assert(rectangleShowcase != null)
            rectangleShowcase!!.setColorFilter(
                mShowcaseColor,
                PorterDuff.Mode.MULTIPLY
            )
        }
        val mBlender = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        setOnTouchListener(this)
        mPaintTitle = TextPaint()
        mPaintTitle!!.isAntiAlias = true
        mPaintDetail = TextPaint()
        mPaintDetail!!.isAntiAlias = true
        mEraser = Paint()
        mEraser!!.color = 0xFFFFFF
        mEraser!!.alpha = 0
        mEraser!!.xfermode = mBlender
        mEraser!!.isAntiAlias = true
        if (!mOptions!!.noButton && mEndButton!!.parent == null) {
            val buttonInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val buttonView = buttonInflater.inflate(R.layout.showcase_button, null, false)
            buttonView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val buttonWidth = buttonView.measuredWidth
            val buttonHeight = buttonView.measuredHeight
            val statusBarHeight = statusBarHeight
            val navigationBarHeight = navigationBarHeight
            val titleBarHeight = titleBarHeight
            var lps = configOptions.buttonLayoutParams
            if (lps == null) {
                lps = generateDefaultLayoutParams() as LayoutParams
                lps.addRule(ALIGN_PARENT_BOTTOM)
                lps.addRule(ALIGN_PARENT_RIGHT)
                val margin = statusBarHeight
                lps.setMargins(0, margin, 0, navigationBarHeight)
                mEndButton.layoutParams = lps
                mEndButton.text = buttonText ?: resources.getString(R.string.ok)
                mEndButton.bringToFront()
                bringToFront()
            }
            if (!hasCustomClickListener) mEndButton.setOnClickListener(this)
            addView(mEndButton)
        }
    }

    fun setShowcaseNoView() {
        setShowcasePosition(1000000f, 1000000f)
    }

    /**
     * Set the view to showcase
     *
     * @param view The [View] to showcase.
     */
    fun setShowcaseView(view: View?) {
        if (isRedundant || view == null) {
            isRedundant = true
            return
        }
        view.post(Runnable {
            init()
            if (mOptions!!.insert == INSERT_TO_VIEW) {
                showcaseX = (view.left + view.width / 2).toFloat()
                showcaseY = (view.top + view.height / 2).toFloat()
            } else {
                val coordinates = IntArray(2)
                view.getLocationInWindow(coordinates)
                showcaseX = (coordinates[0] + view.width / 2).toFloat()
                showcaseY = (coordinates[1] + view.height / 2).toFloat()
            }
            showcaseW = view.width.toFloat()
            showcaseH = view.height.toFloat()
            invalidate()
        })
    }

    fun setShowcaseRehion(X: Float, Y: Float, W: Float, H: Float) {
        showcaseX = X
        showcaseY = Y
        showcaseW = W
        showcaseH = H
        init()
        invalidate()
    }

    fun setShowcaseSize(W: Float, H: Float) {
        showcaseW = W
        showcaseH = H
        init()
        invalidate()
    }

    /**
     * Set a specific position to showcase
     *
     * @param x X co-ordinate
     * @param y Y co-ordinate
     */
    fun setShowcasePosition(x: Float, y: Float) {
        if (isRedundant) {
            return
        }
        showcaseX = x
        showcaseY = y
        init()
        invalidate()
    }

    private fun iterateViews(v: View, type: String): View? {
        var view: View? = null
        if (v is ViewGroup) {
            for (index in 0 until v.childCount) {
                val nextChild = v.getChildAt(index)
                val name = nextChild.javaClass.name
                Log.i("smorodov", name)
                if (name.contains(type)) {
                    return nextChild
                }
                if (nextChild is ViewGroup) {
                    Log.i("smorodov", "enter $name")
                    view = iterateViews(nextChild, type)
                    Log.i("smorodov", "exit $name")
                    if (view != null) {
                        break
                    }
                }
            }
        }
        return view
    }

    private fun iterateViews(v: View, id: Int): View? {
        if (v is ViewGroup) {
            for (index in 0 until v.childCount) {
                val nextChild = v.getChildAt(index)
                val name = nextChild.javaClass.name
                Log.i("smorodov", name)
                if (nextChild.id == id) {
                    return nextChild
                }
                if (nextChild is ViewGroup) {
                    Log.i("smorodov", "enter $name")
                    val view = iterateViews(nextChild, id)
                    Log.i("smorodov", "exit $name")
                    if (view != null) {
                        return view
                    }
                }
            }
        }
        return null
    }

    fun setShowcaseItem(itemType: Int, actionItemId: Int, activity: Activity) {
        post {
            var homeButton = activity.findViewById<View>(android.R.id.home)
            if (homeButton == null) {
                // Thanks to @hameno for this
                val homeId =
                    activity.resources.getIdentifier("abs__home", "id", activity.packageName)
                if (homeId != 0) {
                    homeButton = activity.findViewById(homeId)
                }
            }
            if (homeButton == null) throw RuntimeException(
                "insertShowcaseViewWithType cannot be used when the theme " +
                        "has no ActionBar"
            )
            var p = homeButton.parent.parent //ActionBarView
            if (!p.javaClass.name.contains("ActionBarView")) {
                val previousP = p.javaClass.name
                p = p.parent
                val name = p.javaClass.name
                val throwP = p.javaClass.name
                check(p.javaClass.name.contains("ActionBarView")) {
                    "Cannot find ActionBarView for " +
                            "Activity, instead found " + previousP + " and " + throwP
                }
            }
            Log.i("smorodov", p.javaClass.name + " Contains:")
            when (itemType) {
                ITEM_ACTION_HOME -> setShowcaseView(homeButton)
                ITEM_SPINNER -> showcaseSpinner(p)
                ITEM_TITLE -> setShowcaseView(iterateViews(p as View, "TextView"))
                ITEM_ACTION_ITEM, ITEM_ACTION_OVERFLOW -> showcaseActionItem(
                    p,
                    itemType,
                    actionItemId
                )
                else -> Log.e("TAG", "Unknown item type")
            }
        }
    }

    private fun showcaseActionItem(p: ViewParent, itemType: Int, actionItemId: Int) {
        if (itemType == ITEM_ACTION_OVERFLOW) {
            val mOb = iterateViews(p as View, "ActionMenuView")
            mOb?.let { setShowcaseView(it) }
        } else {
            // Want an ActionItem, so find it
            val i = iterateViews(p as View, actionItemId)
            i?.let { setShowcaseView(it) }
        }
    }

    private fun showcaseSpinner(p: ViewParent) {
        val mSpinnerView = iterateViews(p as View, "Spinner")
        mSpinnerView?.let { setShowcaseView(it) }
    }

    private fun showcaseTitle(p: ViewParent, abv: Class<*>) {
        try {
            val mTitleViewField = abv.getDeclaredField("mTitleView")
            mTitleViewField.isAccessible = true
            val titleView = mTitleViewField[p] as View
            titleView.let { setShowcaseView(it) }
        } catch (e: NoSuchFieldException) {
            Log.e("TAG", "Failed to find actionbar title", e)
        } catch (e: IllegalAccessException) {
            Log.e("TAG", "Failed to access actionbar title", e)
        }
    }

    /**
     * Set the shot method of the showcase - only once or no limit
     *
     * @param shotType either TYPE_ONE_SHOT or TYPE_NO_LIMIT
     */
    @Deprecated("Use the option in {@link ConfigOptions} instead.")
    fun setShotType(shotType: Int) {
        if (shotType == TYPE_NO_LIMIT || shotType == TYPE_ONE_SHOT) {
            mOptions!!.shotType = shotType
        }
    }

    /**
     * Decide whether touches outside the showcased circle should be ignored or not
     *
     * @param block true to block touches, false otherwise. By default, this is true.
     */
    @Deprecated("Use the option in {@link ConfigOptions} instead.")
    fun blockNonShowcasedTouches(block: Boolean) {
        mOptions!!.block = block
    }

    /**
     * Override the standard button click event
     *
     * @param listener Listener to listen to on click events
     */
    fun overrideButtonClick(listener: OnClickListener?) {
        if (isRedundant) {
            return
        }
        mEndButton?.setOnClickListener(listener ?: this)
        hasCustomClickListener = true
    }

    fun setOnShowcaseEventListener(listener: OnShowcaseEventListener?) {
        mEventListener = listener
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (showcaseX < 0 || showcaseY < 0 || isRedundant) {
            super.dispatchDraw(canvas)
            return
        }

        //Draw the semi-transparent background
        canvas.drawColor(backColor)
        val recalculateText = makeVoidedRect() || mAlteredText
        mAlteredText = false
        if (showcaseW > mDisplayWidth / 2.0f || showcaseH > mDisplayHeight / 2.0f) {
            showcaseW = 0f
            showcaseH = 0f
        }
        if (showcaseW > 0 && showcaseH > 0) {
            // circle (cling drawable) hshape
            if (targetHighligtShape == 0) {
                val mm = Matrix()
                val R = (animated_v * Math.sqrt(
                    Math.pow(
                        (showcaseW / 2.0f).toDouble(),
                        2.0
                    ) + Math.pow(
                        (showcaseH / 2.0f).toDouble(),
                        2.0
                    )
                )).toInt()
                val l = (showcaseX - R).toInt()
                val r = (showcaseX + R).toInt()
                val t = (showcaseY - R).toInt()
                val b = (showcaseY + R).toInt()
                //Erase the area for the ring
                canvas.drawCircle(showcaseX, showcaseY, R.toFloat(), mEraser!!)
                // не зависит от параметров, просто должно быть
                circleShowcase!!.setBounds(-1, -1, 1, 1)
                // радиус окна и внешний радиус оверлея (drawable/drawing.xml)
                val holeRadius = 50.0f
                val externalRadius = 150.0f
                scaleMultiplier = R / (holeRadius * (externalRadius / holeRadius))
                mm.postTranslate(showcaseX, showcaseY)
                mm.postScale(scaleMultiplier, scaleMultiplier, showcaseX, showcaseY)
                canvas.setMatrix(mm)

                //circleShowcase.setBounds(voidedArea);
                circleShowcase!!.draw(canvas)
                canvas.setMatrix(Matrix())
            }

            // if rectangle
            if (targetHighligtShape == 1) {
                val dx = Math.abs(1.0f - animated_v) * showcaseW
                val dy = Math.abs(1.0f - animated_v) * showcaseH
                val mx = 130
                val my = 120
                val l = (showcaseX - dx - showcaseW / 2.0f).toInt()
                val r = (showcaseX + dx + showcaseW / 2.0f).toInt()
                val t = (showcaseY - dy - showcaseH / 2.0f).toInt()
                val b = (showcaseY + dy + showcaseH / 2.0f).toInt()
                //Erase the area for the ring
                canvas.drawRect(Rect(l, t, r, b), mEraser!!)
                rectangleShowcase!!.setBounds(l - mx, t - my, r + mx, b + my)
                rectangleShowcase!!.draw(canvas)
            }
        }
        val margin = 40
        if (!TextUtils.isEmpty(mTitleText) || !TextUtils.isEmpty(mSubText)) {
            if (recalculateText) mBestTextPosition =
                getBestTextPosition(canvas.width, canvas.height)
        }
        if (!TextUtils.isEmpty(mSubText)) {
            if (recalculateText) {
                val db = DynamicLayout.Builder.obtain(
                    mSubText!!, mPaintDetail!!, mBestTextPosition[2].toInt()
                )
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.2f, 1.0f)
                    .setIncludePad(true)
                mDynamicDetailLayout = db.build()
            }
        }
        if (!TextUtils.isEmpty(mTitleText)) {
            if (recalculateText) {
                val db = DynamicLayout.Builder.obtain(
                    mTitleText!!, mPaintTitle!!, mBestTextPosition[2].toInt()
                )
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(1.0f, 1.0f)
                    .setIncludePad(true)
                mDynamicTitleLayout = db.build()
            }
        }
        if (!TextUtils.isEmpty(mTitleText) || !TextUtils.isEmpty(mSubText)) {
            if (!TextUtils.isEmpty(mSubText)) {
                canvas.save()
                canvas.translate(
                    mBestTextPosition[0],
                    3 * margin + mBestTextPosition[1] + mDynamicTitleLayout!!.height
                )
                mPaintDetail!!.color = sv_detailTextBgColor
                mPaintDetail!!.style = Paint.Style.FILL
                //canvas.drawRoundRect(new RectF(0-margin,0-margin-mDynamicTitleLayout.getHeight()-2*margin,mDynamicDetailLayout.getWidth()+margin,mDynamicDetailLayout.getHeight()+margin),3*margin,3*margin,mPaintDetail);
                val corners = floatArrayOf(
                    0f,
                    0f,
                    0f,
                    0f,
                    ( // Top right radius in px
                            3 * margin).toFloat(),
                    (3 * margin).toFloat(),
                    ( // Bottom right radius in px
                            3 * margin).toFloat(),
                    (3 * margin // Bottom left radius in px
                            ).toFloat()
                )
                val rect = RectF(
                    (0 - margin).toFloat(),
                    (0 - margin).toFloat(),
                    (mDynamicDetailLayout!!.width + margin).toFloat(),
                    (mDynamicDetailLayout!!.height + margin).toFloat()
                )
                val path = Path()
                path.addRoundRect(rect, corners, Path.Direction.CW)
                canvas.drawPath(path, mPaintDetail!!)
                mDynamicDetailLayout!!.draw(canvas)
                canvas.restore()
            }
            if (!TextUtils.isEmpty(mTitleText)) {
                canvas.save()
                canvas.translate(mBestTextPosition[0], margin + mBestTextPosition[1] - yMargin)
                mPaintTitle!!.color = sv_titleTextBgColor
                mPaintTitle!!.style = Paint.Style.FILL
                //canvas.drawRoundRect(new RectF(0-margin,0-margin,mDynamicTitleLayout.getWidth()+margin,mDynamicTitleLayout.getHeight()+margin),3*margin,3*margin,mPaintTitle);
                val corners = floatArrayOf(
                    (
                            3 * margin).toFloat(),
                    (3 * margin).toFloat(),
                    ( // Top left radius in px
                            3 * margin).toFloat(),
                    (3 * margin).toFloat(),
                    0f,
                    0f,
                    0f,
                    0f
                )
                val rect = RectF(
                    (0 - margin).toFloat(),
                    (0 - margin).toFloat(),
                    (mDynamicTitleLayout!!.width + margin).toFloat(),
                    (mDynamicTitleLayout!!.height + margin).toFloat()
                )
                val path = Path()
                path.addRoundRect(rect, corners, Path.Direction.CW)
                canvas.drawPath(path, mPaintTitle!!)
                mDynamicTitleLayout!!.draw(canvas)
                canvas.restore()
            }
        }
        super.dispatchDraw(canvas)
    }

    /**
     * Calculates the best place to position text
     *
     * @param canvasW width of the screen
     * @param canvasH height of the screen
     * @return best text position
     */
    private fun getBestTextPosition(canvasW: Int, canvasH: Int): FloatArray {

        //if the width isn't much bigger than the voided area, just consider top & bottom
        val spaceTop = voidedArea!!.top.toFloat()
        val spaceBottom =
            canvasH - voidedArea!!.bottom - 64 * metricScale //64dip considers the OK button
        //float spaceLeft = voidedArea.left;
        //float spaceRight = canvasW - voidedArea.right;

        //TODO: currently only considers above or below showcase, deal with left or right
        return floatArrayOf(
            24 * metricScale,
            if (spaceTop > spaceBottom) 128 * metricScale else 24 * metricScale + voidedArea!!.bottom,
            canvasW - 48 * metricScale
        )
    }

    /**
     * Creates a [Rect] which represents the area the showcase covers. Used to calculate
     * where best to place the text
     *
     * @return true if voidedArea has changed, false otherwise.
     */
    private fun makeVoidedRect(): Boolean {

        // This if statement saves resources by not recalculating voidedArea
        // if the X & Y coordinates haven't changed
        if (voidedArea == null || showcaseX != legacyShowcaseX || showcaseY != legacyShowcaseY) {
            val cx = showcaseX.toInt()
            val cy = showcaseY.toInt()
            var dw = 1
            var dh = 1
            if (targetHighligtShape == 0) {
                dw = (Math.max(
                    showcaseW,
                    showcaseH
                ) * 3).toInt() //circleShowcase.getIntrinsicWidth();
                dh = (Math.max(
                    showcaseW,
                    showcaseH
                ) * 3).toInt() //circleShowcase.getIntrinsicHeight();
            }
            if (targetHighligtShape == 1) {
                dw = rectangleShowcase!!.intrinsicWidth
                dh = rectangleShowcase!!.intrinsicHeight
            }
            voidedArea = Rect(cx - dw / 2, cy - dh / 2, cx + dw / 2, cy + dh / 2)
            legacyShowcaseX = showcaseX
            legacyShowcaseY = showcaseY
            return true
        }
        return false
    }

    fun animateGesture(
        offsetStartX: Float,
        offsetStartY: Float,
        offsetEndX: Float,
        offsetEndY: Float
    ) {
        hand!!.bringToFront()
        moveHand(offsetStartX, offsetStartY, offsetEndX, offsetEndY)
    }

    override fun onClick(view: View) {
        // If the type is set to one-shot, store that it has shot
        if (mOptions!!.shotType == TYPE_ONE_SHOT) {
            val internal =
                context.getSharedPreferences(PREFS_SHOWCASE_INTERNAL, Context.MODE_PRIVATE)
            internal.edit().putBoolean("hasShot" + configOptions.showcaseId, true).apply()
        }
        hide()
    }

    fun hide() {
        if (mEventListener != null) {
            mEventListener!!.onShowcaseViewHide(this)
        }
        if (configOptions.fadeOutDuration > 0) {
            fadeOutShowcase()
        } else {
            visibility = GONE
        }
    }

    private fun fadeOutShowcase() {
        AnimationUtils.createFadeOutAnimation(
            this,
            configOptions.fadeOutDuration
        ) {
            visibility = GONE
        }
            .start()
    }

    fun show() {
        if (mEventListener != null) {
            mEventListener!!.onShowcaseViewShow(this)
        }
        if (configOptions.fadeInDuration > 0) {
            fadeInShowcase()
        } else {
            visibility = VISIBLE
        }
    }

    private fun fadeInShowcase() {
        AnimationUtils.createFadeInAnimation(this, configOptions.fadeInDuration) {
            visibility = VISIBLE
        }
            .start()
    }

    // Скрываем по касанию вне цели
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val bounds = RectF(
            showcaseX - showcaseW / 2.0f,
            showcaseY - showcaseH / 2.0f,
            showcaseX + showcaseW / 2.0f,
            showcaseY + showcaseH / 2.0f
        )
        if (mOptions!!.hideOnClickOutside && !bounds.contains(motionEvent.rawX, motionEvent.rawY)) {
            hide()
            return true
        }
        return mOptions!!.block && !bounds.contains(motionEvent.rawX, motionEvent.rawY)
    }

    fun setShowcaseIndicatorScale(scaleMultiplier: Float) {
        this.scaleMultiplier = scaleMultiplier
    }

    fun setText(titleTextResId: Int, subTextResId: Int) {
        val titleText = context.resources.getString(titleTextResId)
        val subText = context.resources.getString(subTextResId)
        setText(titleText, subText)
    }

    fun setText(titleText: String?, subText: String?) {
        val ssbTitle = SpannableString(titleText)
        ssbTitle.setSpan(mTitleSpan, 0, ssbTitle.length, 0)
        mTitleText = ssbTitle
        val ssbDetail = SpannableString(subText)
        ssbDetail.setSpan(mDetailSpan, 0, ssbDetail.length, 0)
        mSubText = ssbDetail
        mAlteredText = true
        invalidate()
    }

    /**
     * Get the ghostly gesture hand for custom gestures
     *
     * @return a View representing the ghostly hand
     */
    val hand: View?
        get() {
            if (mHandy == null) {
                mHandy =
                    (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                        R.layout.handy,
                        null
                    )
            }
            AnimationUtils.hide(mHandy)
            return mHandy
        }

    /**
     * Point to a specific view
     *
     * @param view The [View] to Showcase
     */
    fun pointTo(view: View) {
        val x = AnimationUtils.getX(view) + view.width / 2.0f
        val y = AnimationUtils.getY(view) + view.height / 2.0f
        pointTo(x, y)
    }

    /**
     * Point to a specific point on the screen
     *
     * @param x X-coordinate to point to
     * @param y Y-coordinate to point to
     */
    fun pointTo(x: Float, y: Float) {
        mHandy = hand
        val listener = AnimationEndListener { removeView(mHandy) }
        addView(mHandy)
        AnimationUtils.createPointingAnimation(mHandy, x, y, listener).start()
    }

    private fun moveHand(
        offsetStartX: Float,
        offsetStartY: Float,
        offsetEndX: Float,
        offsetEndY: Float
    ) {
        mHandy = hand
        addView(mHandy)
        val listener = AnimationEndListener { removeView(mHandy) }
        AnimationUtils.createMovementAnimation(
            mHandy, showcaseX, showcaseY,
            offsetStartX, offsetStartY,
            offsetEndX, offsetEndY,
            listener
        ).start()
    }

    // Make sure that this method never returns null
    private var configOptions: ConfigOptions
        private get() =// Make sure that this method never returns null
            if (mOptions == null) ConfigOptions().also { mOptions = it } else mOptions!!
        private set(options) {
            mOptions = options
        }

    interface OnShowcaseEventListener {
        fun onShowcaseViewHide(showcaseView: ShowcaseView?)
        fun onShowcaseViewShow(showcaseView: ShowcaseView?)
    }

    class ConfigOptions {
        @JvmField
        var block = true
        var noButton = false
        var insert = INSERT_TO_DECOR

        @JvmField
        var hideOnClickOutside = false

        /**
         * If you want to use more than one Showcase with the [ConfigOptions.shotType] [ShowcaseView.TYPE_ONE_SHOT] in one Activity, set a unique value for every different Showcase you want to use.
         */
        var showcaseId = 0

        /**
         * If you want to use more than one Showcase with [ShowcaseView.TYPE_ONE_SHOT] in one Activity, set a unique [ConfigOptions.showcaseId] value for every different Showcase you want to use.
         */
        var shotType = TYPE_NO_LIMIT

        /**
         * Default duration for fade in animation. Set to 0 to disable.
         */
        var fadeInDuration = AnimationUtils.DEFAULT_DURATION

        /**
         * Default duration for fade out animation. Set to 0 to disable.
         */
        var fadeOutDuration = AnimationUtils.DEFAULT_DURATION

        /**
         * Allow custom positioning of the button within the showcase view.
         */
        var buttonLayoutParams: LayoutParams? = null
    }

    companion object {
        const val TYPE_NO_LIMIT = 0
        const val TYPE_ONE_SHOT = 1
        const val INSERT_TO_DECOR = 0
        const val INSERT_TO_VIEW = 1
        const val ITEM_ACTION_HOME = 0
        const val ITEM_TITLE = 1
        const val ITEM_SPINNER = 2
        const val ITEM_ACTION_ITEM = 3
        const val ITEM_ACTION_OVERFLOW = 6
        private const val PREFS_SHOWCASE_INTERNAL = "showcase_internal"

        /**
         * Quick method to insert a ShowcaseView into an Activity
         *
         * @param viewToShowcase View to showcase
         * @param activity       Activity to insert into
         * @param title          Text to show as a title. Can be null.
         * @param detailText     More detailed text. Can be null.
         * @param options        A set of options to customise the ShowcaseView
         * @return the created ShowcaseView instance
         */
        fun insertShowcaseView(
            viewToShowcase: View?, activity: Activity, title: String?,
            detailText: String?, options: ConfigOptions?
        ): ShowcaseView {
            val sv = ShowcaseView(activity)
            if (options != null) sv.configOptions = options
            if (sv.configOptions.insert == INSERT_TO_DECOR) {
                (activity.window.decorView as ViewGroup).addView(sv)
            } else {
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).addView(sv)
            }
            sv.setShowcaseView(viewToShowcase)
            sv.setText(title, detailText)
            return sv
        }

        /**
         * Quick method to insert a ShowcaseView into an Activity
         *
         * @param viewToShowcase View to showcase
         * @param activity       Activity to insert into
         * @param title          Text to show as a title. Can be null.
         * @param detailText     More detailed text. Can be null.
         * @param options        A set of options to customise the ShowcaseView
         * @return the created ShowcaseView instance
         */
        fun insertShowcaseView(
            viewToShowcase: View?, activity: Activity, title: Int,
            detailText: Int, options: ConfigOptions?
        ): ShowcaseView {
            val sv = ShowcaseView(activity)
            if (options != null) sv.configOptions = options
            if (sv.configOptions.insert == INSERT_TO_DECOR) {
                (activity.window.decorView as ViewGroup).addView(sv)
            } else {
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).addView(sv)
            }
            sv.setShowcaseView(viewToShowcase)
            sv.setText(title, detailText)
            return sv
        }

        fun insertShowcaseView(
            showcaseViewId: Int, activity: Activity, title: String?,
            detailText: String?, options: ConfigOptions?
        ): ShowcaseView? {
            val v = activity.findViewById<View>(showcaseViewId)
            return if (v != null) {
                insertShowcaseView(v, activity, title, detailText, options)
            } else null
        }

        fun insertShowcaseView(
            showcaseViewId: Int, activity: Activity, title: Int,
            detailText: Int, options: ConfigOptions?
        ): ShowcaseView? {
            val v = activity.findViewById<View>(showcaseViewId)
            return if (v != null) {
                insertShowcaseView(v, activity, title, detailText, options)
            } else null
        }

        @JvmOverloads
        fun insertShowcaseView(
            x: Float, y: Float, activity: Activity, title: String? = null,
            detailText: String? = null, options: ConfigOptions? = null
        ): ShowcaseView {
            val sv = ShowcaseView(activity)
            if (options != null) sv.configOptions = options
            if (sv.configOptions.insert == INSERT_TO_DECOR) {
                (activity.window.decorView as ViewGroup).addView(sv)
            } else {
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).addView(sv)
            }
            sv.setShowcasePosition(x, y)
            sv.setText(title, detailText)
            return sv
        }

        fun insertShowcaseView(
            x: Float, y: Float, activity: Activity, title: Int,
            detailText: Int, options: ConfigOptions?
        ): ShowcaseView {
            val sv = ShowcaseView(activity)
            if (options != null) sv.configOptions = options
            if (sv.configOptions.insert == INSERT_TO_DECOR) {
                (activity.window.decorView as ViewGroup).addView(sv)
            } else {
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).addView(sv)
            }
            sv.setShowcasePosition(x, y)
            sv.setText(title, detailText)
            return sv
        }

        fun insertShowcaseView(showcase: View?, activity: Activity): ShowcaseView {
            return insertShowcaseView(showcase, activity, null, null, null)
        }

        /**
         * Quickly insert a ShowcaseView into an Activity, highlighting an item.
         *
         * @param type       the type of item to showcase (can be ITEM_ACTION_HOME, ITEM_TITLE_OR_SPINNER, ITEM_ACTION_ITEM or ITEM_ACTION_OVERFLOW)
         * @param itemId     the ID of an Action item to showcase (only required for ITEM_ACTION_ITEM
         * @param activity   Activity to insert the ShowcaseView into
         * @param title      Text to show as a title. Can be null.
         * @param detailText More detailed text. Can be null.
         * @param options    A set of options to customise the ShowcaseView
         * @return the created ShowcaseView instance
         */
        fun insertShowcaseViewWithType(
            type: Int,
            itemId: Int,
            activity: Activity,
            title: String?,
            detailText: String?,
            options: ConfigOptions?
        ): ShowcaseView {
            val sv = ShowcaseView(activity)
            if (options != null) sv.configOptions = options
            if (sv.configOptions.insert == INSERT_TO_DECOR) {
                (activity.window.decorView as ViewGroup).addView(sv)
            } else {
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).addView(sv)
            }
            sv.setShowcaseItem(type, itemId, activity)
            sv.setText(title, detailText)
            return sv
        }

        /**
         * Quickly insert a ShowcaseView into an Activity, highlighting an item.
         *
         * @param type       the type of item to showcase (can be ITEM_ACTION_HOME, ITEM_TITLE_OR_SPINNER, ITEM_ACTION_ITEM or ITEM_ACTION_OVERFLOW)
         * @param itemId     the ID of an Action item to showcase (only required for ITEM_ACTION_ITEM
         * @param activity   Activity to insert the ShowcaseView into
         * @param title      Text to show as a title. Can be null.
         * @param detailText More detailed text. Can be null.
         * @param options    A set of options to customise the ShowcaseView
         * @return the created ShowcaseView instance
         */
        @JvmStatic
        fun insertShowcaseViewWithType(
            type: Int,
            itemId: Int,
            activity: Activity,
            title: Int,
            detailText: Int,
            options: ConfigOptions?
        ): ShowcaseView {
            val sv = ShowcaseView(activity)
            if (options != null) sv.configOptions = options
            if (sv.configOptions.insert == INSERT_TO_DECOR) {
                (activity.window.decorView as ViewGroup).addView(sv)
            } else {
                (activity.findViewById<View>(android.R.id.content) as ViewGroup).addView(sv)
            }
            sv.setShowcaseItem(type, itemId, activity)
            sv.setText(title, detailText)
            return sv
        }
    }

    // ---------------------------
    //
    // ---------------------------
    init {

        // Get the attributes for the ShowcaseView
        val styled = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ShowcaseView,
            R.attr.showcaseViewStyle,
            R.style.ShowcaseView
        )
        backColor =
            styled.getInt(R.styleable.ShowcaseView_sv_backgroundColor, Color.argb(128, 80, 80, 80))
        mShowcaseColor =
            styled.getColor(R.styleable.ShowcaseView_sv_showcaseColor, Color.parseColor("#33B5E5"))
        sv_detailTextBgColor = styled.getColor(
            R.styleable.ShowcaseView_sv_titleTextBgColor,
            Color.parseColor("#ff111100")
        )
        sv_titleTextBgColor = styled.getColor(
            R.styleable.ShowcaseView_sv_detailTextBgColor,
            Color.parseColor("#ff1111ff")
        )
        val titleTextAppearance = styled.getResourceId(
            R.styleable.ShowcaseView_sv_titleTextAppearance,
            R.style.TextAppearance_ShowcaseView_Title
        )
        val detailTextAppearance = styled.getResourceId(
            R.styleable.ShowcaseView_sv_detailTextAppearance,
            R.style.TextAppearance_ShowcaseView_Detail
        )
        mTitleSpan = TextAppearanceSpan(context, titleTextAppearance)
        mDetailSpan = TextAppearanceSpan(context, detailTextAppearance)
        buttonText = styled.getString(R.styleable.ShowcaseView_sv_buttonText)
        styled.recycle()
        metricScale = getContext().resources.displayMetrics.density
        mEndButton = LayoutInflater.from(context).inflate(R.layout.showcase_button, null) as Button
        val options = ConfigOptions()
        options.showcaseId = id
        configOptions = options
    }
}