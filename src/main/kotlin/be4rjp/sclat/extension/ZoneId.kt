package be4rjp.sclat.extension

import java.time.LocalDate
import java.time.ZoneId

val ZONE_TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")

fun ZoneId.toLocalDate(): LocalDate = LocalDate.now(this)
