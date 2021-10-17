package com.github.RooneyAndShadows.lightbulb.overlayview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.RooneyAndShadows.lightbulb.commons.utils.ResourceUtils;
import com.rands.lightbulb.overlayview.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OverlayView extends RelativeLayout {
    private Integer layoutId = null;
    private View layoutView = null;
    private int colorBackground;
    private int colorBackgroundAlpha;
    private boolean showing = false;
    private OverlayListeners overlayListeners;
    private boolean isWaitingDelayedShow = false;
    private final Runnable showRunnable = this::show;

    public OverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs);
        initView();
    }

    public void setOverlayListeners(OverlayListeners overlayListeners) {
        this.overlayListeners = overlayListeners;
    }

    public void setColorBackground(int colorBackground) {
        this.colorBackground = colorBackground;
        if (colorBackground != 0)
            setBackgroundColor(colorBackground);
    }

    public void setLayout(View view) {
        if (view == null)
            return;
        layoutId = null;
        layoutView = view;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        removeAllViews();
        addView(layoutView, params);
        if (overlayListeners != null)
            overlayListeners.onInflated(layoutView);
    }

    public void setLayout(int layoutId) {
        this.layoutId = layoutId;
        layoutView = LayoutInflater.from(getContext()).inflate(layoutId, null);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        removeAllViews();
        addView(layoutView, params);
        if (overlayListeners != null)
            overlayListeners.onInflated(layoutView);
    }

    public View getLayoutView() {
        return layoutView;
    }

    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        performClick();
        return true;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.showing = this.showing;
        myState.backgroundColor = this.colorBackground;
        myState.backgroundColorAlpha = this.colorBackgroundAlpha;
        myState.isWaitingDelayedShow = this.isWaitingDelayedShow;
        if (layoutId != null)
            myState.layoutId = layoutId;
        cancelDelayedShowing();
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.showing = savedState.showing;
        this.layoutId = savedState.layoutId;
        this.colorBackground = savedState.backgroundColor;
        this.colorBackgroundAlpha = savedState.backgroundColorAlpha;
        setColorBackground(colorBackground);
        if (savedState.layoutId != null)
            setLayout(savedState.layoutId);
        if (showing || savedState.isWaitingDelayedShow) show();
        else hide();
    }

    public void showDelayed(int delay) {
        if (isShown() || isWaitingDelayedShow)
            return;
        isWaitingDelayedShow = true;
        postDelayed(showRunnable, delay);
    }

    public void show() {
        cancelDelayedShowing();
        showing = true;
        setVisibility(VISIBLE);
        if (overlayListeners != null)
            overlayListeners.onShow(layoutView);
    }

    public void hide() {
        cancelDelayedShowing();
        showing = false;
        setVisibility(GONE);
        if (overlayListeners != null)
            overlayListeners.onHide(layoutView);
    }

    public void cancelDelayedShowing() {
        removeCallbacks(showRunnable);
        isWaitingDelayedShow = false;
    }

    protected void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OverlayView, 0, 0);
        try {
            showing = a.getBoolean(R.styleable.OverlayView_overlayShowing, false);
            colorBackground = a.getColor(R.styleable.OverlayView_overlayColorBackground, ResourceUtils.getColorByAttribute(getContext(), android.R.attr.colorBackground));
            colorBackgroundAlpha = a.getInt(R.styleable.OverlayView_overlayColorBackgroundAlpha, 255);
            int layout = a.getResourceId(R.styleable.OverlayView_overlayLayout, -1);
            if (layout != -1)
                layoutId = layout;
            if (colorBackgroundAlpha > 255)
                colorBackgroundAlpha = 255;
            if (colorBackgroundAlpha < 0)
                colorBackgroundAlpha = 0;
        } finally {
            a.recycle();
        }
    }

    private void initView() {
        setElevation(100);
        inflate(getContext(), R.layout.view_overlay, this);
        if (colorBackground != 0)
            setBackgroundColor(colorBackground);
        getBackground().setAlpha(colorBackgroundAlpha);
        setVisibility(showing ? VISIBLE : GONE);
        if (layoutId != null)
            setLayout(layoutId);
    }

    public abstract static class OverlayListeners {
        public void onInflated(View view) {
        }

        public void onShow(View view) {
        }

        public void onHide(View view) {
        }
    }

    private static class SavedState extends BaseSavedState {
        private Integer layoutId;
        private boolean showing;
        private boolean isWaitingDelayedShow;
        private int backgroundColor;
        private int backgroundColorAlpha;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            layoutId = in.readInt();
            backgroundColorAlpha = in.readInt();
            showing = in.readInt() == 1;
            isWaitingDelayedShow = in.readInt() == 1;
            backgroundColor = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            if (layoutId != null)
                out.writeInt(layoutId);
            out.writeInt(backgroundColor);
            out.writeInt(backgroundColorAlpha);
            out.writeInt(showing ? 1 : 0);
            out.writeInt(isWaitingDelayedShow ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
