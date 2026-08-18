package com.rewardpoints.app.ui.screen.status

import com.rewardpoints.app.domain.model.Rank
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * One-shot rank-up event bus for the Status screen's celebration animation.
 *
 * Backed by a buffered [Channel] rather than a SharedFlow on purpose:
 *  - A rank-up can fire while the Status tab is off-composition (e.g. completing a mission
 *    on another tab). With a SharedFlow(replay=0) that event is dropped, and with replay=1
 *    it would re-fire the animation on every return to the tab. A Channel buffers the event
 *    and delivers it to the next collector **exactly once**, which is the correct semantics
 *    for a one-time UI event.
 *  - [notify] uses `trySend`, which never suspends; the BUFFERED capacity absorbs events
 *    emitted while no collector is active.
 *
 * Single-consumer: the Status screen has exactly one collector (a LaunchedEffect), which is
 * what `receiveAsFlow` expects.
 */
class RankUpNotifier {
    private val _events = Channel<Rank>(capacity = Channel.BUFFERED)
    val events: Flow<Rank> = _events.receiveAsFlow()

    fun notify(rank: Rank) {
        _events.trySend(rank)
    }
}
