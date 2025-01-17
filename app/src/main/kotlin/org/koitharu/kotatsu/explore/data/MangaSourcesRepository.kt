package org.koitharu.kotatsu.explore.data

import androidx.room.withTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koitharu.kotatsu.BuildConfig
import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.core.db.dao.MangaSourcesDao
import org.koitharu.kotatsu.core.db.entity.MangaSourceEntity
import org.koitharu.kotatsu.core.model.MangaSource
import org.koitharu.kotatsu.core.ui.util.ReversibleHandle
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.util.move
import java.util.Collections
import java.util.EnumSet
import javax.inject.Inject

@Reusable
class MangaSourcesRepository @Inject constructor(
	private val db: MangaDatabase,
) {

	private val dao: MangaSourcesDao
		get() = db.sourcesDao

	private val remoteSources = EnumSet.allOf(MangaSource::class.java).apply {
		remove(MangaSource.LOCAL)
		if (!BuildConfig.DEBUG) {
			remove(MangaSource.DUMMY)
		}
	}

	val allMangaSources: Set<MangaSource>
		get() = Collections.unmodifiableSet(remoteSources)

	suspend fun getEnabledSources(): List<MangaSource> {
		return dao.findAllEnabled().toSources()
	}

	fun observeEnabledSources(): Flow<List<MangaSource>> = dao.observeEnabled().map {
		it.toSources()
	}

	fun observeAll(): Flow<List<Pair<MangaSource, Boolean>>> = dao.observeAll().map { entities ->
		val result = ArrayList<Pair<MangaSource, Boolean>>(entities.size)
		for (entity in entities) {
			val source = MangaSource(entity.source)
			if (source in remoteSources) {
				result.add(source to entity.isEnabled)
			}
		}
		result
	}

	suspend fun setSourceEnabled(source: MangaSource, isEnabled: Boolean): ReversibleHandle {
		dao.setEnabled(source.name, isEnabled)
		return ReversibleHandle {
			dao.setEnabled(source.name, !isEnabled)
		}
	}

	suspend fun setSourcesEnabled(sources: Iterable<MangaSource>, isEnabled: Boolean) {
		db.withTransaction {
			for (s in sources) {
				dao.setEnabled(s.name, isEnabled)
			}
		}
	}

	suspend fun disableAllSources() {
		db.withTransaction {
			assimilateNewSources()
			dao.disableAllSources()
		}
	}

	suspend fun setPosition(source: MangaSource, index: Int) {
		db.withTransaction {
			val all = dao.findAll().toMutableList()
			val sourceIndex = all.indexOfFirst { x -> x.source == source.name }
			if (sourceIndex !in all.indices) {
				val entity = MangaSourceEntity(
					source = source.name,
					isEnabled = false,
					sortKey = index,
				)
				all.add(index, entity)
				dao.upsert(entity)
			} else {
				all.move(sourceIndex, index)
			}
			for ((i, e) in all.withIndex()) {
				if (e.sortKey != i) {
					dao.setSortKey(e.source, i)
				}
			}
		}
	}

	fun observeNewSources(): Flow<Set<MangaSource>> = dao.observeAll().map { entities ->
		val result = EnumSet.copyOf(remoteSources)
		for (e in entities) {
			result.remove(MangaSource(e.source))
		}
		result
	}.distinctUntilChanged()

	suspend fun getNewSources(): Set<MangaSource> {
		val entities = dao.findAll()
		val result = EnumSet.copyOf(remoteSources)
		for (e in entities) {
			result.remove(MangaSource(e.source))
		}
		return result
	}

	suspend fun assimilateNewSources(): Set<MangaSource> {
		val new = getNewSources()
		if (new.isEmpty()) {
			return emptySet()
		}
		var maxSortKey = dao.getMaxSortKey()
		val entities = new.map { x ->
			MangaSourceEntity(
				source = x.name,
				isEnabled = false,
				sortKey = ++maxSortKey,
			)
		}
		dao.insertIfAbsent(entities)
		return new
	}

	suspend fun isSetupRequired(): Boolean {
		return dao.findAll().isEmpty()
	}

	private fun List<MangaSourceEntity>.toSources(): List<MangaSource> {
		val result = ArrayList<MangaSource>(size)
		for (entity in this) {
			val source = MangaSource(entity.source)
			if (source in remoteSources) {
				result.add(source)
			}
		}
		return result
	}
}
