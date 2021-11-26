package br.com.jurosbaixos.fizzbuzz.adapters.output

import br.com.jurosbaixos.fizzbuzz.app.domain.*
import br.com.jurosbaixos.fizzbuzz.app.ports.FizzBuzzPort
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@Service
class FizzBuzzAdapter(
    @Value("\${adapters.output.fizz-buzz.api-key}") private val apiKey: String,
    private val fizzBuzzWebClient: WebClient
) : FizzBuzzPort {

    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
        const val X_API_KEY_HEADER_NAME = "X-API-KEY"
        const val HASH_ERROR_HTTP_STATUS = 450
    }

    override suspend fun retrieveNumbers(): Collection<Int> = try {
        logger.info { "[Fizz Buzz API] Retrieving numbers" }
        fizzBuzzWebClient.get()
            .uri("/fizzbuzz")
            .header(X_API_KEY_HEADER_NAME, apiKey)
            .accept(APPLICATION_JSON)
            .awaitExchange { response ->
                when (val statusCode = response.rawStatusCode()) {
                    OK.value() -> {
                        val numbers = response.awaitBody<ArrayList<Int>>()
                        logger.info { "[Fizz Buzz API] Numbers retrieved successfully $numbers" }
                        numbers
                    }
                    UNAUTHORIZED.value() -> {
                        val errorResponse = response.awaitBody<FizzBuzzErrorResponse>()
                        throw FizzBuzzClientErrorException(
                            message = "[Fizz Buzz API] Sorry, unable to retrieve numbers. Reason: '$errorResponse'. Got the status '$statusCode'"
                        )
                    }
                    INTERNAL_SERVER_ERROR.value() -> throw FizzBuzzInternalErrorException(
                        message = "[Fizz Buzz API] Sorry, an internal failure occurred and the numbers could not be retrieved. Got the status '$statusCode'"
                    )
                    else -> throw FizzBuzzUnexpectedResponseException(
                        message = "[Fizz Buzz API] Sorry, we received an unexpected response when trying to retrieve numbers. Got the status '$statusCode'"
                    )
                }
            }
    } catch (e: WebClientRequestException) {
        throw FizzBuzzRequestException(
            message = "[Fizz Buzz API] Sorry, request failed when trying to retrieve numbers",
            cause = e
        )
    }

    override suspend fun translate(shaHash: String, fizzBuzz: String) {
        try {
            logger.info { "[Fizz Buzz API] Translating '$shaHash' '$fizzBuzz'" }
            fizzBuzzWebClient.post()
                .uri("/fizzbuzz/{shaHash}", shaHash)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(fizzBuzz)
                .awaitExchange { response ->
                    when (val statusCode = response.rawStatusCode()) {
                        OK.value() -> logger.info { "[Fizz Buzz API] Translation performed successfully" }
                        BAD_REQUEST.value(), UNAUTHORIZED.value(), NOT_FOUND.value(), HASH_ERROR_HTTP_STATUS -> {
                            val errorResponse = response.awaitBody<FizzBuzzErrorResponse>()
                            throw FizzBuzzClientErrorException(
                                message = "[Fizz Buzz API] Sorry, the translation was not possible. Reason: '$errorResponse'. Got the status '$statusCode'"
                            )
                        }
                        INTERNAL_SERVER_ERROR.value() -> throw FizzBuzzInternalErrorException(
                            message = "[Fizz Buzz API] Sorry, an internal failure occurred and the translation could not be performed. Got the status '$statusCode'"
                        )
                        else -> throw FizzBuzzUnexpectedResponseException(
                            message = "[Fizz Buzz API] Sorry, we received an unexpected response when trying to translate. Got the status '$statusCode'"
                        )
                    }
                }
        } catch (e: WebClientRequestException) {
            throw FizzBuzzRequestException(
                message = "[Fizz Buzz API] Sorry, the request failed while trying to translate",
                cause = e
            )
        }
    }

    override suspend fun findTreasure(shaHash: String): Boolean = try {
        logger.info { "[Fizz Buzz API] Finding the treasure '$shaHash'" }
        fizzBuzzWebClient.get()
            .uri("/fizzbuzz/{shaHash}/canihastreasure", shaHash)
            .header(X_API_KEY_HEADER_NAME, apiKey)
            .accept(APPLICATION_JSON)
            .awaitExchange { response ->
                when (val statusCode = response.rawStatusCode()) {
                    OK.value() -> {
                        logger.info { "[Fizz Buzz API] Treasure found successfully '$shaHash'" }
                        true
                    }
                    BAD_REQUEST.value() -> {
                        logger.info { "[Fizz Buzz API] Treasure not found '$shaHash'" }
                        false
                    }
                    UNAUTHORIZED.value(), HASH_ERROR_HTTP_STATUS -> {
                        val errorResponse = response.awaitBody<FizzBuzzErrorResponse>()
                        throw FizzBuzzClientErrorException(
                            message = "[Fizz Buzz API] Sorry, couldn't find the treasure '$shaHash'. Reason: '$errorResponse'. Got the status '$statusCode'"
                        )
                    }
                    INTERNAL_SERVER_ERROR.value() -> throw FizzBuzzInternalErrorException(
                        message = "[Fizz Buzz API] Sorry, an internal failure occurred and the treasure could not be found '$shaHash'. Got the status '$statusCode'"
                    )
                    else -> throw FizzBuzzUnexpectedResponseException(
                        message = "[Fizz Buzz API] Sorry, we received an unexpected response when trying to find the treasure '$shaHash'. Got the status '$statusCode'"
                    )
                }
            }
    } catch (e: WebClientRequestException) {
        throw FizzBuzzRequestException(
            message = "[Fizz Buzz API] Sorry, request failed while trying to find the treasure '$shaHash'",
            cause = e
        )
    }

    override suspend fun delete(shaHash: String) {
        try {
            logger.info { "[Fizz Buzz API] Deleting SHA256 '$shaHash'" }
            fizzBuzzWebClient.delete()
                .uri("/fizzbuzz/{shaHash}", shaHash)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .accept(APPLICATION_JSON)
                .awaitExchange { response ->
                    when (val statusCode = response.rawStatusCode()) {
                        OK.value() -> logger.info { "[Fizz Buzz API] SHA256 '$shaHash' deleted successfully" }
                        UNAUTHORIZED.value(), NOT_FOUND.value(), HASH_ERROR_HTTP_STATUS -> {
                            val errorResponse = response.awaitBody<FizzBuzzErrorResponse>()
                            throw FizzBuzzClientErrorException(
                                message = "[Fizz Buzz API] Sorry, could not delete SHA256 '$shaHash'. Reason: '$errorResponse'. Got the status '$statusCode'"
                            )
                        }
                        INTERNAL_SERVER_ERROR.value() -> throw FizzBuzzInternalErrorException(
                            message = "[Fizz Buzz API] Sorry, an internal failure occurred and cannot delete SHA256 '$shaHash'. Got the status '$statusCode'"
                        )
                        else -> throw FizzBuzzUnexpectedResponseException(
                            message = "[Fizz Buzz API] Sorry, we received an unexpected response when trying to delete SHA256 '$shaHash'. Got the status '$statusCode'"
                        )
                    }
                }
        } catch (e: WebClientRequestException) {
            throw FizzBuzzRequestException(
                message = "[Fizz Buzz API] Sorry, request failed while trying to delete SHA256 '$shaHash'",
                cause = e
            )
        }
    }

    override suspend fun reset() {
        try {
            logger.info { "[Fizz Buzz API] Restarting session" }
            fizzBuzzWebClient.get()
                .uri("/fizzbuzz/reset")
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .accept(APPLICATION_JSON)
                .awaitExchange { response ->
                    when (val statusCode = response.rawStatusCode()) {
                        OK.value() -> logger.info { "[Fizz Buzz API] Session restarted successfully" }
                        UNAUTHORIZED.value() -> {
                            val errorResponse = response.awaitBody<FizzBuzzErrorResponse>()
                            throw FizzBuzzClientErrorException(
                                message = "[Fizz Buzz API] Sorry, unable to restart session. Reason: '$errorResponse'. Got the status '$statusCode'"
                            )
                        }
                        FORBIDDEN.value() -> logger.info { "[Fizz Buzz API] Session released" }
                        INTERNAL_SERVER_ERROR.value() -> throw FizzBuzzInternalErrorException(
                            message = "[Fizz Buzz API] Sorry, there was an internal failure and it was not possible to restart the session. Got the status '$statusCode'"
                        )
                        else -> throw FizzBuzzUnexpectedResponseException(
                            message = "[Fizz Buzz API] Sorry, we received an unexpected response when trying to restart the session. Got the status '$statusCode'"
                        )
                    }
                }
        } catch (e: WebClientRequestException) {
            throw FizzBuzzRequestException(
                message = "[Fizz Buzz API] Sorry, the request failed while trying to restart the session",
                cause = e
            )
        }
    }
}
