package com.v2x.thing.ble.bleparser

import com.v2x.thing.ble.bleservice.BleService
import com.v2x.thing.ble.bleservice.ServiceType
import com.v2x.thing.headingBetweenPoints
import com.v2x.thing.model.GGAInfo
import com.v2x.thing.model.LatLng
import com.v2x.thing.model.SpeedInfo
import com.v2x.thing.speedBetweenPoints
import net.sf.marineapi.nmea.event.SentenceEvent
import net.sf.marineapi.nmea.event.SentenceListener
import net.sf.marineapi.nmea.io.SentenceReader
import net.sf.marineapi.nmea.sentence.GGASentence
import net.sf.marineapi.nmea.sentence.RMCSentence
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import java.util.regex.Matcher


class GpsNmeaParser private constructor(private val type: ServiceType) : Parser {

    companion object {
        fun newInstance(type: ServiceType): GpsNmeaParser {
            return GpsNmeaParser(type)
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
            println("readingPaused")
        }

        override fun readingStarted() {
            println("readingStarted")
        }

        override fun readingStopped() {
            println("readingStopped")
        }

        private var ggaInfo: GGAInfo? = null
        private var lastGGAInfo: GGAInfo? = null
        private var frameAvailable = false
        override fun sentenceRead(event: SentenceEvent) {
            println("parse for device type \"${type.desc}\"")
            when (type) {
                ServiceType.CP200_SINGLE_OUTPUT -> singleData(event)
                ServiceType.TK1306,
                ServiceType.CP200_DUAL_OUTPUT -> dualData(event)
            }
        }

        private fun singleData(event: SentenceEvent) {
            try {
                val s = event.sentence
                if ("GGA" == s.sentenceId) {
                    val gga = s as GGASentence
                    println("GGA: $gga")
                    ggaInfo = GGAInfo()
                    ggaInfo?.apply {
                        latitude = gga.position.latitude
                        longitude = gga.position.longitude
                        altitude = gga.altitude
                        gpsFixQuality = gga.fixQuality.toInt()
                        satelliteCount = gga.satelliteCount
                        gpsTimeInMills = gga.time.milliseconds
                        lastGGAInfo?.let { last ->
                            course = headingBetweenPoints(
                                LatLng(last.latitude, last.longitude),
                                LatLng(latitude, longitude)
                            )
                            val sp = speedBetweenPoints(
                                LatLng(last.latitude, last.longitude),
                                LatLng(latitude, longitude),
                                durationInMills = (gpsTimeInMills - last.gpsTimeInMills)
                            )
                            speed = if (sp == Double.MAX_VALUE) last.speed else sp
                        }
                        dispatchGGA(this)
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
                    println("RMC: $rmc")
                    if (!frameAvailable) {
                        frameAvailable = true
                        ggaInfo = GGAInfo()
                        ggaInfo?.apply {
                            speed = rmc.speed
                            course = rmc.course
                        }
                    } else {
                        frameAvailable = false
                        ggaInfo?.apply {
                            speed = rmc.speed
                            course = rmc.course
                            dispatchGGA(this)
                            lastGGAInfo = this
                        }
                    }
                } else if ("GGA" == s.sentenceId) {
                    val gga = s as GGASentence
                    println("GGA: $gga")
                    if (!frameAvailable) {
                        frameAvailable = true
                        ggaInfo = GGAInfo()
                        setGGAInfo(gga)
                    } else {
                        frameAvailable = false
                        setGGAInfo(gga)
                        ggaInfo?.apply {
                            dispatchGGA(this)
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
                lastGGAInfo?.let { last ->
                    course = headingBetweenPoints(
                        LatLng(last.latitude, last.longitude),
                        LatLng(latitude, longitude)
                    )
                    val sp = speedBetweenPoints(
                        LatLng(last.latitude, last.longitude),
                        LatLng(latitude, longitude),
                        durationInMills = (gpsTimeInMills - last.gpsTimeInMills)
                    )
                    speed = if (sp == Double.MAX_VALUE) last.speed else sp
                }
            }
        }


    }

    private fun dispatchGGA(ggaInfo: GGAInfo) {
        println("ggaInfo:${ggaInfo}")
        BleService.INSTANCE.getDispatchers(type).forEach {
            it.dispatchGGA(ggaInfo)
        }
    }


    override fun parseData(data: ByteArray) {
        executors.execute {
            task(data)
        }
    }

    private val sb = StringBuffer()
    private fun task(data: ByteArray) {
        val raw = data.contentToString()
        println("raw data:${raw}")
        sb.append(String(data))
        if (sb.length > 20000) {
            sb.delete(0, sb.lastIndexOf("$"))
        }
        println("nmea:$sb")
        var rd = nmeaFilter(data)
        pipOut.write(rd)
    }

    private fun nmeaFilter(nmeaData: ByteArray): ByteArray {
        val raw = String(nmeaData)
        var r = raw.replace(Regex.fromLiteral("\n"), "")
            .replace(Regex.fromLiteral("\r"), "")
            .replace(Regex.fromLiteral(" "), "")
            .replace(Regex.fromLiteral("$"), Matcher.quoteReplacement("\r\n$"))
        val rd = r.toByteArray()
        println("nmeaFilter:${String(rd)}")
        return rd
    }
}