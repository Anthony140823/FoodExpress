package com.example.foodexpress

import com.example.foodexpress.maps.MapPoint
import org.junit.Assert.*
import org.junit.Test

class MapPointTest {
    @Test fun acceptsRealCoordinatesAndZero() {
        assertEquals(MapPoint(-12.0464, -77.0428), MapPoint.from(-12.0464, -77.0428))
        assertEquals(MapPoint(0.0, 0.0), MapPoint.from(0.0, 0.0))
    }

    @Test fun rejectsMissingNonFiniteAndOutOfMapCoordinates() {
        assertNull(MapPoint.from(null, -77.0))
        assertNull(MapPoint.from(-12.0, null))
        assertNull(MapPoint.from(Double.NaN, 0.0))
        assertNull(MapPoint.from(0.0, Double.POSITIVE_INFINITY))
        assertNull(MapPoint.from(90.0, 0.0))
        assertNull(MapPoint.from(0.0, 181.0))
    }
}
