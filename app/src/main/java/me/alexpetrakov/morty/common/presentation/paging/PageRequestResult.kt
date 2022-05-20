package me.alexpetrakov.morty.common.presentation.paging

sealed class PageRequestResult<out T> {
    data class Success<out T>(val pageItems: List<T>) : PageRequestResult<T>()
    data class Failure(val exception: Exception) : PageRequestResult<Nothing>()
}