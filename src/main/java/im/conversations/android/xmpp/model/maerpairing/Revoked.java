package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Revoked extends Extension {

    public Revoked() {
        super(Revoked.class);
    }

    public String getDeviceId() {
        return getAttribute("device-id");
    }
}
