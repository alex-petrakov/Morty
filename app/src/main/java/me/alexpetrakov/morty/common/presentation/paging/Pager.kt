package me.alexpetrakov.morty.common.presentation.paging

import kotlinx.coroutines.*

class Pager<T>(
    private val requestPage: suspend (Int) -> PageRequestResult<T>,
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

    private fun loadPage(page: Int) {
        lastJob?.cancel()
        lastJob = coroutineScope.launch {
            when (val outcome = requestPage(page)) {
                is PageRequestResult.Success -> if (outcome.pageItems.isEmpty()) {
                    currentState.onEmptyPageLoaded()
                } else {
                    currentState.onPageLoaded(outcome.pageItems)
                }
                is PageRequestResult.Failure -> currentState.onError(outcome.exception)
            }
        }
    }

    private interface State<T> {

        fun refresh() {}

        fun loadNextPage() {}

        fun release() {}

        fun onPageLoaded(items: List<T>) {}

        fun onError(e: Exception) {}

        fun onEmptyPageLoaded() {}
    }

    private inner class Initial : State<T> {

        override fun refresh() {
            currentState = Loading()
            viewController.setLoading()
            loadPage(FIRST_PAGE)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Loading : State<T> {

        override fun onPageLoaded(items: List<T>) {
            currentState = Content()
            list = items
            currentPage = FIRST_PAGE
            viewController.setContent(list)
        }

        override fun onError(e: Exception) {
            currentState = Error()
            viewController.setError(e)
        }

        override fun onEmptyPageLoaded() {
            currentState = Empty()
            viewController.setEmpty()
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Content : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            viewController.setRefreshing(list)
            loadPage(FIRST_PAGE)
        }

        override fun loadNextPage() {
            currentState = LoadingPage()
            viewController.setLoadingPage(list)
            loadPage(currentPage + 1)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Error : State<T> {

        override fun refresh() {
            currentState = Loading()
            loadPage(FIRST_PAGE)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Empty : State<T> {

        override fun refresh() {
            currentState = Loading()
            loadPage(FIRST_PAGE)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Refreshing : State<T> {

        override fun onPageLoaded(items: List<T>) {
            currentState = Content()
            list = items
            currentPage = FIRST_PAGE
            viewController.setContent(list)
        }

        override fun onError(e: Exception) {
            currentState = Content()
            viewController.setContent(list)
            viewController.showRefreshError()
        }

        override fun onEmptyPageLoaded() {
            currentState = Empty()
            viewController.setEmpty()
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class LoadingPage : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            viewController.setRefreshing(list)
            loadPage(FIRST_PAGE)
        }

        override fun onPageLoaded(items: List<T>) {
            currentState = Content()
            list = list + items
            currentPage++
            viewController.setContent(list)
        }

        override fun onError(e: Exception) {
            currentState = Content()
            viewController.showPageLoadError()
        }

        override fun onEmptyPageLoaded() {
            currentState = AllContent()
            viewController.setContent(list)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class AllContent : State<T> {

        override fun refresh() {
            currentState = Refreshing()
            viewController.setRefreshing(list)
            loadPage(FIRST_PAGE)
        }

        override fun release() {
            currentState = Released()
            coroutineScope.cancel()
        }
    }

    private inner class Released : State<T> {

        override fun refresh() = throw IllegalStateException("Pager has been released")

        override fun loadNextPage() = throw IllegalStateException("Pager has been released")

        override fun onPageLoaded(items: List<T>) =
            throw IllegalStateException("Pager has been released")

        override fun onError(e: Exception) = throw IllegalStateException("Pager has been released")

        override fun onEmptyPageLoaded() = throw IllegalStateException("Pager has been released")

        override fun release() = throw IllegalStateException("Pager has been released")
    }

    companion object {
        private const val FIRST_PAGE = 1
    }
}