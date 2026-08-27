package eu.siacs.conversations;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppSettingsAppearanceTest {

    @Test
    public void avatarDisplayParsesEverySupportedMode() {
        assertEquals(AppSettings.AvatarDisplay.ALWAYS, AppSettings.AvatarDisplay.of("always"));
        assertEquals(AppSettings.AvatarDisplay.GROUPED, AppSettings.AvatarDisplay.of("grouped"));
        assertEquals(AppSettings.AvatarDisplay.NEVER, AppSettings.AvatarDisplay.of("never"));
    }

    @Test
    public void unknownAvatarDisplayFallsBackToAlways() {
        assertEquals(AppSettings.AvatarDisplay.ALWAYS, AppSettings.AvatarDisplay.of("unsupported"));
    }
}
