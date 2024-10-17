package com.pramod.dailyword.framework.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.pramod.dailyword.R
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.widget.pref.WidgetPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ScrollableWidgetItemService : RemoteViewsService() {

    @Inject
    lateinit var widgetPreference: WidgetPreference

    @Inject
    lateinit var wordCacheDataSource: WordCacheDataSource

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        Timber.i("onGetViewFactory:")
        return ScrollableWidgetItemFactory(
            context = applicationContext,
            widgetPreference = widgetPreference,
            wordCacheDataSource = wordCacheDataSource
        )
    }

}

const val EXTRA_WORD_DATA = "word"

const val EXTRA_CLICK_TYPE = "click_type"
const val EXTRA_ITEM_POSITION = "item_pos"

enum class ClickType {
    PLAY_AUDIO,
    VIEW_FULL_WORD_DETAIL
}

class ScrollableWidgetItemFactory(
    private val context: Context,
    private val widgetPreference: WidgetPreference,
    private val wordCacheDataSource: WordCacheDataSource
) : RemoteViewsService.RemoteViewsFactory {

    private var word: Word? = null

    override fun onCreate() {
        runBlocking(Dispatchers.Default) {
            word = widgetPreference.getCurrentWordShown()?.let {
                wordCacheDataSource.getWordByName(it)
            }
        }
    }

    override fun onDataSetChanged() {
        runBlocking(Dispatchers.Default) {
            word = widgetPreference.getCurrentWordShown()?.let {
                wordCacheDataSource.getWordByName(it)
            }
            Timber.i("ScrollableWidgetItemFactory:onDataSetChanged():" + word?.word)
        }
    }

    override fun onDestroy() {

    }

    override fun getCount() = 3

    override fun getViewAt(position: Int): RemoteViews {
        return when (position) {
            0 -> {
                val views =
                    RemoteViews(
                        context.packageName,
                        R.layout.widget_word_layout_revamp_word_info_item
                    )
                views.setTextViewText(R.id.widget_txtView_word_of_the_day, word?.word.orEmpty())
                views.setTextViewText(R.id.widget_txtView_attribute, word?.attribute.orEmpty())
                views.setTextViewText(R.id.widget_txtView_pronounce, word?.pronounce.orEmpty())
                views.setTextViewText(
                    R.id.widget_txtView_meanings,
                    word?.meanings?.joinToString(separator = "\n") { it })

                //pronounce audio pending intent
                val playAudioIntent = Intent()
                playAudioIntent.putExtras(
                    bundleOf(EXTRA_CLICK_TYPE to ClickType.PLAY_AUDIO)
                )
                views.setOnClickFillInIntent(R.id.widget_img_pronounce, playAudioIntent)
                //end

                val fullRowClickIntent = Intent()
                fullRowClickIntent.putExtras(
                    bundleOf(
                        EXTRA_CLICK_TYPE to ClickType.VIEW_FULL_WORD_DETAIL,
                        EXTRA_WORD_DATA to word
                    )
                )
                views.setOnClickFillInIntent(R.id.root_word_info_item, fullRowClickIntent)

                views
            }

            1 -> {
                val views =
                    RemoteViews(
                        context.packageName,
                        R.layout.widget_word_layout_revamp_word_usage_item
                    )

                views.setTextViewText(R.id.widget_txtView_title, "Examples")
                views.setTextViewText(
                    R.id.widget_txtView_examples,
                    word?.examples?.joinToString(separator = "\n") { it })

                val fullRowClickIntent = Intent()
                fullRowClickIntent.putExtras(
                    bundleOf(
                        EXTRA_CLICK_TYPE to ClickType.VIEW_FULL_WORD_DETAIL,
                        EXTRA_WORD_DATA to word
                    )
                )
                views.setOnClickFillInIntent(R.id.root_word_usage_item, fullRowClickIntent)

                views
            }

            else -> {
                val views = RemoteViews(
                    context.packageName,
                    R.layout.widget_word_layout_revamp_did_you_know_item
                )
                views.setTextViewText(R.id.widget_txtView_title, "Did you know?")
                views.setTextViewText(
                    R.id.widget_txtView_did_you_know, word?.didYouKnow
                )

                val fullRowClickIntent = Intent()
                fullRowClickIntent.putExtras(
                    bundleOf(
                        EXTRA_CLICK_TYPE to ClickType.VIEW_FULL_WORD_DETAIL,
                        EXTRA_WORD_DATA to word
                    )
                )
                views.setOnClickFillInIntent(R.id.root_did_you_know_item, fullRowClickIntent)

                views
            }
        }
    }


    override fun getLoadingView(): RemoteViews = createLoadingWidget(context)

    private fun createLoadingWidget(context: Context): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.VISIBLE)
        return views
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewTypeCount() = 3

}


/*

override fun getItemViewType(position: Int): Int = data[position].type

override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

    var newConvertView = convertView

    val viewHolder: ListViewHolder
    val viewType = getItemViewType(position)

    if (convertView == null) {
        when (viewType) {
            TYPE_WORD_INFO -> {
                viewHolder = WordInfoViewHolder(
                    WidgetWordLayoutRevampWordInfoItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            TYPE_WORD_EXAMPLES -> {
                viewHolder = WordExamplesViewHolder(
                    WidgetWordLayoutRevampWordUsageItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )

            }

            else -> {
                viewHolder = WordDidYouKnowViewHolder(
                    WidgetWordLayoutRevampDidYouKnowItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )

            }
        }
        newConvertView = viewHolder.root
        newConvertView.tag = viewHolder
    } else {
        viewHolder = newConvertView?.tag as ListViewHolder
    }

    when (val data = data[position]) {
        is WOTDWidget.DidYouKnow -> {
            (viewHolder as? WordDidYouKnowViewHolder)?.bind(data)
        }

        is WOTDWidget.WordInfo -> {
            (viewHolder as? WordInfoViewHolder)?.bind(data)
        }

        is WOTDWidget.WordUsage -> {
            (viewHolder as? WordExamplesViewHolder)?.bind(data)
        }
    }

    return newConvertView
}

abstract class ListViewHolder(val root: View)

inner class WordInfoViewHolder(private val binding: WidgetWordLayoutRevampWordInfoItemBinding) :
    ListViewHolder(binding.root) {

    fun bind(data: WOTDWidget.WordInfo) {
        binding.widgetTxtViewWordOfTheDay.text = data.word
        binding.widgetTxtViewAttribute.text = data.attribute
        binding.widgetTxtViewPronounce.text = data.pronounce
        binding.widgetTxtViewMeanings.text = formatListAsBulletList(data.meaning)
    }

}


inner class WordExamplesViewHolder(private val binding: WidgetWordLayoutRevampWordUsageItemBinding) :
    ListViewHolder(binding.root) {

    fun bind(data: WOTDWidget.WordUsage) {
        binding.widgetTxtViewExamples.text = formatListAsBulletList(data.words)
    }

}


inner class WordDidYouKnowViewHolder(private val binding: WidgetWordLayoutRevampDidYouKnowItemBinding) :
    ListViewHolder(binding.root) {

    fun bind(data: WOTDWidget.DidYouKnow) {
        binding.widgetTxtViewDidYouKnow.text = data.didYouKnowDesc
    }

}
*/
