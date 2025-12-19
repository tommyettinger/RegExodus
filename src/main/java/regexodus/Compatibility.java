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

/**
 * Methods for converting floats to and from ints, as well as doubles to and from longs and ints.
 * This includes methods like {@link #floatToReversedIntBits(float)} (which
 * makes converting from an OpenGL packed ABGR float to an RGBA8888 int very easy) and
 * {@link #doubleToMixedIntBits(double)} (which is useful when implementing hashCode() for double
 * values). Everything's optimized for GWT, which is important because some core JDK methods like
 * {@link Float#floatToIntBits(float)} are quite slow on GWT. This makes heavy use of JS typed
 * arrays to accomplish its conversions; these are widespread even on mobile browsers, and are very
 * convenient for this sort of code (in some ways, they're a better fit for this sort of bit-level
 * operation in JavaScript than anything Java provides).
 * <br>
 * There are some other methods that are important for JavaScript math code here. Using
 * {@link #imul(int, int)} instead of normal int multiplication in GWT prevents large multipliers
 * from losing precision. Using {@link #countLeadingZeros(int)} is much faster than GWT's
 * {@link Integer#numberOfLeadingZeros(int)} implementation, and also has a fix for some buggy JDK
 * versions on desktop JVMs (early releases of Java 19 through 24). Those two methods call JS-native
 * methods in its Math class, {@code Math.imul()} and {@code Math.clz32()}.
 *
 * @author Tommy Ettinger
 */
public final class Compatibility {
    /**
     * No need to instantiate.
     */
    private Compatibility() {
    }
    /**
     * Identical to {@link Double#doubleToLongBits(double)} on desktop; optimized on GWT. When compiling to JS via GWT,
     * there is no way to distinguish NaN values with different bits but that are still NaN, so this doesn't try to
     * somehow permit that. Uses JS typed arrays on GWT, which are well-supported now across all recent browsers and
     * have fallbacks in GWT in the unlikely event of a browser not supporting them. JS typed arrays support double, but
     * not long, so this needs to compose a long from two ints, which means the double-to/from-long conversions aren't
     * as fast as float-to/from-int conversions.
     * <br>
     * This method may be a tiny bit slower than {@link #doubleToRawLongBits(double)} on non-HotSpot JVMs.
     *
     * @param value a {@code double} floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static long doubleToLongBits(final double value) {
        return Double.doubleToLongBits(value);
    }

    /**
     * Identical to {@link Double#doubleToRawLongBits(double)} on desktop; optimized on GWT. When compiling to JS via
     * GWT, there is no way to distinguish NaN values with different bits but that are still NaN, so this doesn't try
     * to somehow permit that. Uses JS typed arrays on GWT, which are well-supported now across all recent browsers and
     * have fallbacks in GWT in the unlikely event of a browser not supporting them. JS typed arrays support double, but
     * not long, so this needs to compose a long from two ints, which means the double-to/from-long conversions aren't
     * as fast as float-to/from-int conversions on GWT. Note that on GWT, the "Raw" conversions aren't available in
     * Double or Float, and the versions here are identical to the "non-Raw" versions on GWT.
     *
     * @param value a {@code double} floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static long doubleToRawLongBits(final double value) {
        return Double.doubleToRawLongBits(value);
    }

    /**
     * Gets the bit representation of the given double {@code value}, but with reversed byte order. On desktop, this is
     * equivalent to calling {@code Long.reverseBytes(Double.doubleToRawLongBits(value))}, but it is implemented using
     * typed arrays on GWT. Note that this reverses byte order, not bit order.
     * <br>
     * This method runs at the expected speed on desktop and mobile, where it should compile down to two (very fast)
     * intrinsics, but GWT should run it much more quickly than a direct translation of the Java would provide.
     *
     * @param value any double
     * @return the bits that represent the floating-point value, with their byte order reversed from normal.
     */
    public static long doubleToReversedLongBits(final double value) {
        return Long.reverseBytes(Double.doubleToRawLongBits(value));
    }

    /**
     * Reverses the byte order of {@code bits} and converts that to a double. On desktop, this is equivalent to calling
     * {@code Double.longBitsToDouble(Long.reverseBytes(bits))}, but it is implemented using typed arrays on GWT. Note
     * that this reverses byte order, not bit order.
     * <br>
     * This method runs at the expected speed on desktop and mobile, where it should compile down to two (very fast)
     * intrinsics, but GWT should run it much more quickly than a direct translation of the Java would provide.
     *
     * @param bits a long
     * @return the {@code double} floating-point value with the given bits using their byte order reversed from normal.
     */
    public static double reversedLongBitsToDouble(final long bits) {
        return Double.longBitsToDouble(Long.reverseBytes(bits));
    }

    /**
     * Identical to {@link Double#longBitsToDouble(long)} on desktop; optimized on GWT. Uses JS typed arrays on GWT,
     * which are well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a
     * browser not supporting them. JS typed arrays support double, but not long, so this needs to compose a long from
     * two ints, which means the double-to/from-long conversions aren't as fast as float-to/from-int conversions.
     *
     * @param bits a long.
     * @return the {@code double} floating-point value with the same bit pattern.
     */
    public static double longBitsToDouble(final long bits) {
        return Double.longBitsToDouble(bits);
    }

    /**
     * Converts the raw bits of {@code value} to a long and gets the lower 32 bits of that long, as an int. This
     * performs better on GWT than casting the result of {@link #doubleToRawLongBits(double)} to int, because it doesn't
     * create a {@code long} internally on that platform, and longs are quite slow on GWT.
     *
     * @param value a {@code double} precision floating-point number.
     * @return the lower half of the bits that represent the floating-point number, as an int.
     */
    public static int doubleToLowIntBits(final double value) {
        return (int) Double.doubleToRawLongBits(value);
    }

    /**
     * Converts the raw bits of {@code value} to a long and gets the upper 32 bits of that long, as an int. This
     * performs better on GWT than casting the result of {@link #doubleToRawLongBits(double)} to int, because it doesn't
     * create a {@code long} internally on that platform, and longs are quite slow on GWT.
     *
     * @param value a {@code double} precision floating-point number.
     * @return the upper half of the bits that represent the floating-point number, as an int.
     */
    public static int doubleToHighIntBits(final double value) {
        return (int) (Double.doubleToRawLongBits(value) >>> 32);
    }

    /**
     * Converts the bits of {@code value} to a long and gets the XOR of its upper and lower 32-bit sections. Useful for
     * numerical code where a 64-bit double needs to be reduced to a 32-bit value with some hope of keeping different
     * doubles giving different ints. This performs better on GWT than working with {@link #doubleToRawLongBits(double)}
     * and XORing its upper and lower halves, because it doesn't create a {@code long} internally on that platform, and
     * longs are quite slow on GWT.
     *
     * @param value a {@code double} precision floating-point number.
     * @return the XOR of the lower and upper halves of the bits that represent the floating-point number.
     */
    public static int doubleToMixedIntBits(final double value) {
        final long l = Double.doubleToRawLongBits(value);
        return (int) (l ^ l >>> 32);
    }

    /**
     * Identical to {@link Float#floatToIntBits(float)} on desktop; optimized on GWT. Uses JS typed arrays on GWT, which
     * are well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a browser
     * not supporting them.
     * <br>
     * This method may be a tiny bit slower than {@link #floatToRawIntBits(float)} on non-HotSpot JVMs.
     *
     * @param value a floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static int floatToIntBits(final float value) {
        return Float.floatToIntBits(value);
    }

    /**
     * Identical to {@link Float#floatToRawIntBits(float)} on desktop; optimized on GWT. When compiling to JS via GWT,
     * there is no way to distinguish NaN values with different bits but that are still NaN, so this doesn't try to
     * somehow permit that. Uses JS typed arrays on GWT, which are well-supported now across all recent browsers and
     * have fallbacks in GWT in the unlikely event of a browser not supporting them. Note that on GWT, the "Raw"
     * conversions aren't available in Double or Float, and the versions here are identical to the "non-Raw" versions on
     * GWT.
     *
     * @param value a floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static int floatToRawIntBits(final float value) {
        return Float.floatToRawIntBits(value);
    }

    /**
     * Gets the bit representation of the given float {@code value}, but with reversed byte order. On desktop, this is
     * equivalent to calling {@code Integer.reverseBytes(Float.floatToRawIntBits(value))}, but it is implemented using
     * typed arrays on GWT. Note that this reverses byte order, not bit order.
     * <br>
     * This is primarily intended for a common task in libGDX's internals: converting between RGBA8888 int colors and
     * ABGR packed float colors. This method runs at the expected speed on desktop and mobile, where it should compile
     * down to two (very fast) intrinsics, but GWT should run it much more quickly than a direct translation of the Java
     * would provide.
     *
     * @param value a floating-point number
     * @return the bits that represent the floating-point number, with their byte order reversed from normal.
     */
    public static int floatToReversedIntBits(final float value) {
        return Integer.reverseBytes(Float.floatToRawIntBits(value));
    }

    /**
     * Reverses the byte order of {@code bits} and converts that to a float. On desktop, this is
     * equivalent to calling {@code Float.intBitsToFloat(Integer.reverseBytes(bits))}, but it is implemented using
     * typed arrays on GWT. Note that this reverses byte order, not bit order.
     * <br>
     * This is primarily intended for a common task in libGDX's internals: converting between RGBA8888 int colors and
     * ABGR packed float colors. This method runs at the expected speed on desktop and mobile, where it should compile
     * down to two (very fast) intrinsics, but GWT should run it much more quickly than a direct translation of the Java
     * would provide.
     *
     * @param bits an integer
     * @return the {@code float} floating-point value with the given bits using their byte order reversed from normal.
     */
    public static float reversedIntBitsToFloat(final int bits) {
        return Float.intBitsToFloat(Integer.reverseBytes(bits));
    }

    /**
     * Identical to {@link Float#intBitsToFloat(int)} on desktop; optimized on GWT. Uses JS typed arrays on GWT, which
     * are well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a browser
     * not supporting them.
     *
     * @param bits an integer.
     * @return the {@code float} floating-point value with the same bit pattern.
     */
    public static float intBitsToFloat(final int bits) {
        return Float.intBitsToFloat(bits);
    }

    /**
     * Returns an int value with at most a single one-bit, in the position of the lowest-order ("rightmost") one-bit in
     * the specified int value. Returns zero if the specified value has no one-bits in its two's complement binary
     * representation, that is, if it is equal to zero.
     * <br>
     * Identical to {@link Integer#lowestOneBit(int)}, including on GWT. GWT calculates Integer.lowestOneBit() correctly,
     * but does not always calculate Long.lowestOneBit() correctly. This overload is here so you can use lowestOneBit on
     * an int value and get an int value back (which could be assigned to a long without losing data), or use it on a
     * long value and get the correct long result on both GWT and other platforms.
     *
     * @param num the value whose lowest one bit is to be computed
     * @return an int value with a single one-bit, in the position of the lowest-order one-bit in the specified value,
     * or zero if the specified value is itself equal to zero.
     */
    public static int lowestOneBit(int num) {
        return num & -num;
    }

    /**
     * Returns an long value with at most a single one-bit, in the position of the lowest-order ("rightmost") one-bit in
     * the specified long value. Returns zero if the specified value has no one-bits in its two's complement binary
     * representation, that is, if it is equal to zero.
     * <br>
     * Identical to {@link Long#lowestOneBit(long)}, but super-sourced to act correctly on GWT. At least on GWT 2.8.2,
     * {@link Long#lowestOneBit(long)} does not provide correct results for certain inputs on GWT. For example, when given
     * -17592186044416L, Long.lowestOneBit() returns 0 on GWT, possibly because it converts to an int at some point. On
     * other platforms, like desktop JDKs, {@code Long.lowestOneBit(-17592186044416L)} returns 17592186044416L.
     *
     * @param num the value whose lowest one bit is to be computed
     * @return a long value with a single one-bit, in the position of the lowest-order one-bit in the specified value,
     * or zero if the specified value is itself equal to zero.
     */
    public static long lowestOneBit(long num) {
        return num & -num;
    }

    /**
     * 32-bit signed integer multiplication that is correct on all platforms, including GWT. Unlike desktop, Android,
     * and iOS targets, GWT uses the equivalent of a {@code double} to represent an {@code int}. This means any
     * multiplication where the product is large enough (over 2 to the 53) can start to lose precision instead of being
     * wrapped, like it would on overflow in a normal JDK. Using this will prevent the possibility of precision loss.
     * <br>
     * This should compile down to a call to {@code Math.imul()} on GWT, hence the name here.
     * @param left the multiplicand
     * @param right the multiplier
     * @return the product of left times right, wrapping on overflow as is normal for Java
     */
    public static int imul(int left, int right) {
        return left * right;
    }

    /**
     * Returns the unbiased exponent used in the representation of a float. This delegates to
     * {@link Math#getExponent(float)} on most platforms, but on GWT it uses a super-sourced implementation.
     * @param num any float
     * @return the unbiased exponent of num, from {@link Float#MIN_EXPONENT} - 1 to {@link Float#MAX_EXPONENT} + 1
     */
    public static int getExponent(float num) {
        return Math.getExponent(num);
    }

    /**
     * Returns the unbiased exponent used in the representation of a double. This delegates to
     * {@link Math#getExponent(double)} on most platforms, but on GWT it uses a super-sourced implementation.
     * @param num any double
     * @return the unbiased exponent of num, from {@link Double#MIN_EXPONENT} - 1 to {@link Double#MAX_EXPONENT} + 1
     */
    public static int getExponent(double num) {
        return Math.getExponent(num);
    }

    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the sign bit and checking towards the
     * least-significant bit, stopping just before a '1' bit is encountered. Returns 0 for any negative input.
     * Returns 32 for an input of 0.
     * <br>
     * This simply calls {@link Integer#numberOfLeadingZeros(int)} on most platforms, but on GWT, it calls the
     * JS built-in function {@code Math.clz32(n)}. This probably performs better than the Integer method on GWT.
     * <br>
     * As of March 16, 2025, this does not use {@link Integer#numberOfLeadingZeros(int)} because JDK versions 19 through
     * at least some versions of Java 24 can (rarely) miscalculate this, producing a value 1 less than the correct
     * result. The conditions for the incorrect output were
     * <a href="https://bugs.openjdk.org/browse/JDK-8349637">discussed by OpenJDK developers here</a>. This currently
     * manages to continue using an intrinsic (hopefully) by changing:
     * {@code Integer.numberOfLeadingZeros(n)} to {@code Long.numberOfLeadingZeros(n & 0xFFFFFFFFL) - 32} . GWT will
     * continue to use the (correct) {@code Math.clz32(n)} method, which returns the same results this does.
     * @param n any int
     * @return the number of '0' bits starting at the sign bit and going until just before a '1' bit is encountered
     */
    public static int countLeadingZeros(int n) {
        return Long.numberOfLeadingZeros(n & 0xFFFFFFFFL) - 32;
    }

    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the least-significant bit and checking towards
     * the sign bit, stopping just before a '1' bit is encountered. Returns 0 for any odd-number input.
     * Returns 32 for an input of 0.
     * <br>
     * This simply calls {@link Integer#numberOfTrailingZeros(int)} on most platforms, but on GWT, it uses the
     * JS built-in function {@code Math.clz32(n)} with some extra steps to get the trailing, rather than leading,
     * zeros. This probably performs better than the Integer method on GWT, though not as well as
     * {@link #countLeadingZeros(int)}. On GWT, this uses an algorithm from Hacker's Delight (2012), section 5-4,
     * "Counting Trailing 0's".
     * @param n any int
     * @return the number of '0' bits starting at the least-significant bit and going until just before a '1' bit is encountered
     */
    public static int countTrailingZeros(int n) {
        return Integer.numberOfTrailingZeros(n);
    }
    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the sign bit and checking towards the
     * least-significant bit, stopping just before a '1' bit is encountered. Returns 0 for any negative input.
     * Returns 64 for an input of 0.
     * <br>
     * This simply calls {@link Long#numberOfLeadingZeros(long)} on most platforms, but on GWT, it calls
     * {@link #countLeadingZeros(int)}, which calls the JS built-in function {@code Math.clz32(n)}.
     * This probably performs better than the Long method on GWT.
     * @param n any long
     * @return the number of '0' bits starting at the sign bit and going until just before a '1' bit is encountered
     */
    public static int countLeadingZeros(long n) {
        return Long.numberOfLeadingZeros(n);
    }
    /**
     * Returns the number of contiguous '0' bits in {@code n} starting at the least-significant bit and checking towards
     * the sign bit, stopping just before a '1' bit is encountered. Returns 0 for any odd-number input.
     * Returns 64 for an input of 0.
     * <br>
     * This simply calls {@link Long#numberOfTrailingZeros(long)} on most platforms, but on GWT, it calls
     * {@link #countTrailingZeros(int)}, which uses the JS built-in function {@code Math.clz32(n)}. This probably
     * performs better than the Long method on GWT, though not as well as {@link #countLeadingZeros(long)}.
     * @param n any int
     * @return the number of '0' bits starting at the least-significant bit and going until just before a '1' bit is encountered
     */
    public static int countTrailingZeros(long n) {
        return Long.numberOfTrailingZeros(n);
    }
}
