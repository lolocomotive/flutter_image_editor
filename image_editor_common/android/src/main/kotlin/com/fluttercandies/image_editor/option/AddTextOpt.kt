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
    val fontSize: Double,
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int,
    val fontName: String
)
