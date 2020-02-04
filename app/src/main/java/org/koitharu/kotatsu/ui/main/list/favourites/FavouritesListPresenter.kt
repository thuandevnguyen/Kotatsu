package org.koitharu.kotatsu.ui.main.list.favourites

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import org.koitharu.kotatsu.BuildConfig
import org.koitharu.kotatsu.domain.favourites.FavouritesRepository
import org.koitharu.kotatsu.ui.common.BasePresenter
import org.koitharu.kotatsu.ui.main.list.MangaListView

@InjectViewState
class FavouritesListPresenter : BasePresenter<MangaListView<Unit>>() {

	private lateinit var repository: FavouritesRepository

	override fun onFirstViewAttach() {
		repository = FavouritesRepository()
		super.onFirstViewAttach()
	}

	fun loadList(offset: Int) {
		launch {
			viewState.onLoadingChanged(true)
			try {
				val list = withContext(Dispatchers.IO) {
					repository.getAllManga(offset = offset)
				}
				if (offset == 0) {
					viewState.onListChanged(list)
				} else {
					viewState.onListAppended(list)
				}
			} catch (e: Exception) {
				if (BuildConfig.DEBUG) {
					e.printStackTrace()
				}
				viewState.onError(e)
			} finally {
				viewState.onLoadingChanged(false)
			}
		}
	}
}