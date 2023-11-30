package voidchess.common.helper

import voidchess.common.board.other.Chess960Index
import voidchess.common.board.other.StartConfig
import kotlin.math.sign

fun String.splitAndTrim(delimiter: Char): List<String> = this.split(delimiter).map {it.trim()}.filter {it!=""}

val Double.signAsInt: Int get() = sign.toInt()

fun Int.toChess960Config() = StartConfig.Chess960Config(Chess960Index(this))