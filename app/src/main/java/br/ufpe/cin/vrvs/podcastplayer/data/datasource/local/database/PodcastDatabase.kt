package br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.EpisodePersisted
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.PodcastPersisted
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.converter.MapConverter

@Database(
    entities = [
        EpisodePersisted::class,
        PodcastPersisted::class
    ],
    version = 1
)
@TypeConverters(MapConverter::class)
abstract class PodcastDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao
}
