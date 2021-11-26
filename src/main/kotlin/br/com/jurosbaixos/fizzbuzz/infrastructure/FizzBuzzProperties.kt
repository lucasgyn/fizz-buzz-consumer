package br.com.jurosbaixos.fizzbuzz.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ConfigurationProperties("adapters.output.fizz-buzz")
class FizzBuzzProperties {
    lateinit var baseUrl: String
    lateinit var connTimeout: Duration
    lateinit var connReadTimeout: Duration
}
