package com.jaimegc.covid19tracker.domain.model

data class CovidTracker(
    val countriesStats: List<CountryStats>,
    val worldStats: WorldStats
)

data class WorldStats(
    val date: String,
    val updatedAt: String,
    val stats: Stats
)

data class CountryStats(
    val id: String,
    val name: String,
    val nameEs: String,
    val stats: Stats
)

data class Stats(
    val date: String,
    val source: String,
    val confirmed: Long,
    val deaths: Long,
    val newConfirmed: Long,
    val newDeaths: Long,
    val newOpenCases: Long,
    val newRecovered: Long,
    val openCases: Long,
    val recovered: Long,
    val vsYesterdayConfirmed: Double,
    val vsYesterdayDeaths: Double,
    val vsYesterdayOpenCases: Double,
    val vsYesterdayRecovered: Double
)


