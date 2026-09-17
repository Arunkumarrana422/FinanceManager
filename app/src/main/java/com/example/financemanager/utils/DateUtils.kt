package com.example.financemanager.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(millis: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

    fun monthKey(millis: Long): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))

    fun currentMonthKey(): String = monthKey(System.currentTimeMillis())

    fun monthKeyToLabel(monthKey: String): String = try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(sdf.parse(monthKey)!!)
    } catch (e: Exception) { monthKey }

    fun isToday(millis: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = millis }
        val b = Calendar.getInstance()
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    fun lastNMonthKeys(n: Int): List<String> {
        val result = mutableListOf<String>()
        val cal = Calendar.getInstance()
        repeat(n) {
            result.add(monthKey(cal.timeInMillis))
            cal.add(Calendar.MONTH, -1)
        }
        return result
    }
}
