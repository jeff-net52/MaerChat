package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Inspect extends Extension {

    public Inspect() {
        super(Inspect.class);
    }

    public void setSession(final String session) {
        setAttribute("session", session);
    }

    public void setCode(final String code) {
        setAttribute("code", code);
    }
}
