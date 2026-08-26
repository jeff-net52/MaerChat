package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;
import java.time.Instant;

@XmlElement
public class Device extends Extension {

    public Device() {
        super(Device.class);
    }

    public String getId() {
        return getAttribute("id");
    }

    public String getLabel() {
        return getAttribute("label");
    }

    public String getPlatform() {
        return getAttribute("platform");
    }

    public Instant getCreatedAt() {
        return getAttributeAsInstant("created");
    }

    public Instant getLastSeenAt() {
        return getAttributeAsInstant("last-seen");
    }

    public String getLastSeenValue() {
        return getAttribute("last-seen");
    }

    public Instant getExpiresAt() {
        return getAttributeAsInstant("expires");
    }
}
