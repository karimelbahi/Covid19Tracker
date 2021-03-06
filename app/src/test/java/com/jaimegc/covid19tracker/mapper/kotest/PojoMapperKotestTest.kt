package com.jaimegc.covid19tracker.mapper.kotest

import com.jaimegc.covid19tracker.data.room.mapper.toPojoCountriesOrdered
import com.jaimegc.covid19tracker.data.room.mapper.toPojoRegionsOrdered
import com.jaimegc.covid19tracker.data.room.mapper.toPojoSubRegionsOrdered
import com.jaimegc.covid19tracker.ModelFactoryTest.countryAndStatsPojo
import com.jaimegc.covid19tracker.ModelFactoryTest.listCountryAndOneStatsPojo
import com.jaimegc.covid19tracker.ModelFactoryTest.listRegionAndOneStatsPojo
import com.jaimegc.covid19tracker.ModelFactoryTest.listSubRegionAndOneStatsPojo
import com.jaimegc.covid19tracker.ModelFactoryTest.regionAndStatsPojo
import com.jaimegc.covid19tracker.ModelFactoryTest.subRegionAndStatsPojo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class PojoMapperKotestTest : StringSpec({

    beforeTest {
        Locale.setDefault(Locale.US)
    }

    "listCountryAndOneStatsPojo to pojo countries ordered" {
        listCountryAndOneStatsPojo.toPojoCountriesOrdered() shouldBe listOf(countryAndStatsPojo)
    }

    "listRegionAndOneStatsPojo to pojo regions ordered" {
        listRegionAndOneStatsPojo.toPojoRegionsOrdered() shouldBe listOf(regionAndStatsPojo)
    }

    "listSubRegionAndOneStatsPojo to pojo subregions ordered" {
        listSubRegionAndOneStatsPojo.toPojoSubRegionsOrdered() shouldBe listOf(subRegionAndStatsPojo)
    }
})