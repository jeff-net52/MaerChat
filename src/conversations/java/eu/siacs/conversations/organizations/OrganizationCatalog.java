package eu.siacs.conversations.organizations;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import eu.siacs.conversations.R;
import eu.siacs.conversations.xmpp.Jid;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Public catalogue mapping a customer-facing organization to an XMPP virtual host.
 *
 * <p>This catalogue is discovery metadata, not an authorization boundary. The server must still
 * authenticate the account against the selected virtual host and enforce the customer's service
 * entitlement. Its deliberately strict schema prevents credentials or deployment secrets from being
 * added accidentally.
 */
public final class OrganizationCatalog {

    public static final int SCHEMA_VERSION = 1;

    private static final Pattern ORGANIZATION_ID = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,62})");

    private final List<Organization> organizations;
    private final Organization defaultOrganization;

    private OrganizationCatalog(final List<Organization> organizations) throws IOException {
        if (organizations.isEmpty()) {
            throw new IOException("The organization catalogue is empty");
        }
        final Set<String> ids = new HashSet<>();
        final Set<String> domains = new HashSet<>();
        Organization selectedDefault = null;
        for (final Organization organization : organizations) {
            if (!ids.add(organization.id())) {
                throw new IOException("Duplicate organization id: " + organization.id());
            }
            if (!domains.add(organization.xmppDomain())) {
                throw new IOException(
                        "Duplicate organization XMPP domain: " + organization.xmppDomain());
            }
            if (organization.isDefault()) {
                if (selectedDefault != null) {
                    throw new IOException(
                            "The catalogue declares more than one default organization");
                }
                selectedDefault = organization;
            }
        }
        if (selectedDefault == null) {
            throw new IOException("The catalogue has no default organization");
        }
        this.organizations = List.copyOf(organizations);
        this.defaultOrganization = selectedDefault;
    }

    @NonNull
    public static OrganizationCatalog load(@NonNull final Context context) {
        try (final Reader reader =
                new InputStreamReader(
                        context.getResources().openRawResource(R.raw.maer_organizations),
                        StandardCharsets.UTF_8)) {
            return parse(reader);
        } catch (final IOException | RuntimeException e) {
            throw new IllegalStateException("Unable to load the MAER organization catalogue", e);
        }
    }

    @NonNull
    public static OrganizationCatalog parse(@NonNull final Reader source) throws IOException {
        final JsonReader reader = new JsonReader(source);
        reader.setLenient(false);
        Integer schemaVersion = null;
        List<Organization> organizations = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String field = reader.nextName();
            switch (field) {
                case "schema_version" -> {
                    requireUnset(schemaVersion, field);
                    schemaVersion = reader.nextInt();
                }
                case "organizations" -> {
                    if (organizations != null) {
                        throw new IOException("Duplicate catalogue field: organizations");
                    }
                    organizations = readOrganizations(reader);
                }
                default -> throw new IOException("Unsupported catalogue field: " + field);
            }
        }
        reader.endObject();
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            throw new IOException("Unexpected content after the organization catalogue");
        }
        if (schemaVersion == null || schemaVersion != SCHEMA_VERSION) {
            throw new IOException("Unsupported organization catalogue schema: " + schemaVersion);
        }
        if (organizations == null) {
            throw new IOException("The catalogue has no organizations field");
        }
        return new OrganizationCatalog(organizations);
    }

    @NonNull
    public List<Organization> organizations() {
        return organizations;
    }

    @NonNull
    public Organization defaultOrganization() {
        return defaultOrganization;
    }

    @Nullable
    public Organization findById(@Nullable final String id) {
        if (id == null) {
            return null;
        }
        for (final Organization organization : organizations) {
            if (organization.id().equals(id)) {
                return organization;
            }
        }
        return null;
    }

    @Nullable
    public Organization findByDomain(@Nullable final String domain) {
        if (domain == null) {
            return null;
        }
        final String normalizedDomain;
        try {
            normalizedDomain = normalizeDomain(domain);
        } catch (final IOException e) {
            return null;
        }
        for (final Organization organization : organizations) {
            if (organization.xmppDomain().equals(normalizedDomain)) {
                return organization;
            }
        }
        return null;
    }

    private static List<Organization> readOrganizations(final JsonReader reader)
            throws IOException {
        final List<Organization> result = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            result.add(readOrganization(reader));
        }
        reader.endArray();
        return result;
    }

    private static Organization readOrganization(final JsonReader reader) throws IOException {
        String id = null;
        String displayName = null;
        String xmppDomain = null;
        Boolean isDefault = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String field = reader.nextName();
            switch (field) {
                case "id" -> {
                    requireUnset(id, field);
                    id = reader.nextString();
                }
                case "display_name" -> {
                    requireUnset(displayName, field);
                    displayName = reader.nextString();
                }
                case "xmpp_domain" -> {
                    requireUnset(xmppDomain, field);
                    xmppDomain = reader.nextString();
                }
                case "default" -> {
                    requireUnset(isDefault, field);
                    isDefault = reader.nextBoolean();
                }
                default ->
                        throw new IOException(
                                "Unsupported organization field (catalogues must not contain"
                                        + " secrets): "
                                        + field);
            }
        }
        reader.endObject();

        final String normalizedId = requireText(id, "organization id").toLowerCase(Locale.ROOT);
        if (!ORGANIZATION_ID.matcher(normalizedId).matches() || !normalizedId.equals(id)) {
            throw new IOException("Invalid organization id: " + id);
        }
        final String normalizedName = requireText(displayName, "organization display name");
        final String normalizedDomain = normalizeDomain(requireText(xmppDomain, "XMPP domain"));
        if (isDefault == null) {
            throw new IOException("Missing organization field: default");
        }
        return new Organization(normalizedId, normalizedName, normalizedDomain, isDefault);
    }

    private static String normalizeDomain(final String value) throws IOException {
        try {
            final Jid domain = Jid.ofUserInput(value.trim());
            if (!domain.isDomainJid() || !domain.isBareJid() || domain.isFullJid()) {
                throw new IOException("The organization XMPP domain is invalid: " + value);
            }
            return domain.toString();
        } catch (final IllegalArgumentException e) {
            throw new IOException("The organization XMPP domain is invalid: " + value, e);
        }
    }

    private static String requireText(@Nullable final String value, final String label)
            throws IOException {
        if (value == null || value.isBlank()) {
            throw new IOException("Missing " + label);
        }
        return value.trim();
    }

    private static void requireUnset(@Nullable final Object value, final String field)
            throws IOException {
        if (value != null) {
            throw new IOException("Duplicate catalogue field: " + field);
        }
    }

    /** Immutable, non-secret organization metadata suitable for direct use in a UI adapter. */
    public static final class Organization {

        private final String id;
        private final String displayName;
        private final String xmppDomain;
        private final boolean isDefault;

        private Organization(
                final String id,
                final String displayName,
                final String xmppDomain,
                final boolean isDefault) {
            this.id = id;
            this.displayName = displayName;
            this.xmppDomain = xmppDomain;
            this.isDefault = isDefault;
        }

        @NonNull
        public String id() {
            return id;
        }

        @NonNull
        public String displayName() {
            return displayName;
        }

        @NonNull
        public String xmppDomain() {
            return xmppDomain;
        }

        public boolean isDefault() {
            return isDefault;
        }

        @Override
        @NonNull
        public String toString() {
            return displayName;
        }
    }
}
