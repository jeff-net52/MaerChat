package eu.siacs.conversations.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.siacs.conversations.R;
import im.conversations.android.xmpp.model.error.Condition;
import im.conversations.android.xmpp.model.error.Error;
import org.junit.Test;

public class LinkedDevicesActivityTest {

    @Test
    public void throttlingAndDeviceLimitRemainDistinct() {
        assertEquals(
                R.string.pairing_too_many_attempts,
                LinkedDevicesActivity.pairingError(new Condition.PolicyViolation(), -1));
        assertEquals(
                R.string.pairing_device_limit_reached,
                LinkedDevicesActivity.pairingError(new Condition.ResourceConstraint(), -1));
    }

    @Test
    public void policyViolationUsesTheStandardWaitMapping() {
        final Condition.ErrorTypeCode mapping =
                Condition.ERROR_CONDITION_MAPPING.get(Condition.PolicyViolation.class);
        assertEquals("policy-violation", new Condition.PolicyViolation().getName());
        assertEquals(Error.Type.WAIT, mapping.errorType());
        assertEquals(500, mapping.legacyErrorCode());
    }

    @Test
    public void activePairingAbortsOnlyWhenTheAccountGoesOffline() {
        assertTrue(
                LinkedDevicesActivity.shouldAbortPairing(
                        true, LinkedDevicesActivity.PairingStage.INSPECTING, false));
        assertTrue(
                LinkedDevicesActivity.shouldAbortPairing(
                        true, LinkedDevicesActivity.PairingStage.AWAITING_APPROVAL, false));
        assertTrue(
                LinkedDevicesActivity.shouldAbortPairing(
                        true, LinkedDevicesActivity.PairingStage.APPROVING, false));

        assertFalse(
                LinkedDevicesActivity.shouldAbortPairing(
                        true, LinkedDevicesActivity.PairingStage.NONE, false));
        assertFalse(
                LinkedDevicesActivity.shouldAbortPairing(
                        false, LinkedDevicesActivity.PairingStage.INSPECTING, false));
        assertFalse(
                LinkedDevicesActivity.shouldAbortPairing(
                        true, LinkedDevicesActivity.PairingStage.INSPECTING, true));
    }
}
