package com.life.lapse.stop.motion.video.data.repository

import android.content.Context
import com.google.gson.Gson
import com.life.lapse.stop.motion.video.data.model.Project
import java.io.File

class ProjectRepository(context: Context) {

    private val projectsDir = File(context.filesDir, "projects").apply { mkdirs() }
    private val gson = Gson()

    fun saveProject(project: Project) {
        val file = File(projectsDir, "${project.id}.json")
        file.writeText(gson.toJson(project))
    }

    fun loadProject(projectId: String): Project? {
        val file = File(projectsDir, "$projectId.json")
        return if (file.exists()) {
            gson.fromJson(file.readText(), Project::class.java)
        } else {
            null
        }
    }

    fun loadProjects(): List<Project> {
        return projectsDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    gson.fromJson(file.readText(), Project::class.java)
                } catch (e: Exception) {
                    null // Ignore corrupted files
                }
            }
            ?.sortedByDescending { it.dateModified }
            ?: emptyList()
    }

    // ✅ This new function handles deleting the project file
    fun deleteProject(projectId: String) {
        val file = File(projectsDir, "$projectId.json")
        if (file.exists()) {
            file.delete()
        }
        // Note: This does not delete the image frame files, only the project data.
    }
}