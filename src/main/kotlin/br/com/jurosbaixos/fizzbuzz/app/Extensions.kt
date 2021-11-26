package br.com.jurosbaixos.fizzbuzz.app

import com.google.gson.Gson

private const val FIZZ = "fizz"
private const val BUZZ = "buzz"
private const val FIZZ_BUZZ = "fizzbuzz"

fun Int.toFizzBuzz(): String =
    when {
        this.divisibleBy(15) -> FIZZ_BUZZ
        this.divisibleBy(3) -> FIZZ
        this.divisibleBy(5) -> BUZZ
        else -> this.toString()
    }

fun Int.divisibleBy(number: Int) = this % number == 0

fun Collection<Any>.toJson(): String = Gson().toJson(this)
