package com.jaimegc.covid19tracker.ui.country

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.MergeAdapter
import com.jaimegc.covid19tracker.R
import com.jaimegc.covid19tracker.databinding.FragmentCountryBinding
import com.jaimegc.covid19tracker.databinding.LoadingBinding
import com.jaimegc.covid19tracker.common.extensions.*
import com.jaimegc.covid19tracker.data.preference.CountryPreferences
import com.jaimegc.covid19tracker.databinding.EmptyDatabaseBinding
import com.jaimegc.covid19tracker.ui.adapter.*
import com.jaimegc.covid19tracker.ui.base.BaseFragment
import com.jaimegc.covid19tracker.ui.model.StatsChartUI
import com.jaimegc.covid19tracker.ui.base.states.PlaceStateScreen
import com.jaimegc.covid19tracker.ui.base.states.ScreenState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.get

@FlowPreview
@ExperimentalCoroutinesApi
class CountryFragment : BaseFragment<CountryViewModel, PlaceStateScreen>(R.layout.fragment_country) {

    override val viewModel: CountryViewModel by viewModel()

    private val countryPreferences: CountryPreferences = get()
    private val placeTotalAdapter = PlaceTotalAdapter()
    private val placeAdapter = PlaceAdapter()
    private val placeTotalBarChartAdapter = PlaceTotalBarChartAdapter()
    private val placeBarChartAdapter = PlaceBarChartAdapter()
    private val placeTotalPieChartAdapter = PlaceTotalPieChartAdapter()
    private val placePieChartAdapter = PlacePieChartAdapter()
    private val placeLineChartAdapter = PlaceLineChartAdapter()
    private val mergeAdapter = MergeAdapter()

    private lateinit var binding: FragmentCountryBinding
    private lateinit var loadingBinding: LoadingBinding
    private lateinit var emptyDatabaseBinding: EmptyDatabaseBinding
    private lateinit var menu: Menu
    private lateinit var countrySpinnerAdapter: CountrySpinnerAdapter
    private lateinit var placeSpinnerAdapter: PlaceSpinnerAdapter
    private lateinit var statsParent: StatsChartUI

    private var countryJustSelected = false
    private var currentMenuItem = MENU_ITEM_LIST

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCountryBinding.bind(view)
        loadingBinding = LoadingBinding.bind(view)
        emptyDatabaseBinding = EmptyDatabaseBinding.bind(view)

        binding.recyclerPlace.adapter = mergeAdapter

        viewModel.screenState.observe(viewLifecycleOwner, Observer { screenState ->
            when (screenState) {
                ScreenState.Loading ->
                    if (mergeAdapter.adapters.isEmpty()) {
                        emptyDatabaseBinding.groupEmpty.hide()
                        loadingBinding.loading.show()
                    }
                ScreenState.EmptyData ->
                    if (currentMenuItem == MENU_ITEM_LINE_CHART) {
                        emptyDatabaseBinding.groupEmpty.show()
                    }
                is ScreenState.Render<PlaceStateScreen> -> {
                    loadingBinding.loading.hide()
                    handleRenderState(screenState.renderState)
                }
                is ScreenState.Error<PlaceStateScreen> -> {
                    // Not implemented
                }
            }
        })

        viewModel.getCountries()
        setHasOptionsMenu(true)
    }

    override fun handleRenderState(renderState: PlaceStateScreen) {
        when (renderState) {
            is PlaceStateScreen.SuccessSpinnerCountries -> {
                countrySpinnerAdapter = CountrySpinnerAdapter(renderState.data)
                binding.countrySpinner.adapter = countrySpinnerAdapter

                binding.countrySpinner.setSelection(
                    renderState.data.indexOf(renderState.data.first { country ->
                        country.id == countryPreferences.getId() })
                    )

                binding.countrySpinner.onItemSelected { pos ->
                    countrySpinnerAdapter.getCountryId(pos).let { idCountry ->
                        countryPreferences.save(idCountry)
                        countryJustSelected = true
                        countrySpinnerAdapter.saveCurrentPosition(pos)
                        viewModel.getRegionsByCountry(idCountry)
                        selectMenu(idCountry)
                    }
                }
            }
            is PlaceStateScreen.SuccessSpinnerRegions -> {
                if (renderState.data.isNotEmpty()) {
                    binding.regionSpinner.show()
                    binding.icExpandRegion.show()
                    placeSpinnerAdapter =
                        PlaceSpinnerAdapter(requireContext(), renderState.data.toMutableList())
                    binding.regionSpinner.adapter = placeSpinnerAdapter

                    binding.regionSpinner.onItemSelected(ignoreFirst = false) { pos ->
                        if (countryJustSelected.not()) {
                            placeSpinnerAdapter.saveCurrentPosition(pos)
                            selectMenu(countrySpinnerAdapter.getCurrentCountryId(),
                                placeSpinnerAdapter.getId(pos))
                        }
                        countryJustSelected = false
                    }
                } else {
                    binding.regionSpinner.hide()
                    binding.icExpandRegion.hide()
                }
            }
            is PlaceStateScreen.SuccessPlaceAndStats -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_LIST)) {
                    mergeAdapter.addAdapter(placeTotalAdapter)
                    placeTotalAdapter.submitList(listOf(renderState.data))
                    binding.recyclerPlace.scrollToPosition(0)
                }
            }
            is PlaceStateScreen.SuccessPlaceStats -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_LIST)) {
                    mergeAdapter.addAdapter(placeAdapter)
                    placeAdapter.submitList(renderState.data)
                    binding.recyclerPlace.scrollToPosition(0)
                }
            }
            is PlaceStateScreen.SuccessPlaceTotalStatsBarChart -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_BAR_CHART)) {
                    mergeAdapter.addAdapter(0, placeTotalBarChartAdapter)
                    if (mergeAdapter.containsAdapter(placeBarChartAdapter)) {
                        binding.recyclerPlace.scrollToPosition(0)
                    }
                    placeTotalBarChartAdapter.submitList(listOf(renderState.data))
                }
            }
            is PlaceStateScreen.SuccessPlaceStatsBarChart -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_BAR_CHART)) {
                    if (mergeAdapter.containsAdapter(placeTotalBarChartAdapter)) {
                        mergeAdapter.addAdapter(1, placeBarChartAdapter)
                    } else {
                        mergeAdapter.addAdapter(0, placeBarChartAdapter)
                    }
                    placeBarChartAdapter.submitList(renderState.data)
                }
            }
            is PlaceStateScreen.SuccessPlaceTotalStatsPieChart -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_PIE_CHART)) {
                    statsParent = renderState.data

                    if (statsParent.isNotEmpty()) {
                        mergeAdapter.addAdapter(0, placeTotalPieChartAdapter)

                        if (mergeAdapter.containsAdapter(placePieChartAdapter)) {
                            binding.recyclerPlace.scrollToPosition(0)
                        }

                        placeTotalPieChartAdapter.submitList(listOf(statsParent))
                    } else {
                        emptyDatabaseBinding.groupEmpty.show()
                    }
                }
            }
            is PlaceStateScreen.SuccessPlaceAndStatsPieChart -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_PIE_CHART)) {
                    if (mergeAdapter.containsAdapter(placeTotalPieChartAdapter)) {
                        if (placeTotalPieChartAdapter.currentList.isNotEmpty()) {
                            renderState.data.map { placeStats ->
                                placeStats.statsParent = statsParent
                            }
                        }
                        mergeAdapter.addAdapter(1, placePieChartAdapter)
                    } else {
                        mergeAdapter.addAdapter(0, placePieChartAdapter)
                    }

                    placePieChartAdapter.submitList(renderState.data)
                    binding.recyclerPlace.scrollToPosition(0)
                }
            }
            is PlaceStateScreen.SuccessPlaceStatsLineCharts -> {
                if (menu.isCurrentItemChecked(MENU_ITEM_LINE_CHART)) {
                    mergeAdapter.addAdapter(placeLineChartAdapter)
                    placeLineChartAdapter.submitList(listOf(renderState.data))
                    placeLineChartAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_world, menu).also {
            this.menu = menu
            menu.enableItem(currentMenuItem)
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (::countrySpinnerAdapter.isInitialized) {
            when (item.itemId) {
                R.id.list_view -> {
                    if (menu.isCurrentItemChecked(MENU_ITEM_LIST).not()) {
                        mergeAdapter.removeAllAdapters()
                        menu.enableItem(MENU_ITEM_LIST)
                        selectMenu(getSelectedCountry(), getSelectedPlace())
                    }
                    true
                }
                R.id.bar_chart_view -> {
                    if (menu.isCurrentItemChecked(MENU_ITEM_BAR_CHART).not()) {
                        menu.enableItem(MENU_ITEM_BAR_CHART)
                        selectMenu(getSelectedCountry(), getSelectedPlace())
                    }
                    true
                }
                R.id.line_chart_view -> {
                    if (menu.isCurrentItemChecked(MENU_ITEM_LINE_CHART).not()) {
                        menu.enableItem(MENU_ITEM_LINE_CHART)
                        selectMenu(getSelectedCountry(), getSelectedPlace())
                    }
                    true
                }
                R.id.pie_chart_view -> {
                    if (menu.isCurrentItemChecked(MENU_ITEM_PIE_CHART).not()) {
                        menu.enableItem(MENU_ITEM_PIE_CHART)
                        selectMenu(getSelectedCountry(), getSelectedPlace())
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            super.onOptionsItemSelected(item)
        }

    private fun getSelectedCountry(): String =
        countrySpinnerAdapter.getCountryId(
            binding.countrySpinner.selectedItemId.toInt()
        )

    private fun getSelectedPlace(): String =
        if (binding.regionSpinner.isVisible() && ::placeSpinnerAdapter.isInitialized) {
            placeSpinnerAdapter.getCurrentPlaceId()
        } else {
            ""
        }

    private fun selectMenu(idCountry: String, idRegion: String = "") {
        mergeAdapter.removeAllAdapters()
        emptyDatabaseBinding.groupEmpty.hide()
        loadingBinding.loading.hide()
        currentMenuItem = menu.isCurrentItemChecked()

        when (currentMenuItem) {
            MENU_ITEM_LIST ->
                viewModel.getListStats(idCountry, idRegion)
            MENU_ITEM_BAR_CHART ->
                viewModel.getBarChartStats(idCountry, idRegion)
            MENU_ITEM_LINE_CHART ->
                viewModel.getLineChartStats(idCountry, idRegion)
            else -> viewModel.getPieChartStats(idCountry, idRegion)
        }
    }
}
