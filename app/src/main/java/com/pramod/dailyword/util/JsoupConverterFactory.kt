package com.pramod.dailyword.util

import android.util.Log
import com.google.gson.Gson
import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.WordOfTheDay
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.Exception
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

object JsoupConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return MerrianWebsiteDataConverter()
    }


    class MerrianWebsiteDataConverter : Converter<ResponseBody, ApiResponse<WordOfTheDay>> {

        override fun convert(value: ResponseBody): ApiResponse<WordOfTheDay> {

            val apiResponse: ApiResponse<WordOfTheDay> = ApiResponse<WordOfTheDay>()
            val responseString = value.string()
            val wordOfTheDay = WordOfTheDay()


            val document = Jsoup.parse(responseString) as Document
            val articleTitleAndDate = document.selectFirst("div.article-header-container > span")

            //setting date
            val calender: Calendar = Calendar.getInstance()

            try {
                //date format MMMM d, yyyy
                val dateString = articleTitleAndDate.text().split(":")[1].trim()
                val serverCal = CalenderUtil.convertStringToCalender(
                    dateString,
                    CalenderUtil.MERRIAN_DATE_FORMAT
                ) ?: throw Exception()
                wordOfTheDay.dateTimeInMillis = serverCal.timeInMillis
                wordOfTheDay.date =
                    CalenderUtil.convertCalenderToString(serverCal, CalenderUtil.DATE_FORMAT)

            } catch (e: Exception) {
                apiResponse.code = "0"
                apiResponse.message = "Server date is not parsable"
                return apiResponse
            }

            val wordElement =
                document.selectFirst("div.word-and-pronunciation h1:first-of-type")
            val attribute = document.selectFirst("div.word-attributes span.main-attr")
            val pronounce = document.selectFirst("div.word-attributes span.word-syllables")
            val pronounceAudio = document.selectFirst("div.word-and-pronunciation a")
            val pronounceAudioLang =
                pronounceAudio.attributes().get("data-lang").replace("_", "/")
            val pronounceAudioDir = pronounceAudio.attributes().get("data-dir")
            val pronounceAudioFile = pronounceAudio.attributes().get("data-file")
            Log.d("URL", pronounceAudio.attributes().toString())
            //demo audio url https://media.merriam-webster.com/audio/prons/en/us/mp3/s/soleci01.mp3
            val pronounceAudioUrl =
                "https://media.merriam-webster.com/audio/prons/$pronounceAudioLang/mp3/$pronounceAudioDir/$pronounceAudioFile.mp3"
//                        "https://www.merriam-webster.com/word-of-the-day?pronunciation&lang=$pronounceAudioLang&dir=$pronounceAudioDir&file=$pronounceAudioFile"

            val defination = document.select("div.wod-definition-container > p")
            val example = document.select("div.wotd-examples p")

            wordOfTheDay.word = wordElement.text()
            val meanings = ArrayList<String>()
            for (a in defination) {
                meanings.add(a.text())
            }
            wordOfTheDay.meanings = meanings
            wordOfTheDay.attribute = attribute.text()
            wordOfTheDay.pronounce = pronounce.text()
            wordOfTheDay.pronounceAudio = pronounceAudioUrl
            val examples = ArrayList<String>()
            for (a in example) {
                examples.add(a.text())
            }
            wordOfTheDay.examples = examples

            Log.d("WORD OF THE DAY", Gson().toJson(wordOfTheDay))

            if (wordOfTheDay.word!!.isEmpty()) {
                apiResponse.code = "0"
                apiResponse.message = "Unable to fetch new word"
            } else {
                apiResponse.code = "200"
                apiResponse.message = "Found new word"
                apiResponse.data = wordOfTheDay
            }
            return apiResponse
        }
    }
}

