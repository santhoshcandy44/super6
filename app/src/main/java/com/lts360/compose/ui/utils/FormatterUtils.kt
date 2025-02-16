package com.lts360.compose.ui.utils

import android.util.Patterns
import java.text.CharacterIterator
import java.text.NumberFormat
import java.text.StringCharacterIterator
import java.util.Currency
import java.util.Locale

object FormatterUtils {

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val locale = when (currencyCode) {
            "INR" -> Locale("en", "IN")  // India Locale for INR
            "USD" -> Locale("en", "US")  // US Locale for USD
            else -> Locale.getDefault()  // Default locale if currency code is unknown
        }

        val currency = Currency.getInstance(currencyCode)

        // Get an instance of NumberFormat for the appropriate currency
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = currency  // Set the currency type

        return format.format(amount)
    }


    fun humanReadableBytesSize(bytes: Long): String {
        val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        if (absB < 1024) {
            return "${bytes}B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        return String.format(
            Locale("en", "IN"),
            "%.2f%cB",
            value / 1024.0,
            ci.current()
        ) // Explicit Locale used
    }




    fun isValidEmail(email: String): Boolean {
        // Implement email validation logic
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}