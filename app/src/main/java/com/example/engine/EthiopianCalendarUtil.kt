package com.example.engine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EthiopianDate(
    val year: Int,
    val month: Int, // 1..13
    val day: Int,   // 1..30 (Pagume 1..6)
) {
    val monthNameAmharic: String
        get() = ethiopicMonthNamesAmharic.getOrElse(month - 1) { "" }

    val monthNameLatin: String
        get() = ethiopicMonthNamesLatin.getOrElse(month - 1) { "" }

    fun formatEthiopic(): String = "$monthNameAmharic $day, $year"

    fun formatEthiopicWithGeezNumerals(): String =
        "$monthNameAmharic ${toGeezNumber(day)}, ${toGeezNumber(year)}"

    fun formatLatin(): String = "$monthNameLatin $day, $year E.C."

    companion object {
        private const val ETHIOPIC_EPOCH_JDN = 1723856

        private val ethiopicMonthNamesAmharic = listOf(
            "መስከረም", "ጥቅምት", "ሕዳር", "ታኅሣሥ", "ጥር", "የካቲት",
            "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
        )

        private val ethiopicMonthNamesLatin = listOf(
            "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
            "Megabit", "Miyazya", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
        )

        fun today(): EthiopianDate = fromGregorian(LocalDate.now())

        fun fromGregorian(date: LocalDate): EthiopianDate {
            val gYear = date.year
            val gMonth = date.monthValue
            val gDay = date.dayOfMonth

            val a = (14 - gMonth) / 12
            val y = gYear + 4800 - a
            val m = gMonth + 12 * a - 3
            val jdn = gDay + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045

            val r = (jdn - ETHIOPIC_EPOCH_JDN) % 1461
            val n = (r % 365) + 365 * (r / 1460)
            val year = 4 * ((jdn - ETHIOPIC_EPOCH_JDN) / 1461) + r / 365 - r / 1460
            val month = n / 30 + 1
            val day = (n % 30) + 1
            return EthiopianDate(year = year, month = month, day = day)
        }

        fun formatGregorian(date: LocalDate = LocalDate.now()): String =
            date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

        fun toGeezNumber(number: Int): String {
            if (number <= 0) return number.toString()
            val units = arrayOf("", "፩", "፪", "፫", "፬", "፭", "፮", "፯", "፰", "፱")
            val tens = arrayOf("", "፲", "፳", "፴", "፵", "፶", "፷", "፸", "፹", "፺")

            fun convertTwoDigits(n: Int): String {
                val t = n / 10
                val u = n % 10
                return "${tens[t]}${units[u]}"
            }

            if (number < 100) return convertTwoDigits(number)
            val hundreds = number / 100
            val remainder = number % 100
            val prefix = if (hundreds == 1) "" else convertTwoDigits(hundreds)
            val suffix = if (remainder == 0) "" else convertTwoDigits(remainder)
            return "${prefix}፻$suffix"
        }
    }
}
