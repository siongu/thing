package com.v2x.thing.ble.bleparser

import android.location.Location
import com.v2x.thing.ble.bleservice.BleService
import com.v2x.thing.ble.bleservice.ServiceType
import com.v2x.thing.headingBetweenPoints
import com.v2x.thing.model.GGAInfo
import com.v2x.thing.model.LatLng
import net.sf.marineapi.nmea.event.SentenceEvent
import net.sf.marineapi.nmea.event.SentenceListener
import net.sf.marineapi.nmea.io.SentenceReader
import net.sf.marineapi.nmea.sentence.GGASentence
import net.sf.marineapi.nmea.sentence.RMCSentence
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors


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
        override fun readingPaused() {}
        override fun readingStarted() {}
        override fun readingStopped() {}
        private var ggaInfo: GGAInfo? = null
        private var lastGGAInfo: GGAInfo? = null
        override fun sentenceRead(event: SentenceEvent) {
            println("parse for device type \"${type.desc}\"")
            try {
                val s = event.sentence
                if ("RMC" == s.sentenceId) {
                    if (type == ServiceType.TK1306) {
                        ggaInfo = GGAInfo()
                    }
                    val rmc = s as RMCSentence
                    println("RMC: $rmc")
                    ggaInfo?.apply {
                        speed = rmc.speed
                        course = rmc.course
                    }
                } else if ("GGA" == s.sentenceId) {
                    if (type == ServiceType.CP200) {
                        ggaInfo = GGAInfo()
                    }
                    val gga = s as GGASentence
                    println("GGA: $gga")
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
                        }
                        dispatchGGA(this)
                        lastGGAInfo = this
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        println("raw data:${data.contentToString()}")
        sb.append(String(data))
        if (sb.length > 10000) {
            sb.delete(0, sb.lastIndexOf("$"))
        }
        println("nmea:$sb")
        pipOut.write(data)
    }
}