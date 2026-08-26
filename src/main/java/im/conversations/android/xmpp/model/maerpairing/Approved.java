package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Approved extends Extension {

    public Approved() {
        super(Approved.class);
    }

    public String getDeviceId() {
        return getAttribute("device-id");
    }
}
