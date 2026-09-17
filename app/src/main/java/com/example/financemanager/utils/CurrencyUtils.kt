package com.example.financemanager.utils

import java.text.NumberFormat
import java.util.Locale

/** All money stored as Long paise (1 Rupee = 100 paise) to avoid float rounding errors. */
object CurrencyUtils {
    private val inrFormat: NumberFormat by lazy { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    fun rupeesToPaise(rupees: String): Long {
        val value = rupees.trim().toDoubleOrNull() ?: return 0L
        return Math.round(value * 100.0)
    }

    fun paiseToRupeesDouble(paise: Long): Double = paise / 100.0

    fun formatPaise(paise: Long): String = inrFormat.format(paiseToRupeesDouble(paise))
}
