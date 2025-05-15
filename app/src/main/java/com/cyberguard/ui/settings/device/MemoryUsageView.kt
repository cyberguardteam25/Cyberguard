package com.cyberguard.ui.settings.device

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class MemoryUsageView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private var usagePercentage: Int = 0
    private val circleBounds = RectF()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
        paint.strokeCap = Paint.Cap.ROUND

        textPaint.isAntiAlias = true
        textPaint.color = Color.BLACK
        textPaint.textSize = 50f
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //  background circle
        paint.color = Color.LTGRAY
        canvas.drawCircle(width / 2f, height / 2f, width / 2f - paint.strokeWidth, paint)

        when {
            usagePercentage <= 20 -> paint.color = Color.parseColor("#E40000") // red
            usagePercentage in 21..69 -> paint.color = Color.parseColor("#FFA500") // orange
            usagePercentage in 70..89 -> paint.color = Color.parseColor("#3CB371") // green
            usagePercentage in 90..100 -> paint.color = Color.parseColor("#3b82f6") // blue
        }

        // progress circle
        val angle = (usagePercentage / 100f) * 360f
        canvas.drawArc(circleBounds, -90f, angle, false, paint)

        // percentage text
        textPaint.setColor(Color.GRAY)
        val text = "$usagePercentage%"
        canvas.drawText(text, width / 2f, height / 2f + (textPaint.textSize / 3), textPaint)
    }

    fun setMemoryUsagePercentage(percentage: Int) {
        this.usagePercentage = percentage
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleBounds.set(20f, 20f, (w - 20).toFloat(), (h - 20).toFloat())
    }
}