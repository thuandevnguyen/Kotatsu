package org.koitharu.kotatsu.bookmarks.ui.adapter

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import org.koitharu.kotatsu.bookmarks.domain.Bookmark
import org.koitharu.kotatsu.bookmarks.ui.model.BookmarksGroup
import org.koitharu.kotatsu.core.ui.BaseListAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.ui.list.SectionedSelectionController
import org.koitharu.kotatsu.list.ui.adapter.ListStateHolderListener
import org.koitharu.kotatsu.list.ui.adapter.emptyStateListAD
import org.koitharu.kotatsu.list.ui.adapter.errorStateListAD
import org.koitharu.kotatsu.list.ui.adapter.loadingFooterAD
import org.koitharu.kotatsu.list.ui.adapter.loadingStateAD
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.parsers.model.Manga

class BookmarksGroupAdapter(
	coil: ImageLoader,
	lifecycleOwner: LifecycleOwner,
	selectionController: SectionedSelectionController<Manga>,
	listener: ListStateHolderListener,
	bookmarkClickListener: OnListItemClickListener<Bookmark>,
	groupClickListener: OnListItemClickListener<BookmarksGroup>,
) : BaseListAdapter<ListModel>() {

	init {
		val pool = RecyclerView.RecycledViewPool()
		delegatesManager
			.addDelegate(
				bookmarksGroupAD(
					coil = coil,
					lifecycleOwner = lifecycleOwner,
					sharedPool = pool,
					selectionController = selectionController,
					bookmarkClickListener = bookmarkClickListener,
					groupClickListener = groupClickListener,
				),
			)
			.addDelegate(loadingStateAD())
			.addDelegate(loadingFooterAD())
			.addDelegate(emptyStateListAD(coil, lifecycleOwner, listener))
			.addDelegate(errorStateListAD(listener))
	}
}
