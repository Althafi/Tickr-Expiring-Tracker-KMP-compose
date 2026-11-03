package com.project.tickr.data.mapper

import com.project.tickr.db.Category as CategoryRow
import com.project.tickr.db.Item as ItemRow
import com.project.tickr.db.Notification_log as NotificationRow
import com.project.tickr.domain.model.Category
import com.project.tickr.domain.model.Item
import com.project.tickr.domain.model.NotificationLog

internal fun ItemRow.toDomain() = Item(
    id = id,
    categoryId = category_id,
    name = name,
    barcode = barcode,
    expiryDate = expiry_date,
    imageUrl = image_url,
    notes = notes,
    isConsumed = is_consumed != 0L,
    quantity = quantity.toInt(),
    unit = unit,
    createdAt = created_at,
    updatedAt = updated_at,
)

internal fun CategoryRow.toDomain() = Category(
    id = id,
    name = name,
    iconName = icon_name,
    colorHex = color_hex,
    createdAt = created_at,
    key = key,
)

internal fun NotificationRow.toDomain() = NotificationLog(
    id = id,
    itemId = item_id,
    scheduledDate = scheduled_date,
    isSent = is_sent != 0L,
    createdAt = created_at,
)
