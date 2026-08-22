package eu.siacs.conversations.ui.widget;

import static org.junit.Assert.assertTrue;

import android.view.View;
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
        textView.setText("A deliberately long message that would otherwise fill the whole row");
        layout.addView(textView);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));

        assertTrue(layout.getMeasuredWidth() > 0);
        assertTrue(layout.getMeasuredWidth() <= 820);
    }
}
