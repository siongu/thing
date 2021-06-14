package com.v2x.thing.ble.bleparser

import com.v2x.thing.ble.bleservice.BleService
import com.v2x.thing.model.GGAInfo
import net.sf.marineapi.nmea.event.SentenceEvent
import net.sf.marineapi.nmea.event.SentenceListener
import net.sf.marineapi.nmea.io.SentenceReader
import net.sf.marineapi.nmea.sentence.GGASentence
import net.sf.marineapi.nmea.sentence.GLLSentence
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors


class GpsNmeaParser : Parser {
    companion object {
        val INSTANCE = GpsNmeaParser()
        private fun dispatchGGA(gga: GGASentence) {
            val ggaInfo = GGAInfo(
                gga.position.latitude,
                gga.position.longitude,
                gga.altitude,
                gga.fixQuality.toInt(),
                gga.satelliteCount,
                gga.time.milliseconds
            )
            BleService.INSTANCE.getDispatchers().forEach {
                it.dispatchGGA(ggaInfo)
            }
        }
    }

    private constructor()

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
        override fun sentenceRead(event: SentenceEvent) {
            try {
                val s = event.sentence
                if ("GLL" == s.sentenceId) {
                    val gll = s as GLLSentence
                    println("GLL: $gll")
                } else if ("GGA" == s.sentenceId) {
                    val gga = s as GGASentence
                    dispatchGGA(gga)
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