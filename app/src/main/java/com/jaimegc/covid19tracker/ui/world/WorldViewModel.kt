package com.jaimegc.covid19tracker.ui.world

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jaimegc.covid19tracker.domain.model.*
import com.jaimegc.covid19tracker.domain.states.State
import com.jaimegc.covid19tracker.domain.states.StateError
import com.jaimegc.covid19tracker.domain.usecase.GetCountryStats
import com.jaimegc.covid19tracker.domain.usecase.GetCovidTrackerLast
import com.jaimegc.covid19tracker.domain.usecase.GetWorldStats
import com.jaimegc.covid19tracker.ui.model.toChartUI
import com.jaimegc.covid19tracker.ui.model.toUI
import com.jaimegc.covid19tracker.ui.viewmodel.BaseScreenStateViewModel
import com.jaimegc.covid19tracker.ui.states.ScreenState
import com.jaimegc.covid19tracker.ui.states.WorldStateScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WorldViewModel(
    val getCovidTrackerLast: GetCovidTrackerLast,
    val getWorldStats: GetWorldStats,
    val getCountryStats: GetCountryStats
) : BaseScreenStateViewModel<WorldStateScreen>() {

    override val _screenState = MutableLiveData<ScreenState<WorldStateScreen>>()
    override val screenState: LiveData<ScreenState<WorldStateScreen>> = _screenState

    fun getCovidTrackerLast() =
        viewModelScope.launch {
            getCovidTrackerLast.getCovidTrackerByDate("2020-04-21").collect { result ->
                result.fold(::handleError, ::handleScreenStateCovidTracker)
            }
        }

    fun getWorldAllStats() =
        viewModelScope.launch {
            getWorldStats.getWorldAllStats().collect { result ->
                result.fold(::handleError, ::handleScreenStateWorldStats)
            }
        }

    fun getCountriesStatsOrderByConfirmed() =
        viewModelScope.launch {
            getCountryStats.getCountriesStatsOrderByConfirmed().collect { result ->
                result.fold(::handleError, ::handleScreenStateCountriesBarStats)
            }
        }

    fun getCountriesAndStatsWithMostConfirmed() =
        viewModelScope.launch {
            getCountryStats.getCountriesAndStatsWithMostConfirmed().collect { result ->
                result.fold(::handleError, ::handleScreenStateCountriesLineStats)
            }
        }

    private fun handleScreenStateCovidTracker(state: State<CovidTracker>) =
        when (state) {
            is State.Success ->
                _screenState.postValue(ScreenState.Render(WorldStateScreen.SuccessCovidTracker(state.data.toUI())))
            is State.Loading ->
                _screenState.postValue(ScreenState.Loading)
    }

    private fun handleScreenStateWorldStats(state: State<List<WorldStats>>) =
        when (state) {
            is State.Success ->
                _screenState.postValue(ScreenState.Render(WorldStateScreen.SuccessWorldStatsBarCharts(
                    state.data.map { worldStats -> worldStats.toChartUI() })))
            is State.Loading ->
                _screenState.postValue(ScreenState.Loading)
        }

    private fun handleScreenStateCountriesBarStats(state: State<List<CountryListStats>>) =
        when (state) {
            is State.Success ->
                _screenState.postValue(ScreenState.Render(WorldStateScreen.SuccessCountriesStatsBarCharts(
                    state.data.map { countryStats -> countryStats.toChartUI() })))
            is State.Loading ->
                _screenState.postValue(ScreenState.Loading)
        }

    private fun handleScreenStateCountriesLineStats(state: State<List<CountryListStats>>) =
        when (state) {
            is State.Success ->
                _screenState.postValue(ScreenState.Render(WorldStateScreen.SuccessCountriesStatsLineCharts(
                    state.data.map { countryStats -> countryStats.toChartUI() })))
            is State.Loading ->
                _screenState.postValue(ScreenState.Loading)
        }

    private fun handleError(state: StateError<DomainError>) {

    }
}