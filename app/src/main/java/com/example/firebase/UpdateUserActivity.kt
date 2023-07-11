package com.example.firebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebase.databinding.ActivityMainBinding
import com.example.firebase.databinding.ActivityUpdateUserBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class UpdateUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateUserBinding

    private val databaseReference = FirebaseDatabase.getInstance().reference.child("Users")

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // action bar
        supportActionBar?.title = "Update User"
        val colorDrawable = ColorDrawable(Color.parseColor("#FFE91E63"))
        supportActionBar?.setBackgroundDrawable(colorDrawable)

        // register activityResultLauncher
        registerActivityForResult()

        getAndSetData()

        binding.btnUpdateUser.setOnClickListener {
            uploadPhoto()
        }

        binding.ivProfileImage.setOnClickListener {
            chooseImage()
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activityResultLauncher.launch(intent)
    }

    private fun registerActivityForResult() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                val resultCode = result.resultCode
                val imageData = result.data

                if (resultCode == RESULT_OK && imageData != null) {
                    imageUri = imageData.data

                    imageUri?.let {
                        Picasso.get().load(it).into(binding.ivProfileImage)
                    }
                }
            })
    }


    private fun uploadPhoto() {
        binding.btnUpdateUser.isClickable = false
        binding.progressBar.visibility = View.VISIBLE


        val imageName = intent.getStringExtra("userProfileImageName").toString()

        val imageReference = storageReference.child("Profile Pictures").child(imageName)

        imageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(applicationContext, "Image Updated", Toast.LENGTH_SHORT).show()

                // downloadable link
                val myUploadedImageReference = storageReference.child("Profile Pictures").child(imageName)
                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageURL = url.toString()

                    updateData(imageURL, imageName)
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun getAndSetData() {
        val userName = intent.getStringExtra("userName")
        val userAge = intent.getIntExtra("userAge", 0).toString()
        val userEmail = intent.getStringExtra("userEmail")
        val imageURL = intent.getStringExtra("userProfileImageURL").toString()

        binding.etName.setText(userName)
        binding.etAge.setText(userAge)
        binding.etEmail.setText(userEmail)
        Picasso.get().load(imageURL).into(binding.ivProfileImage)
    }

    private fun updateData(imageUrl: String, imageName: String) {
        val updatedName = binding.etName.text.toString()
        val updatedAge = binding.etAge.text.toString().toInt()
        val updatedEmail = binding.etEmail.text.toString()
        val userId = intent.getStringExtra("userId").toString()

        val usersMap = mutableMapOf<String,Any>()
        usersMap["userName"] = updatedName
        usersMap["userAge"] = updatedAge
        usersMap["userEmail"] = updatedEmail
        usersMap["userProfileImageURL"] = imageUrl
        usersMap["userProfileImageName"] = imageName

        databaseReference.child(userId).updateChildren(usersMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "The user has been updated!", Toast.LENGTH_LONG).show()

                binding.btnUpdateUser.isClickable = true
                binding.progressBar.visibility = View.INVISIBLE

                finish()
            } else {
                Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}