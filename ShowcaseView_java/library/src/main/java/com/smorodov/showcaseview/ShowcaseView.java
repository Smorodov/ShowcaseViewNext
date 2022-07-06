package com.smorodov.showcaseview;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.smorodov.showcaseview.anim.AnimationUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A view which allows you to showcase areas of your app with an explanation.
 */
public class ShowcaseView extends RelativeLayout implements View.OnClickListener, View.OnTouchListener {

    public static final int TYPE_NO_LIMIT = 0;
    public static final int TYPE_ONE_SHOT = 1;
    public static final int INSERT_TO_DECOR = 0;
    public static final int INSERT_TO_VIEW = 1;
    public static final int ITEM_ACTION_HOME = 0;
    public static final int ITEM_TITLE = 1;
    public static final int ITEM_SPINNER = 2;
    public static final int ITEM_ACTION_ITEM = 3;
    public static final int ITEM_ACTION_OVERFLOW = 6;

    private static final String PREFS_SHOWCASE_INTERNAL = "showcase_internal";
    private final float metricScale;
    private final int backColor;
    private final int sv_titleTextBgColor;
    private final int sv_detailTextBgColor;
    private final Button mEndButton;
    private final TextAppearanceSpan mDetailSpan;
    private final TextAppearanceSpan mTitleSpan;
    private final String buttonText;
    private final int mShowcaseColor;

    int mDisplayWidth=0;
    int mDisplayHeight=0;
    // 0 - circle
    // 1 - rectangle
    public int targetHighligtShape = 1;
    int yMargin;
    ValueAnimator va;
    private float showcaseX = -1;
    private float showcaseY = -1;
    private float showcaseW = -1;
    private float showcaseH = -1;
    private float legacyShowcaseX = -1;
    private float legacyShowcaseY = -1;
    private boolean isRedundant = false;
    private boolean hasCustomClickListener = false;
    private ConfigOptions mOptions;
    private Paint mEraser;
    private TextPaint mPaintDetail, mPaintTitle;
    private LayerDrawable circleShowcase;
    private NinePatchDrawable rectangleShowcase;
    private float animated_v = 1;

    private View mHandy;
    private OnShowcaseEventListener mEventListener;
    private Rect voidedArea;
    private CharSequence mTitleText, mSubText;
    private DynamicLayout mDynamicTitleLayout;
    private DynamicLayout mDynamicDetailLayout;
    private float[] mBestTextPosition;
    private boolean mAlteredText = false;
    private float scaleMultiplier = 1f;

    // ---------------------------
    //
    // ---------------------------
    protected ShowcaseView(Context context) {
        this(context, null, R.styleable.CustomTheme_showcaseViewStyle);
    }

    // ---------------------------
    //
    // ---------------------------
    protected ShowcaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get the attributes for the ShowcaseView
        final TypedArray styled = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShowcaseView, R.attr.showcaseViewStyle, R.style.ShowcaseView);
        backColor = styled.getInt(R.styleable.ShowcaseView_sv_backgroundColor, Color.argb(128, 80, 80, 80));
        mShowcaseColor = styled.getColor(R.styleable.ShowcaseView_sv_showcaseColor, Color.parseColor("#33B5E5"));

        sv_detailTextBgColor=styled.getColor(R.styleable.ShowcaseView_sv_titleTextBgColor, Color.parseColor("#ff111100"));
        sv_titleTextBgColor=styled.getColor(R.styleable.ShowcaseView_sv_detailTextBgColor, Color.parseColor("#ff1111ff"));



        int titleTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_titleTextAppearance, R.style.TextAppearance_ShowcaseView_Title);
        int detailTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_detailTextAppearance, R.style.TextAppearance_ShowcaseView_Detail);
        mTitleSpan = new TextAppearanceSpan(context, titleTextAppearance);
        mDetailSpan = new TextAppearanceSpan(context, detailTextAppearance);

        buttonText = styled.getString(R.styleable.ShowcaseView_sv_buttonText);
        styled.recycle();

        metricScale = getContext().getResources().getDisplayMetrics().density;

        mEndButton = (Button) LayoutInflater.from(context).inflate(R.layout.showcase_button, null);

        ConfigOptions options = new ConfigOptions();
        options.showcaseId = getId();
        setConfigOptions(options);
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
    public static ShowcaseView insertShowcaseView(View viewToShowcase, Activity activity, String title,
                                                  String detailText, ConfigOptions options) {
        ShowcaseView sv = new ShowcaseView(activity);
        if (options != null)
            sv.setConfigOptions(options);
        if (sv.getConfigOptions().insert == INSERT_TO_DECOR) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(sv);
        } else {
            ((ViewGroup) activity.findViewById(android.R.id.content)).addView(sv);
        }
        sv.setShowcaseView(viewToShowcase);
        sv.setText(title, detailText);
        return sv;
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
    public static ShowcaseView insertShowcaseView(View viewToShowcase, Activity activity, int title,
                                                  int detailText, ConfigOptions options) {
        ShowcaseView sv = new ShowcaseView(activity);
        if (options != null)
            sv.setConfigOptions(options);
        if (sv.getConfigOptions().insert == INSERT_TO_DECOR) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(sv);
        } else {
            ((ViewGroup) activity.findViewById(android.R.id.content)).addView(sv);
        }
        sv.setShowcaseView(viewToShowcase);
        sv.setText(title, detailText);
        return sv;
    }

    public static ShowcaseView insertShowcaseView(int showcaseViewId, Activity activity, String title,
                                                  String detailText, ConfigOptions options) {
        View v = activity.findViewById(showcaseViewId);
        if (v != null) {
            return insertShowcaseView(v, activity, title, detailText, options);
        }
        return null;
    }

    public static ShowcaseView insertShowcaseView(int showcaseViewId, Activity activity, int title,
                                                  int detailText, ConfigOptions options) {
        View v = activity.findViewById(showcaseViewId);
        if (v != null) {
            return insertShowcaseView(v, activity, title, detailText, options);
        }
        return null;
    }

    public static ShowcaseView insertShowcaseView(float x, float y, Activity activity, String title,
                                                  String detailText, ConfigOptions options) {
        ShowcaseView sv = new ShowcaseView(activity);
        if (options != null)
            sv.setConfigOptions(options);
        if (sv.getConfigOptions().insert == INSERT_TO_DECOR) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(sv);
        } else {
            ((ViewGroup) activity.findViewById(android.R.id.content)).addView(sv);
        }
        sv.setShowcasePosition(x, y);
        sv.setText(title, detailText);
        return sv;
    }

    public static ShowcaseView insertShowcaseView(float x, float y, Activity activity, int title,
                                                  int detailText, ConfigOptions options) {
        ShowcaseView sv = new ShowcaseView(activity);
        if (options != null)
            sv.setConfigOptions(options);
        if (sv.getConfigOptions().insert == INSERT_TO_DECOR) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(sv);
        } else {
            ((ViewGroup) activity.findViewById(android.R.id.content)).addView(sv);
        }
        sv.setShowcasePosition(x, y);
        sv.setText(title, detailText);
        return sv;
    }

    public static ShowcaseView insertShowcaseView(View showcase, Activity activity) {
        return insertShowcaseView(showcase, activity, null, null, null);
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
    public static ShowcaseView insertShowcaseViewWithType(int type, int itemId, Activity activity, String title, String detailText, ConfigOptions options) {
        ShowcaseView sv = new ShowcaseView(activity);
        if (options != null)
            sv.setConfigOptions(options);
        if (sv.getConfigOptions().insert == INSERT_TO_DECOR) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(sv);
        } else {
            ((ViewGroup) activity.findViewById(android.R.id.content)).addView(sv);
        }
        sv.setShowcaseItem(type, itemId, activity);
        sv.setText(title, detailText);
        return sv;
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
    public static ShowcaseView insertShowcaseViewWithType(int type, int itemId, Activity activity, int title, int detailText, ConfigOptions options) {
        ShowcaseView sv = new ShowcaseView(activity);
        if (options != null)
            sv.setConfigOptions(options);
        if (sv.getConfigOptions().insert == INSERT_TO_DECOR) {
            ((ViewGroup) activity.getWindow().getDecorView()).addView(sv);
        } else {
            ((ViewGroup) activity.findViewById(android.R.id.content)).addView(sv);
        }
        sv.setShowcaseItem(type, itemId, activity);
        sv.setText(title, detailText);
        return sv;
    }

    public static ShowcaseView insertShowcaseView(float x, float y, Activity activity) {
        return insertShowcaseView(x, y, activity, null, null, null);
    }

    public int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public Point getDisplaySize() {
        Display currentDisplay = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        currentDisplay.getSize(size);
        return size;
    }

    public boolean hasSoftNavigationBar() {
        Context context = Objects.requireNonNull(getActivity()).getBaseContext();
        Resources resources = context.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return Build.FINGERPRINT.startsWith("generic") || (id > 0 && resources.getBoolean(id));
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    int getStatusBarHeight() {
        Rect r = new Rect();
        Window w = Objects.requireNonNull(getActivity()).getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        return r.top;
    }

    int getTitleBarHeight() {
        int viewTop = Objects.requireNonNull(getActivity()).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return (viewTop - getStatusBarHeight());
    }

    private void init() {
        va = new ValueAnimator().ofFloat(1.0f, 1.1f);
        va.setDuration(500);
        va.addUpdateListener(valueAnimator -> {
            animated_v = (float) va.getAnimatedValue();
            invalidate();
        });
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.REVERSE);
        va.start();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        boolean hasShot = getContext().getSharedPreferences(PREFS_SHOWCASE_INTERNAL, Context.MODE_PRIVATE)
                .getBoolean("hasShot" + getConfigOptions().showcaseId, false);
        if (hasShot && mOptions.shotType == TYPE_ONE_SHOT) {
            // The showcase has already been shot once, so we don't need to do anything
            setVisibility(View.GONE);
            isRedundant = true;
            return;
        }
        // load drawables
        if(targetHighligtShape==0) {
            circleShowcase = (LayerDrawable) getContext().getDrawable(R.drawable.drawing);
        }
        if(targetHighligtShape==1)
        {
            rectangleShowcase = (NinePatchDrawable) getContext().getDrawable(R.drawable.shadow_rect);
        }

        // Парамеры экрана
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mDisplayWidth = mDisplayMetrics.widthPixels;
        mDisplayHeight = mDisplayMetrics.heightPixels;



            if (targetHighligtShape == 0) {
                assert circleShowcase != null;
                circleShowcase.setColorFilter(mShowcaseColor,
                        PorterDuff.Mode.MULTIPLY);
            }
            if (targetHighligtShape == 1) {
                assert rectangleShowcase != null;
                rectangleShowcase.setColorFilter(mShowcaseColor,
                        PorterDuff.Mode.MULTIPLY);
            }

        PorterDuffXfermode mBlender = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
        setOnTouchListener(this);

        mPaintTitle = new TextPaint();
        mPaintTitle.setAntiAlias(true);

        mPaintDetail = new TextPaint();
        mPaintDetail.setAntiAlias(true);

        mEraser = new Paint();
        mEraser.setColor(0xFFFFFF);
        mEraser.setAlpha(0);
        mEraser.setXfermode(mBlender);
        mEraser.setAntiAlias(true);

        if (!mOptions.noButton && mEndButton.getParent() == null) {

            LayoutInflater buttonInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View buttonView = buttonInflater.inflate(R.layout.showcase_button, null, false);
            buttonView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int buttonWidth = buttonView.getMeasuredWidth();
            int buttonHeight = buttonView.getMeasuredHeight();
            int statusBarHeight = getStatusBarHeight();
            int navigationBarHeight = getNavigationBarHeight();
            int titleBarHeight = getTitleBarHeight();

            RelativeLayout.LayoutParams lps = getConfigOptions().buttonLayoutParams;

            if (lps == null) {
                lps = (LayoutParams) generateDefaultLayoutParams();
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                int margin = statusBarHeight;
                lps.setMargins(0, margin, 0, navigationBarHeight);
                mEndButton.setLayoutParams(lps);
                mEndButton.setText(buttonText != null ? buttonText : getResources().getString(R.string.ok));
                mEndButton.bringToFront();
                bringToFront();
            }
            if (!hasCustomClickListener) mEndButton.setOnClickListener(this);
            addView(mEndButton);
        }

    }

    public void setShowcaseNoView() {
        setShowcasePosition(1000000, 1000000);
    }

    /**
     * Set the view to showcase
     *
     * @param view The {@link View} to showcase.
     */
    public void setShowcaseView(final View view) {
        if (isRedundant || view == null) {
            isRedundant = true;
            return;
        }

        view.post(() -> {
            init();
            if (mOptions.insert == INSERT_TO_VIEW) {
                showcaseX = (float) (view.getLeft() + view.getWidth() / 2);
                showcaseY = (float) (view.getTop() + view.getHeight() / 2);
            } else {
                int[] coordinates = new int[2];
                view.getLocationInWindow(coordinates);
                showcaseX = (float) (coordinates[0] + view.getWidth() / 2);
                showcaseY = (float) (coordinates[1] + view.getHeight() / 2);
            }
            showcaseW = (float) (view.getWidth());
            showcaseH = (float) (view.getHeight());
            invalidate();
        });
    }

    public void setShowcaseRehion(float X, float Y,float W, float H)
    {
        showcaseX = X;
        showcaseY = Y;
        showcaseW = W;
        showcaseH = H;
        init();
        invalidate();
    }

    public void setShowcaseSize(float W, float H)
    {
        showcaseW = W;
        showcaseH = H;
        init();
        invalidate();
    }

    /**
     * Set a specific position to showcase
     *
     * @param x X co-ordinate
     * @param y Y co-ordinate
     */
    public void setShowcasePosition(float x, float y) {
        if (isRedundant) {
            return;
        }
        showcaseX = x;
        showcaseY = y;
        init();
        invalidate();
    }

    private View iterateViews(View v, String type) {
        View view = null;
        if ((v instanceof ViewGroup)) {
            for (int index = 0; index < ((ViewGroup) v).getChildCount(); index++) {
                View nextChild = ((ViewGroup) v).getChildAt(index);
                String name = nextChild.getClass().getName();
                Log.i("smorodov", name);
                if (name.contains(type)) {
                    return nextChild;
                }
                if ((nextChild instanceof ViewGroup)) {
                    Log.i("smorodov", "enter " + name);
                    view = iterateViews(nextChild, type);
                    Log.i("smorodov", "exit " + name);
                    if (view != null) {
                        break;
                    }
                }
            }
        }
        return view;
    }

    private View iterateViews(View v, Integer id) {
        if ((v instanceof ViewGroup)) {
            for (int index = 0; index < ((ViewGroup) v).getChildCount(); index++) {
                View nextChild = ((ViewGroup) v).getChildAt(index);
                String name = nextChild.getClass().getName();
                Log.i("smorodov", name);
                if (nextChild.getId() == id) {
                    return nextChild;
                }
                if ((nextChild instanceof ViewGroup)) {
                    Log.i("smorodov", "enter " + name);
                    View view = iterateViews(nextChild, id);
                    Log.i("smorodov", "exit " + name);
                    if (view != null) {
                        return view;
                    }
                }
            }
        }
        return null;
    }

    public void setShowcaseItem(final int itemType, final int actionItemId, final Activity activity) {
        post(() -> {
            View homeButton = activity.findViewById(android.R.id.home);
            if (homeButton == null) {
                // Thanks to @hameno for this
                int homeId = activity.getResources().getIdentifier("abs__home", "id", activity.getPackageName());
                if (homeId != 0) {
                    homeButton = activity.findViewById(homeId);
                }
            }
            if (homeButton == null)
                throw new RuntimeException("insertShowcaseViewWithType cannot be used when the theme " +
                        "has no ActionBar");
            ViewParent p = homeButton.getParent().getParent(); //ActionBarView


            if (!p.getClass().getName().contains("ActionBarView")) {
                String previousP = p.getClass().getName();
                p = p.getParent();
                String name = p.getClass().getName();

                String throwP = p.getClass().getName();
                if (!p.getClass().getName().contains("ActionBarView"))
                    throw new IllegalStateException("Cannot find ActionBarView for " +
                            "Activity, instead found " + previousP + " and " + throwP);
            }

            Log.i("smorodov", p.getClass().getName() + " Contains:");


            switch (itemType) {
                case ITEM_ACTION_HOME:
                    setShowcaseView(homeButton);
                    break;
                case ITEM_SPINNER:
                    showcaseSpinner(p);
                    break;
                case ITEM_TITLE:
                    setShowcaseView(iterateViews((View) p, "TextView"));
                    //showcaseTitle(p, abv);
                    break;
                case ITEM_ACTION_ITEM:
                case ITEM_ACTION_OVERFLOW:
                    showcaseActionItem(p, itemType, actionItemId);
                    break;
                default:
                    Log.e("TAG", "Unknown item type");
            }
        });

    }

    private void showcaseActionItem(ViewParent p, int itemType, int actionItemId) {

        if (itemType == ITEM_ACTION_OVERFLOW) {
            View mOb = iterateViews((View) p, "ActionMenuView");
            if (mOb != null)
                setShowcaseView(mOb);
        } else {
            // Want an ActionItem, so find it
            View i = iterateViews((View) p, actionItemId);
            if (i != null) {
                setShowcaseView(i);
            }
        }

    }

    private void showcaseSpinner(ViewParent p) {

        View mSpinnerView = iterateViews((View) p, "Spinner");
        if (mSpinnerView != null) {
            setShowcaseView(mSpinnerView);
        }
    }

    private void showcaseTitle(ViewParent p, Class abv) {
        try {
            Field mTitleViewField = abv.getDeclaredField("mTitleView");
            mTitleViewField.setAccessible(true);
            View titleView = (View) mTitleViewField.get(p);
            if (titleView != null) {
                setShowcaseView(titleView);
            }
        } catch (NoSuchFieldException e) {
            Log.e("TAG", "Failed to find actionbar title", e);
        } catch (IllegalAccessException e) {
            Log.e("TAG", "Failed to access actionbar title", e);

        }
    }

    /**
     * Set the shot method of the showcase - only once or no limit
     *
     * @param shotType either TYPE_ONE_SHOT or TYPE_NO_LIMIT
     * @deprecated Use the option in {@link ConfigOptions} instead.
     */
    @Deprecated
    public void setShotType(int shotType) {
        if (shotType == TYPE_NO_LIMIT || shotType == TYPE_ONE_SHOT) {
            mOptions.shotType = shotType;
        }
    }

    /**
     * Decide whether touches outside the showcased circle should be ignored or not
     *
     * @param block true to block touches, false otherwise. By default, this is true.
     * @deprecated Use the option in {@link ConfigOptions} instead.
     */
    @Deprecated
    public void blockNonShowcasedTouches(boolean block) {
        mOptions.block = block;
    }

    /**
     * Override the standard button click event
     *
     * @param listener Listener to listen to on click events
     */
    public void overrideButtonClick(OnClickListener listener) {
        if (isRedundant) {
            return;
        }
        if (mEndButton != null) {
            mEndButton.setOnClickListener(listener != null ? listener : this);
        }
        hasCustomClickListener = true;
    }

    public void setOnShowcaseEventListener(OnShowcaseEventListener listener) {
        mEventListener = listener;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (showcaseX < 0 || showcaseY < 0 || isRedundant) {
            super.dispatchDraw(canvas);
            return;
        }

        //Draw the semi-transparent background
        canvas.drawColor(backColor);

        boolean recalculateText = makeVoidedRect() || mAlteredText;
        mAlteredText = false;

        if(showcaseW>mDisplayWidth/2.0f || showcaseH>mDisplayHeight/2.0f)
        {
            showcaseW=0;
            showcaseH=0;
        }

        if(showcaseW>0 && showcaseH>0) {
            // circle (cling drawable) hshape
            if (targetHighligtShape == 0) {
                Matrix mm = new Matrix();
                int R = (int) (animated_v * Math.sqrt(Math.pow(showcaseW / 2.0f, 2.0f) + Math.pow(showcaseH / 2.0f, 2.0f)));

                int l = (int) (showcaseX - R);
                int r = (int) (showcaseX + R);
                int t = (int) (showcaseY - R);
                int b = (int) (showcaseY + R);
                //Erase the area for the ring
                canvas.drawCircle(showcaseX, showcaseY, R, mEraser);
                // не зависит от параметров, просто должно быть
                circleShowcase.setBounds(-1, -1, 1, 1);
                // радиус окна и внешний радиус оверлея (drawable/drawing.xml)
                float holeRadius = 50.0f;
                float externalRadius = 150.0f;
                scaleMultiplier = R / (holeRadius * (externalRadius / holeRadius));
                mm.postTranslate(showcaseX, showcaseY);
                mm.postScale(scaleMultiplier, scaleMultiplier, showcaseX, showcaseY);


                canvas.setMatrix(mm);

                //circleShowcase.setBounds(voidedArea);
                circleShowcase.draw(canvas);
                canvas.setMatrix(new Matrix());
            }

            // if rectangle
            if (targetHighligtShape == 1) {
                float dx=Math.abs(1.0f-animated_v) * showcaseW;
                float dy=Math.abs(1.0f-animated_v) * showcaseH;
                int mx=130;
                int my=120;
                int l = (int) (showcaseX - dx - showcaseW / 2.0f);
                int r = (int) (showcaseX + dx + showcaseW / 2.0f);
                int t = (int) (showcaseY - dy - showcaseH / 2.0f);
                int b = (int) (showcaseY + dy + showcaseH / 2.0f);
                //Erase the area for the ring
                canvas.drawRect(new Rect(l, t, r, b), mEraser);
                rectangleShowcase.setBounds(l - mx, t - my, r + mx, b + my);
                rectangleShowcase.draw(canvas);
            }
        }

        int margin=40;
        if (!TextUtils.isEmpty(mTitleText) || !TextUtils.isEmpty(mSubText)) {
            if (recalculateText)
                mBestTextPosition = getBestTextPosition(canvas.getWidth(), canvas.getHeight());
        }

        if (!TextUtils.isEmpty(mSubText)) {
            if (recalculateText) {
                DynamicLayout.Builder db = DynamicLayout.Builder.obtain(mSubText, mPaintDetail, (int) mBestTextPosition[2])
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(1.2f, 1.0f)
                        .setIncludePad(true);
                mDynamicDetailLayout = db.build();
            }
        }
        if (!TextUtils.isEmpty(mTitleText)) {
            if (recalculateText) {
                DynamicLayout.Builder db = DynamicLayout.Builder.obtain(mTitleText, mPaintTitle, (int) mBestTextPosition[2])
                        .setAlignment(Layout.Alignment.ALIGN_CENTER)
                        .setLineSpacing(1.0f, 1.0f)
                        .setIncludePad(true);
                mDynamicTitleLayout = db.build();
            }
        }
            if (!TextUtils.isEmpty(mTitleText) || !TextUtils.isEmpty(mSubText)) {
            if (!TextUtils.isEmpty(mSubText)) {
                canvas.save();
                canvas.translate(mBestTextPosition[0], 3*margin+mBestTextPosition[1] + mDynamicTitleLayout.getHeight());
                mPaintDetail.setColor(sv_detailTextBgColor);
                mPaintDetail.setStyle(Paint.Style.FILL);
                //canvas.drawRoundRect(new RectF(0-margin,0-margin-mDynamicTitleLayout.getHeight()-2*margin,mDynamicDetailLayout.getWidth()+margin,mDynamicDetailLayout.getHeight()+margin),3*margin,3*margin,mPaintDetail);
                float[] corners = new float[]{
                        0,0,        // Top left radius in px
                        0,0,        // Top right radius in px
                        3*margin, 3*margin,          // Bottom right radius in px
                        3*margin, 3*margin           // Bottom left radius in px
                };

                RectF rect = new RectF(0-margin,0-margin,mDynamicDetailLayout.getWidth()+margin,mDynamicDetailLayout.getHeight()+margin);
                final Path path = new Path();
                path.addRoundRect(rect, corners, Path.Direction.CW);
                canvas.drawPath(path, mPaintDetail);

                mDynamicDetailLayout.draw(canvas);
                canvas.restore();
            }

            if (!TextUtils.isEmpty(mTitleText)) {
                canvas.save();
                canvas.translate(mBestTextPosition[0], margin+mBestTextPosition[1] - yMargin);
                mPaintTitle.setColor(sv_titleTextBgColor);
                mPaintTitle.setStyle(Paint.Style.FILL);
                //canvas.drawRoundRect(new RectF(0-margin,0-margin,mDynamicTitleLayout.getWidth()+margin,mDynamicTitleLayout.getHeight()+margin),3*margin,3*margin,mPaintTitle);

                float[] corners = new float[]{
                        3*margin, 3*margin,        // Top left radius in px
                        3*margin, 3*margin,        // Top right radius in px
                        0,0,          // Bottom right radius in px
                        0,0           // Bottom left radius in px
                };

                RectF rect = new RectF(0-margin,0-margin,mDynamicTitleLayout.getWidth()+margin,mDynamicTitleLayout.getHeight()+margin);
                final Path path = new Path();
                path.addRoundRect(rect, corners, Path.Direction.CW);
                canvas.drawPath(path, mPaintTitle);

                mDynamicTitleLayout.draw(canvas);
                canvas.restore();
            }

        }
        super.dispatchDraw(canvas);
    }

    /**
     * Calculates the best place to position text
     *
     * @param canvasW width of the screen
     * @param canvasH height of the screen
     * @return best text position
     */
    private float[] getBestTextPosition(int canvasW, int canvasH) {

        //if the width isn't much bigger than the voided area, just consider top & bottom
        float spaceTop = voidedArea.top;
        float spaceBottom = canvasH - voidedArea.bottom - 64 * metricScale; //64dip considers the OK button
        //float spaceLeft = voidedArea.left;
        //float spaceRight = canvasW - voidedArea.right;

        //TODO: currently only considers above or below showcase, deal with left or right
        return new float[]{24 * metricScale, spaceTop > spaceBottom ? 128 * metricScale : 24 * metricScale + voidedArea.bottom, canvasW - 48 * metricScale};

    }

    /**
     * Creates a {@link Rect} which represents the area the showcase covers. Used to calculate
     * where best to place the text
     *
     * @return true if voidedArea has changed, false otherwise.
     */
    private boolean makeVoidedRect() {

        // This if statement saves resources by not recalculating voidedArea
        // if the X & Y coordinates haven't changed
        if (voidedArea == null || (showcaseX != legacyShowcaseX || showcaseY != legacyShowcaseY)) {

            int cx = (int) showcaseX, cy = (int) showcaseY;
            int dw=1,dh=1;
            if(targetHighligtShape==0) {
                dw = (int) (Math.max(showcaseW,showcaseH)*3);//circleShowcase.getIntrinsicWidth();
                dh = (int) (Math.max(showcaseW,showcaseH)*3);//circleShowcase.getIntrinsicHeight();

            }
            if(targetHighligtShape==1) {
                dw = rectangleShowcase.getIntrinsicWidth();
                dh = rectangleShowcase.getIntrinsicHeight();
            }

            voidedArea = new Rect(cx - dw / 2, cy - dh / 2, cx + dw / 2, cy + dh / 2);

            legacyShowcaseX = showcaseX;
            legacyShowcaseY = showcaseY;

            return true;

        }
        return false;

    }

    public void animateGesture(float offsetStartX, float offsetStartY, float offsetEndX, float offsetEndY) {


        getHand().bringToFront();
        moveHand(offsetStartX, offsetStartY, offsetEndX, offsetEndY);
    }



    @Override
    public void onClick(View view) {
        // If the type is set to one-shot, store that it has shot
        if (mOptions.shotType == TYPE_ONE_SHOT) {
            SharedPreferences internal = getContext().getSharedPreferences(PREFS_SHOWCASE_INTERNAL, Context.MODE_PRIVATE);
            internal.edit().putBoolean("hasShot" + getConfigOptions().showcaseId, true).apply();
        }
        hide();
    }

    public void hide() {
        if (mEventListener != null) {
            mEventListener.onShowcaseViewHide(this);
        }

        if (getConfigOptions().fadeOutDuration > 0) {
            fadeOutShowcase();
        } else {
            setVisibility(View.GONE);
        }
    }

    private void fadeOutShowcase() {
        AnimationUtils.createFadeOutAnimation(this, getConfigOptions().fadeOutDuration, () -> setVisibility(View.GONE)).start();
    }

    public void show() {
        if (mEventListener != null) {
            mEventListener.onShowcaseViewShow(this);
        }
        if (getConfigOptions().fadeInDuration > 0) {
            fadeInShowcase();
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    private void fadeInShowcase() {
        AnimationUtils.createFadeInAnimation(this, getConfigOptions().fadeInDuration, () -> setVisibility(View.VISIBLE)).start();
    }

    // Скрываем по касанию вне цели
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        RectF bounds = new RectF(showcaseX - showcaseW / 2.0f,
                showcaseY - showcaseH / 2.0f,
                showcaseX + showcaseW / 2.0f,
                showcaseY + showcaseH / 2.0f);

        if (mOptions.hideOnClickOutside && !bounds.contains(motionEvent.getRawX(), motionEvent.getRawY())) {
            this.hide();
            return true;
        }
        return mOptions.block && !(bounds.contains(motionEvent.getRawX(), motionEvent.getRawY()));
    }

    public void setShowcaseIndicatorScale(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public void setText(int titleTextResId, int subTextResId) {
        String titleText = getContext().getResources().getString(titleTextResId);
        String subText = getContext().getResources().getString(subTextResId);
        setText(titleText, subText);
    }

    public void setText(String titleText, String subText) {
        SpannableString ssbTitle = new SpannableString(titleText);
        ssbTitle.setSpan(mTitleSpan, 0, ssbTitle.length(), 0);
        mTitleText = ssbTitle;
        SpannableString ssbDetail = new SpannableString(subText);
        ssbDetail.setSpan(mDetailSpan, 0, ssbDetail.length(), 0);
        mSubText = ssbDetail;
        mAlteredText = true;
        invalidate();
    }

    /**
     * Get the ghostly gesture hand for custom gestures
     *
     * @return a View representing the ghostly hand
     */
    public View getHand() {
        if (mHandy == null) {
            mHandy = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.handy, null);
        }
        AnimationUtils.hide(mHandy);
        return mHandy;
    }

    /**
     * Point to a specific view
     *
     * @param view The {@link View} to Showcase
     */
    public void pointTo(View view) {
        float x = AnimationUtils.getX(view) + view.getWidth() / 2.0f;
        float y = AnimationUtils.getY(view) + view.getHeight() / 2.0f;
        pointTo(x, y);
    }

    /**
     * Point to a specific point on the screen
     *
     * @param x X-coordinate to point to
     * @param y Y-coordinate to point to
     */
    public void pointTo(float x, float y)
    {
        mHandy = getHand();
        AnimationUtils.AnimationEndListener listener= () -> removeView(mHandy);
        addView(mHandy);
        AnimationUtils.createPointingAnimation(mHandy, x, y, listener).start();
    }
    private void moveHand(float offsetStartX, float offsetStartY, float offsetEndX, float offsetEndY) {
        mHandy = getHand();
        addView(mHandy);
        AnimationUtils.AnimationEndListener listener= () -> removeView(mHandy);

        AnimationUtils.createMovementAnimation(mHandy, showcaseX, showcaseY,
                offsetStartX, offsetStartY,
                offsetEndX, offsetEndY,
                listener).start();
    }
    private ConfigOptions getConfigOptions() {
        // Make sure that this method never returns null
        if (mOptions == null) return mOptions = new ConfigOptions();
        return mOptions;
    }

    private void setConfigOptions(ConfigOptions options) {
        mOptions = options;
    }

    public interface OnShowcaseEventListener {

        void onShowcaseViewHide(ShowcaseView showcaseView);

        void onShowcaseViewShow(ShowcaseView showcaseView);

    }

    public static class ConfigOptions {
        public boolean block = true, noButton = false;
        public int insert = INSERT_TO_DECOR;
        public boolean hideOnClickOutside = false;

        /**
         * If you want to use more than one Showcase with the {@link ConfigOptions#shotType} {@link ShowcaseView#TYPE_ONE_SHOT} in one Activity, set a unique value for every different Showcase you want to use.
         */
        public int showcaseId = 0;

        /**
         * If you want to use more than one Showcase with {@link ShowcaseView#TYPE_ONE_SHOT} in one Activity, set a unique {@link ConfigOptions#showcaseId} value for every different Showcase you want to use.
         */
        public int shotType = TYPE_NO_LIMIT;

        /**
         * Default duration for fade in animation. Set to 0 to disable.
         */
        public int fadeInDuration = AnimationUtils.DEFAULT_DURATION;

        /**
         * Default duration for fade out animation. Set to 0 to disable.
         */
        public int fadeOutDuration = AnimationUtils.DEFAULT_DURATION;
        /**
         * Allow custom positioning of the button within the showcase view.
         */
        public LayoutParams buttonLayoutParams = null;
    }

}
