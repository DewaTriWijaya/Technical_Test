package com.dewa.technicaltest.presentation.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dewa.technicaltest.databinding.ItemUserBinding
import com.dewa.technicaltest.data.model.User

class UserAdapter :
    ListAdapter<User, UserAdapter.UserViewHolder>(
        DIFF_CALLBACK
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {

        val binding =
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {

            binding.tvName.text = user.name

            binding.tvAge.text =
                "Age: ${user.age}"

            binding.tvStatus.text =
                if (user.isActive) {
                    "Active"
                } else {
                    "Inactive"
                }
        }
    }

    companion object {

        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<User>() {

                override fun areItemsTheSame(
                    oldItem: User,
                    newItem: User
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: User,
                    newItem: User
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}