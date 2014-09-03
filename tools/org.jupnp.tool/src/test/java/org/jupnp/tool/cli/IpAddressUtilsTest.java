/**
 * Copyright (C) 2014 4th Line GmbH, Switzerland and others
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.jupnp.tool.cli;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for IpAddressUtils class.
 * 
 * @author Jochen Hiller - Initial contribution
 */
public class IpAddressUtilsTest {

	@Test
	public void testIsSameIpAddressOK() {
		checkSame("192.168.3.106", "192.168.3.106");
		checkSame("192.168.0.10", "192.168.0.10");
		checkSame("192.168.0.0010", "192.168.0.0010");
		checkSame("192.168.03.001", "192.168.3.1");
	}

	@Test
	public void testIsSameIpAddressNull() {
		checkEX("", "");
		checkNotSame(null, "");
		checkNotSame("", null);
		checkNotSame(null, "192.168.1.1");
		checkNotSame("192.168.1.1", null);
	}

	@Test
	public void testIsSameIpAddressDifferent() {
		checkNotSame("192.168.3.106", "192.168.3.107");
		checkNotSame("192.168.0.10", "192.168.0.11");
		checkNotSame("192.168.0.0010", "192.168.0.0011");
		checkNotSame("192.168.03.001", "192.168.3.0");
	}

	@Test
	public void testIsSameIpAddressInvalid() {
		// missing digits ip2
		checkSame("192.168.1.1", "192.168.1.1");
		checkEX("192.168.1.1", "192.168.1");
		checkEX("192.168.1.1", "192.168");
		checkEX("192.168.1.1", "192");
		checkEX("192.168.1.1", "");

		// missing digits ip1
		checkSame("192.168.1.1", "192.168.1.1");
		checkEX("192.168.1", "192.168.1.1");
		checkEX("192.168", "192.168.1.1");
		checkEX("192", "192.168.1.1");
		checkEX("", "192.168.1.1");

		// no digits ip2
		checkSame("192.168.1.1", "192.168.1.1");
		checkEX("192.168.1.1", "192A.168.1.1");
		checkEX("192.168.1.1", "192.168A.1.1");
		checkEX("192.168.1.1", "192.168.1A.1");
		checkEX("192.168.1.1", "192.168.1.1A");

		// no digits ip1
		checkSame("192.168.1.1", "192.168.1.1");
		checkEX("192A.168.1.1", "192.168.1.1");
		checkEX("192.168A.1.1", "192.168.1.1");
		checkEX("192.168.1A.1", "192.168.1.1");
		checkEX("192.168.1.1A", "192.168.1.1");
	}

	@Test
	public void testCompareIpAddressEquals() {
		checkEQ("192.168.3.106", "192.168.3.106");
		checkEQ("192.168.1.1", "192.168.1.001");
	}

	@Test
	public void testCompareIpAddressGreaterThan() {
		checkGT("192.168.3.106", "192.168.3.105");
		checkGT("192.168.1.2", "192.168.1.001");
	}

	@Test
	public void testCompareIpAddressLessThan() {
		checkLT("192.168.3.105", "192.168.3.106");
		checkLT("192.168.1.001", "192.168.1.2");
	}

	// private methods

	private void checkSame(String s1, String s2) {
		Assert.assertTrue(IpAddressUtils.isSameIpAddress(s1, s2));
	}

	private void checkNotSame(String s1, String s2) {
		Assert.assertFalse(IpAddressUtils.isSameIpAddress(s1, s2));
	}

	private void checkEX(String s1, String s2) {
		try {
			IpAddressUtils.isSameIpAddress(s1, s2);
			Assert.fail("Oops, IllegalArgumentException expected");
		} catch (IllegalArgumentException ex) {
			// OK, expected
		}
	}

	private void checkEQ(String s1, String s2) {
		Assert.assertEquals(0, IpAddressUtils.compareIpAddress(s1, s2));
	}

	private void checkGT(String s1, String s2) {
		Assert.assertEquals(1, IpAddressUtils.compareIpAddress(s1, s2));
	}

	private void checkLT(String s1, String s2) {
		Assert.assertEquals(-1, IpAddressUtils.compareIpAddress(s1, s2));
	}

}
