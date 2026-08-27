package eu.siacs.conversations.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

/**
 * A {@link LinearLayout} that stays wrap-content while capping long content at 82 percent of a
 * stable ancestor width.
 */
public class MaxWidthLinearLayout extends LinearLayout {

    private static final float MAX_WIDTH_FRACTION = 0.82f;

    public MaxWidthLinearLayout(final Context context) {
        super(context);
    }

    public MaxWidthLinearLayout(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxWidthLinearLayout(
            final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
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
        if (widthMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int availableWidth = findMeasuredAncestorWidth();
        if (availableWidth > 0) {
            final int maximumWidth = Math.round(availableWidth * MAX_WIDTH_FRACTION);
            final int constrainedWidth =
                    widthMode == MeasureSpec.AT_MOST
                            ? Math.min(MeasureSpec.getSize(widthMeasureSpec), maximumWidth)
                            : maximumWidth;
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(constrainedWidth, MeasureSpec.AT_MOST),
                    heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int findMeasuredAncestorWidth() {
        ViewParent ancestor = getParent();
        while (ancestor instanceof View) {
            final View ancestorView = (View) ancestor;
            final int width = Math.max(ancestorView.getWidth(), ancestorView.getMeasuredWidth());
            if (width > 0) {
                return Math.max(
                        0, width - ancestorView.getPaddingStart() - ancestorView.getPaddingEnd());
            }
            ancestor = ancestorView.getParent();
        }
        return 0;
    }
}
