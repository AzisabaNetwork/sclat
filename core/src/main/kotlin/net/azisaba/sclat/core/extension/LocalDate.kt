package net.azisaba.sclat.core.extension

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val SIMPLE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun LocalDate.toSimpleFormat(): String = this.format(SIMPLE_DATE_FORMATTER)
