package com.life.lapse.stop.motion.video.data.model

/**
 * This data class represents a single stop-motion project.
 * It holds all the necessary information for displaying a project in the gallery
 * and for loading it into the editor.
 *
 * @property id A unique identifier for the project.
 * @property title The user-defined name of the project.
 * @property frameCount The total number of images (frames) in the project.
 * @property dateModified A user-friendly string indicating when the project was last saved.
 * @property thumbnailUrl The file path or URL to the project's thumbnail image.
 * @property durationInSeconds The calculated length of the final video in seconds.
 */
data class Project(
    val id: String = "",
    val title: String = "Untitled Project",
    val frameCount: Int = 0,
    val dateModified: String = "Just now",
    val thumbnailUrl: String = "",
    val durationInSeconds: Float = 0.0f
)
