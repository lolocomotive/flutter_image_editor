package com.fluttercandies.image_editor.option

import android.graphics.Matrix

class AddTextOpt : Option {
    val texts = ArrayList<Text>()

    fun addText(text: Text) {
        texts.add(text)
    }
}

data class Text(
    val text: String,
    val transform: Matrix,
    val fontSize: Float,
    val fill: Int,
    val outline: Int,
    val fontName: String,
    val outlineWidth: Float,
    val background: Int,
    )
