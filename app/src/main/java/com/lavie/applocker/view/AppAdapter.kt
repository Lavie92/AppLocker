package com.lavie.applocker.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lavie.applocker.R
import com.lavie.applocker.databinding.ItemAppLockBinding
import com.lavie.applocker.model.App

class AppAdapter(private val onLockChanged: (App, Boolean) -> Unit) : ListAdapter<App, AppAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemAppLockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, onLockChanged)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app)
    }

    class ItemViewHolder(
        private val binding: ItemAppLockBinding,
        private val onLockChanged: (App, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: App) {
            binding.appName.text = app.name
            binding.appPackage.text = app.appPackage
            binding.appLockSwitch.isChecked = app.isLocked

            Glide.with(binding.root.context)
                .load(app.icon)
                .into(binding.appIcon)

            binding.appLockSwitch.setOnCheckedChangeListener { _, isChecked ->
                onLockChanged(app, isChecked)
            }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<App>() {
        override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem == newItem
        }
    }
}