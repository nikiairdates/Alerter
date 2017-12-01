package com.tapadoo.alerter;

import android.app.Activity;
import android.graphics.Bitmap;
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
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tapadoo.android.R;

import java.lang.ref.WeakReference;

/**
 * The type Alerter.
 */
public final class Alerter {

    private static WeakReference<Activity> activityWeakReference;

    private Alert alert;

    private Alerter() {
        //Utility classes should not be instantiated
    }

    /**
     * Create alerter.
     *
     * @param activity the activity
     * @return the alerter
     */
    public static Alerter create(@NonNull final Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null!");
        }

        final Alerter alerter = new Alerter();

        //Hide current Alert, if one is active
        Alerter.clearCurrent(activity);

        alerter.setActivity(activity);
        alerter.setAlert(new Alert(activity));

        return alerter;
    }

    /**
     * Clear current.
     *
     * @param activity the activity
     */
    public static void clearCurrent(@NonNull final Activity activity) {
        if (activity == null) {
            return;
        }

        try {
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

            //Find all Alert Views in Parent layout
            for (int i = 0; i < decorView.getChildCount(); i++) {
                final Alert childView = decorView.getChildAt(i) instanceof Alert ? (Alert) decorView.getChildAt(i) : null;
                if (childView != null && childView.getWindowToken() != null) {
                    ViewCompat.animate(childView).alpha(0).withEndAction(getRemoveViewRunnable(childView));
                }
            }

        } catch (Exception ex) {
            Log.e(Alerter.class.getClass().getSimpleName(), Log.getStackTraceString(ex));
        }
    }

    /**
     * Hide.
     */
    public static void hide() {
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            clearCurrent(activityWeakReference.get());
        }
    }

    @NonNull
    private static Runnable getRemoveViewRunnable(final Alert childView) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    ((ViewGroup) childView.getParent()).removeView(childView);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
                }
            }
        };
    }

    /**
     * Is showing boolean.
     *
     * @return the boolean
     */
    public static boolean isShowing() {
        boolean isShowing = false;
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            isShowing = activityWeakReference.get().findViewById(R.id.flAlertBackground) != null;
        }

        return isShowing;
    }

    /**
     * Show alert.
     *
     * @return the alert
     */
    public Alert show() {
        //This will get the Activity Window's DecorView
        if (getActivityWeakReference() != null) {
            getActivityWeakReference().get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Add the new Alert to the View Hierarchy
                    final ViewGroup decorView = getActivityDecorView();
                    if (decorView != null && getAlert().getParent() == null) {
                        decorView.addView(getAlert());
                    }
                }
            });
        }

        return getAlert();
    }

    /**
     * Sets title.
     *
     * @param titleId the title id
     * @return the title
     */
    public Alerter setTitle(@StringRes final int titleId) {
        if (getAlert() != null) {
            getAlert().setTitle(titleId);
        }

        return this;
    }

    /**
     * Sets title.
     *
     * @param title the title
     * @return the title
     */
    public Alerter setTitle(final String title) {
        if (getAlert() != null) {
            getAlert().setTitle(title);
        }

        return this;
    }

    /**
     * Sets title typeface.
     *
     * @param typeface the typeface
     * @return the title typeface
     */
    public Alerter setTitleTypeface(@NonNull final Typeface typeface) {
        if (getAlert() != null) {
            getAlert().setTitleTypeface(typeface);
        }

        return this;
    }

    /**
     * Sets title appearance.
     *
     * @param textAppearance the text appearance
     * @return the title appearance
     */
    public Alerter setTitleAppearance(@StyleRes final int textAppearance) {
        if (getAlert() != null) {
            getAlert().setTitleAppearance(textAppearance);
        }

        return this;
    }

    /**
     * Sets content gravity.
     *
     * @param gravity the gravity
     * @return the content gravity
     */
    public Alerter setContentGravity(final int gravity) {
        if (getAlert() != null) {
            getAlert().setContentGravity(gravity);
        }

        return this;
    }

    /**
     * Sets bar height.
     *
     * @param barHeight the bar height
     * @return the bar height
     */
    public Alerter setBarHeight(final int barHeight) {
        if (getAlert() != null) {
            getAlert().setBarHeight(barHeight);
        }

        return this;
    }

    /**
     * Sets bar width.
     *
     * @param barWidth the bar width
     * @return the bar width
     */
    public Alerter setBarWidth(final int barWidth) {
        if (getAlert() != null) {
            getAlert().setBarWidth(barWidth);
        }

        return this;
    }

    /**
     * Sets bar margin.
     *
     * @param barMargin the bar margin
     * @return the bar margin
     */
    public Alerter setBarMargin(final int barMargin) {
        if (getAlert() != null) {
            getAlert().setBarMargin(barMargin);
        }

        return this;
    }

    /**
     * Sets text.
     *
     * @param textId the text id
     * @return the text
     */
    public Alerter setText(@StringRes final int textId) {
        if (getAlert() != null) {
            getAlert().setText(textId);
        }

        return this;
    }

    /**
     * Sets text.
     *
     * @param text the text
     * @return the text
     */
    public Alerter setText(final String text) {
        if (getAlert() != null) {
            getAlert().setText(text);
        }

        return this;
    }

    /**
     * Sets text typeface.
     *
     * @param typeface the typeface
     * @return the text typeface
     */
    public Alerter setTextTypeface(@NonNull final Typeface typeface) {
        if (getAlert() != null) {
            getAlert().setTextTypeface(typeface);
        }

        return this;
    }

    /**
     * Sets text appearance.
     *
     * @param textAppearance the text appearance
     * @return the text appearance
     */
    public Alerter setTextAppearance(@StyleRes final int textAppearance) {
        if (getAlert() != null) {
            getAlert().setTextAppearance(textAppearance);
        }

        return this;
    }

    /**
     * Sets background color int.
     *
     * @param colorInt the color int
     * @return the background color int
     */
    public Alerter setBackgroundColorInt(@ColorInt final int colorInt) {
        if (getAlert() != null) {
            getAlert().setAlertBackgroundColor(colorInt);
        }

        return this;
    }

    /**
     * Sets progress background color intermediate.
     *
     * @param colorInt the color int
     * @return the progress background color intermediate
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Alerter setProgressBackgroundColorIntermediate(@ColorInt final int colorInt) {
        if (getAlert() != null) {
            getAlert().setProgressIntermediateColor(colorInt);
        }

        return this;
    }

    /**
     * Sets progress background color tint.
     *
     * @param colorInt the color int
     * @return the progress background color tint
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Alerter setProgressBackgroundColorTint(@ColorInt final int colorInt) {
        if (getAlert() != null) {
            getAlert().setProgressBackgroundTintColor(colorInt);
        }

        return this;
    }

    /**
     * Sets background color res.
     *
     * @param colorResId the color res id
     * @return the background color res
     */
    public Alerter setBackgroundColorRes(@ColorRes final int colorResId) {
        if (getAlert() != null && getActivityWeakReference() != null) {
            getAlert().setAlertBackgroundColor(ContextCompat.getColor(getActivityWeakReference().get(), colorResId));
        }

        return this;
    }

    /**
     * Sets background drawable.
     *
     * @param drawable the drawable
     * @return the background drawable
     */
    public Alerter setBackgroundDrawable(final Drawable drawable) {
        if (getAlert() != null) {
            getAlert().setAlertBackgroundDrawable(drawable);
        }

        return this;
    }

    /**
     * Sets background resource.
     *
     * @param drawableResId the drawable res id
     * @return the background resource
     */
    public Alerter setBackgroundResource(@DrawableRes final int drawableResId) {
        if (getAlert() != null) {
            getAlert().setAlertBackgroundResource(drawableResId);
        }

        return this;
    }

    /**
     * Sets icon.
     *
     * @param iconId the icon id
     * @return the icon
     */
    public Alerter setIcon(@DrawableRes final int iconId) {
        if (getAlert() != null) {
            getAlert().setIcon(iconId);
        }

        return this;
    }

    /**
     * Sets icon.
     *
     * @param bitmap the bitmap
     * @return the icon
     */
    public Alerter setIcon(@NonNull final Bitmap bitmap) {
        if (getAlert() != null) {
            getAlert().setIcon(bitmap);
        }

        return this;
    }

    /**
     * Sets second icon.
     *
     * @param drawable the drawable
     * @return the second icon
     */
    public Alerter setSecondIcon(@NonNull final Drawable drawable) {
        if (getAlert() != null) {
            getAlert().setSecondIcon(drawable);
        }

        return this;
    }

    /**
     * Sets second icon.
     *
     * @param iconId the icon id
     * @return the second icon
     */
    public Alerter setSecondIcon(@DrawableRes final int iconId) {
        if (getAlert() != null) {
            getAlert().setSecondIcon(iconId);
        }

        return this;
    }

    /**
     * Sets second icon.
     *
     * @param bitmap the bitmap
     * @return the second icon
     */
    public Alerter setSecondIcon(@NonNull final Bitmap bitmap) {
        if (getAlert() != null) {
            getAlert().setSecondIcon(bitmap);
        }

        return this;
    }

    /**
     * Sets icon.
     *
     * @param drawable the drawable
     * @return the icon
     */
    public Alerter setIcon(@NonNull final Drawable drawable) {
        if (getAlert() != null) {
            getAlert().setIcon(drawable);
        }

        return this;
    }

    /**
     * Hide icon alerter.
     *
     * @return the alerter
     */
    public Alerter hideIcon() {
        if (getAlert() != null) {
            getAlert().getIcon().setVisibility(View.GONE);
        }

        return this;
    }

    /**
     * Hide second icon alerter.
     *
     * @return the alerter
     */
    public Alerter hideSecondIcon() {
        if (getAlert() != null) {
            getAlert().getSecondIcon().setVisibility(View.GONE);
        }

        return this;
    }

    /**
     * Sets on click listener.
     *
     * @param onClickListener the on click listener
     * @return the on click listener
     */
    public Alerter setOnClickListener(@NonNull final View.OnClickListener onClickListener) {
        if (getAlert() != null) {
            getAlert().setOnClickListener(onClickListener);
        }

        return this;
    }

    /**
     * Sets duration.
     *
     * @param milliseconds the milliseconds
     * @return the duration
     */
    public Alerter setDuration(@NonNull final long milliseconds) {
        if (getAlert() != null) {
            getAlert().setDuration(milliseconds);
        }
        return this;
    }

    /**
     * Enable icon pulse alerter.
     *
     * @param pulse the pulse
     * @return the alerter
     */
    public Alerter enableIconPulse(final boolean pulse) {
        if (getAlert() != null) {
            getAlert().pulseIcon(pulse);
        }
        return this;
    }

    /**
     * Show icon alerter.
     *
     * @param showIcon the show icon
     * @return the alerter
     */
    public Alerter showIcon(final boolean showIcon) {
        if (getAlert() != null) {
            getAlert().showIcon(showIcon);
        }
        return this;
    }

    /**
     * Show progress bar alerter.
     *
     * @param showIcon the show icon
     * @return the alerter
     */
    public Alerter showProgressBar(final boolean showIcon) {
        if (getAlert() != null) {
            getAlert().setProgressBarEnabled(showIcon);
        }
        return this;
    }

    /**
     * Show second icon alerter.
     *
     * @param showIcon the show icon
     * @return the alerter
     */
    public Alerter showSecondIcon(final boolean showIcon) {
        if (getAlert() != null) {
            getAlert().showSecondIcon(showIcon);
        }
        return this;
    }

    /**
     * Enable infinite duration alerter.
     *
     * @param infiniteDuration the infinite duration
     * @return the alerter
     */
    public Alerter enableInfiniteDuration(final boolean infiniteDuration) {
        if (getAlert() != null) {
            getAlert().setEnableInfiniteDuration(infiniteDuration);
        }
        return this;
    }

    /**
     * Sets on show listener.
     *
     * @param listener the listener
     * @return the on show listener
     */
    public Alerter setOnShowListener(@NonNull final OnShowAlertListener listener) {
        if (getAlert() != null) {
            getAlert().setOnShowListener(listener);
        }
        return this;
    }

    /**
     * Sets on hide listener.
     *
     * @param listener the listener
     * @return the on hide listener
     */
    public Alerter setOnHideListener(@NonNull final OnHideAlertListener listener) {
        if (getAlert() != null) {
            getAlert().setOnHideListener(listener);
        }
        return this;
    }

    /**
     * Enable swipe to dismiss alerter.
     *
     * @return the alerter
     */
    public Alerter enableSwipeToDismiss() {
        if (getAlert() != null) {
            getAlert().enableSwipeToDismiss();
        }
        return this;
    }

    /**
     * Enable vibration alerter.
     *
     * @param enable the enable
     * @return the alerter
     */
    public Alerter enableVibration(final boolean enable) {
        if (getAlert() != null) {
            getAlert().setVibrationEnabled(enable);
        }

        return this;
    }

    /**
     * Disable outside touch alerter.
     *
     * @return the alerter
     */
    public Alerter disableOutsideTouch() {
        if (getAlert() != null) {
            getAlert().disableOutsideTouch();
        }

        return this;
    }

    /**
     * Enable progress alerter.
     *
     * @param enable the enable
     * @return the alerter
     */
    public Alerter enableProgress(final boolean enable) {
        if (getAlert() != null) {
            getAlert().setEnableProgress(enable);
        }

        return this;
    }

    /**
     * Show progress bar intermediate alerter.
     *
     * @param enable the enable
     * @return the alerter
     */
    public Alerter showProgressBarIntermediate(final boolean enable) {
        if (getAlert() != null) {
            getAlert().setProgressBarIntermediate(enable);
        }

        return this;
    }

    /**
     * Sets progress color res.
     *
     * @param color the color
     * @return the progress color res
     */
    public Alerter setProgressColorRes(@ColorRes final int color) {
        if (getAlert() != null) {
            getAlert().setProgressColorRes(color);
        }

        return this;
    }

    /**
     * Sets progress color int.
     *
     * @param color the color
     * @return the progress color int
     */
    public Alerter setProgressColorInt(@ColorInt final int color) {
        if (getAlert() != null) {
            getAlert().setProgressColorInt(color);
        }

        return this;
    }

    private Alert getAlert() {
        return alert;
    }

    private void setAlert(final Alert alert) {
        this.alert = alert;
    }

    @Nullable
    private WeakReference<Activity> getActivityWeakReference() {
        return activityWeakReference;
    }

    @Nullable
    private ViewGroup getActivityDecorView() {
        ViewGroup decorView = null;

        if (getActivityWeakReference() != null && getActivityWeakReference().get() != null) {
            decorView = (ViewGroup) getActivityWeakReference().get().getWindow().getDecorView();
        }

        return decorView;
    }

    private void setActivity(@NonNull final Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }
}