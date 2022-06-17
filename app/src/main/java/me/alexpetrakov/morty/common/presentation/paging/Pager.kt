package me.alexpetrakov.morty.common.presentation.paging

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Pager<T>(
    private val requestPage: suspend (page: Int, forceRefresh: Boolean) -> PageRequestResult<T>,
    hostScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) {

    private val coroutineScope = hostScope.childScope(context = dispatcher)

    private val _pagingState = MutableStateFlow<PagingState<T>>(PagingState.Initial)
    val pagingState: StateFlow<PagingState<T>> get() = _pagingState

    private val _pagingEffect = MutableSharedFlow<PagingEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val pagingEffect: SharedFlow<PagingEffect> get() = _pagingEffect

    private var currentState: State<T> = Initial()

    private var currentPage: Int = 0

    private var list: List<T> = emptyList()

    private var lastJob: Job? = null

    fun refresh() {
        currentState.refresh()
    }

    fun loadNextPage() {
        currentState.loadNextPage()
    }

    private fun loadPage(page: Int, forceRefresh: Boolean) {
        if (!coroutineScope.isActive) {
            throw IllegalStateException("Host Coroutine Scope has been cancelled")
        }
        lastJob?.cancel()
        lastJob = coroutineScope.launch {
            when (val result = requestPage(page, forceRefresh)) {
                is PageRequestResult.Success -> currentState.onPageLoaded(
                    result.pageItems,
                    result.hasMorePages
                )
                is PageRequestResult.Failure -> currentState.onError(result.exception)
            }
        }
    }

    private fun CoroutineScope.childScope(
        context: CoroutineContext = EmptyCoroutineContext
    ): CoroutineScope {
        val parentJob = coroutineContext[Job]
        return CoroutineScope(coroutineContext + SupervisorJob(parent = parentJob)) + context
    }

    private interface State<T> {

        fun refresh() {}

        fun loadNextPage() {}

        fun onPageLoaded(items: List<T>, hasMorePages: Boolean) {}

        fun onError(e: Exception) {}
    }

    private inner class Initial : State<T> {

        override fun refresh() {
            currentState = Loading()
            _pagingState.value = PagingState.Loading
            loadPage(FIRST_PAGE, forceRefresh = false)
        }
    }

    private inner class Loading : State<T> {

        override fun onPageLoaded(items: List<T>, hasMorePages: Boolean) {
            if (items.isEmpty()) {
                currentState = Empty()
                _pagingState.value = PagingState.Empty
            } else {
                currentState = Content()
                list = items
                currentPage = FIRST_PAGE
                _pagingState.value = PagingState.Content(list)
            }
        }

        override fun onError(e: Exception) {
            currentState = Error()
            _pagingState.value = PagingState.Error(e)
        }
    }

    private inner class Content : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            _pagingState.value = PagingState.Refreshing(list)
            loadPage(FIRST_PAGE, forceRefresh = true)
        }

        override fun loadNextPage() {
            currentState = LoadingPage()
            _pagingState.value = PagingState.LoadingPage(list)
            loadPage(currentPage + 1, forceRefresh = false)
        }
    }

    private inner class Error : State<T> {

        override fun refresh() {
            currentState = Loading()
            _pagingState.value = PagingState.Loading
            loadPage(FIRST_PAGE, forceRefresh = false)
        }
    }

    private inner class Empty : State<T> {

        override fun refresh() {
            currentState = Loading()
            _pagingState.value = PagingState.Loading
            loadPage(FIRST_PAGE, forceRefresh = false)
        }
    }

    private inner class Refreshing : State<T> {

        override fun onPageLoaded(items: List<T>, hasMorePages: Boolean) {
            if (items.isEmpty()) {
                currentState = Empty()
                _pagingState.value = PagingState.Empty
            } else {
                currentState = Content()
                list = items
                currentPage = FIRST_PAGE
                _pagingState.value = PagingState.Content(list)
            }
        }

        override fun onError(e: Exception) {
            currentState = Content()
            _pagingState.value = PagingState.Content(list)
            _pagingEffect.tryEmit(PagingEffect.RefreshError(e))
        }
    }

    private inner class LoadingPage : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            _pagingState.value = PagingState.Refreshing(list)
            loadPage(FIRST_PAGE, forceRefresh = false)
        }

        override fun onPageLoaded(items: List<T>, hasMorePages: Boolean) {
            if (items.isEmpty() || !hasMorePages) {
                currentState = AllContent()
                list = list + items
                currentPage++
                _pagingState.value = PagingState.Content(list)
            } else {
                currentState = Content()
                list = list + items
                currentPage++
                _pagingState.value = PagingState.Content(list)
            }
        }

        override fun onError(e: Exception) {
            currentState = PageLoadError()
            _pagingState.value = PagingState.PageLoadError(list)
            _pagingEffect.tryEmit(PagingEffect.PageLoadingError(e))
        }
    }

    private inner class PageLoadError : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            _pagingState.value = PagingState.Refreshing(list)
            loadPage(FIRST_PAGE, forceRefresh = false)
        }

        override fun loadNextPage() {
            currentState = LoadingPage()
            _pagingState.value = PagingState.LoadingPage(list)
            loadPage(currentPage + 1, forceRefresh = false)
        }
    }

    private inner class AllContent : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            _pagingState.value = PagingState.Refreshing(list)
            loadPage(FIRST_PAGE, forceRefresh = true)
        }
    }

    companion object {
        private const val FIRST_PAGE = 1
    }
}