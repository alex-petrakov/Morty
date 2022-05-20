package me.alexpetrakov.morty.common.presentation.paging

interface ViewController<in T> {
    fun setLoading()
    fun setEmpty()
    fun setError(e: Exception)
    fun setContent(content: List<T>)
    fun setRefreshing(content: List<T>)
    fun setLoadingPage(content: List<T>)
    fun showRefreshError()
    fun showPageLoadError()
}