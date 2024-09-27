package br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode

@Entity(
    tableName = "episode_table",
    foreignKeys = [ForeignKey(
        entity = PodcastPersisted::class,
        parentColumns = ["id"],
        childColumns = ["podcastId"],
        onDelete = CASCADE
    )]
)
data class EpisodePersisted(
    @PrimaryKey(autoGenerate = false) var id: String,
    @ColumnInfo(name = "podcastId") var podcastId: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "audioUrl") var audioUrl: String,
    @ColumnInfo(name = "audioType") var audioType: String,
    @ColumnInfo(name = "imageUrl") var imageUrl: String,
    @ColumnInfo(name = "datePublished") var datePublished: Long,
    @ColumnInfo(name = "duration") var duration: Int,
    @ColumnInfo(name = "episode") var episode: Int,
    @ColumnInfo(name = "season") var season: Int,
    @ColumnInfo(name = "downloadId") var downloadId: Long? = null,
    @ColumnInfo(name = "path") var path: String = "",
    @ColumnInfo(name = "playing") var playing: Boolean = false
)

@Entity
data class EpisodePersistedDownloaded(
    @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "downloadId")  var downloadId: Long? = null,
    @ColumnInfo(name = "path") var path: String = ""
)

@Entity
data class EpisodePersistedPlaying(
    @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "playing") var playing: Boolean = false
)

fun Episode.Companion.toEpisode(episodePersisted: EpisodePersisted) = Episode(
    id = episodePersisted.id,
    podcastId = episodePersisted.podcastId,
    title = episodePersisted.title,
    description = episodePersisted.description,
    audioUrl = episodePersisted.audioUrl,
    audioType = episodePersisted.audioType,
    imageUrl = episodePersisted.imageUrl,
    datePublished = episodePersisted.datePublished,
    duration = episodePersisted.duration,
    episode = episodePersisted.episode,
    season = episodePersisted.season,
    downloadId = episodePersisted.downloadId,
    path = episodePersisted.path,
    playing = episodePersisted.playing
)

fun Episode.Companion.fromEpisode(episode: Episode) = EpisodePersisted(
    id = episode.id,
    podcastId = episode.podcastId,
    title = episode.title,
    description = episode.description,
    audioUrl = episode.audioUrl,
    audioType = episode.audioType,
    imageUrl = episode.imageUrl,
    datePublished = episode.datePublished,
    duration = episode.duration,
    episode = episode.episode,
    season = episode.season,
    downloadId = episode.downloadId,
    path = episode.path,
    playing = episode.playing
)