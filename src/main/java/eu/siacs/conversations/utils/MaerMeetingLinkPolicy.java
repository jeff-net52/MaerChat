package eu.siacs.conversations.utils;

import java.net.URI;
import java.util.regex.Pattern;

/** Prevents meeting URLs from bypassing the validated MAER-CALL invitation button. */
public final class MaerMeetingLinkPolicy {
    private static final Pattern ROOM_PATH = Pattern.compile("/MAER-[A-Za-z0-9]{16,128}");

    private MaerMeetingLinkPolicy() {}

    public static boolean mustSuppressRawLink(final String value) {
        try {
            final URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && MaerCallInvite.ALLOWED_MEETING_HOST.equalsIgnoreCase(uri.getHost())
                    && ROOM_PATH.matcher(uri.getPath()).matches();
        } catch (final RuntimeException e) {
            return false;
        }
    }
}
