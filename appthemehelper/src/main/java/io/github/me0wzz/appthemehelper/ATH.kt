package io.github.me0wzz.appthemehelper

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import androidx.annotation.ColorInt
import io.github.me0wzz.appthemehelper.util.ColorUtil
import io.github.me0wzz.appthemehelper.util.TintHelper
import io.github.me0wzz.appthemehelper.util.ToolbarContentTintHelper
import io.github.me0wzz.appthemehelper.util.VersionUtils

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object ATH {

    fun didThemeValuesChange(context: Context, since: Long): Boolean {
        return ThemeStore.isConfigured(context) && ThemeStore.prefs(context).getLong(
            ThemeStorePrefKeys.VALUES_CHANGED,
            -1
        ) > since
    }

    fun setTint(view: View, @ColorInt color: Int) {
        TintHelper.setTintAuto(view, color, false)
    }

    fun setBackgroundTint(view: View, @ColorInt color: Int) {
        TintHelper.setTintAuto(view, color, true)
    }
}