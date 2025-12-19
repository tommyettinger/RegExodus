/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <br>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */

package regexodus;

import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.client.DataViewNative;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.DataView;

public final class Compatibility {
	private Compatibility() {
	}

	public static final Int8Array wba = Int8ArrayNative.create(8);
	public static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
	public static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 2);
	public static final Float64Array wda = Float64ArrayNative.create(wba.buffer(), 0, 1);
	public static final DataView dv = DataViewNative.create(wba.buffer());

	public static long doubleToLongBits (final double value) {
		wda.set(0, value);
		return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
	}

	public static long doubleToRawLongBits (final double value) {
		wda.set(0, value);
		return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
	}

	public static double longBitsToDouble (final long bits) {
		wia.set(1, (int)(bits >>> 32));
		wia.set(0, (int)(bits & 0xffffffffL));
		return wda.get(0);
	}

	public static long doubleToReversedLongBits (final double value) {
		dv.setFloat64(0, value, true);
		return ((long)dv.getInt32(0, false) << 32) | (dv.getInt32(4, false) & 0xffffffffL);
	}

	public static double reversedLongBitsToDouble (final long bits) {
		dv.setInt32(4, (int)(bits >>> 32), true);
		dv.setInt32(0, (int)(bits & 0xffffffffL), true);
		return dv.getFloat64(0, false);
	}

	public static int doubleToLowIntBits (final double value) {
		wda.set(0, value);
		return wia.get(0);
	}

	public static int doubleToHighIntBits (final double value) {
		wda.set(0, value);
		return wia.get(1);
	}

	public static int doubleToMixedIntBits (final double value) {
		wda.set(0, value);
		return wia.get(0) ^ wia.get(1);
	}

	public static int floatToIntBits (final float value) {
		wfa.set(0, value);
		return wia.get(0);
	}

	public static int floatToRawIntBits (final float value) {
		wfa.set(0, value);
		return wia.get(0);
	}

	public static int floatToReversedIntBits (final float value) {
		dv.setFloat32(0, value, true);
		return dv.getInt32(0, false);
	}

	public static float reversedIntBitsToFloat (final int bits) {
		dv.setInt32(0, bits, true);
		return dv.getFloat32(0, false);
	}

	public static float intBitsToFloat (final int bits) {
		wia.set(0, bits);
		return wfa.get(0);
	}

	public static int lowestOneBit(int num) {
		return num & -num;
	}

	public static long lowestOneBit(long num) {
		return num & ~(num - 1L);
	}

	public static native int imul(int left, int right)/*-{
	    return Math.imul(left, right);
	}-*/;

	public static int getExponent(float num) {
		wfa.set(0, num);
		return (wia.get(0) >>> 23 & 0xFF) - 0x7F;
	}

	public static int getExponent(double num) {
		wda.set(0, num);
		return (wia.get(1) >>> 52 & 0x7FF) - 0x3FF;
	}

	public static native int countLeadingZeros(int n)/*-{
	    return Math.clz32(n);
	}-*/;

	/**
	 * Hacker's Delight (2012), section 5-4, "Counting Trailing 0's".
	 * @param n any int
	 * @return the number of '0' bits starting at the least-significant bit and going until just before a '1' bit is encountered
	 */
	public static native int countTrailingZeros(int n)/*-{
	    return 32 - Math.clz32(~n & n - 1);
	}-*/;

	public static int countLeadingZeros(long n) {
		// we store the top 32 bits first.
		int x = (int)(n >>> 32);
		// if the top 32 bits are 0, we know we don't need to count zeros in them.
		// if they aren't 0, we know there is a 1 bit in there, so we don't need to count the low 32 bits.
		return x == 0 ? 32 + countLeadingZeros((int)n) : countLeadingZeros(x);
	}


	public static int countTrailingZeros(long n) {
		// we store the bottom 32 bits first.
		int x = (int)n;
		// if the bottom 32 bits are 0, we know we don't need to count zeros in them.
		// if they aren't 0, we know there is a 1 bit in there, so we don't need to count the high 32 bits.
		return x == 0 ? 32 + countTrailingZeros((int)(n >>> 32)) : countTrailingZeros(x);
	}

}
