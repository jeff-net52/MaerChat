package eu.siacs.conversations;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

/** Repository-level guards against accidentally committing credentials to production code. */
public class CredentialLeakageGuardTest {

    private static final Pattern PERSONAL_MAER_JID =
            Pattern.compile(
                    "[\\p{L}\\p{N}.!#$%&'*+/=?^_`{|}~-]+@contacts\\.chaumont\\.me",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern DIAGNOSTIC_CALL =
            Pattern.compile("(?:Log\\s*\\.\\s*(?:wtf|[vdiew])|quickLog)\\s*\\(");
    private static final Pattern AUTH_SECRET_REFERENCE =
            Pattern.compile(
                    "\\b(?:getPassword|getFastToken|password|passphrase|fastToken)\\b",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern STANDARD_STREAM_WRITE =
            Pattern.compile("\\bSystem\\s*\\.\\s*(?:out|err)\\s*\\.");
    private static final List<SensitiveCallGuard> SENSITIVE_CALL_GUARDS =
            List.of(
                    guard(
                            "src/main/java/eu/siacs/conversations/xmpp/jingle/WebRTCWrapper.java",
                            "\\bevent\\s*\\.\\s*(?:remote|local)\\b|"
                                    + "\\bsessionDescription\\s*\\.\\s*description\\b|\\bline\\b"),
                    guard(
                            "src/main/java/im/conversations/android/xmpp/model/disco/external/Services.java",
                            "\\biceServer\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/xmpp/jingle/JingleRtpConnection.java",
                            "\\b(?:newCredentials|iceCandidate|iq)\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/xmpp/jingle/transports/"
                                    + "WebRTCDataChannelTransport.java",
                            "\\bevent\\s*\\.\\s*(?:remote|local)\\b|"
                                    + "\\bsessionDescription\\s*\\.\\s*description\\b|\\bline\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/xmpp/manager/MultiUserChatManager.java",
                            "\\binvite\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/http/HttpUploadConnection.java",
                            "\\bslot\\s*\\.\\s*(?:put|get)\\b|\\btransportSecurity\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/services/XmppConnectionService.java",
                            "\\burl\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/services/UnifiedPushBroker.java",
                            "\\b(?:renewal|pushTarget|endpoint|instance)\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/receiver/UnifiedPushDistributor.java",
                            "\\b(?:instance|features)\\b"),
                    guard(
                            "src/main/java/eu/siacs/conversations/services/CallIntegration.java",
                            "\\breplyMessage\\b"),
                    guard(
                            "src/main/java/im/conversations/android/xmpp/StreamElementWriter.java",
                            "\\btext\\b"),
                    guard(
                            "src/main/java/im/conversations/android/xmpp/model/data/Data.java",
                            "\\b(?:collection|subValue|value|name)\\b"),
                    guard(
                            "src/playstore/java/eu/siacs/conversations/services/PushManagementService.java",
                            "\\bfcmToken\\b"),
                    guard(
                            "src/playstore/java/eu/siacs/conversations/receiver/PushMessageReceiver.java",
                            "\\bregistrationId\\b"));

    @Test
    public void productionSourcesDoNotContainAPersonalMaerAccount() throws Exception {
        final List<String> violations = new ArrayList<>();
        for (final Path path : productionTextFiles()) {
            final Matcher matcher = PERSONAL_MAER_JID.matcher(readSource(path));
            if (matcher.find()) {
                violations.add(relative(path));
            }
        }

        assertTrue(
                "Personal Maer JID found in production files: " + violations, violations.isEmpty());
    }

    @Test
    public void logCallsDoNotDirectlyInterpolateAuthenticationSecrets() throws Exception {
        final List<String> violations = new ArrayList<>();
        for (final Path path : javaSources()) {
            final String maskedSource = maskCommentsAndLiterals(readSource(path));
            for (final String invocation : invocations(maskedSource, DIAGNOSTIC_CALL)) {
                if (AUTH_SECRET_REFERENCE.matcher(invocation).find()) {
                    violations.add(relative(path));
                    break;
                }
            }
        }

        assertTrue(
                "Authentication secret referenced directly from a diagnostic call: " + violations,
                violations.isEmpty());
    }

    @Test
    public void targetedOperationalLogsDoNotExposeSensitivePayloads() throws Exception {
        final List<String> violations = new ArrayList<>();
        for (final SensitiveCallGuard guard : SENSITIVE_CALL_GUARDS) {
            final Path path = projectRoot().resolve(guard.relativePath());
            final String maskedSource = maskCommentsAndLiterals(readSource(path));
            for (final String invocation : invocations(maskedSource, DIAGNOSTIC_CALL)) {
                if (guard.forbiddenReference().matcher(invocation).find()) {
                    violations.add(guard.relativePath());
                    break;
                }
            }
        }

        assertTrue(
                "Sensitive media, upload, or push value referenced from a diagnostic call: "
                        + violations,
                violations.isEmpty());
    }

    @Test
    public void productionSourcesDoNotWriteValuesToStandardStreams() throws Exception {
        final List<String> violations = new ArrayList<>();
        for (final Path path : javaSources()) {
            final String maskedSource = maskCommentsAndLiterals(readSource(path));
            if (STANDARD_STREAM_WRITE.matcher(maskedSource).find()) {
                violations.add(relative(path));
            }
        }

        assertTrue(
                "Production code writes values to stdout or stderr: " + violations,
                violations.isEmpty());
    }

    @Test
    public void sensitiveValueObjectsRedactTheirStringRepresentation() {
        final String iceUfrag = "maer-test-ice-ufrag";
        final String icePassword = "maer-test-ice-password";
        final var credentials =
                new eu.siacs.conversations.xmpp.jingle.stanzas.IceUdpTransportInfo.Credentials(
                        iceUfrag, icePassword);
        final String renderedCredentials = credentials.toString();
        assertTrue(!renderedCredentials.contains(iceUfrag));
        assertTrue(!renderedCredentials.contains(icePassword));

        final String invitePassword = "maer-test-invite-password";
        final var invite =
                new eu.siacs.conversations.xmpp.manager.MultiUserChatManager.Invite(
                        eu.siacs.conversations.xmpp.Jid.of("room@example.invalid"),
                        eu.siacs.conversations.xmpp.Jid.of("inviter@example.invalid"),
                        invitePassword);
        final String renderedInvite = invite.toString();
        assertTrue(!renderedInvite.contains("room@example.invalid"));
        assertTrue(!renderedInvite.contains("inviter@example.invalid"));
        assertTrue(!renderedInvite.contains(invitePassword));
    }

    @Test
    public void releaseOptimizerRulesStripLoggingCalls() throws Exception {
        final String rules = readSource(projectRoot().resolve("proguard-rules.pro"));
        assertTrue(rules.contains("-assumenosideeffects class android.util.Log"));
        assertTrue(rules.contains("public static int v(...);"));
        assertTrue(rules.contains("public static int d(...);"));
        assertTrue(rules.contains("public static int i(...);"));
        assertTrue(rules.contains("public static int w(...);"));
        assertTrue(rules.contains("public static int e(...);"));
        assertTrue(rules.contains("public static int wtf(...);"));
        assertTrue(rules.contains("public static int println(...);"));
        assertTrue(rules.contains("-assumenosideeffects class java.util.logging.Logger"));
    }

    private static String readSource(final Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static List<Path> javaSources() throws IOException {
        final Path sourceRoot = projectRoot().resolve("src");
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !isTestSource(sourceRoot, path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }
    }

    private static List<Path> productionTextFiles() throws IOException {
        final Path sourceRoot = projectRoot().resolve("src");
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !isTestSource(sourceRoot, path))
                    .filter(CredentialLeakageGuardTest::isTextFile)
                    .toList();
        }
    }

    private static boolean isTestSource(final Path sourceRoot, final Path path) {
        final Path relative = sourceRoot.relativize(path);
        if (relative.getNameCount() == 0) {
            return false;
        }
        final String sourceSet = relative.getName(0).toString();
        return "test".equals(sourceSet) || "androidTest".equals(sourceSet);
    }

    private static boolean isTextFile(final Path path) {
        final String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".java")
                || name.endsWith(".xml")
                || name.endsWith(".json")
                || name.endsWith(".properties")
                || name.endsWith(".html")
                || name.endsWith(".txt");
    }

    private static List<String> invocations(final String source, final Pattern callPattern) {
        final List<String> invocations = new ArrayList<>();
        final Matcher matcher = callPattern.matcher(source);
        while (matcher.find()) {
            int depth = 1;
            int index = matcher.end();
            while (index < source.length() && depth > 0) {
                final char current = source.charAt(index);
                if (current == '(') {
                    depth++;
                } else if (current == ')') {
                    depth--;
                }
                index++;
            }
            if (depth == 0) {
                invocations.add(source.substring(matcher.start(), index));
            }
        }
        return invocations;
    }

    private static SensitiveCallGuard guard(
            final String relativePath, final String forbiddenReference) {
        return new SensitiveCallGuard(
                relativePath, Pattern.compile(forbiddenReference, Pattern.CASE_INSENSITIVE));
    }

    private static String maskCommentsAndLiterals(final String source) {
        final StringBuilder masked = new StringBuilder(source);
        boolean lineComment = false;
        boolean blockComment = false;
        boolean stringLiteral = false;
        boolean characterLiteral = false;
        boolean escaped = false;
        for (int i = 0; i < source.length(); i++) {
            final char current = source.charAt(i);
            final char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';

            if (lineComment) {
                if (current == '\n') {
                    lineComment = false;
                } else {
                    masked.setCharAt(i, ' ');
                }
                continue;
            }
            if (blockComment) {
                masked.setCharAt(i, current == '\n' ? '\n' : ' ');
                if (current == '*' && next == '/') {
                    masked.setCharAt(i + 1, ' ');
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (stringLiteral || characterLiteral) {
                masked.setCharAt(i, current == '\n' ? '\n' : ' ');
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if ((stringLiteral && current == '"')
                        || (characterLiteral && current == '\'')) {
                    stringLiteral = false;
                    characterLiteral = false;
                }
                continue;
            }
            if (current == '/' && next == '/') {
                masked.setCharAt(i, ' ');
                masked.setCharAt(i + 1, ' ');
                lineComment = true;
                i++;
            } else if (current == '/' && next == '*') {
                masked.setCharAt(i, ' ');
                masked.setCharAt(i + 1, ' ');
                blockComment = true;
                i++;
            } else if (current == '"') {
                masked.setCharAt(i, ' ');
                stringLiteral = true;
            } else if (current == '\'') {
                masked.setCharAt(i, ' ');
                characterLiteral = true;
            }
        }
        return masked.toString();
    }

    private static String relative(final Path path) {
        return projectRoot().relativize(path).toString();
    }

    private static Path projectRoot() {
        Path candidate = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath();
        while (candidate != null) {
            if (Files.isDirectory(candidate.resolve("src/main/java"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new AssertionError("Unable to locate project root");
    }

    private record SensitiveCallGuard(String relativePath, Pattern forbiddenReference) {}
}
