package com.life.lapse.stop.motion.video.data.repository

import android.content.Context
import com.google.gson.Gson
import com.life.lapse.stop.motion.video.data.model.Project
import java.io.File

class ProjectRepository(private val context: Context) {

    private val gson = Gson()
    private val projectsDir = File(context.filesDir, "projects")

    init {
        if (!projectsDir.exists()) {
            projectsDir.mkdirs()
        }
    }

    fun saveProject(project: Project) {
        val projectFile = File(projectsDir, "${project.id}.json")
        projectFile.writeText(gson.toJson(project))
    }

    fun loadProjects(): List<Project> {
        return projectsDir.listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    val json = file.readText()
                    gson.fromJson(json, Project::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            ?.sortedByDescending { it.dateModified } ?: emptyList()
    }

    fun loadProject(projectId: String): Project? {
        val projectFile = File(projectsDir, "$projectId.json")
        if (!projectFile.exists()) return null
        return try {
            val json = projectFile.readText()
            gson.fromJson(json, Project::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteProject(projectId: String) {
        val projectFile = File(projectsDir, "$projectId.json")
        if (projectFile.exists()) {
            projectFile.delete()
        }
    }
}
