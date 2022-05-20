package me.alexpetrakov.morty.common.presentation.paging

interface ViewController<in T> {
    fun setPagingState(state: PagingState<T>)
    fun displayRefreshError(e: Exception)
    fun displayPageLoadError(e: Exception)
}