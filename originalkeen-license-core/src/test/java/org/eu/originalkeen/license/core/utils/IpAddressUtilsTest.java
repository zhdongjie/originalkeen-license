package org.eu.originalkeen.license.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpAddressUtilsTest {

    @Test
    void shouldReturnEmptyStringWhenMacBytesAreMissing() {
        assertEquals("", IpAddressUtils.formatMacAddress(null));
        assertEquals("", IpAddressUtils.formatMacAddress(new byte[0]));
    }

    @Test
    void shouldFormatMacBytesAsUppercaseHexadecimal() {
        assertEquals("0A-1B-2C-3D-4E-5F", IpAddressUtils.formatMacAddress(
                new byte[] {0x0A, 0x1B, 0x2C, 0x3D, 0x4E, 0x5F}
        ));
    }
}
