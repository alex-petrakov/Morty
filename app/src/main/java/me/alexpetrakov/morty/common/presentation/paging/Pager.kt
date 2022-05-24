package me.alexpetrakov.morty.common.presentation.paging

import kotlinx.coroutines.*

class Pager<T>(
    private val requestPage: suspend (page: Int, forceRefresh: Boolean) -> PageRequestResult<T>,
    private val viewController: ViewController<T>,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

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
            viewController.setPagingState(PagingState.Loading)
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
                viewController.setPagingState(PagingState.Empty)
            } else {
                currentState = Content()
                list = items
                currentPage = FIRST_PAGE
                viewController.setPagingState(PagingState.Content(list))
            }
        }

        override fun onError(e: Exception) {
            currentState = Error()
            viewController.setPagingState(PagingState.Error(e))
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Content : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            viewController.setPagingState(PagingState.Refreshing(list))
            loadPage(FIRST_PAGE, forceRefresh = true)
        }

        override fun loadNextPage() {
            currentState = LoadingPage()
            viewController.setPagingState(PagingState.LoadingPage(list))
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
                viewController.setPagingState(PagingState.Empty)
            } else {
                currentState = Content()
                list = items
                currentPage = FIRST_PAGE
                viewController.setPagingState(PagingState.Content(list))
            }
        }

        override fun onError(e: Exception) {
            currentState = Content()
            viewController.setPagingState(PagingState.Content(list))
            viewController.displayRefreshError(e)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class LoadingPage : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            viewController.setPagingState(PagingState.Refreshing(list))
            loadPage(FIRST_PAGE, forceRefresh = false)
        }

        override fun onPageLoaded(items: List<T>, hasMorePages: Boolean) {
            if (items.isEmpty() || !hasMorePages) {
                currentState = AllContent()
                viewController.setPagingState(PagingState.Content(list))
            } else {
                currentState = Content()
                list = list + items
                currentPage++
                viewController.setPagingState(PagingState.Content(list))
            }
        }

        override fun onError(e: Exception) {
            currentState = Content()
            viewController.displayPageLoadError(e)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class AllContent : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            viewController.setPagingState(PagingState.Refreshing(list))
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