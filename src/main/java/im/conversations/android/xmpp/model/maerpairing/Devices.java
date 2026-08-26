package im.conversations.android.xmpp.model.maerpairing;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;
import java.util.Collection;

@XmlElement
public class Devices extends Extension {

    public Devices() {
        super(Devices.class);
    }

    public Collection<Device> getDevices() {
        return getExtensions(Device.class);
    }
}
