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

class FollowButton : AppCompatButton, View.OnClickListener {

    //    val fontPath: String = context.getString(R.string.custom_bold_font)
    private val widthNormal = 240.px
    private val widthSmall = 80.px
    private val heightNormal = 40.px
    private val heightSmall = 20.px
    private val padding = 16.px
    private val paddingSmall = 4.px
    private val paddingExpanded = 32.px
    private val paddingSmallExpanded = 16.px
    private val textNormal = 12
    private val textSmall = 7

    /* Public APIs*/
    /* ======================================================================================== */

    // A boolean to specify whether the button should expands, when followed is true
    var expandable: Boolean by Delegates.observable(true, this::onExpandableChanges)

    // A boolean to specify whether the user is followed to render the style properly
    var followed: Boolean by Delegates.observable(false, this::onFollowedChanges)

    // An option to specify size of the button. The available options are: small, normal.
    // 0 = normal, 1 = small
    var size: Int by Delegates.observable(0, this::onSizeChanges)

    /* Private APIs */
    /* ======================================================================================== */

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var mBackgroundDrawable: Drawable
    private var mTextColor: Int = 0
    private var mPadding: Int = 0
    private var mText: String = ""

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
            expandable = typed.getBoolean(R.styleable.FollowButton_expandable, expandable)
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

        // Update size here
        setMeasuredDimension(mWidth, mHeight)
    }

    /* Property-observed functions */

    private fun onExpandableChanges(prop: KProperty<*>, from: Boolean, to: Boolean) {
        log("expandable", to.toString())
    }

    private fun onFollowedChanges(prop: KProperty<*>, from: Boolean, to: Boolean) {
        log("followed", to.toString())

        if (expandable) {
            resolveWidth(expandable, to, size)
        }

        resolveBackgroundColor(to)
        resolveTextColor(to)
        resolveText(to)

        render()
    }

    private fun onSizeChanges(prop: KProperty<*>, from: Int, to: Int) {
        log("size", to.toString())

        when (to) {
            1 -> {
                minHeight = heightSmall
                minimumHeight = heightSmall
                minWidth = widthSmall
                minimumWidth = widthSmall
            }
            else -> {
                minHeight = suggestedMinimumHeight
                minimumHeight = suggestedMinimumHeight

                if (expandable) {
                    minWidth = suggestedMinimumWidth
                    minimumWidth = suggestedMinimumWidth
                }
            }
        }

        resolveWidth(expandable, followed, to)
        resolveHeight(height)

        render()
    }

    /* Internal mechanism */

    private fun resolveWidth(mExpandable: Boolean, mFollowed: Boolean, mSize: Int) {
        mWidth = getWidthSize(mSize)
        when {
            mFollowed && mSize == 0 -> {
                mPadding = paddingExpanded
            }
            mFollowed && mSize == 1 -> {
                mPadding = paddingSmallExpanded
            }
            !mFollowed && mSize == 0 -> {
                mPadding = if (mExpandable)
                    padding
                else
                    paddingExpanded
            }
            !mFollowed && mSize == 1 -> {
                mPadding = if (mExpandable)
                    paddingSmall
                else
                    paddingSmallExpanded
            }
        }
    }

    private fun resolveHeight(mSize: Int) {
        mHeight = getHeightSize(mSize)
    }

    private fun resolveBackgroundColor(mFollowed: Boolean) {
        mBackgroundDrawable = when (mFollowed) {
            true -> context.getDrawable(R.drawable.bg_rounded_solid_aqua) ?: return
            false -> context.getDrawable(R.drawable.bg_rounded_stroke_aqua) ?: return
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
        setTextColor(mTextColor)
        setBackground(mBackgroundDrawable)
        setPadding(mPadding, 0, mPadding, 0)

        val textSize = when (size) {
            1 -> textSmall
            else -> textNormal
        }

        setTextSize(textSize.toFloat())

        requestLayout()
    }

    private fun getWidthSize(mSize: Int): Int {
        return when (mSize) {
            1 -> widthSmall
            else -> widthNormal
        }
    }

    private fun getHeightSize(mSize: Int): Int {
        return when (mSize) {
            1 -> heightSmall
            else -> heightNormal
        }
    }

    /* Debugging purpose. TODO: Remove when works */
    private fun log(tag: String, value: String) {
        Log.d("FollowButton.$tag", value)
    }
}
