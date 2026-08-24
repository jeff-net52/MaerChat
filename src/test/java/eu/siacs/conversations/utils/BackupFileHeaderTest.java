package eu.siacs.conversations.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import eu.siacs.conversations.xmpp.Jid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.junit.Test;

public class BackupFileHeaderTest {

    @Test
    public void currentHeaderRoundTripsWithoutExposingSensitiveMetadataInToString()
            throws Exception {
        final var header =
                new BackupFileHeader(
                        "Maer Chat",
                        Jid.of("alice@xmpp.maer.fr"),
                        42L,
                        new byte[12],
                        new byte[16]);
        final var output = new ByteArrayOutputStream();
        header.write(new DataOutputStream(output));

        final var restored =
                BackupFileHeader.read(
                        new DataInputStream(new ByteArrayInputStream(output.toByteArray())));

        assertEquals(BackupFileHeader.CURRENT_VERSION, restored.getVersion());
        assertEquals(Jid.of("alice@xmpp.maer.fr"), restored.getJid());
        assertFalse(restored.toString().contains("alice@"));
        assertFalse(restored.toString().contains("salt"));
        assertFalse(restored.toString().contains("iv="));
    }
}
