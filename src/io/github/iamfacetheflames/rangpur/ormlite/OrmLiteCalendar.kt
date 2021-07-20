package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.data.AudioField
import io.github.iamfacetheflames.rangpur.repository.database.Database

class OrmLiteCalendar(var source: ConnectionSource) : Database.Calendar {

    override fun getDateList(): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        val request = "SELECT date_created FROM audio GROUP BY date_created ORDER BY timestamp_created DESC"
        dao.queryRaw(request).results.forEach { results.add( it.first() ) }
        return results
    }

    override fun getYears(): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        val request = "SELECT strftime('%Y', date_created) as t1 FROM audio GROUP BY t1 ORDER BY timestamp_created DESC"
        dao.queryRaw(request).results.forEach { results.add( it.first() ) }
        return results
    }

    override fun getMonths(year: String): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        AudioField.apply {
            val request = "SELECT strftime('%Y.%m', $DATE_CREATED) as t1 " +
                    "FROM $AUDIO_TABLE_NAME WHERE $DATE_CREATED LIKE '$year%' " +
                    "GROUP BY t1 ORDER BY $TIMESTAMP_CREATED DESC"
            dao.queryRaw(request).results.forEach { results.add( it.first() ) }
            return results
        }
    }

    override fun getDays(yearAndMonth: String): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        AudioField.apply {
            val request = "SELECT strftime('%Y.%m.%d', $DATE_CREATED) as t1 " +
                    "FROM $AUDIO_TABLE_NAME WHERE $DATE_CREATED LIKE '$yearAndMonth%' " +
                    "GROUP BY t1 ORDER BY $TIMESTAMP_CREATED DESC"
            dao.queryRaw(request).results.forEach { results.add( it.first() ) }
            return results
        }
    }

}