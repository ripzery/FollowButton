package co.ripzery.followbutton

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * I hate the nested layout. I hate the inconsistency button properties.
 * So instead of doing a 2-minutes nested layout, I want to spend my too much time by doing a 2-hours custom view.
 * Most importantly, it sparks joy.
 */
class FollowButton : AppCompatButton, View.OnClickListener {

    /* PUBLIC APIs
    * `followed` and `size` are the only two properties you can control, but normally you just don't need to.
    */

    /* A boolean to specify whether the user is followed to render the style properly */
    var followed: Boolean by Delegates.observable(false, this::onFollowedChanged)

    /* An option to specify size of the button. The available options are: small, normal. */
    var size: Int by Delegates.observable(0, this::onSizeChanged)

    /* INTERNAL WORKS. DON'T GET YOUR EYES DIRTY. */

    //    val fontPath: String = context.getString(R.string.custom_bold_font)
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var mBackgroundDrawable: Drawable
    private var mTextColor: Int = 0
    private var mPadding: Int = 0
    private var mText: String = ""

    companion object {
        // Size
        const val WIDTH_NORMAL = 240
        const val WIDTH_SMALL = 100
        const val HEIGHT_NORMAL = 40
        const val HEIGHT_SMALL = 24
        const val PADDING = 16
        const val PADDING_SMALL = 4
        const val PADDING_EXPANDED = 32
        const val PADDING_SMALL_EXPANDED = 16
        const val TEXT_NORMAL = 12
        const val TEXT_SMALL = 8

        // Mode
        const val SIZE_NORMAL = 0
        const val SIZE_SMALL = 1

        const val DEBUG = false
    }

    constructor(context: Context) : super(context) {
        initTypeFace()
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initWithAttrs(attrs, 0, 0)
        initTypeFace()
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initWithAttrs(attrs, defStyle, 0)
        initTypeFace()
        init()
    }

    private fun initWithAttrs(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typed = context.theme.obtainStyledAttributes(attrs, R.styleable.FollowButton, defStyleAttr, defStyleRes)

        try {
            followed = typed.getBoolean(R.styleable.FollowButton_followed, followed)
            size = typed.getInt(R.styleable.FollowButton_size, size)
        } finally {
            typed.recycle()
        }
    }

    private fun initTypeFace() {
//        typeface = Typeface.createFromAsset(context.assets, fontPath)
    }

    private fun init() {
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        followed = !followed
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val layoutWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val layoutHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        mWidth = when (layoutWidthMode) {
            MeasureSpec.AT_MOST -> Math.min(mWidth, measuredWidth)
            MeasureSpec.UNSPECIFIED -> mWidth
            else -> measuredWidth
        }

        mHeight = when (layoutHeightMode) {
            MeasureSpec.AT_MOST -> Math.min(mHeight, measuredHeight)
            MeasureSpec.UNSPECIFIED -> mHeight
            else -> measuredHeight
        }

        setMeasuredDimension(mWidth, mHeight)
    }

    /* Property-observed functions */
    private fun onFollowedChanged(prop: KProperty<*>, from: Boolean, to: Boolean) {
        log("followed", to.toString())

        resolveWidth(to, size)
        resolveBackgroundColor(to)
        resolveTextColor(to)
        resolveText(to)

        render()
    }

    private fun onSizeChanged(prop: KProperty<*>, from: Int, to: Int) {
        log("size", to.toString())

        when (to) {
            SIZE_SMALL -> {
                minHeight = HEIGHT_SMALL
                minimumHeight = HEIGHT_SMALL
                minWidth = WIDTH_SMALL
                minimumWidth = WIDTH_SMALL
            }
            else -> {
                minHeight = suggestedMinimumHeight
                minimumHeight = suggestedMinimumHeight
                minWidth = suggestedMinimumWidth
                minimumWidth = suggestedMinimumWidth
            }
        }

        resolveWidth(followed, to)
        resolveHeight(height)

        render()
    }

    /* Internal mechanism */

    private fun resolveWidth(mFollowed: Boolean, mSize: Int) {
        mWidth = getWidthSize(mSize)

        mPadding = when {
            mFollowed && mSize == 0 -> PADDING_EXPANDED
            mFollowed && mSize == 1 -> PADDING_SMALL_EXPANDED
            !mFollowed && mSize == 0 -> PADDING
            else -> PADDING_SMALL
        }
    }

    private fun resolveHeight(mSize: Int) {
        mHeight = getHeightSize(mSize)
    }

    // COMPILER, YOU'RE TALKING NONSENSE.
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun resolveBackgroundColor(mFollowed: Boolean) {
        mBackgroundDrawable = when (mFollowed) {
            true -> context.getDrawable(R.drawable.bg_rounded_solid_aqua)
            false -> context.getDrawable(R.drawable.bg_rounded_stroke_aqua)
        }
    }

    private fun resolveTextColor(mFollowed: Boolean) {
        mTextColor = when (mFollowed) {
            true -> ContextCompat.getColor(context, R.color.colorWhite)
            false -> ContextCompat.getColor(context, R.color.colorBgSubscribed)
        }
    }

    private fun resolveText(mFollowed: Boolean) {
        mText = when (mFollowed) {
            true -> context.getString(R.string.button_live_call_subscribed)
            false -> context.getString(R.string.button_live_call_subscribe)
        }
    }

    private fun render() {
        setText(mText)
        setTextSize(getTextSize(size))
        setTextColor(mTextColor)
        setBackground(mBackgroundDrawable)
        setPadding(mPadding, 0, mPadding, 0)

        requestLayout()
    }

    private fun getWidthSize(mSize: Int): Int {
        return when (mSize) {
            SIZE_SMALL -> WIDTH_SMALL
            else -> WIDTH_NORMAL
        }
    }

    private fun getHeightSize(mSize: Int): Int {
        return when (mSize) {
            SIZE_SMALL -> HEIGHT_SMALL
            else -> HEIGHT_NORMAL
        }
    }

    private fun getTextSize(mSize: Int): Float {
        return when (mSize) {
            SIZE_SMALL -> TEXT_SMALL
            else -> TEXT_NORMAL
        }.toFloat()
    }

    /* debug the bug. */
    private fun log(tag: String, value: String) {
        if (DEBUG)
            Log.d("FollowButton.$tag", value)
    }
}
