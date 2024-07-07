package br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast.*

@Entity(tableName = "podcast_table")
data class PodcastPersisted(
    @PrimaryKey(autoGenerate = false) var id: String,
    @ColumnInfo(name = "author") var author: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "imageUrl") var imageUrl: String,
    @ColumnInfo(name = "categories") var categories: Map<String, String>,
    @ColumnInfo(name = "subscribed") var subscribed: Boolean = false
)

fun Companion.toPodcast(podcastPersisted: PodcastPersisted) = Podcast(
    id = podcastPersisted.id,
    author = podcastPersisted.author,
    description = podcastPersisted.description,
    title = podcastPersisted.title,
    imageUrl = podcastPersisted.imageUrl,
    categories = podcastPersisted.categories,
    subscribed = podcastPersisted.subscribed
)

fun Companion.fromPodcast(podcast: Podcast) = PodcastPersisted(
    id = podcast.id,
    author = podcast.author ?: "",
    description = podcast.description ?: "",
    title = podcast.title,
    imageUrl = podcast.imageUrl,
    categories = podcast.categories,
    subscribed = podcast.subscribed
)