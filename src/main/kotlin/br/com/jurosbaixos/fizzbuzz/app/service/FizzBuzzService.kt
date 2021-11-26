package br.com.jurosbaixos.fizzbuzz.app.service

import br.com.jurosbaixos.fizzbuzz.app.ports.FizzBuzzPort
import br.com.jurosbaixos.fizzbuzz.app.toFizzBuzz
import br.com.jurosbaixos.fizzbuzz.app.toJson
import mu.KLogger
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

@Service
class FizzBuzzService(
    private val fizzBuzzPort: FizzBuzzPort
) {

    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
    }

    suspend fun reset() {
        fizzBuzzPort.reset()
    }

    suspend fun execute() {
        while (true) {
            try {
                val numbers = fizzBuzzPort.retrieveNumbers()
                val fizzBuzzJson = numbers.map { number -> number.toFizzBuzz() }.toJson()
                val shaHash = DigestUtils.sha256Hex(fizzBuzzJson)
                fizzBuzzPort.translate(shaHash, fizzBuzzJson)
                if (fizzBuzzPort.findTreasure(shaHash)) {
                    logger.info { "[Fizz Buzz] Wow! The treasure has been found :)" }
                    exitProcess(0)
                } else {
                    fizzBuzzPort.delete(shaHash)
                    logger.info { "[Fizz Buzz] The treasure has not yet been found :( Let's try again..." }
                    execute()
                }
            } catch (e: Exception) {
                logger.error { e }
                logger.info { "[Fizz Buzz] A failure has occurred :( Let's try again..." }
            }
        }
    }
}
