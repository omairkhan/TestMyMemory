package com.android.testmymemory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.android.testmymemory.databinding.ActivityAppBinding
import com.android.testmymemory.databinding.ActivityCreateBinding
import com.android.testmymemory.models.BoardSize
import com.android.testmymemory.utils.EXTRA_BOARD_SIZE
import com.android.testmymemory.utils.isPermissionGranted
import com.android.testmymemory.utils.requestPermission
import java.security.Permissions

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val READ_EXTERNAL_PHOTO_CODE = 67065
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var  boardSize : BoardSize
    private lateinit var binding: ActivityCreateBinding

    private var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>()

    private lateinit var imagePickLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.topAppBar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE, BoardSize::class.java) ?: BoardSize.EASY
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"
        // anonymous class declared in param below -> object: ImagePickerAdapter.ImageClickListener
        binding.rvImagePicker.adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceholderClicked() {
                //set permission in AndroidManifest.xml
                // check if user has given permission
                if(isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION )) {
                    // photo choosing flow, to pick the photo , type of intents:
                    // Implicit, request to perform an action based on desired action
                    // Explicit: launch other activities within your app (like done in appActivity to createActivity
                    launchIntentForPhotos()
                }
                else
                {
                    requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTO_CODE )
                }
            }

        })
        binding.rvImagePicker.setHasFixedSize(true)
        binding.rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())


        // Initialize the launcher
        imagePickLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle your result here
                Toast.makeText(this, "Returned: $data", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        if(requestCode == READ_EXTERNAL_PHOTO_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentForPhotos()
            }
            else{
                Toast.makeText(this, "In order to create custom game, you need to provide access to your photos", Toast.LENGTH_LONG).show()
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchIntentForPhotos(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        // Navigate to new activity
        imagePickLauncher.launch(intent)

    }

    fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12 or below
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= 34) {
            permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
}