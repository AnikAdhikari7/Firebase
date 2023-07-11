package com.example.firebase

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase.authentication.LoginActivity
import com.example.firebase.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val userList = ArrayList<Users>()
    val userImageList = ArrayList<String>()
    lateinit var usersAdapter: UsersAdapter

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = database.reference.child("Users")

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val storageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // action bar
        supportActionBar?.title = "Firebase"
        val colorDrawable = ColorDrawable(Color.parseColor("#FFE91E63"))
        supportActionBar?.setBackgroundDrawable(colorDrawable)

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }

        // delete an item by swapping right or left
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val userId = usersAdapter.getUserId(viewHolder.adapterPosition)

                databaseReference.child(userId).removeValue()

                // delete userProfileImage from Firebase Storage
                val imageName = usersAdapter.getUserProfileImageName(viewHolder.adapterPosition)
                val imageReference = storageReference.child("Profile Pictures").child(imageName)
                imageReference.delete()


                Toast.makeText(applicationContext, "The user was deleted!", Toast.LENGTH_LONG).show()
            }

        }).attachToRecyclerView(binding.recyclerView)

        // retrieving the data from firebase
        retrieveDataFromDatabase()
    }

    private fun retrieveDataFromDatabase() {
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for (eachUser in snapshot.children) {
                    val user = eachUser.getValue(Users::class.java)

                    if (user != null) {
                        println("userId: ${user.userId}")
                        println("userName: ${user.userName}")
                        println("userAge: ${user.userAge}")
                        println("userEmail: ${user.userEmail}")
                        println("********************************")


                        userList.add(user)
                    }

                    usersAdapter = UsersAdapter(this@MainActivity, userList)

                    binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.recyclerView.adapter = usersAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_items, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.btnDeleteAll) {
            showDialogMessage()
        } else if (item.itemId == R.id.btnSignOut) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDialogMessage() {
        val dialogMessage = AlertDialog.Builder(this)
        dialogMessage.setTitle("Delete All Users")
        dialogMessage.setMessage("If click Yes, all users will be deleted.\n" +
                "If you want to delete a specific user, you can swipe the item you want to delete right or left.")
        dialogMessage.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.cancel()
        })
        dialogMessage.setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (eachUser in snapshot.children) {
                        val user = eachUser.getValue(Users::class.java)

                        if (user != null) {
                            userImageList.add(user.userProfileImageName)
                            Log.d("array", "created")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


            databaseReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    for (imageName in userImageList) {
                        val imageReference = storageReference.child("Profile Pictures").child(imageName)
                        imageReference.delete()
                        Log.d("user", "deleted")

                    }

                    usersAdapter.notifyDataSetChanged()
                    Toast.makeText(applicationContext, "All users were deleted!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })

        dialogMessage.create().show()
    }


}