package com.super6.pot.app.database.models.service

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.super6.pot.api.models.service.EditableImage
import com.super6.pot.api.models.service.EditableThumbnailImage


@Entity(
    tableName = "draft_image",
    foreignKeys = [
        ForeignKey(
            entity = DraftService::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [Index(value = ["service_id"])]  // Add an index on the service_id column
)
data class DraftImage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "service_id")
    val serviceId: Long,
    @ColumnInfo(name = "image_path")
    val data: String,
    @ColumnInfo(name = "format")
    val format: String,
    @ColumnInfo(name = "width")
    val width:Int,
    @ColumnInfo(name = "height")
    val height:Int,
)



@Entity(
    tableName = "draft_thumbnail",
    foreignKeys = [
        ForeignKey(
            entity = DraftService::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["service_id"])]  // Add an index on the service_id column
)
data class DraftThumbnail(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "service_id")
    val serviceId: Long,
    @ColumnInfo(name = "image_path")
    val data: String,
    @ColumnInfo(name = "format")
    val format: String,
    @ColumnInfo(name = "width")
    val width:Int,
    @ColumnInfo(name = "height")
    val height:Int,
)



fun DraftImage.toImage(): EditableImage {
    return EditableImage(
        imageId = id, // Assuming id maps to imageId in the network model
        width = 0, // Set default or appropriate value
        height = 0, // Set default or appropriate value
        size = 0, // Set default or appropriate value
        format = format, // Set default or appropriate value
        imageData = data
    )
}




fun DraftThumbnail.toThumbnail(): EditableThumbnailImage {
    return EditableThumbnailImage(
        imageId = id, // Assuming id maps to imageId in the network model
        width = 0, // Set default or appropriate value
        height = 0, // Set default or appropriate value
        size = 0, // Set default or appropriate value
        format = format, // Set default or appropriate value
        imageData = data
    )
}


