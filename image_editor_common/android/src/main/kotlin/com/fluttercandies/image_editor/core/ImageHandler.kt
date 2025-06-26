package com.fluttercandies.image_editor.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.fluttercandies.image_editor.common.font.FontUtils
import com.fluttercandies.image_editor.option.AddTextOpt
import com.fluttercandies.image_editor.option.ClipOption
import com.fluttercandies.image_editor.option.ColorOption
import com.fluttercandies.image_editor.option.FlipOption
import com.fluttercandies.image_editor.option.FormatOption
import com.fluttercandies.image_editor.option.MixImageOpt
import com.fluttercandies.image_editor.option.Option
import com.fluttercandies.image_editor.option.RotateOption
import com.fluttercandies.image_editor.option.ScaleOption
import com.fluttercandies.image_editor.option.Text
import com.fluttercandies.image_editor.option.draw.DrawOption
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream

class ImageHandler(private var bitmap: Bitmap) {
    fun handle(options: List<Option>) {
        for (option in options) {
            bitmap = when (option) {
                is ColorOption -> handleColor(option)
                is ScaleOption -> handleScale(option)
                is FlipOption -> handleFlip(option)
                is ClipOption -> handleClip(option)
                is RotateOption -> handleRotate(option)
                is AddTextOpt -> handleText(option)
                is MixImageOpt -> handleMixImage(option)
                is DrawOption -> bitmap.draw(option)
                else -> throw Exception("Illegal option")
            }
        }
    }

    private fun handleMixImage(option: MixImageOpt): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(bitmap, 0F, 0F, null)
        val src = BitmapFactory.decodeByteArray(option.img, 0, option.img.count())
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(option.porterDuffMode)
        val dstRect = Rect(option.x, option.y, option.x + option.w, option.y + option.h)
        canvas.drawBitmap(src, null, dstRect, paint)
        return newBitmap
    }

    private fun handleScale(option: ScaleOption): Bitmap {
        var w = option.width
        var h = option.height
        if (option.keepRatio) {
            val srcRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            if (option.keepWidthFirst) {
                h = (w / srcRatio).toInt()
            } else {
                w = (srcRatio * h).toInt()
            }
        }
        val newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        val p = Paint()
        val m = Matrix()
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        if (width != w || height != h) {
            val sx: Float = w / width.toFloat()
            val sy: Float = h / height.toFloat()
            m.setScale(sx, sy)
        }
        canvas.drawBitmap(bitmap, m, p)
        return newBitmap
    }

    private fun handleRotate(option: RotateOption): Bitmap {
        val matrix = Matrix().apply {
            //      val rotate = option.angle.toFloat() / 180 * Math.PI
            this.postRotate(option.angle.toFloat())
        }
        val out = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val canvas = Canvas()
        canvas.drawBitmap(out, matrix, null)
        return out
    }

    private fun handleFlip(option: FlipOption): Bitmap {
        val matrix = Matrix().apply {
            val x = if (option.horizontal) -1F else 1F
            val y = if (option.vertical) -1F else 1F
            postScale(x, y)
        }
        val out = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val canvas = Canvas()
        canvas.drawBitmap(out, matrix, null)
        return out
    }

    private fun handleClip(option: ClipOption): Bitmap {
        val x = option.x
        val y = option.y
        return Bitmap.createBitmap(bitmap, x, y, option.width, option.height, null, false)
    }

    private fun handleColor(option: ColorOption): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(newBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(option.matrix)
        canvas.drawBitmap(bitmap, 0F, 0F, paint)
        return newBitmap
    }

    private fun handleText(option: AddTextOpt): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(newBitmap)
        val paint = Paint()
        canvas.drawBitmap(bitmap, 0F, 0F, paint)
        for (text in option.texts) {
            drawText(text, canvas)
        }
        return newBitmap
    }

    private fun drawText(text: Text, canvas: Canvas) {
        val fillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        fillPaint.color = text.fill
        strokePaint.color = text.outline
        fillPaint.textSize = text.fontSize
        strokePaint.textSize = text.fontSize
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND

        if (text.fontName.isNotEmpty()) {
            try {
                val typefaceFromAsset = FontUtils.getFont(text.fontName)
                fillPaint.typeface = typefaceFromAsset
                strokePaint.typeface = typefaceFromAsset
            } catch (_: Exception) {
            }
        }
        fillPaint.textAlign = Paint.Align.LEFT
        strokePaint.textAlign = Paint.Align.LEFT
        strokePaint.style = Paint.Style.STROKE;
        strokePaint.strokeWidth = text.outlineWidth;

        @Suppress("DEPRECATION") val previous = canvas.matrix


        val fillLayout = getStaticLayout(text, fillPaint, canvas.width)
        val strokeLayout = getStaticLayout(text, strokePaint, canvas.width)
        val bounds = Rect()
        canvas.getClipBounds(bounds)
        val cHeight = bounds.height()
        val y = cHeight / 2f - fillLayout.height / 2f
        val transform = Matrix(text.transform)
        transform.preTranslate(0f, y)
        canvas.setMatrix(transform)

        strokeLayout.draw(canvas)
        fillLayout.draw(canvas)

        canvas.setMatrix(previous)
    }

    @Suppress("DEPRECATION")
    private fun getStaticLayout(text: Text, textPaint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= 23) {
            val builder = StaticLayout.Builder.obtain(
                text.text, 0, text.text.length, textPaint, width
            )
            builder.setAlignment(Layout.Alignment.ALIGN_CENTER)
            return builder.build()
        } else {
            StaticLayout(
                text.text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true
            )
        }
    }

    fun outputToFile(dstPath: String, formatOption: FormatOption) {
        val outputStream = FileOutputStream(dstPath)
        output(outputStream, formatOption)
    }

    fun outputByteArray(formatOption: FormatOption): ByteArray {
        val outputStream = ByteArrayOutputStream()
        output(outputStream, formatOption)
        return outputStream.toByteArray()
    }


    private fun output(outputStream: OutputStream, formatOption: FormatOption) {
        outputStream.use {
            bitmap.compress(
                getCompressFormat(formatOption), formatOption.quality, outputStream
            )
        }
    }
}

@Suppress("DEPRECATION")
fun getCompressFormat(formatOption: FormatOption): Bitmap.CompressFormat {
    if (Build.VERSION.SDK_INT < 30) {
        return when (formatOption.format) {
            0 -> Bitmap.CompressFormat.PNG
            1 -> Bitmap.CompressFormat.JPEG
            else -> Bitmap.CompressFormat.WEBP
        }
    } else {
        return when (formatOption.format) {
            0 -> Bitmap.CompressFormat.PNG
            1 -> Bitmap.CompressFormat.JPEG
            2 -> Bitmap.CompressFormat.WEBP
            3 -> Bitmap.CompressFormat.WEBP_LOSSY
            else -> Bitmap.CompressFormat.WEBP_LOSSLESS

        }
    }
}