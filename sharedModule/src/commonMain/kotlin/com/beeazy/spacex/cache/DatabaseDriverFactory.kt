package com.beeazy.spacex.cache

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import beeazy.spacexlaunches.AppDatabase
import migrations.Launch

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}