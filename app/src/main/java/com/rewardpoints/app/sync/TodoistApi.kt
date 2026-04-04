package com.rewardpoints.app.sync

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TodoistApi(private val httpClient: HttpClient) {

    companion object {
        private const val SYNC_URL = "https://api.todoist.com/api/v1/sync"
    }

    suspend fun getTasks(token: String): Result<List<TodoistTask>> {
        return try {
            val response = httpClient.submitForm(
                url = SYNC_URL,
                formParameters = parameters {
                    append("sync_token", "*")
                    append("resource_types", "[\"items\"]")
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            val syncResponse: SyncResponse = response.body()
            val tasks = syncResponse.items.filter { !it.isCompleted }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompletedTasks(token: String, since: String? = null): Result<List<CompletedTask>> {
        return try {
            val response = httpClient.submitForm(
                url = "$SYNC_URL/completed/get_all",
                formParameters = parameters {
                    since?.let { append("since", it) }
                    append("limit", "50")
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val wrapper: CompletedTasksResponse = response.body()
            Result.success(wrapper.items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(token: String): Result<Boolean> {
        return try {
            val response = httpClient.submitForm(
                url = SYNC_URL,
                formParameters = parameters {
                    append("sync_token", "*")
                    append("resource_types", "[\"user\"]")
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class SyncResponse(
    @SerialName("sync_token")
    val syncToken: String? = null,
    val items: List<TodoistTask> = emptyList()
)

@Serializable
data class TodoistTask(
    val id: String,
    val content: String,
    val description: String? = null,
    val priority: Int = 1,
    @SerialName("project_id")
    val projectId: String? = null,
    val labels: List<String> = emptyList(),
    val due: TodoistDue? = null,
    @SerialName("checked")
    val isCompleted: Boolean = false
)

@Serializable
data class TodoistDue(
    val date: String? = null,
    val datetime: String? = null,
    val string: String? = null
)

@Serializable
data class CompletedTask(
    @SerialName("task_id")
    val taskId: String,
    val content: String,
    @SerialName("completed_at")
    val completedAt: String
)

@Serializable
data class CompletedTasksResponse(
    val items: List<CompletedTask>
)
