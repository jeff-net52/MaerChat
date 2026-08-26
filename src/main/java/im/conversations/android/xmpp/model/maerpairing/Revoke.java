package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Revoke extends Extension {

    public Revoke() {
        super(Revoke.class);
    }

    public void setDeviceId(final String deviceId) {
        setAttribute("device-id", deviceId);
    }
}
