package eu.siacs.conversations.ui.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class MaxWidthLinearLayoutTest {

    @Test
    public void longContentIsCappedAtEightyTwoPercent() {
        final var layout = new MaxWidthLinearLayout(RuntimeEnvironment.getApplication());
        final var textView = new TextView(RuntimeEnvironment.getApplication());
        textView.setMinWidth(1000);
        layout.addView(textView);
        attachToMeasuredParent(layout, 1000);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));

        assertEquals(820, layout.getMeasuredWidth());
    }

    @Test
    public void repeatedAtMostMeasureDoesNotCollapseShortContent() {
        final var layout = new MaxWidthLinearLayout(RuntimeEnvironment.getApplication());
        final var textView = new TextView(RuntimeEnvironment.getApplication());
        textView.setText("Hermes?");
        layout.addView(textView);
        attachToMeasuredParent(layout, 1000);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
        final int initialWidth = layout.getMeasuredWidth();

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(initialWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));

        assertTrue(initialWidth > 0);
        assertEquals(initialWidth, layout.getMeasuredWidth());
    }

    @Test
    public void exactWidthMeasureSpecIsHonored() {
        final var layout = new MaxWidthLinearLayout(RuntimeEnvironment.getApplication());

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));

        assertEquals(300, layout.getMeasuredWidth());
    }

    private static void attachToMeasuredParent(final MaxWidthLinearLayout layout, final int width) {
        final var parent = new FrameLayout(RuntimeEnvironment.getApplication());
        parent.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
        parent.addView(layout);
    }
}
