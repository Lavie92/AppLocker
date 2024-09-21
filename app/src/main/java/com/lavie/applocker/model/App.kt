package com.lavie.applocker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class App(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val icon: String,
    val isLocked: Boolean
)