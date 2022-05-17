package me.alexpetrakov.morty.common.data

import java.time.Clock
import java.time.Duration

operator fun Clock.plus(duration: Duration): Clock {
    return Clock.offset(this, duration)
}

operator fun Clock.minus(duration: Duration): Clock {
    return Clock.offset(this, duration.negated())
}

val Int.minutes: Duration get() = Duration.ofMinutes(this.toLong())

val Int.hours: Duration get() = Duration.ofHours(this.toLong())