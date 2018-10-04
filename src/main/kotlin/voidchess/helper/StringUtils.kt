package voidchess.helper

fun String.splitAndTrim(delimiter: Char): List<String> = this.split(delimiter).map {it.trim()}.filter {it!=""}
fun List<String>.trim() = this.map{it.trim()}