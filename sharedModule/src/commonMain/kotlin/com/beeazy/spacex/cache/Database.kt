package com.beeazy.spacex.cache

import app.cash.sqldelight.ColumnAdapter
import beeazy.spacexlaunches.AppDatabase
import com.beeazy.spacex.entity.Launch
import com.beeazy.spacex.entity.Links
import com.beeazy.spacex.entity.Patch

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver(), migrations.Launch.Adapter(
        launchSuccessAdapter = object : ColumnAdapter<Boolean, Int> {
            override fun decode(databaseValue: Int): Boolean {
                return databaseValue == 1
            }

            override fun encode(value: Boolean): Int {
                return if (value) 1 else 0
            }
        }
    ))
    private val dbQuery = database.launchQueries

    internal fun ClearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllLaunches()
        }
    }

    internal fun GetAllLaunches(): List<Launch> {
        return dbQuery.selectAllLaunches(::MapLaunchSelecting).executeAsList()
    }

    private fun MapLaunchSelecting(
        flightNumber: Int,
        missionName: String,
        details: String?,
        launchSuccess: Boolean?,
        launchDateUTC: String,
        patchUrlSmall: String?,
        patchUrlLarge: String?,
        articleUrl: String?
    ): Launch {
        return Launch(
            flightNumber = flightNumber.toInt(),
            missionName = missionName,
            details = details,
            launchDateUTC = launchDateUTC,
            launchSuccess = launchSuccess,
            links = Links(
                patch = Patch(
                    small = patchUrlSmall,
                    large = patchUrlLarge
                ),
                article = articleUrl
            )
        )
    }

    internal fun createLaunches(launches: List<Launch>) {
        dbQuery.transaction {
            launches.forEach { launch ->
                insertLaunch(launch)
            }
        }
    }

    private fun insertLaunch(launch: Launch) {
        dbQuery.insert(
            flightNumber = launch.flightNumber,
            missionName = launch.missionName,
            details = launch.details,
            launchSuccess = launch.launchSuccess ?: false,
            launchDateUTC = launch.launchDateUTC,
            patchUrlSmall = launch.links?.patch?.small,
            patchUrlLarge = launch.links?.patch?.large,
            articleUrl = launch.links?.article
        )
    }
}