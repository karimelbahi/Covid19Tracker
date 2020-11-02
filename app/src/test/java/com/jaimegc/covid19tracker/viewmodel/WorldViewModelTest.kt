package com.jaimegc.covid19tracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.jaimegc.covid19tracker.domain.usecase.GetCountryStats
import com.jaimegc.covid19tracker.domain.usecase.GetWorldAndCountries
import com.jaimegc.covid19tracker.domain.usecase.GetWorldStats
import com.jaimegc.covid19tracker.utils.getOrAwaitValue
import com.jaimegc.covid19tracker.utils.observeForTesting
import com.jaimegc.covid19tracker.ui.base.states.MenuItemViewType
import com.jaimegc.covid19tracker.ui.base.states.ScreenState
import com.jaimegc.covid19tracker.ui.base.states.WorldStateScreen
import com.jaimegc.covid19tracker.ui.world.WorldViewModel
import com.jaimegc.covid19tracker.utils.MainCoroutineRule
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateCovidSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateErrorDatabaseEmpty
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateCovidTrackerLoading
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListCountryAndStatsLoading
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListCountryAndStatsMostConfirmedSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListCountryAndStatsMostDeathsSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListCountryAndStatsMostOpenCasesSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListCountryAndStatsMostRecoveredSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListCountryAndStatsSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListWorldStatsLoading
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateListWorldStatsSuccess
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenErrorDatabaseEmptyData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessCountriesStatsPieChartData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessCovidTrackerData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessListCountryAndStatsBarChartData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessListCountryAndStatsLineChartMostConfirmedData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessListCountryAndStatsLineChartMostDeathsData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessListCountryAndStatsLineChartMostOpenCasesData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessListCountryAndStatsLineChartMostRecoveredData
import com.jaimegc.covid19tracker.utils.ScreenStateBuilder.stateScreenSuccessListWorldStatsPieChartData
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class WorldViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineRule()

    private val captor = argumentCaptor<ScreenState<WorldStateScreen>>()

    private lateinit var worldViewModel: WorldViewModel
    private val getWorldAndCountries: GetWorldAndCountries = mock()
    private val getWorldStats: GetWorldStats = mock()
    private val getCountryStats: GetCountryStats = mock()

    private val stateObserver: Observer<ScreenState<WorldStateScreen>> = mock()

    @Before
    fun setup() {
        worldViewModel = WorldViewModel(getWorldAndCountries, getWorldStats, getCountryStats)
        worldViewModel.screenState.observeForever(stateObserver)
    }

    @Test
    fun `get list chart stats should return loading and success if date exists`() {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.right(stateCovidSuccess))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val success = captor.secondValue

        assertEquals(ScreenState.Loading, loading)
        assertTrue(success is ScreenState.Render)
        assertTrue((success as ScreenState.Render).renderState is WorldStateScreen.SuccessCovidTracker)
        assertEquals(stateScreenSuccessCovidTrackerData,
            (success.renderState as WorldStateScreen.SuccessCovidTracker).data)
    }

    /*****************************************************************
     *  getOrAwaitValue and observeForTesting from Google repository *
     *****************************************************************/

    @Test
    fun `get list chart stats should return loading and success if date exists using getOrAwaitValue & observeForTesting`() {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            delay(10)
            emit(Either.right(stateCovidSuccess))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListChartStats()

        worldViewModel.screenState.getOrAwaitValue {
            val loading = worldViewModel.screenState.value
            coroutineScope.advanceUntilIdle()
            val success = worldViewModel.screenState.value

            assertEquals(ScreenState.Loading, loading)
            assertNotNull(success)
            assertTrue(success is ScreenState.Render)
            assertTrue((success as ScreenState.Render).renderState is WorldStateScreen.SuccessCovidTracker)
            assertEquals(stateScreenSuccessCovidTrackerData,
                (success.renderState as WorldStateScreen.SuccessCovidTracker).data)
        }

        worldViewModel.getListChartStats()

        worldViewModel.screenState.observeForTesting {
            val loading = worldViewModel.screenState.value
            coroutineScope.advanceUntilIdle()
            val success = worldViewModel.screenState.value

            assertEquals(ScreenState.Loading, loading)
            assertNotNull(success)
            assertTrue(success is ScreenState.Render)
            assertTrue((success as ScreenState.Render).renderState is WorldStateScreen.SuccessCovidTracker)
            assertEquals(stateScreenSuccessCovidTrackerData,
                (success.renderState as WorldStateScreen.SuccessCovidTracker).data)
        }
    }

    /***********************************************************************************************/

    @Test
    fun `get list chart stats should return loading and error database empty if date doesnt exist`() {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getListChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val error = captor.secondValue

        assertEquals(ScreenState.Loading, loading)
        assertTrue(error is ScreenState.Error)
        assertTrue((error as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (error.errorState as WorldStateScreen.SomeError).data)
    }

    @Test
    fun `get pie chart stats should return loading and success if date exists`() {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.right(stateCovidSuccess))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getPieChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val success = captor.secondValue

        assertEquals(ScreenState.Loading, loading)
        assertTrue(success is ScreenState.Render)
        assertTrue((success as ScreenState.Render).renderState is WorldStateScreen.SuccessCountriesStatsPieCharts)
        assertEquals(stateScreenSuccessCountriesStatsPieChartData,
            (success.renderState as WorldStateScreen.SuccessCountriesStatsPieCharts).data)
    }

    @Test
    fun `get pie chart stats should return loading and error database empty if date doesnt exist`() {
        val flow = flow {
            emit(Either.right(stateCovidTrackerLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        whenever(getWorldAndCountries.getWorldAndCountriesByDate()).thenReturn(flow)

        worldViewModel.getPieChartStats()

        verify(stateObserver, Mockito.times(2)).onChanged(captor.capture())

        val loading = captor.firstValue
        val error = captor.secondValue

        assertEquals(ScreenState.Loading, loading)
        assertTrue(error is ScreenState.Error)
        assertTrue((error as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (error.errorState as WorldStateScreen.SomeError).data)
    }

    @Test
    fun `get bar chart stats should return loading and success if date exists`() {
        val worldFlow = flow {
            emit(Either.right(stateListWorldStatsLoading))
            emit(Either.right(stateListWorldStatsSuccess))
        }

        val countriesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsSuccess))
        }

        whenever(getWorldStats.getWorldAllStats()).thenReturn(worldFlow)
        whenever(getCountryStats.getCountriesStatsOrderByConfirmed()).thenReturn(countriesFlow)

        worldViewModel.getBarChartStats()

        verify(stateObserver, Mockito.times(4)).onChanged(captor.capture())

        val worldLoading = captor.firstValue
        val worldSuccess = captor.secondValue
        val countriesLoading = captor.thirdValue
        val countriesSuccess = captor.lastValue

        assertEquals(ScreenState.Loading, worldLoading)
        assertEquals(ScreenState.Loading, countriesLoading)
        assertTrue(worldSuccess is ScreenState.Render)
        assertTrue((worldSuccess as ScreenState.Render).renderState is WorldStateScreen.SuccessWorldStatsBarCharts)
        assertEquals(stateScreenSuccessListWorldStatsPieChartData,
            (worldSuccess.renderState as WorldStateScreen.SuccessWorldStatsBarCharts).data)
        assertTrue(countriesSuccess is ScreenState.Render)
        assertTrue((countriesSuccess as ScreenState.Render).renderState is WorldStateScreen.SuccessCountriesStatsBarCharts)
        assertEquals(stateScreenSuccessListCountryAndStatsBarChartData,
            (countriesSuccess.renderState as WorldStateScreen.SuccessCountriesStatsBarCharts).data)
    }

    @Test
    fun `get bar chart stats should return loading and error database empty if date doesnt exist`() {
        val worldFlow = flow {
            emit(Either.right(stateListWorldStatsLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        val countriesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        whenever(getWorldStats.getWorldAllStats()).thenReturn(worldFlow)
        whenever(getCountryStats.getCountriesStatsOrderByConfirmed()).thenReturn(countriesFlow)

        worldViewModel.getBarChartStats()

        verify(stateObserver, Mockito.times(4)).onChanged(captor.capture())

        val worldLoading = captor.firstValue
        val worldError = captor.secondValue
        val countriesLoading = captor.thirdValue
        val countriesError = captor.lastValue

        assertEquals(ScreenState.Loading, worldLoading)
        assertEquals(ScreenState.Loading, countriesLoading)
        assertTrue(worldError is ScreenState.Error)
        assertTrue((worldError as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (worldError.errorState as WorldStateScreen.SomeError).data)
        assertTrue(countriesError is ScreenState.Error)
        assertTrue((countriesError as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (countriesError.errorState as WorldStateScreen.SomeError).data)
    }

    @Test
    fun `get line charts stats should return loading and success if date exists`() {
        val mostConfirmedFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostConfirmedSuccess))
        }

        val mostDeathsFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostDeathsSuccess))
        }

        val mostOpenCasesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostOpenCasesSuccess))
        }

        val mostRecoveredFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.right(stateListCountryAndStatsMostRecoveredSuccess))
        }

        whenever(getCountryStats.getCountriesAndStatsWithMostConfirmed()).thenReturn(mostConfirmedFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostDeaths()).thenReturn(mostDeathsFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostOpenCases()).thenReturn(mostOpenCasesFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostRecovered()).thenReturn(mostRecoveredFlow)

        worldViewModel.getLineChartsStats()

        verify(stateObserver, Mockito.times(8)).onChanged(captor.capture())

        val mostConfirmedLoading = captor.allValues[0]
        val mostConfirmedSuccess = captor.allValues[1]
        val mostDeathsLoading = captor.allValues[2]
        val mostDeathsSuccess = captor.allValues[3]
        val mostOpenCasesLoading = captor.allValues[4]
        val mostOpenCasesSuccess = captor.allValues[5]
        val mostRecoveredLoading = captor.allValues[6]
        val mostRecoveredSuccess = captor.allValues[7]

        assertEquals(ScreenState.Loading, mostConfirmedLoading)
        assertEquals(ScreenState.Loading, mostDeathsLoading)
        assertEquals(ScreenState.Loading, mostOpenCasesLoading)
        assertEquals(ScreenState.Loading, mostRecoveredLoading)
        assertTrue(mostConfirmedSuccess is ScreenState.Render)
        assertTrue((mostConfirmedSuccess as ScreenState.Render).renderState is WorldStateScreen.SuccessCountriesStatsLineCharts)
        assertEquals(stateScreenSuccessListCountryAndStatsLineChartMostConfirmedData[MenuItemViewType.LineChartMostConfirmed],
            (mostConfirmedSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts).data[MenuItemViewType.LineChartMostConfirmed])
        assertTrue(mostDeathsSuccess is ScreenState.Render)
        assertTrue((mostDeathsSuccess as ScreenState.Render).renderState is WorldStateScreen.SuccessCountriesStatsLineCharts)
        assertEquals(stateScreenSuccessListCountryAndStatsLineChartMostDeathsData[MenuItemViewType.LineChartMostDeaths],
            (mostDeathsSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts).data[MenuItemViewType.LineChartMostDeaths])
        assertTrue(mostOpenCasesSuccess is ScreenState.Render)
        assertTrue((mostOpenCasesSuccess as ScreenState.Render).renderState is WorldStateScreen.SuccessCountriesStatsLineCharts)
        assertEquals(stateScreenSuccessListCountryAndStatsLineChartMostOpenCasesData[MenuItemViewType.LineChartMostOpenCases],
            (mostOpenCasesSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts).data[MenuItemViewType.LineChartMostOpenCases])
        assertTrue(mostRecoveredSuccess is ScreenState.Render)
        assertTrue((mostRecoveredSuccess as ScreenState.Render).renderState is WorldStateScreen.SuccessCountriesStatsLineCharts)
        assertEquals(stateScreenSuccessListCountryAndStatsLineChartMostRecoveredData[MenuItemViewType.LineChartMostRecovered],
            (mostRecoveredSuccess.renderState as WorldStateScreen.SuccessCountriesStatsLineCharts).data[MenuItemViewType.LineChartMostRecovered])
    }

    @Test
    fun `get line charts stats should return loading and error database empty if date doesnt exist`() {
        val mostConfirmedFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        val mostDeathsFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        val mostOpenCasesFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        val mostRecoveredFlow = flow {
            emit(Either.right(stateListCountryAndStatsLoading))
            emit(Either.left(stateErrorDatabaseEmpty))
        }

        whenever(getCountryStats.getCountriesAndStatsWithMostConfirmed()).thenReturn(mostConfirmedFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostDeaths()).thenReturn(mostDeathsFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostOpenCases()).thenReturn(mostOpenCasesFlow)
        whenever(getCountryStats.getCountriesAndStatsWithMostRecovered()).thenReturn(mostRecoveredFlow)

        worldViewModel.getLineChartsStats()

        verify(stateObserver, Mockito.times(8)).onChanged(captor.capture())

        val mostConfirmedLoading = captor.allValues[0]
        val mostConfirmedError = captor.allValues[1]
        val mostDeathsLoading = captor.allValues[2]
        val mostDeathsError = captor.allValues[3]
        val mostOpenCasesLoading = captor.allValues[4]
        val mostOpenCasesError = captor.allValues[5]
        val mostRecoveredLoading = captor.allValues[6]
        val mostRecoveredError = captor.allValues[7]

        assertEquals(ScreenState.Loading, mostConfirmedLoading)
        assertEquals(ScreenState.Loading, mostDeathsLoading)
        assertEquals(ScreenState.Loading, mostOpenCasesLoading)
        assertEquals(ScreenState.Loading, mostRecoveredLoading)
        assertTrue(mostConfirmedError is ScreenState.Error)
        assertTrue((mostConfirmedError as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (mostConfirmedError.errorState as WorldStateScreen.SomeError).data)
        assertTrue(mostDeathsError is ScreenState.Error)
        assertTrue((mostDeathsError as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (mostDeathsError.errorState as WorldStateScreen.SomeError).data)
        assertTrue(mostOpenCasesError is ScreenState.Error)
        assertTrue((mostOpenCasesError as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (mostOpenCasesError.errorState as WorldStateScreen.SomeError).data)
        assertTrue(mostRecoveredError is ScreenState.Error)
        assertTrue((mostRecoveredError as ScreenState.Error).errorState is WorldStateScreen.SomeError)
        assertEquals(stateScreenErrorDatabaseEmptyData,
            (mostRecoveredError.errorState as WorldStateScreen.SomeError).data)
    }
}