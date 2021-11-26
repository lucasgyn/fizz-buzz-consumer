package br.com.jurosbaixos.fizzbuzz

import br.com.jurosbaixos.fizzbuzz.app.service.FizzBuzzService
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile

@Profile("!test")
@SpringBootApplication
class FizzBuzzConsumerApplication(
    private val fizzBuzzService: FizzBuzzService
) : CommandLineRunner {

    override fun run(vararg args: String?) = runBlocking {
        fizzBuzzService.reset()
        fizzBuzzService.execute()
    }
}

fun main(args: Array<String>) {
    runApplication<FizzBuzzConsumerApplication>(*args)
}
