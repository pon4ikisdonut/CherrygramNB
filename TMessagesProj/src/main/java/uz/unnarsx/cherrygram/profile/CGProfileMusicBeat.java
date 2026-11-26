package uz.unnarsx.cherrygram.profile;

import java.nio.ByteBuffer;

public class CGProfileMusicBeat {
    public static final long DISPATCH_DELAY_MS = 0L;

    public static boolean hasListeners() {
        return false;
    }

    public float consume(ByteBuffer buffer) {
        return -1f;
    }

    public static void dispatch(float beat) {
    }
}
