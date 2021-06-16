package com.v2x.thing.ble.bleparser

import com.v2x.thing.ble.bleservice.BleService
import com.v2x.thing.model.GGAInfo
import net.sf.marineapi.nmea.event.SentenceEvent
import net.sf.marineapi.nmea.event.SentenceListener
import net.sf.marineapi.nmea.io.SentenceReader
import net.sf.marineapi.nmea.sentence.GGASentence
import net.sf.marineapi.nmea.sentence.GLLSentence
import net.sf.marineapi.nmea.sentence.RMCSentence
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors


class GpsNmeaParser private constructor() : Parser {
    companion object {
        fun newInstance(): GpsNmeaParser {
            return GpsNmeaParser()
        }

        private fun dispatchGGA(ggaInfo: GGAInfo) {
            println("ggaInfo:${ggaInfo}")
            BleService.INSTANCE.getDispatchers().forEach {
                it.dispatchGGA(ggaInfo)
            }
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

    class MultiSentenceListener : SentenceListener {
        override fun readingPaused() {}
        override fun readingStarted() {}
        override fun readingStopped() {}
        var ggaInfo: GGAInfo? = null
        override fun sentenceRead(event: SentenceEvent) {
            try {
                val s = event.sentence
                if ("RMC" == s.sentenceId) {
                    ggaInfo = GGAInfo()
                    val rmc = s as RMCSentence
                    println("RMC: $rmc")
                    ggaInfo?.apply {
                        speed = rmc.speed
                        course = rmc.correctedCourse
                    }
                } else if ("GGA" == s.sentenceId) {
                    val gga = s as GGASentence
                    println("GGA: $gga")
                    ggaInfo?.apply {
                        latitude = gga.position.latitude
                        longitude = gga.position.longitude
                        altitude = gga.altitude
                        gpsFixQuality = gga.fixQuality.toInt()
                        satelliteCount = gga.satelliteCount
                        gpsTimeInMills = gga.time.milliseconds
                        dispatchGGA(this)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    override fun parseData(data: ByteArray) {
        executors.execute {
            task(data)
        }
    }

    private fun task(data: ByteArray) {
        println("raw data:${data.contentToString()}")
        pipOut.write(data)
    }
}