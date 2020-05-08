package voidchess.common.helper

import kotlin.math.sign

fun String.splitAndTrim(delimiter: Char): List<String> = this.split(delimiter).map {it.trim()}.filter {it!=""}

val Double.signAsInt: Int get() = sign.toInt()