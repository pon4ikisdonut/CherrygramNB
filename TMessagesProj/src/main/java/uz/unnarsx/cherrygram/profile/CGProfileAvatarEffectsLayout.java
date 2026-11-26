package uz.unnarsx.cherrygram.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import org.telegram.messenger.Utilities;

public class CGProfileAvatarEffectsLayout extends FrameLayout {

    public CGProfileAvatarEffectsLayout(Context context) {
        super(context);
    }

    public void applyPatternPulse(Canvas canvas, View target) {
    }

    public boolean dispatchGiftTouch(MotionEvent ev) {
        return false;
    }

    public void setGiftsView(int currentAccount, CGProfileGiftsView cgGiftsView, View avatarGooey, View avatarContainer, View storyView, View topView, Utilities.Callback<Boolean> onMusicPlaying) {
    }

    public void setStoryRingDim(float dim) {
    }
}
