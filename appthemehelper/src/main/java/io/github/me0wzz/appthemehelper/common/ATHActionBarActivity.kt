package io.github.me0wzz.appthemehelper.common

import androidx.appcompat.widget.Toolbar

import io.github.me0wzz.appthemehelper.util.ToolbarContentTintHelper

class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
