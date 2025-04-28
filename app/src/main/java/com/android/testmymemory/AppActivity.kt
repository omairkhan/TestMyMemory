package com.android.testmymemory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.GridLayoutManager
import com.android.testmymemory.databinding.ActivityAppBinding
import com.android.testmymemory.models.BoardSize
import com.android.testmymemory.models.MemoryGame
import com.android.testmymemory.utils.EXTRA_BOARD_SIZE
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.snackbar.Snackbar
import android.animation.ArgbEvaluator

class AppActivity : AppCompatActivity() {

companion object {
    private const val TAG = "AppActivity"
}

    private lateinit var clRoot: CoordinatorLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAppBinding
    private var boardSize: BoardSize = BoardSize.EASY

    private lateinit var memoryGame : MemoryGame
    private lateinit var adapter : MemoryBoardAdapter

    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clRoot = binding.clRoot

        setSupportActionBar(binding.toolbar)
        // Initialize the launcher
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle your result here
                Toast.makeText(this, "Returned: $data", Toast.LENGTH_SHORT).show()
            }
        }

        //for development purpose only to go directly to desired activity
        val intent = Intent(this, CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.MEDIUM)
        startActivity(intent)

//        resultLauncher = this.registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data = result.data?.getStringExtra("user_input")
//                Toast.makeText(this, "Returned: $data", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        val choosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
//        val randomizedImages = (choosenImages + choosenImages).shuffled()
//        val memoryCards = randomizedImages.map { MemoryCard(it) }

        setupBoardGame()

//        binding.rvBoard.addItemDecoration(GridSpacingItemDecoration(16))

//        val navController = findNavController(R.id.nav_host_fragment_content_app)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//
//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
    }
//    override fun onSupportNavigateUp(): Boolean {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
        //   return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh ->{
                //Setup the game again.
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?", null, View.OnClickListener{
                        setupBoardGame()
                    })
                }
                else {
                    setupBoardGame()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true;
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
                //set a new value for the board size
                boardSize = when (radioGroupSize.checkedRadioButtonId) {
                    R.id.rbEasy -> BoardSize.EASY
                    R.id.rbMedium -> BoardSize.MEDIUM
                    else -> BoardSize.HARD
                }
            setupBoardGame()
            })
    }

    private fun showCreationDialog(){

        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener {
            //set a new value for the board size
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // Navigate to new activity
            val intent = Intent( this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            resultLauncher.launch(intent)
        })

    }

    private fun showAlertDialog(title: String, view : View?, positiveButtonClickListener: View.OnClickListener){
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK"){ _,_ ->
            positiveButtonClickListener.onClick(null)
            }.show()
    }

    private fun setupBoardGame(){
        when(boardSize){
            BoardSize.EASY -> {
                binding.tvNumMoves.text = "Easy: 4 x 2"
                binding.tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                binding.tvNumMoves.text = "Medium: 6 x 3"
                binding.tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                binding.tvNumMoves.text = "Hard: 6 x 4"
                binding.tvNumPairs.text = "Pairs: 0 / 12"
            }
        }

        binding.tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)

        // Optionally override title in code
        supportActionBar?.title = "Test My Memory"
        adapter = MemoryBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
//                    Log.i(TAG, "Card clicked $position")
                    updateGameWithFlip(position)

                }
            })
        binding.rvBoard.adapter = adapter
        binding.rvBoard.setHasFixedSize(true)
        binding.rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())

    }

    private fun updateGameWithFlip(position : Int){
        //Error checking
        if(memoryGame.haveWonGame())
        {
            //Alert the user of an invalid move
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            // Alert the user of an invalid move
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }

        if(memoryGame.flipCard(position)){
            Log.i(TAG, "Found a match! num pairs found: ${memoryGame.numPairsFound}")
            // Just to give colors from 0 progress to 100% progress it automatically handles
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            binding.tvNumPairs.setTextColor(color)
            binding.tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot, "You won! Congratulations.", Snackbar.LENGTH_LONG).show()

            }
        }
        binding.tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        adapter.notifyDataSetChanged()
    }
}
