package eu.efti.datatools.populate

import java.time.Instant
import kotlin.random.Random
import kotlin.random.asKotlinRandom

@Suppress("MemberVisibilityCanBePrivate")
class EftiValueGeneratorFactory(private val seed: Long) {
    fun forPath(valuePath: ValuePath): EftiValueGenerator =
        EftiValueGenerator(java.util.Random("${valuePath.path.joinToString(".")}.$seed".hashCode().toLong()).asKotlinRandom())

    class EftiValueGenerator(private val random: Random) {
        fun nextAsciiChar(): Char = "abcdefghijklmnopqrstuvwxyz".random(random)

        fun <T> nextChoice(choices: Collection<T>): T = choices.random(random)

        fun nextDouble(startInclusive: Double = 0.0, endExclusive: Double = Double.MAX_VALUE): Double =
            random.nextDouble(startInclusive, endExclusive)

        fun nextBoolean(): Boolean = random.nextBoolean()

        fun nextInstant(): Instant {
            val start = Instant.parse("2020-01-01T00:00:00.00Z").epochSecond
            val end = Instant.parse("2040-01-01T00:00:00.00Z").epochSecond
            val instantEpochSecond = nextLong(start, end)
            return Instant.ofEpochSecond(instantEpochSecond)
        }

        fun nextInt(startInclusive: Int = 0, endExclusive: Int = Int.MAX_VALUE): Int =
            random.nextInt(startInclusive, endExclusive)

        fun nextLong(startInclusive: Long = 0, endExclusive: Long = Long.MAX_VALUE): Long =
            random.nextLong(startInclusive, endExclusive)

        fun nextToken(length: Int = 6): String = (1..length).joinToString("") { nextAsciiChar().toString() }
    }
}
