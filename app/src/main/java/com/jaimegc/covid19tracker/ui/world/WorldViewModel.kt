package com.jaimegc.covid19tracker.ui.world

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jaimegc.covid19tracker.domain.model.CovidTracker
import com.jaimegc.covid19tracker.domain.model.DomainError
import com.jaimegc.covid19tracker.domain.states.State
import com.jaimegc.covid19tracker.domain.states.StateError
import com.jaimegc.covid19tracker.domain.usecase.GetCovidTrackerLast
import com.jaimegc.covid19tracker.ui.model.toUI
import com.jaimegc.covid19tracker.ui.viewmodel.BaseScreenStateViewModel
import com.jaimegc.covid19tracker.ui.states.ScreenState
import com.jaimegc.covid19tracker.ui.states.WorldStateScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WorldViewModel(
    val getCovidTrackerLast: GetCovidTrackerLast
) : BaseScreenStateViewModel<WorldStateScreen>() {

    override val _screenState = MutableLiveData<ScreenState<WorldStateScreen>>()
    override val screenState: LiveData<ScreenState<WorldStateScreen>> = _screenState

    fun getCovidTrackerLast() =
        viewModelScope.launch {
            getCovidTrackerLast.getCovidTrackerByDate("2020-04-18").collect { result ->
                result.fold(::handleError, ::handleScreenState)
            }
        }

    private fun handleScreenState(state: State<CovidTracker>) =
        when (state) {
            is State.Success ->
                _screenState.postValue(ScreenState.Render(WorldStateScreen.Success(state.data.toUI())))
            is State.Loading ->
                _screenState.postValue(ScreenState.Loading)
    }

    private fun handleError(state: StateError<DomainError>) {

    }
}