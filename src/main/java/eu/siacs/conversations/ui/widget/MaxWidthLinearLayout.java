package eu.siacs.conversations.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * A {@link LinearLayout} that stays wrap-content while capping long content at 82 percent of
 * the width offered by its parent.
 */
public class MaxWidthLinearLayout extends LinearLayout {

    private static final float MAX_WIDTH_FRACTION = 0.82f;

    public MaxWidthLinearLayout(final Context context) {
        super(context);
    }

    public MaxWidthLinearLayout(
            final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxWidthLinearLayout(
            final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaxWidthLinearLayout(
            final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr,
            final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int availableWidth = MeasureSpec.getSize(widthMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED && getParent() instanceof View) {
            final View parent = (View) getParent();
            availableWidth =
                    Math.max(
                            0,
                            parent.getMeasuredWidth()
                                    - parent.getPaddingStart()
                                    - parent.getPaddingEnd());
        }

        if (availableWidth > 0) {
            final int maximumWidth = Math.round(availableWidth * MAX_WIDTH_FRACTION);
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(maximumWidth, MeasureSpec.AT_MOST),
                    heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
