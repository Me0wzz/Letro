package io.github.me0wzz.music.views.insets

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import io.github.me0wzz.music.extensions.drawAboveSystemBarsWithPadding
import io.github.me0wzz.music.util.RetroUtil

class InsetsConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        if (!RetroUtil.isLandscape())
            drawAboveSystemBarsWithPadding()
    }
}