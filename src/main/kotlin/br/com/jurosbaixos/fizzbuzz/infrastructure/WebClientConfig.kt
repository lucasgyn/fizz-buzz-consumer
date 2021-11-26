package br.com.jurosbaixos.fizzbuzz.infrastructure

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebClientConfig(
    private val fizzBuzzProperties: FizzBuzzProperties,
) {
    @Bean
    fun fizzBuzzWebClient() = WebClientFactory.build(
        baseUrl = fizzBuzzProperties.baseUrl,
        connTimeout = fizzBuzzProperties.connTimeout,
        connReadTimeout = fizzBuzzProperties.connReadTimeout
    )
}
