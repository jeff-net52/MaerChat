package eu.siacs.conversations.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class MaerPairingUri {

    public static final String CANONICAL_HOST = "xmpp.maer.fr";

    private static final int MAX_URI_LENGTH = 2048;
    private static final Pattern SESSION_ID = Pattern.compile("[A-Za-z0-9_-]{16,128}");
    private static final Pattern VERIFICATION_CODE = Pattern.compile("[0-9]{6}");

    private final String host;
    private final String sessionId;
    private final String verificationCode;

    private MaerPairingUri(
            final String host, final String sessionId, final String verificationCode) {
        this.host = host;
        this.sessionId = sessionId;
        this.verificationCode = verificationCode;
    }

    public static MaerPairingUri parse(final String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_URI_LENGTH) {
            throw invalidUri();
        }
        for (int i = 0; i < value.length(); i++) {
            final char character = value.charAt(i);
            if (character <= 0x20 || character >= 0x7f) {
                throw invalidUri();
            }
        }

        final URI uri;
        try {
            uri = new URI(value);
        } catch (final URISyntaxException e) {
            throw invalidUri();
        }
        if (!"maerchat".equals(uri.getScheme())
                || !"pair".equals(uri.getRawAuthority())
                || uri.getRawUserInfo() != null
                || uri.getPort() != -1
                || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
                || uri.getRawFragment() != null) {
            throw invalidUri();
        }

        final String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            throw invalidUri();
        }
        final Map<String, String> parameters = new HashMap<>();
        for (final String parameter : rawQuery.split("&", -1)) {
            final int separator = parameter.indexOf('=');
            if (separator <= 0
                    || separator != parameter.lastIndexOf('=')
                    || separator == parameter.length() - 1
                    || parameter.indexOf('%') >= 0
                    || parameter.indexOf('+') >= 0) {
                throw invalidUri();
            }
            final String name = parameter.substring(0, separator);
            final String parameterValue = parameter.substring(separator + 1);
            if (!isKnownParameter(name) || parameters.put(name, parameterValue) != null) {
                throw invalidUri();
            }
        }

        if (parameters.size() != 4
                || !"1".equals(parameters.get("v"))
                || !CANONICAL_HOST.equals(parameters.get("host"))) {
            throw invalidUri();
        }
        final String sessionId = parameters.get("sid");
        final String verificationCode = parameters.get("code");
        if (sessionId == null
                || !SESSION_ID.matcher(sessionId).matches()
                || verificationCode == null
                || !VERIFICATION_CODE.matcher(verificationCode).matches()) {
            throw invalidUri();
        }
        return new MaerPairingUri(CANONICAL_HOST, sessionId, verificationCode);
    }

    private static boolean isKnownParameter(final String name) {
        return "v".equals(name) || "host".equals(name) || "sid".equals(name) || "code".equals(name);
    }

    private static IllegalArgumentException invalidUri() {
        return new IllegalArgumentException("Invalid MAER pairing URI");
    }

    public String getHost() {
        return host;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getVerificationCode() {
        return verificationCode;
    }
}
