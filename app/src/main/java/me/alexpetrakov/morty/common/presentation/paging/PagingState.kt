package me.alexpetrakov.morty.common.presentation.paging

sealed class PagingState<out T> {
    object Initial : PagingState<Nothing>()
    object Loading : PagingState<Nothing>()
    object Empty : PagingState<Nothing>()
    data class Error(val e: Exception) : PagingState<Nothing>()
    data class Content<T>(val items: List<T>) : PagingState<T>()
    data class LoadingPage<T>(val items: List<T>) : PagingState<T>()
    data class PageLoadError<T>(val items: List<T>) : PagingState<T>()
    data class Refreshing<T>(val items: List<T>) : PagingState<T>()
}