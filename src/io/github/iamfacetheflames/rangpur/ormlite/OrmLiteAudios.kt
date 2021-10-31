package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.dao.GenericRawResults
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.data.*
import io.github.iamfacetheflames.rangpur.repository.database.Database
import java.lang.StringBuilder
import java.sql.SQLException

class OrmLiteAudios(var source: ConnectionSource) : Database.Audios {

    override fun getFiltered(filter: Filter): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val request = StringBuilder().apply {
            append("SELECT a.* FROM audio as a INNER JOIN directory as p ON a.directory_uuid = p.uuid ")
            AudioField.apply {
                val conditions = ArrayList<String>().apply {
                    if (filter.isSearchRequest()) {
                        val item: Pair<Int, Keys.Key>? = Keys.keyMap.filter { it.value.lancelot.equals(filter.searchRequest, true) }.toList().firstOrNull()
                        if (item != null) {
                            add(
                                "(" + like(FILE_NAME, filter.searchRequest) + " OR $KEY = ${item.first} ) "
                            )
                        } else {
                            add(
                                like(FILE_NAME, filter.searchRequest)
                            )
                        }
                    }
                    if (filter.isDirectoriesFiltered()) {
                        val locationDirs = mutableListOf<String>()
                        filter.directories.forEach {
                            it.locationInMusicDirectory?.let { location ->
                                locationDirs.add(location)
                            }
                        }
                        add(
                            likeOrExpression("p.location", locationDirs, true)
                        )
                    }
                    if (filter.isDateFiltered()) {
                        add(
                            likeOrExpression(DATE_CREATED, filter.dateList)
                        )
                    }
                    if (filter.isOnlyWithoutPlaylist) {
                        add("(SELECT COUNT(*) FROM audio_in_playlist AS aip WHERE aip.audio_uuid = a.uuid) == 0 ")
                    }
                }
                append(where(conditions))
                filter.sort.apply {
                    append("ORDER BY $columnName $direction ")
                }
                append(";")
            }
        }.toString()
        val queryResult: GenericRawResults<OrmLiteAudio> = dao.queryRaw(request, dao.rawRowMapper)
        return queryResult.results
    }

    override fun create(items: List<Audio>) {
        val dao: Dao<OrmLiteAudio, String> =
            DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.callBatchTasks {
            for (audio in items) {
                try {
                    dao.createIfNotExists(audio as OrmLiteAudio)
                } catch (e: SQLException) {
                    if (e.cause?.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                        // ignore
                    } else {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getAll(): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val request = "SELECT * FROM audio ORDER BY ${AudioField.TIMESTAMP_CREATED} DESC;"
        val queryResult: GenericRawResults<OrmLiteAudio> = dao.queryRaw(request, dao.rawRowMapper)
        return queryResult.results
    }

    override fun update(items: List<Audio>) {
        val dao: Dao<OrmLiteAudio, String> =
            DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.callBatchTasks {
            for (audio in items) {
                dao.update(audio as OrmLiteAudio)
            }
        }
    }

    override fun delete(items: List<Audio>) {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.delete(items as List<OrmLiteAudio>)
    }

}