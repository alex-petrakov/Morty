package me.alexpetrakov.morty.common.presentation.paging

sealed class PagingEffect {
    data class RefreshError(val exception: Exception) : PagingEffect()
    data class PageLoadingError(val exception: Exception) : PagingEffect()
}