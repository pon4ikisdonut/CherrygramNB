/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.ui.folders

import android.content.Context
import android.graphics.RectF
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl
import org.telegram.ui.Components.FilterTabsView
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory
import org.telegram.ui.Components.blur3.DownscaleScrollableNoiseSuppressor
import org.telegram.ui.Components.blur3.capture.IBlur3Capture
import org.telegram.ui.DialogsActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramNBConfig

object FoldersHelper {

    @JvmStatic
    fun moveFoldersToBottom() : Boolean {
        return CherrygramAppearanceConfig.foldersAtBottom || CherrygramNBConfig.foldersAtBottom
    }

    @JvmStatic
    fun getFloatingButtonsOffset(filterTabsView: FilterTabsView?) : Int {
        return if (moveFoldersToBottom() && filterTabsView != null && filterTabsView.visibility == android.view.View.VISIBLE) AndroidUtilities.dp(54f) else 0
    }

    @JvmStatic
    fun setupFilterTabs(
        context: Context,
        contentView: ViewGroup,
        filterTabsView: FilterTabsView,
        resourceProvider: Theme.ResourcesProvider?,
        iBlur3FactoryLiquidGlass: BlurredBackgroundDrawableViewFactory,
        iBlur3FactoryFade: BlurredBackgroundDrawableViewFactory,
        inForwardMode: Boolean
    ) {
        val filterTabsViewBackground = iBlur3FactoryLiquidGlass.create(filterTabsView, BlurredBackgroundProviderImpl.topPanel(resourceProvider))
        filterTabsViewBackground.setRadius(AndroidUtilities.dp(18f).toFloat())
        filterTabsViewBackground.setPadding(AndroidUtilities.dp(6.666f))
        filterTabsView.setPadding(0, AndroidUtilities.dp(7f), 0, AndroidUtilities.dp(7f))
        filterTabsView.setBlurredBackground(filterTabsViewBackground)
        contentView.addView(filterTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50f, Gravity.BOTTOM, 4f, 0f, 4f, 4f))
        filterTabsView.bringToFront()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Suppress("FunctionName")
    @JvmStatic
    fun blur3_InvalidateBlur(
        dialogsActivity: DialogsActivity,
        iBlur3Capture: IBlur3Capture,
        iBlur3PositionActionBar: RectF,
        iBlur3PositionFolders: RectF,
        iBlur3PositionMainTabs: RectF,
        iBlur3Positions: ArrayList<RectF>,
        scrollableViewNoiseSuppressor: DownscaleScrollableNoiseSuppressor
    ) {

    }

    @JvmStatic
    fun updateFoldersOffset(
        dialogsActivity: DialogsActivity,
        inForwardMode: Boolean,
    ) {
        val filterTabsView = dialogsActivity.filterTabsView ?: return
        if (!moveFoldersToBottom()) return

        val hasMainTabs = dialogsActivity.hasMainTabs && CherrygramAppearanceConfig.showMainTabs
        val mainTabsHeight = if (hasMainTabs) AndroidUtilities.dp(DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS.toFloat()) else 0
        val offset = dialogsActivity.navigationBarHeight + mainTabsHeight
        filterTabsView.translationY = -offset.toFloat()
    }

}
