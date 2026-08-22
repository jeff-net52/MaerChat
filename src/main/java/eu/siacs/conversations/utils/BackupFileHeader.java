package eu.siacs.conversations.utils;

import androidx.annotation.NonNull;
import eu.siacs.conversations.xmpp.Jid;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;

public class BackupFileHeader {

    public static final int LEGACY_VERSION = 2;
    public static final int CURRENT_VERSION = 3;

    private final int version;
    private final String app;
    private final Jid jid;
    private final long timestamp;
    private final byte[] iv;
    private final byte[] salt;

    @NonNull
    @Override
    public String toString() {
        return "BackupFileHeader{version="
                + version
                + ", app='"
                + app
                + '\''
                + ", timestamp="
                + timestamp
                + '}';
    }

    public BackupFileHeader(String app, Jid jid, long timestamp, byte[] iv, byte[] salt) {
        this(CURRENT_VERSION, app, jid, timestamp, iv, salt);
    }

    private BackupFileHeader(
            int version, String app, Jid jid, long timestamp, byte[] iv, byte[] salt) {
        this.version = version;
        this.app = app;
        this.jid = jid;
        this.timestamp = timestamp;
        this.iv = iv;
        this.salt = salt;
    }

    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(version);
        dataOutputStream.writeUTF(app);
        dataOutputStream.writeUTF(jid.asBareJid().toString());
        dataOutputStream.writeLong(timestamp);
        dataOutputStream.write(iv);
        dataOutputStream.write(salt);
    }

    public static BackupFileHeader read(DataInputStream inputStream) throws IOException {
        final int version = inputStream.readInt();
        final String app = inputStream.readUTF();
        final String jid = inputStream.readUTF();
        long timestamp = inputStream.readLong();
        final byte[] iv = new byte[12];
        inputStream.readFully(iv);
        final byte[] salt = new byte[16];
        inputStream.readFully(salt);
        if (version < LEGACY_VERSION) {
            throw new OutdatedBackupFileVersion();
        }
        if (version > CURRENT_VERSION) {
            throw new IllegalArgumentException(
                    "Backup File version was "
                            + version
                            + " but app only supports version "
                            + CURRENT_VERSION);
        }
        return new BackupFileHeader(version, app, Jid.of(jid), timestamp, iv, salt);
    }

    public int getVersion() {
        return version;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getIv() {
        return iv;
    }

    public Jid getJid() {
        return jid;
    }

    public String getApp() {
        return app;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(this.timestamp);
    }

    public static class OutdatedBackupFileVersion extends RuntimeException {}
}
