package com.pramod.todaysword.view

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.*
import com.pramod.todaysword.R
import com.pramod.todaysword.databinding.CustomItsLayoutBinding
import com.pramod.todaysword.util.CommonUtils

class ITSCustomLayout : LinearLayout {
    private lateinit var customItsLayoutBinding: CustomItsLayoutBinding
    private var title: String? = null
    private var titleTextSize: Float? = null
    private var subTitle: String? = null
    private var showSwitch = false

    @DrawableRes
    private var icon = -1
    private var iconTintResId = -1
    private var showIconBackground = false
    private var iconBackgroundTintResId = -1

    private var titlePaddingStart = 0f
    private var titlePaddingEnd = 0f


    companion object {
        @JvmStatic
        @BindingAdapter("app:checked")
        fun setChecked(view: ITSCustomLayout, checked: Boolean) {
            if (view.customItsLayoutBinding.itsSwitch.isChecked != checked) {
                view.customItsLayoutBinding.itsSwitch.isChecked = checked
            }
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "app:checked")
        fun getChecked(view: ITSCustomLayout) = view.customItsLayoutBinding.itsSwitch.isChecked

        @JvmStatic
        @BindingAdapter(
            value = ["app:onCheckedChanged", "app:checkedAttrChanged"],
            requireAll = false
        )
        fun setListeners(
            view: ITSCustomLayout,
            listener: CompoundButton.OnCheckedChangeListener?,
            attrChange: InverseBindingListener?
        ) {
            if (attrChange == null) {
                view.customItsLayoutBinding.itsSwitch.setOnCheckedChangeListener(listener)
            } else {
                view.customItsLayoutBinding.itsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    listener?.onCheckedChanged(buttonView, isChecked)
                    attrChange.onChange()
                }
            }
        }
    }


    private var onSwitchChangeListener: OnSwitchChangeListener? = null
    fun setOnSwitchChangeListener(onSwitchChangeListener: OnSwitchChangeListener?) {
        this.onSwitchChangeListener = onSwitchChangeListener
    }


    @Deprecated("")
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ITSCustomLayout, 0, 0)
        title = a.getString(R.styleable.ITSCustomLayout_title)
        subTitle = a.getString(R.styleable.ITSCustomLayout_subTitle)
        icon = a.getResourceId(R.styleable.ITSCustomLayout_icon, -1)
        showSwitch = a.getBoolean(R.styleable.ITSCustomLayout_showRadioButton, false)
        iconTintResId = a.getResourceId(R.styleable.ITSCustomLayout_iconColorTint, -1)
        showIconBackground = a.getBoolean(R.styleable.ITSCustomLayout_showIconBackground, false)
        iconBackgroundTintResId =
            a.getResourceId(R.styleable.ITSCustomLayout_iconBackgroundTint, -1)
        titleTextSize = CommonUtils.pixelToSp(
            context,
            a.getDimensionPixelSize(
                R.styleable.ITSCustomLayout_titleTextSize,
                0
            ).toFloat()
        )
        titlePaddingStart =
            a.getDimensionPixelSize(
                R.styleable.ITSCustomLayout_titlePaddingStart,
                0
            ).toFloat()

        titlePaddingEnd =
            a.getDimensionPixelSize(
                R.styleable.ITSCustomLayout_titlePaddingEnd,
                0
            ).toFloat()


        init()
        a.recycle()
    }

    private fun init() {
        customItsLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.custom_its_layout,
            this,
            true
        )
        addPaddingIfSaid()
        if (titleTextSize != 0f) {
            customItsLayoutBinding.txtViewCustomTitle.textSize = titleTextSize!!
        }
        setTitle(title)
        setSubTitle(subTitle)
        setIcon(icon)
        shouldShowSwitch(showSwitch)
    }

    private fun addPaddingIfSaid() {
        val paddingStart = customItsLayoutBinding.itLinearLayoutTitleSubTitle.paddingStart
        val paddingEnd = customItsLayoutBinding.itLinearLayoutTitleSubTitle.paddingEnd
        val paddingTop = customItsLayoutBinding.itLinearLayoutTitleSubTitle.paddingTop
        val paddingBottom = customItsLayoutBinding.itLinearLayoutTitleSubTitle.paddingBottom
        customItsLayoutBinding.itLinearLayoutTitleSubTitle.setPadding(
            (paddingStart + titlePaddingStart).toInt(),
            paddingTop,
            (paddingEnd + titlePaddingEnd).toInt(),
            paddingBottom
        )

    }

    private fun shouldShowBackgroundTint() {
        if (showIconBackground) {
            val color =
                if (iconTintResId != -1)
                    ContextCompat.getColor(context, iconTintResId)
                else
                    ContextCompat.getColor(context, R.color.app_icon_tint)
            val colorWithAlphaComponent =
                ColorUtils.setAlphaComponent(color, 30)
            customItsLayoutBinding.itsFrameLayoutImageIcon
                .backgroundTintList = ColorStateList.valueOf(colorWithAlphaComponent)
        }
    }

    fun getTitle(): String? {
        return title
    }

    fun getSubTitle(): String? {
        return subTitle
    }

    fun setTitle(title: String?) {
        customItsLayoutBinding.txtViewCustomTitle.text = title
        invalidate()
    }

    fun setSubTitle(subTitle: String?) {
        if (!TextUtils.isEmpty(subTitle)) {
            customItsLayoutBinding.txtViewCustomSubtitle.text = subTitle
            customItsLayoutBinding.txtViewCustomSubtitle.visibility = View.VISIBLE
            invalidate()
        }
    }

    fun setIcon(@DrawableRes resId: Int) {
        if (resId != -1) {
            customItsLayoutBinding.imageIconCustomLayout.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    resId
                )
            )
            customItsLayoutBinding.imageIconCustomLayout.imageTintList =
                if (iconTintResId != -1)
                    ColorStateList.valueOf(ContextCompat.getColor(context, iconTintResId))
                else
                    ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.app_icon_tint)
                    )
            customItsLayoutBinding.itsFrameLayoutImageIcon.visibility = View.VISIBLE
            shouldShowBackgroundTint()
        } else {
            customItsLayoutBinding.itsFrameLayoutImageIcon.visibility = View.GONE
        }
        invalidate()
    }

    private fun shouldShowSwitch(show: Boolean) {
        if (show) {
            customItsLayoutBinding.itsSwitch.visibility = View.VISIBLE
            //setSwitchClickListener()
        } else {
            customItsLayoutBinding.itsSwitch.visibility = View.GONE
        }
    }

    private fun setSwitchClickListener() {
        customItsLayoutBinding.root.setOnClickListener {
            val isChecked = !customItsLayoutBinding.itsSwitch.isChecked
            customItsLayoutBinding.itsSwitch.isChecked = isChecked
        }
    }

    interface OnSwitchChangeListener {
        fun changed(isChecked: Boolean)
    }


}