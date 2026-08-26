package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;
import java.time.Instant;

@XmlElement
public class Session extends Extension {

    public Session() {
        super(Session.class);
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

    public Instant getExpiresAt() {
        return getAttributeAsInstant("expires");
    }
}
