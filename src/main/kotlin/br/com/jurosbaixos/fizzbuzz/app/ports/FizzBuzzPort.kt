package br.com.jurosbaixos.fizzbuzz.app.ports

interface FizzBuzzPort {

    suspend fun retrieveNumbers(): Collection<Int>
    suspend fun translate(shaHash: String, fizzBuzz: String)
    suspend fun findTreasure(shaHash: String): Boolean
    suspend fun delete(shaHash: String)
    suspend fun reset()
}
