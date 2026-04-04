package com.rewardpoints.app.sync

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class TodoistApi(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.todoist.com/api/v1"
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getTasks(token: String): Result<List<TodoistTask>> {
        return try {
            val response = httpClient.submitForm(
                url = "$BASE_URL/sync",
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
            val response = httpClient.get("$BASE_URL/tasks/completed/by_completion_date") {
                header(HttpHeaders.Authorization, "Bearer $token")
                since?.let { parameter("since", it) }
                parameter("limit", "200")
            }

            // Parse flexibly — response may be {items:[...]}, {results:[...]}, or [...]
            val responseText: String = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(responseText)

            val items: List<CompletedTask> = when {
                jsonElement is JsonArray -> {
                    json.decodeFromJsonElement(jsonElement)
                }
                jsonElement is JsonObject -> {
                    val arr = jsonElement["items"]
                        ?: jsonElement["results"]
                        ?: jsonElement["completed_items"]
                    if (arr != null && arr is JsonArray) {
                        json.decodeFromJsonElement(arr)
                    } else {
                        emptyList()
                    }
                }
                else -> emptyList()
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(token: String): Result<Boolean> {
        return try {
            val response = httpClient.submitForm(
                url = "$BASE_URL/sync",
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

// --- Sync API response (for active tasks) ---

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

// --- Completed tasks response (v1 API) ---

@Serializable
data class CompletedTask(
    val id: String = "",
    @SerialName("task_id")
    val taskId: String = "",
    val content: String = "",
    val priority: Int = 1,
    val labels: List<String> = emptyList(),
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("completed_date")
    val completedDate: String? = null
) {
    /** Stable identifier — prefer id, fall back to task_id */
    val stableId: String get() = id.ifBlank { taskId }
}
