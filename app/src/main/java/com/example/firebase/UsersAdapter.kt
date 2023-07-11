package com.example.firebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.firebase.databinding.UsersItemBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class UsersAdapter(private val context: Context, private val userList: ArrayList<Users>): RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    inner class UsersViewHolder(val adapterBinding: UsersItemBinding) : RecyclerView.ViewHolder(adapterBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding = UsersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return UsersViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.adapterBinding.tvUserName.text = userList[position].userName
        holder.adapterBinding.tvUserAge.text = userList[position].userAge.toString()
        holder.adapterBinding.tvUserEmail.text = userList[position].userEmail

        val profileImageUrl = userList[position].userProfileImageURL

        Picasso.get().load(profileImageUrl).into(holder.adapterBinding.ivProfileImage, object : Callback {
            override fun onSuccess() {
                holder.adapterBinding.progressBar.visibility = View.INVISIBLE
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, e?.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })

        holder.adapterBinding.linearLayout.setOnClickListener {
            val intent = Intent(context, UpdateUserActivity::class.java)
            intent.putExtra("userId", userList[position].userId)
            intent.putExtra("userName", userList[position].userName)
            intent.putExtra("userAge", userList[position].userAge)
            intent.putExtra("userEmail", userList[position].userEmail)
            intent.putExtra("userProfileImageURL", userList[position].userProfileImageURL)
            intent.putExtra("userProfileImageName", userList[position].userProfileImageName)

            context.startActivity(intent)
        }
    }

    fun getUserId(position: Int): String {
        return userList[position].userId
    }

    fun getUserProfileImageName(position: Int): String {
        return userList[position].userProfileImageName
    }
}