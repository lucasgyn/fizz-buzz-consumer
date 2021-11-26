package br.com.jurosbaixos.fizzbuzz.app.domain

data class FizzBuzzErrorResponse(
    val message: String,
    val errors: Collection<Error>
) {
    data class Error(
        val message: String,
        val path: String
    )

    override fun toString(): String {
        val error = errors.first()
        return "[Message: $message, Error: ${error.message}, Path: ${error.path}]"
    }
}
