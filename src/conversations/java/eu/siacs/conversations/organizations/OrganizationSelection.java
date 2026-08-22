package eu.siacs.conversations.organizations;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import eu.siacs.conversations.organizations.OrganizationCatalog.Organization;

/** Persists only the public catalogue id of the selected organization. */
public final class OrganizationSelection {

    public static final String PREFERENCE_KEY = "maer_selected_organization_v1";

    private OrganizationSelection() {}

    @NonNull
    public static Organization selected(
            @NonNull final Context context, @NonNull final OrganizationCatalog catalog) {
        final String storedId =
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(PREFERENCE_KEY, null);
        final Organization stored = catalog.findById(storedId);
        return stored == null ? catalog.defaultOrganization() : stored;
    }

    @NonNull
    public static Organization select(
            @NonNull final Context context,
            @NonNull final OrganizationCatalog catalog,
            @NonNull final String organizationId) {
        final Organization organization = catalog.findById(organizationId);
        if (organization == null) {
            throw new IllegalArgumentException("Unknown organization id: " + organizationId);
        }
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREFERENCE_KEY, organization.id())
                .apply();
        return organization;
    }

    @Nullable
    public static Organization findForAccountDomain(
            @NonNull final OrganizationCatalog catalog, @Nullable final String accountDomain) {
        return catalog.findByDomain(accountDomain);
    }
}
