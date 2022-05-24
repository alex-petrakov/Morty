package me.alexpetrakov.morty.common.data

import me.alexpetrakov.morty.common.data.local.db.page.PageEntity
import me.alexpetrakov.morty.common.domain.model.Character
import java.time.Instant

data class Page(val id: Int, val characters: List<Character>, val hasMorePages: Boolean)

fun Page.toPageEntity(updatedAt: Instant): PageEntity {
    return PageEntity(id, hasMorePages, updatedAt)
}