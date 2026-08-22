package eu.siacs.conversations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.res.XmlResourceParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ConscryptMode;
import org.xmlpull.v1.XmlPullParser;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class NetworkSecurityConfigurationTest {

    @Test
    public void cleartextIsDisabledAndOnlySystemCertificateAuthoritiesAreTrusted()
            throws Exception {
        final List<String> certificateSources = new ArrayList<>();
        boolean cleartextPermitted = true;
        try (final XmlResourceParser parser =
                RuntimeEnvironment.getApplication()
                        .getResources()
                        .getXml(R.xml.network_security_configuration)) {
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (event != XmlPullParser.START_TAG) {
                    continue;
                }
                if ("base-config".equals(parser.getName())) {
                    cleartextPermitted =
                            parser.getAttributeBooleanValue(
                                    null, "cleartextTrafficPermitted", true);
                } else if ("certificates".equals(parser.getName())) {
                    certificateSources.add(parser.getAttributeValue(null, "src"));
                }
            }
        }

        assertFalse(cleartextPermitted);
        assertEquals(List.of("system"), certificateSources);
    }
}
