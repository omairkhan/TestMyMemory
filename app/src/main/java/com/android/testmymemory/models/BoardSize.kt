package com.android.testmymemory.models

enum class BoardSize (val numCards: Int){
    EASY (8),
    MEDIUM(18),
    HARD(24);

    //getting columns in different board size
    fun getWidth(): Int {
        //similar to switch statement
        return when (this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }
    fun getHeight(): Int{
        return numCards / getWidth()
    }
    fun getNumPairs(): Int{
        return numCards / 2
    }
}