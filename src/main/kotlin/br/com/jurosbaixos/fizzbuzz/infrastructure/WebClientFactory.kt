package br.com.jurosbaixos.fizzbuzz.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

class WebClientFactory {

    companion object {
        fun build(
            baseUrl: String,
            connTimeout: Duration,
            connReadTimeout: Duration,
        ): WebClient =
            WebClient.builder()
                .clientConnector(
                    clientHttpConnector(
                        connectionTimeout = connTimeout,
                        readTimeout = connReadTimeout
                    )
                )
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies(jacksonObjectMapper()))
                .build()

        private fun clientHttpConnector(
            connectionTimeout: Duration,
            readTimeout: Duration
        ): ReactorClientHttpConnector =
            ReactorClientHttpConnector(
                httpClient(
                    connectionTimeout = connectionTimeout,
                    readTimeout = readTimeout
                )
            )

        private fun httpClient(connectionTimeout: Duration, readTimeout: Duration) =
            HttpClient
                .create(connectionProvider())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout.toMillis().toInt())
                .doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(readTimeout.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(WriteTimeoutHandler(readTimeout.toSeconds(), TimeUnit.SECONDS))
                }

        private fun connectionProvider() =
            ConnectionProvider.builder("fixed")
                .maxConnections(200)
                .build()

        private fun strategies(objectMapper: ObjectMapper): ExchangeStrategies =
            ExchangeStrategies
                .builder()
                .codecs { clientDefaultCodecsConfigurer: ClientCodecConfigurer ->
                    clientDefaultCodecsConfigurer.defaultCodecs()
                        .jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
                    clientDefaultCodecsConfigurer.defaultCodecs()
                        .jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
                }
                .build()

    }
}
