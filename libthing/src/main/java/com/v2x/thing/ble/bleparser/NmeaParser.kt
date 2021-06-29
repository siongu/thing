package com.v2x.thing.ble.bleparser

import android.util.Log
import com.v2x.thing.ble.bleservice.*
import com.v2x.thing.model.NmeaInfo
import net.sf.marineapi.nmea.event.SentenceEvent
import net.sf.marineapi.nmea.event.SentenceListener
import net.sf.marineapi.nmea.io.SentenceReader
import net.sf.marineapi.nmea.sentence.GGASentence
import net.sf.marineapi.nmea.sentence.RMCSentence
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import java.util.regex.Matcher


class NmeaParser private constructor(serviceType: ServiceType, dispatcher: Dispatcher?) :
    AbstractParser(serviceType, dispatcher) {

    companion object {
        fun getInstance(type: ServiceType, dispatcher: Dispatcher? = null): NmeaParser {
            return NmeaParser(type, dispatcher)
        }
    }

    private val executors = Executors.newSingleThreadExecutor()
    private val pipIn = PipedInputStream()
    private val pipOut = PipedOutputStream()
    private val sentenceReader = SentenceReader(pipIn)

    init {
        pipIn.connect(pipOut)
        sentenceReader.addSentenceListener(MultiSentenceListener())
        sentenceReader.start()
    }

    inner class MultiSentenceListener : SentenceListener {
        override fun readingPaused() {
            Log.d(TAG, "readingPaused")
        }

        override fun readingStarted() {
            Log.d(TAG, "readingStarted")
        }

        override fun readingStopped() {
            Log.d(TAG, "readingStopped")
        }

        private var ggaInfo: NmeaInfo? = null
        private var lastGGAInfo: NmeaInfo? = null
        private var frameAvailable = false
        override fun sentenceRead(event: SentenceEvent) {
            Log.d(TAG, "sentenceRead: ${event.sentence}")
            Log.d(TAG, "parse for device type: ${serviceType.desc}")
            when (getType()) {
                CP200Single -> singleData(event)
                TK1306,
                CP200Dual -> dualData(event)
                else -> {
                    Log.d(TAG, "sentenceRead: no available parser for type:${serviceType.desc}")
                }
            }
        }

        private fun singleData(event: SentenceEvent) {
            try {
                val s = event.sentence
                if ("GGA" == s.sentenceId) {
                    val gga = s as GGASentence
                    Log.d(TAG, "singleData GGA: $gga")
                    ggaInfo = NmeaInfo()
                    ggaInfo?.apply {
                        latitude = gga.position.latitude
                        longitude = gga.position.longitude
                        altitude = gga.altitude
                        gpsFixQuality = gga.fixQuality.toInt()
                        satelliteCount = gga.satelliteCount
                        gpsTimeInMills = gga.time.milliseconds
                        lastGGAInfo?.let { last ->
//                            course = headingBetweenPoints(
//                                LatLng(last.latitude, last.longitude),
//                                LatLng(latitude, longitude)
//                            )
//                            val sp = speedBetweenPoints(
//                                LatLng(last.latitude, last.longitude),
//                                LatLng(latitude, longitude),
//                                durationInMills = (gpsTimeInMills - last.gpsTimeInMills)
//                            )
//                            speed = if (sp < 0) last.speed else sp
                        }
                        dispatch(this)
                        lastGGAInfo = this
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun dualData(event: SentenceEvent) {
            try {
                val s = event.sentence
                if ("RMC" == s.sentenceId) {
                    val rmc = s as RMCSentence
                    Log.d(TAG, "dualData RMC: $rmc")
                    if (!frameAvailable) {
                        frameAvailable = true
                        ggaInfo = NmeaInfo()
                        ggaInfo?.apply {
                            rmc.position?.let { pos ->
                                latitude = pos.latitude
                                longitude = pos.longitude
                            }
                            speed = rmc.speed
                            course = rmc.course
                        }
                    } else {
                        frameAvailable = false
                        ggaInfo?.apply {
                            speed = rmc.speed
                            course = rmc.course
                            dispatch(this)
                            lastGGAInfo = this
                        }
                    }
                } else if ("GGA" == s.sentenceId) {
                    val gga = s as GGASentence
                    Log.d(TAG, "dualData GGA: $gga")
                    if (!frameAvailable) {
                        frameAvailable = true
                        ggaInfo = NmeaInfo()
                        setGGAInfo(gga)
                    } else {
                        frameAvailable = false
                        setGGAInfo(gga)
                        ggaInfo?.apply {
                            dispatch(this)
                            lastGGAInfo = this
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun setGGAInfo(gga: GGASentence) {
            ggaInfo?.apply {
                latitude = gga.position.latitude
                longitude = gga.position.longitude
                altitude = gga.altitude
                gpsFixQuality = gga.fixQuality.toInt()
                satelliteCount = gga.satelliteCount
                gpsTimeInMills = gga.time.milliseconds
//                lastGGAInfo?.let { last ->
//                    course = headingBetweenPoints(
//                        LatLng(last.latitude, last.longitude),
//                        LatLng(latitude, longitude)
//                    )
//                    val sp = speedBetweenPoints(
//                        LatLng(last.latitude, last.longitude),
//                        LatLng(latitude, longitude),
//                        durationInMills = (gpsTimeInMills - last.gpsTimeInMills)
//                    )
//                    speed = if (sp < 0) last.speed else sp
//                }
            }
        }


    }

    private fun dispatch(nmeaInfo: NmeaInfo) {
        Log.d(TAG, "dispatchNmea:${nmeaInfo}")
        if (serviceType is SpecifiedType)
            BleService.INSTANCE.getDispatcher(serviceType).let {
                if (it is NmeaDispatcher) it.dispatchNmea(nmeaInfo)
            }
        if (dispatcher is NmeaDispatcher) {
            dispatcher.dispatchNmea(nmeaInfo)
        }
    }


    override fun parseData(data: ByteArray) {
        executors.execute {
            super.parseData(data)
            task(data)
        }
    }

    private val sb = StringBuffer()
    private fun task(data: ByteArray) {
        val raw = data.contentToString()
        Log.d(TAG, "raw data:${raw}")
        sb.append(String(data))
        if (sb.length > 20000) {
            val index = sb.lastIndexOf("$")
            sb.delete(0, if (index <= 0) sb.length else index)
        }
        Log.d(TAG, "nmea:$sb")
        var rd = data
        rd = nmeaFilter(rd)
        pipOut.write(rd)
    }

    private fun nmeaFilter(nmeaData: ByteArray): ByteArray {
        val raw = String(nmeaData)
        var r = raw.replace(Regex.fromLiteral("\t\n|\r|\\s"), "")
            .replace(Regex.fromLiteral("$"), Matcher.quoteReplacement("\r\n$"))
        val rd = r.toByteArray()
        Log.d(TAG, "nmeaFilter: ${String(rd)}")
        return rd
    }
}