package com.android.testmymemory.models

data class MemoryCard (
    val identifier: Int,
    var isFaceUp: Boolean = false,
    var isMatched : Boolean = false
)

