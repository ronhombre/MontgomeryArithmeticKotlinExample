//These values below should be saved since we assume that Q is a constant.
//The Q value for Kyber (ML-KEM). This is a prime number ((2^8 * 13) + 1)
const val Q: Short = 3329
//Negative Modular Inverse of Q base 2^16
const val Q_INV = -62209
//Base 2^16.
const val R_shift = 16
//R
const val R = 1 shl R_shift
//R * R = R^2
const val R_squared = R.toLong() shl R_shift

fun main() {
    println("Short Int Montgomery Arithmetic in Kotlin")
    println("Author: Ron Lauren Hombre\n")

    val a: Short = 3228
    val b: Short = 3228

    val aMont = toMontgomeryForm(a)
    val bMont = toMontgomeryForm(b)

    val mulMont = montgomeryMultiply(aMont, bMont)

    //Simple Test Case
    println("Modulo    : " + (a * b % Q))
    println("Montgomery: " + montgomeryReduce(mulMont.toInt()))

    //Comprehensive Test Case
    comprehensiveTest()
}

fun comprehensiveTest() {
    //0 up to 32768 (excluding 32768).
    println("Running comprehensive test [0, 32768)...")
    var count = 0
    var correct = 0
    for(i in 0..<Short.MAX_VALUE.toInt()) {
        count++

        val certain = (i * (Q - 1) % Q).toShort()

        val aMont = toMontgomeryForm(i.toShort())
        val bMont = toMontgomeryForm((Q - 1).toShort())

        val mulMont = montgomeryMultiply(aMont, bMont)

        val guess = montgomeryReduce(mulMont.toInt())

        if(guess == certain) correct++
        else println("$i/$certain/$guess")
    }

    println("Ratio(Correct/Total/Ratio): $correct/$count/" + (correct/count.toDouble() * 100) + "%")
}

//Convert values to Montgomery Form
fun toMontgomeryForm(a: Short): Short {
    //Here R_squared % Q can be precomputed and Barrett Reduction can be used for a % Q.
    // (a * R_squared) % Q = ((a % Q) * (R_squared % Q)) % Q
    return montgomeryReduce((a * R_squared % Q).toInt())
}

/*
 * Montgomery Reduction (REDC)
 * Source: Montgomery modular multiplication. (2023, November 28). In Wikipedia. https://en.wikipedia.org/wiki/Montgomery_modular_multiplication
 */
fun montgomeryReduce(t: Int): Short {
    //N  = Q
    //N' = Q_INV
    //TN' mod R
    //Modulo for base 2 values is a simple AND operation.
    val m = (t * Q_INV) and ((R shl 1) - 1) //0xFFFF
    //(T + mN) / R
    val u = (t + m * Q) ushr R_shift
    return if (u >= Q) (u - Q).toShort()
    else u.toShort()
}

fun montgomeryMultiply(a: Short, b: Short): Short {
    val t = a * b //Automatically converts to Int

    return montgomeryReduce(t)
}
