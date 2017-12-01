package com.tapadoo.alerter;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tapadoo.android.R;

/**
 * The type Alert.
 */
public class Alert extends FrameLayout implements View.OnClickListener, Animation.AnimationListener, SwipeDismissTouchListener.DismissCallbacks {

    private static final int CLEAN_UP_DELAY_MILLIS = 100;

    private static final long DISPLAY_TIME_IN_SECONDS = 3000;
    private static final int MUL = 0xFF000000;

    //UI
    private FrameLayout flClickShield;
    private FrameLayout flBackground;
    private TextView tvTitle;
    private TextView tvText;
    private ImageView ivIcon;
    private ImageView lvIcon;
    private ViewGroup rlContainer;
    private ProgressBar pbProgress;

    private Animation slideInAnimation;
    private Animation slideOutAnimation;

    private OnShowAlertListener onShowListener;
    private OnHideAlertListener onHideListener;

    private long duration = DISPLAY_TIME_IN_SECONDS;

    private boolean enableIconPulse = true;
    private boolean enableInfiniteDuration;
    private boolean enableProgress;

    private Runnable runningAnimation;

    private boolean marginSet;
    private boolean vibrationEnabled = true;

    /**
     * Instantiates a new Alert.
     *
     * @param context the context
     */
    public Alert(@NonNull final Context context) {
        super(context, null, R.attr.alertStyle);
        initView();
    }

    /**
     * Instantiates a new Alert.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public Alert(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs, R.attr.alertStyle);
        initView();
    }

    /**
     * Instantiates a new Alert.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public Alert(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.alerter_alert_view, this);
        setHapticFeedbackEnabled(true);

        flBackground = (FrameLayout) findViewById(R.id.flAlertBackground);
        flClickShield = (FrameLayout) findViewById(R.id.flClickShield);
        ivIcon = (ImageView) findViewById(R.id.ivIcon);
        lvIcon = (ImageView) findViewById(R.id.lvIcon);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvText = (TextView) findViewById(R.id.tvText);
        rlContainer = (ViewGroup) findViewById(R.id.rlContainer);
        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);

        flBackground.setOnClickListener(this);

        //Setup Enter & Exit Animations
        slideInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
        slideOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_out_to_top);
        slideInAnimation.setAnimationListener(this);

        //Set Animation to be Run when View is added to Window
        setAnimation(slideInAnimation);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!marginSet) {
            marginSet = true;

            // Add a negative top margin to compensate for overshoot enter animation
            final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
            params.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.alerter_alert_negative_margin_top);
            requestLayout();
        }
    }

    // Release resources once view is detached.
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        slideInAnimation.setAnimationListener(null);
    }

    /* Override Methods */

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        performClick();
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(final View v) {
        hide();
    }

    @Override
    public void setOnClickListener(final OnClickListener listener) {
        flBackground.setOnClickListener(listener);
    }

    @Override
    public void setVisibility(final int visibility) {
        super.setVisibility(visibility);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(visibility);
        }
    }

    /* Interface Method Implementations */

    @Override
    public void onAnimationStart(final Animation animation) {
        if (!isInEditMode()) {
            if (vibrationEnabled) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationEnd(final Animation animation) {
        //Start the Icon Animation once the Alert is settled
        if (enableIconPulse && ivIcon.getVisibility() == VISIBLE) {
            try {
                ivIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.alerter_pulse));
            } catch (Exception ex) {
                Log.e(getClass().getSimpleName(), Log.getStackTraceString(ex));
            }
        }

        if (onShowListener != null) {
            onShowListener.onShow();
        }

        startHideAnimation();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startHideAnimation() {
        //Start the Handler to clean up the Alert
        if (!enableInfiniteDuration) {
            runningAnimation = new Runnable() {
                @Override
                public void run() {
                    hide();
                }
            };
            postDelayed(runningAnimation, duration);
        }

        if (enableProgress && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            pbProgress.setVisibility(View.VISIBLE);

            final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
            valueAnimator.setDuration(duration);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    pbProgress.setProgress((int) animation.getAnimatedValue());
                }
            });
            valueAnimator.start();
        }

    }

    @Override
    public void onAnimationRepeat(final Animation animation) {
        //Ignore
    }

    /* Clean Up Methods */

    /**
     * Hide.
     */
    public void hide() {
        try {
            slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                    flBackground.setOnClickListener(null);
                    flBackground.setClickable(false);
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    removeFromParent();
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                    //Ignore
                }
            });
            startAnimation(slideOutAnimation);
        } catch (Exception ex) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(ex));
        }
    }

    private void removeFromParent() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (getParent() == null) {
                        Log.e(getClass().getSimpleName(), "getParent() returning Null");
                    } else {
                        try {
                            ((ViewGroup) getParent()).removeView(Alert.this);

                            if (onHideListener != null) {
                                onHideListener.onHide();
                            }
                        } catch (Exception ex) {
                            Log.e(getClass().getSimpleName(), "Cannot remove from parent layout");
                        }
                    }
                } catch (Exception ex) {
                    Log.e(getClass().getSimpleName(), Log.getStackTraceString(ex));
                }
            }
        }, CLEAN_UP_DELAY_MILLIS);
    }

    /* Setters and Getters */

    /**
     * Sets alert background color.
     *
     * @param color the color
     */
    public void setAlertBackgroundColor(@ColorInt final int color) {
        flBackground.setBackgroundColor(color);
    }

    /**
     * Sets alert background resource.
     *
     * @param resource the resource
     */
    public void setAlertBackgroundResource(@DrawableRes final int resource) {
        flBackground.setBackgroundResource(resource);
    }

    /**
     * Sets alert background drawable.
     *
     * @param drawable the drawable
     */
    public void setAlertBackgroundDrawable(final Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flBackground.setBackground(drawable);
        } else {
            flBackground.setBackgroundDrawable(drawable);
        }
    }

    /**
     * Sets title.
     *
     * @param titleId the title id
     */
    public void setTitle(@StringRes final int titleId) {
        setTitle(getContext().getString(titleId));
    }

    /**
     * Sets text.
     *
     * @param textId the text id
     */
    public void setText(@StringRes final int textId) {
        setText(getContext().getString(textId));
    }

    /**
     * Gets content gravity.
     *
     * @return the content gravity
     */
    public int getContentGravity() {
        return ((LayoutParams) rlContainer.getLayoutParams()).gravity;
    }

    /**
     * Sets bar height.
     *
     * @param barHeight the bar height
     */
    public void setBarHeight(final int barHeight) {
        ((LayoutParams) rlContainer.getLayoutParams()).height = barHeight;
        rlContainer.requestLayout();
    }

    /**
     * Sets bar width.
     *
     * @param barWidth the bar width
     */
    public void setBarWidth(final int barWidth) {
        ((LayoutParams) rlContainer.getLayoutParams()).width = barWidth;
        rlContainer.requestLayout();
    }

    /**
     * Sets bar margin.
     *
     * @param barMargin the bar margin
     */
    public void setBarMargin(final int barMargin) {
        ((LayoutParams) flBackground.getLayoutParams()).topMargin = barMargin;
        flBackground.requestLayout();
    }

    /**
     * Sets content gravity.
     *
     * @param contentGravity the content gravity
     */
    public void setContentGravity(final int contentGravity) {
        ((LayoutParams) rlContainer.getLayoutParams()).gravity = contentGravity;
        rlContainer.requestLayout();
    }

    /**
     * Disable outside touch.
     */
    public void disableOutsideTouch() {
        flClickShield.setClickable(true);
    }

    /**
     * Gets alert background.
     *
     * @return the alert background
     */
    public FrameLayout getAlertBackground() {
        return flBackground;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public TextView getTitle() {
        return tvTitle;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(@NonNull final String title) {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(VISIBLE);
            tvTitle.setText(title);
        }
    }

    /**
     * Sets title appearance.
     *
     * @param textAppearance the text appearance
     */
    public void setTitleAppearance(@StyleRes final int textAppearance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTitle.setTextAppearance(textAppearance);
        } else {
            tvTitle.setTextAppearance(tvTitle.getContext(), textAppearance);
        }
    }

    /**
     * Sets title typeface.
     *
     * @param typeface the typeface
     */
    public void setTitleTypeface(@NonNull final Typeface typeface) {
        tvTitle.setTypeface(typeface);
    }

    /**
     * Sets text typeface.
     *
     * @param typeface the typeface
     */
    public void setTextTypeface(@NonNull final Typeface typeface) {
        tvText.setTypeface(typeface);
    }

    /**
     * Gets text.
     *
     * @return the text
     */
    public TextView getText() {
        return tvText;
    }

    /**
     * Sets text.
     *
     * @param text the text
     */
    public void setText(final String text) {
        if (!TextUtils.isEmpty(text)) {
            tvText.setVisibility(VISIBLE);
            tvText.setText(text);
        }
    }

    /**
     * Sets text appearance.
     *
     * @param textAppearance the text appearance
     */
    public void setTextAppearance(@StyleRes final int textAppearance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvText.setTextAppearance(textAppearance);
        } else {
            tvText.setTextAppearance(tvText.getContext(), textAppearance);
        }
    }

    /**
     * Gets icon.
     *
     * @return the icon
     */
    public ImageView getIcon() {
        return ivIcon;
    }

    /**
     * Sets icon.
     *
     * @param iconId the icon id
     */
    public void setIcon(@DrawableRes final int iconId) {
        ivIcon.setImageDrawable(AppCompatResources.getDrawable(getContext(), iconId));
    }

    /**
     * Sets icon.
     *
     * @param bitmap the bitmap
     */
    public void setIcon(@NonNull final Bitmap bitmap) {
        ivIcon.setImageBitmap(bitmap);
    }

    /**
     * Sets icon.
     *
     * @param drawable the drawable
     */
    public void setIcon(@NonNull final Drawable drawable) {
        ivIcon.setImageDrawable(drawable);
    }

    /**
     * Show icon.
     *
     * @param showIcon the show icon
     */
    public void showIcon(final boolean showIcon) {
        ivIcon.setVisibility(showIcon ? View.VISIBLE : View.GONE);
    }

    /**
     * Enable swipe to dismiss.
     */
    public void enableSwipeToDismiss() {
        flBackground.setOnTouchListener(new SwipeDismissTouchListener(flBackground, null, this));
    }

    /**
     * Gets second icon.
     *
     * @return the second icon
     */
    public ImageView getSecondIcon() {
        return lvIcon;
    }

    /**
     * Sets second icon.
     *
     * @param iconId the icon id
     */
    public void setSecondIcon(@DrawableRes final int iconId) {
        lvIcon.setImageDrawable(AppCompatResources.getDrawable(getContext(), iconId));
    }

    /**
     * Sets second icon.
     *
     * @param bitmap the bitmap
     */
    public void setSecondIcon(@NonNull final Bitmap bitmap) {
        lvIcon.setImageBitmap(bitmap);
    }

    /**
     * Sets second icon.
     *
     * @param drawable the drawable
     */
    public void setSecondIcon(@NonNull final Drawable drawable) {
        lvIcon.setImageDrawable(drawable);
    }

    /**
     * Show second icon.
     *
     * @param showIcon the show icon
     */
    public void showSecondIcon(final boolean showIcon) {
        lvIcon.setVisibility(showIcon ? View.VISIBLE : View.GONE);
    }

    /**
     * Gets duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets duration.
     *
     * @param duration the duration
     */
    public void setDuration(final long duration) {
        this.duration = duration;
    }

    /**
     * Pulse icon.
     *
     * @param shouldPulse the should pulse
     */
    public void pulseIcon(final boolean shouldPulse) {
        this.enableIconPulse = shouldPulse;
    }

    /**
     * Sets enable infinite duration.
     *
     * @param enableInfiniteDuration the enable infinite duration
     */
    public void setEnableInfiniteDuration(final boolean enableInfiniteDuration) {
        this.enableInfiniteDuration = enableInfiniteDuration;
    }

    /**
     * Sets enable progress.
     *
     * @param enableProgress the enable progress
     */
    public void setEnableProgress(final boolean enableProgress) {
        this.enableProgress = enableProgress;
    }

    /**
     * Sets progress color res.
     *
     * @param color the color
     */
    public void setProgressColorRes(@ColorRes final int color) {
        pbProgress.getProgressDrawable().setColorFilter(new LightingColorFilter(MUL, ContextCompat.getColor(getContext(), color)));
    }

    /**
     * Sets progress color int.
     *
     * @param color the color
     */
    public void setProgressColorInt(@ColorInt final int color) {
        pbProgress.getProgressDrawable().setColorFilter(new LightingColorFilter(MUL, color));
    }

    /**
     * Sets progress intermediate color.
     *
     * @param color the color
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProgressIntermediateColor(@ColorInt final int color) {
        pbProgress.setIndeterminateTintList(ColorStateList.valueOf(color));
    }

    /**
     * Sets progress background tint color.
     *
     * @param color the color
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProgressBackgroundTintColor(@ColorInt final int color) {
        pbProgress.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    /**
     * Set progress bar intermediate.
     *
     * @param bol the bol
     */
    public void setProgressBarIntermediate(Boolean bol){
        pbProgress.setIndeterminate(bol);
    }

    /**
     * Set progress bar enabled.
     *
     * @param bol the bol
     */
    public void setProgressBarEnabled(Boolean bol){
        if (bol) {
            pbProgress.setVisibility(VISIBLE);
        } else {
            pbProgress.setVisibility(GONE);
        }
    }

    /**
     * Sets on show listener.
     *
     * @param listener the listener
     */
    public void setOnShowListener(@NonNull final OnShowAlertListener listener) {
        this.onShowListener = listener;
    }

    /**
     * Sets on hide listener.
     *
     * @param listener the listener
     */
    public void setOnHideListener(@NonNull final OnHideAlertListener listener) {
        this.onHideListener = listener;
    }

    /**
     * Sets vibration enabled.
     *
     * @param vibrationEnabled the vibration enabled
     */
    public void setVibrationEnabled(final boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }

    @Override
    public boolean canDismiss(final Object token) {
        return true;
    }

    @Override
    public void onDismiss(final View view, final Object token) {
        flClickShield.removeView(flBackground);
    }

    @Override
    public void onTouch(final View view, final boolean touch) {
        if (touch) {
            removeCallbacks(runningAnimation);
        } else {
            startHideAnimation();
        }
    }
}