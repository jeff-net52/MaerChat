package eu.siacs.conversations.organizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import androidx.preference.PreferenceManager;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class OrganizationCatalogTest {

    @Test
    public void bundledCatalogueLoadsAndMapsTheDefaultOrganization() {
        final OrganizationCatalog catalog =
                OrganizationCatalog.load(RuntimeEnvironment.getApplication());

        assertEquals("maer-engineering", catalog.defaultOrganization().id());
        assertEquals("contacts.chaumont.me", catalog.defaultOrganization().xmppDomain());
        assertEquals(
                catalog.defaultOrganization(), catalog.findByDomain("CONTACTS.CHAUMONT.ME"));
    }

    @Test
    public void selectionPersistsOnlyAKnownPublicOrganizationId() throws Exception {
        final Context context = RuntimeEnvironment.getApplication();
        final OrganizationCatalog catalog = parse(twoOrganizationCatalogue());
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();

        assertEquals("maer-engineering", OrganizationSelection.selected(context, catalog).id());
        assertEquals(
                "client-demo",
                OrganizationSelection.select(context, catalog, "client-demo").id());
        assertEquals("client-demo", OrganizationSelection.selected(context, catalog).id());
        assertEquals(
                "client-demo",
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(OrganizationSelection.PREFERENCE_KEY, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> OrganizationSelection.select(context, catalog, "missing"));

        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    }

    @Test
    public void mapsPublicOrganizationToXmppVirtualHost() throws Exception {
        final OrganizationCatalog catalog = parse(twoOrganizationCatalogue());

        assertEquals(2, catalog.organizations().size());
        assertEquals("contacts.chaumont.me", catalog.defaultOrganization().xmppDomain());
        assertEquals("chat.client.example", catalog.findById("client-demo").xmppDomain());
        assertEquals("client-demo", catalog.findByDomain("CHAT.CLIENT.EXAMPLE").id());
        assertNull(catalog.findById("missing"));
        assertNull(catalog.findByDomain("not a domain"));
    }

    @Test
    public void rejectsSecretOrUndocumentedFields() {
        final IOException exception =
                assertThrows(
                        IOException.class,
                        () ->
                                parse(
                                        """
                                        {
                                          "schema_version": 1,
                                          "organizations": [
                                            {
                                              "id": "unsafe",
                                              "display_name": "Unsafe",
                                              "xmpp_domain": "chat.example.org",
                                              "default": true,
                                              "password": "must-never-be-here"
                                            }
                                          ]
                                        }
                                        """));

        assertNotNull(exception.getMessage());
    }

    @Test
    public void rejectsDuplicateDomains() {
        assertThrows(
                IOException.class,
                () ->
                        parse(
                                """
                                {
                                  "schema_version": 1,
                                  "organizations": [
                                    {
                                      "id": "first",
                                      "display_name": "First",
                                      "xmpp_domain": "chat.example.org",
                                      "default": true
                                    },
                                    {
                                      "id": "second",
                                      "display_name": "Second",
                                      "xmpp_domain": "chat.example.org",
                                      "default": false
                                    }
                                  ]
                                }
                                """));
    }

    @Test
    public void requiresExactlyOneDefault() {
        assertThrows(
                IOException.class,
                () ->
                        parse(
                                """
                                {
                                  "schema_version": 1,
                                  "organizations": [
                                    {
                                      "id": "only",
                                      "display_name": "Only",
                                      "xmpp_domain": "chat.example.org",
                                      "default": false
                                    }
                                  ]
                                }
                                """));
    }

    private static OrganizationCatalog parse(final String value) throws IOException {
        return OrganizationCatalog.parse(new StringReader(value));
    }

    private static String twoOrganizationCatalogue() {
        return """
                {
                  "schema_version": 1,
                  "organizations": [
                    {
                      "id": "maer-engineering",
                      "display_name": "MAER Engineering",
                      "xmpp_domain": "contacts.chaumont.me",
                      "default": true
                    },
                    {
                      "id": "client-demo",
                      "display_name": "Client Démo",
                      "xmpp_domain": "chat.client.example",
                      "default": false
                    }
                  ]
                }
                """;
    }
}
