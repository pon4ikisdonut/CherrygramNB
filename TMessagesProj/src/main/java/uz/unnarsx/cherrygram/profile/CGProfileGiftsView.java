package uz.unnarsx.cherrygram.profile;

import android.content.Context;
import android.view.View;
import org.telegram.messenger.ImageReceiver;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Stars.ProfileGiftsView;

public class CGProfileGiftsView extends ProfileGiftsView {

    public CGProfileGiftsView(Context context, int currentAccount, long dialogId, View avatarContainer, ImageReceiver avatarImage, Theme.ResourcesProvider resourcesProvider) {
        super(context, currentAccount, dialogId, avatarContainer, avatarImage, resourcesProvider);
    }

    public boolean isInCarouselZoneOnScreen(float rawX, float rawY) {
        return false;
    }
}
