package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [
        Index(value = ["groupId"], unique = true),
        Index(value = ["name"])
    ]
)
data class GroupEntity(
    @PrimaryKey val groupId: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#3B82F6",
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "pigeonId"],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["pigeonId"])
    ]
)
data class GroupMemberEntity(
    val groupId: String,
    val pigeonId: String,
    val addedAt: Long = System.currentTimeMillis()
)
