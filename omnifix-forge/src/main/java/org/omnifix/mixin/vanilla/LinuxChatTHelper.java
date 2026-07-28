package org.omnifix.mixin.vanilla;

/**
 * Shared state for MC-122477: after chat opens, suppress the first char event that would type
 * the open-chat / command key into the field (Linux DE split key/char polls).
 */
public final class LinuxChatTHelper {

    private static int suppressChars;

    private LinuxChatTHelper() {}

    public static void onChatScreenOpened() {
        // One char from the key that opened chat (usually 't' or '/') may arrive after open.
        suppressChars = 1;
    }

    public static boolean consumeSuppressedChar() {
        if (suppressChars <= 0) {
            return false;
        }
        suppressChars--;
        return true;
    }
}
