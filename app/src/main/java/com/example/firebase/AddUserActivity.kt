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
import com.example.firebase.databinding.ActivityAddUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class AddUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUserBinding

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = database.reference.child("Users")

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // action bar
        supportActionBar?.title = "Add User"
        val colorDrawable = ColorDrawable(Color.parseColor("#FFE91E63"))
        supportActionBar?.setBackgroundDrawable(colorDrawable)

        // register activityResultLauncher
        registerActivityForResult()

        binding.btnAddUser.setOnClickListener {
            uploadPhoto()
        }

        binding.ivProfileImage.setOnClickListener {
            chooseImage()
        }
    }

    private fun chooseImage() {
        // user external storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        } else {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    private fun registerActivityForResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
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

    private fun addUserToDatabase(url: String, imageName: String) {
        val name = binding.etName.text.toString()
        val age = binding.etAge.text.toString().toInt()
        val email = binding.etEmail.text.toString()
        val id = databaseReference.push().key.toString()

        val user = Users(id, name, age, email, url, imageName)

        databaseReference.child(id).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "The new user has been added to the database!", Toast.LENGTH_LONG).show()

                binding.btnAddUser.isClickable = true
                binding.progressBar.visibility = View.INVISIBLE

                finish()
            } else {
                Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadPhoto() {
        binding.btnAddUser.isClickable = false
        binding.progressBar.visibility = View.VISIBLE

        // UUID
        val imageName = UUID.randomUUID().toString()

        val imageReference = storageReference.child("Profile Pictures").child(imageName)

        imageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(applicationContext, "Image Uploaded", Toast.LENGTH_SHORT).show()

                // downloadable link
                val myUploadedImageReference = storageReference.child("Profile Pictures").child(imageName)
                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageURL = url.toString()

                    addUserToDatabase(imageURL, imageName)
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }
}