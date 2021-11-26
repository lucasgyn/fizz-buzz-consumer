package br.com.jurosbaixos.fizzbuzz.app.domain

sealed class FizzBuzzException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class FizzBuzzRequestException(message: String, cause: Throwable) : FizzBuzzException(message, cause)
class FizzBuzzClientErrorException(message: String) : FizzBuzzException(message)
class FizzBuzzInternalErrorException(message: String) : FizzBuzzException(message)
class FizzBuzzUnexpectedResponseException(message: String) : FizzBuzzException(message)