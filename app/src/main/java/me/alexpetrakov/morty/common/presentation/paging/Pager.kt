package me.alexpetrakov.morty.common.presentation.paging

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class Pager<T>(
    private val requestPage: suspend (page: Int, forceRefresh: Boolean) -> PageRequestResult<T>,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

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

    fun release() {
        currentState.release()
    }

    private fun loadPage(page: Int, forceRefresh: Boolean) {
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

    private interface State<T> {

        fun refresh() {}

        fun loadNextPage() {}

        fun release() {}

        fun onPageLoaded(items: List<T>, hasMorePages: Boolean) {}

        fun onError(e: Exception) {}
    }

    private inner class Initial : State<T> {

        override fun refresh() {
            currentState = Loading()
            _pagingState.value = PagingState.Loading
            loadPage(FIRST_PAGE, forceRefresh = false)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
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

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
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

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Error : State<T> {

        override fun refresh() {
            currentState = Loading()
            loadPage(FIRST_PAGE, forceRefresh = false)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Empty : State<T> {

        override fun refresh() {
            currentState = Loading()
            loadPage(FIRST_PAGE, forceRefresh = false)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
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

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
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
                _pagingState.value = PagingState.Content(list)
            } else {
                currentState = Content()
                list = list + items
                currentPage++
                _pagingState.value = PagingState.Content(list)
            }
        }

        override fun onError(e: Exception) {
            currentState = Content()
            _pagingEffect.tryEmit(PagingEffect.PageLoadingError(e))
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class AllContent : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            _pagingState.value = PagingState.Refreshing(list)
            loadPage(FIRST_PAGE, forceRefresh = true)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Released : State<T> {

        override fun refresh() = throw IllegalStateException("Pager has been released")

        override fun loadNextPage() = throw IllegalStateException("Pager has been released")

        override fun onPageLoaded(items: List<T>, hasMorePages: Boolean) =
            throw IllegalStateException("Pager has been released")

        override fun onError(e: Exception) = throw IllegalStateException("Pager has been released")

        override fun release() = throw IllegalStateException("Pager has been released")
    }

    companion object {
        private const val FIRST_PAGE = 1
    }
}