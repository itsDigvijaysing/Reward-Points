package dev.statup.app.data.local.db

import androidx.room.withTransaction
import dev.statup.app.rpg.Transactor

/** [Transactor] backed by Room's [withTransaction]. */
class RoomTransactor(private val database: AppDatabase) : Transactor {
    override suspend fun <R> transaction(block: suspend () -> R): R =
        database.withTransaction { block() }
}
