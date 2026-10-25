package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressesTest {
    @Test fun recognisesPlusCodes() {
        assertTrue(isPlusCode("C9XR+8H5"))
        assertTrue(isPlusCode("7JCMC9XR+8H"))
        assertFalse(isPlusCode("Madhapur"))
        assertFalse(isPlusCode("12-2-831/A"))
    }

    @Test fun readableAddressDropsPlusCodesAndRepeats() {
        assertEquals("Madhapur", readableAddress("C9XR+8H5", null, "Madhapur", "Madhapur"))
        assertEquals("12 Hitech City Road, Madhapur", readableAddress("12 Hitech City Road", "", "Madhapur"))
    }

    @Test fun stripsPlusCodeFromAddressLine() {
        assertEquals("Madhapur, Hyderabad, Telangana 500081, India", stripPlusCode("C9XR+8H5, Madhapur, Hyderabad, Telangana 500081, India"))
    }
}
