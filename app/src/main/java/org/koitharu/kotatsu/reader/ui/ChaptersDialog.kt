package org.koitharu.kotatsu.reader.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_chapters.*
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.AlertDialogFragment
import org.koitharu.kotatsu.base.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.model.MangaChapter
import org.koitharu.kotatsu.details.ui.adapter.ChaptersAdapter
import org.koitharu.kotatsu.utils.ext.withArgs

class ChaptersDialog : AlertDialogFragment(R.layout.dialog_chapters),
	OnListItemClickListener<MangaChapter> {

	override fun onBuildDialog(builder: AlertDialog.Builder) {
		builder.setTitle(R.string.chapters)
			.setNegativeButton(R.string.close, null)
			.setCancelable(true)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		recyclerView_chapters.addItemDecoration(
			DividerItemDecoration(
				requireContext(),
				RecyclerView.VERTICAL
			)
		)
		recyclerView_chapters.adapter = ChaptersAdapter(this).apply {
			// arguments?.getParcelableArrayList<MangaChapter>(ARG_CHAPTERS)?.let(this::setItems)
			// currentChapterId = arguments?.getLong(ARG_CURRENT_ID, 0L)?.takeUnless { it == 0L }
		}
	}

	override fun onItemClick(item: MangaChapter, view: View) {
		((parentFragment as? OnChapterChangeListener)
			?: (activity as? OnChapterChangeListener))?.let {
			dismiss()
			it.onChapterChanged(item)
		}
	}

	fun interface OnChapterChangeListener {

		fun onChapterChanged(chapter: MangaChapter)
	}

	companion object {

		private const val TAG = "ChaptersDialog"

		private const val ARG_CHAPTERS = "chapters"
		private const val ARG_CURRENT_ID = "current_id"

		fun show(fm: FragmentManager, chapters: List<MangaChapter>, currentId: Long = 0L) =
			ChaptersDialog()
				.withArgs(2) {
					putParcelableArrayList(ARG_CHAPTERS, ArrayList(chapters))
					putLong(ARG_CURRENT_ID, currentId)
				}.show(fm, TAG)
	}
}