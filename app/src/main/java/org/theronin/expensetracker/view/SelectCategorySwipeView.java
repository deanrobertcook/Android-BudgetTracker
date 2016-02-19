package org.theronin.expensetracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.BuildConfig;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Category;

import timber.log.Timber;

public class SelectCategorySwipeView extends LinearLayout implements View.OnClickListener {
    private Listener listener;
    private Category category;

    private boolean isOpen;
    private int translatableDistance;

    private TextView categoryNameView;
    private ViewGroup optionsMenu;

    private View optionsButton;

    public SelectCategorySwipeView(Context context) {
        this(context, null);
    }

    public SelectCategorySwipeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectCategorySwipeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Timber.v("onFinishInflate");
        categoryNameView = (TextView) findViewById(R.id.category_name);
        optionsMenu = (ViewGroup) findViewById(R.id.options_menu);
        optionsButton = findViewById(R.id.more);

        categoryNameView.setOnClickListener(this);
        optionsButton.setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.merge).setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Timber.v("onMeasure");

        translatableDistance = optionsMenu.getWidth() - optionsButton.getWidth();

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setCategory(Category category) {
        this.category = category;

        String text = WordUtils.capitalize(category.name);
        if (BuildConfig.DEBUG) {
            text += String.format(" (%d)", category.frequency);
        }
        categoryNameView.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (listener == null) {
            throw new IllegalStateException("The category swipe view needs a listener!");
        }
        switch (v.getId()) {
            case R.id.category_name:
                listener.onCategorySelected(category);
                break;
            case R.id.more:
                if (isOpen) {
                    closeOptions(false);
                } else {
                    listener.onOptionsExpanding();
                    expandOptions();
                }
                break;
            case R.id.edit:
                listener.onEditClicked(category);
                break;
            case R.id.merge:
                listener.onMergeClicked(category);
                break;
            case R.id.delete:
                listener.onDeleteClicked(category);
                break;
        }
    }

    private void expandOptions() {
        Timber.v("expandOptions");
        if (isOpen) {
            return;
        }
        startAnimation(new MarginAnimation(true));
        isOpen = true;
    }

    public void closeOptions(boolean fast) {
        if (!isOpen) {
            return;
        }
        Timber.v("closeOptions");
        if (fast) {
            MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
            params.rightMargin = -translatableDistance;
            setLayoutParams(params);

        } else {
            startAnimation(new MarginAnimation(false));
        }
        isOpen = false;
    }

    public void reset() {
        closeOptions(true);
        category = null;
    }

    public interface Listener {
        void onCategorySelected(Category category);

        void onEditClicked(Category category);

        void onMergeClicked(Category category);

        void onDeleteClicked(Category category);

        void onOptionsExpanding();
    }

    private class MarginAnimation extends Animation {
        public static final int ANIM_DURATION = 200;
        private final int startMargin;
        private final int endMargin;

        public MarginAnimation(boolean open) {
            startMargin = open ? -translatableDistance : 0;
            endMargin = open ? 0 : -translatableDistance;

            setFillEnabled(true);
            setDuration(ANIM_DURATION);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newMargin = startMargin + (int) ((endMargin - startMargin) * interpolatedTime);

            ((MarginLayoutParams) getLayoutParams()).rightMargin = newMargin;
            requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
