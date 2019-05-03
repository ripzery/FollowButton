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
    private val expandWidth = 16 // dp
    private val textPadding = 8 // dp
    private val textNormal = 14
    private val textSmall = 10

    private var mUnfollowedWidth: Int = 0
    private var mFollowedWidth: Int = 0

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
    private var mTextSize: Int = 0
    private var mText: String = ""

    constructor(context: Context) : super(context) {
        initTypeFace()
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initWithAttrs(attrs, 0, 0)
        initTypeFace()
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initWithAttrs(attrs, defStyle, 0)
        initTypeFace()
        setOnClickListener(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (followed) {
            mFollowedWidth = MeasureSpec.getSize(widthMeasureSpec)
        } else {
            mUnfollowedWidth = MeasureSpec.getSize(widthMeasureSpec)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
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

    override fun onClick(v: View?) {
        followed = !followed
    }

    /* Property-observed functions */

    private fun onExpandableChanges(prop: KProperty<*>, from: Boolean, to: Boolean) {
        log("expandable", to.toString())
    }

    private fun onFollowedChanges(prop: KProperty<*>, from: Boolean, to: Boolean) {
        log("followed", to.toString())

        if (expandable) {
            resolveWidth(to)
        }

        resolveBackgroundColor(to)
        resolveTextColor(to)
        resolveText(to)

        render()
    }

    private fun onSizeChanges(prop: KProperty<*>, from: Int, to: Int) {
        log("size", to.toString())

        resolveWidth(followed)
        resolveHeight(height)

        render()
    }

    /* Internal mechanism */

    private fun resolveWidth(mFollowed: Boolean) {

//        mWidth = when (mFollowed) {
//            true -> mFollowedWidth
//            false -> mUnfollowedWidth
//        }
        mWidth = when (mFollowed) {
            true -> paint.measureText(text.toString()).toInt() + expandWidth.px
            else -> paint.measureText(text.toString()).toInt()
        }
        log("width", mWidth.toString())
    }

    private fun resolveHeight(mSize: Int) {
//        mHeight = mSize
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
        setPadding(textPadding + mWidth, 0, textPadding + mWidth, 0)

        val textSize = when (size) {
            1 -> textSmall
            else -> textNormal
        }

        setTextSize(textSize.toFloat())
    }

    /* Debugging purpose. TODO: Remove when works */
    private fun log(tag: String, value: String) {
        Log.d("FollowButton.$tag", value)
    }
}
