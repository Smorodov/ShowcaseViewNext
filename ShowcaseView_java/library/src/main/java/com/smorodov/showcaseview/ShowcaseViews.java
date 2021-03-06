package com.smorodov.showcaseview;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ShowcaseViews {

    private final List<ShowcaseView> views = new ArrayList<>();
    private final Activity activity;
    private OnShowcaseAcknowledged showcaseAcknowledgedListener = showcaseView -> {
        //DEFAULT LISTENER - DOESN'T DO ANYTHING!
    };

    public ShowcaseViews(Activity activity, int showcaseTemplateLayout) {
        this.activity = activity;
    }

    public ShowcaseViews(Activity activity, int showcaseTemplateLayout, OnShowcaseAcknowledged acknowledgedListener) {
        this(activity, showcaseTemplateLayout);
        this.showcaseAcknowledgedListener = acknowledgedListener;
    }

    public void addView(ItemViewProperties properties) {
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(activity)
                .setText(properties.titleResId, properties.messageResId);

        if (showcaseActionBar(properties)) {
            builder.setShowcaseItem(properties.itemType, properties.id, activity);
        } else if (properties.id == ItemViewProperties.ID_NO_SHOWCASE) {
            builder.setShowcaseNoView();
        } else {
            builder.setShowcaseView(activity.findViewById(properties.id));
        }

        ShowcaseView showcaseView = builder.build();
        showcaseView.overrideButtonClick(createShowcaseViewDismissListener(showcaseView));
        views.add(showcaseView);
    }

    private boolean showcaseActionBar(ItemViewProperties properties) {
        return properties.itemType > ItemViewProperties.ID_NOT_IN_ACTIONBAR;
    }

    private View.OnClickListener createShowcaseViewDismissListener(final ShowcaseView showcaseView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showcaseView.hide();
                if (views.isEmpty()) {
                    showcaseAcknowledgedListener.onShowCaseAcknowledged(showcaseView);
                } else {
                    show();
                }
            }
        };
    }

    public void show() {
        if (views.isEmpty()) {
            return;
        }
        final ShowcaseView view = views.get(0);
        ((ViewGroup) activity.getWindow().getDecorView()).addView(view);
        views.remove(0);
    }

    public boolean hasViews() {
        return !views.isEmpty();
    }

    public interface OnShowcaseAcknowledged {
        void onShowCaseAcknowledged(ShowcaseView showcaseView);
    }

    public static class ItemViewProperties {

        public static final int ID_NO_SHOWCASE = -2202;
        public static final int ID_NOT_IN_ACTIONBAR = -1;
        public static final int ID_SPINNER = 0;
        public static final int ID_TITLE = 1;
        public static final int ID_OVERFLOW = 2;
        private static final float DEFAULT_SCALE = 1f;

        protected final int titleResId;
        protected final int messageResId;
        protected final int id;
        protected final int itemType;
        protected final float scale;

        public ItemViewProperties(int titleResId, int messageResId) {
            this(ID_NO_SHOWCASE, titleResId, messageResId, ID_NOT_IN_ACTIONBAR, DEFAULT_SCALE);
        }

        public ItemViewProperties(int id, int titleResId, int messageResId) {
            this(id, titleResId, messageResId, ID_NOT_IN_ACTIONBAR, DEFAULT_SCALE);
        }

        public ItemViewProperties(int id, int titleResId, int messageResId, float scale) {
            this(id, titleResId, messageResId, ID_NOT_IN_ACTIONBAR, scale);
        }

        public ItemViewProperties(int id, int titleResId, int messageResId, int itemType) {
            this(id, titleResId, messageResId, itemType, DEFAULT_SCALE);
        }

        public ItemViewProperties(int id, int titleResId, int messageResId, int itemType, float scale) {
            this.id = id;
            this.titleResId = titleResId;
            this.messageResId = messageResId;
            this.itemType = itemType;
            this.scale = scale;
        }
    }
}
