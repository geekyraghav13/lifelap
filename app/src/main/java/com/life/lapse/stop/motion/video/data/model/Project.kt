package com.life.lapse.stop.motion.video.data.model

import java.util.UUID

// This class represents a single project that can be saved to a file.
data class Project(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Untitled Project",
    val dateModified: Long = System.currentTimeMillis(),
    val frameUris: List<String> = emptyList(), // Store URIs as Strings for saving
    val speed: Float = 1.0f
)
