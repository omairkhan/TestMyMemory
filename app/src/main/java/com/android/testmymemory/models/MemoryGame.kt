package com.android.testmymemory.models

import com.android.testmymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize : BoardSize){
    val cards : List<MemoryCard>
    var numPairsFound = 0
    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null


    init{
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int) : Boolean {
        numCardFlips++
        val card = cards[position]

        // Three cases:
        // 0 Card previously flipped over =>  restore cards (face down (just to reduce op. combine with 2 cards condition) +  over the selected card.
        // 1 Card previously flipped over => flip over the selected card + check if the images match

        // 2 Card previously flipped over -> restore cards (face down) + flip over the selected card
        var foundMatch = false
        if(indexOfSingleSelectedCard == null){
            // 0 or 2 cards previously flipped over
            restoreCards()
            indexOfSingleSelectedCard = position
        }
        else{
            //exactly 1 card previously flipped over
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean{
        if(cards[position1].identifier != cards[position2].identifier){
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true

    }

    private fun restoreCards(){
        for (card in cards){
            //if card is not matched don't flip over
            if(!card.isMatched){
                card.isFaceUp = false
            }
        }
    }
    fun haveWonGame(): Boolean{
        return numPairsFound == boardSize.getNumPairs()
    }
    fun isCardFaceUp(position: Int): Boolean{
        return cards[position].isFaceUp
    }
    fun getNumMoves(): Int{
        return numCardFlips / 2
    }
}
