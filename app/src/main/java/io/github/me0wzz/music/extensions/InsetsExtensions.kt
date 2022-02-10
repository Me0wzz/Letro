package io.github.me0wzz.music.extensions

import androidx.core.view.WindowInsetsCompat
import io.github.me0wzz.music.util.PreferenceUtil
import io.github.me0wzz.music.util.RetroUtil

fun WindowInsetsCompat?.safeGetBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: RetroUtil.getNavigationBarHeight()
    }
}
