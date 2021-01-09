/*******************************************************************************
 * Copyright 2020 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package regexodus.ds;

import java.io.Serializable;
import java.util.Arrays;

/**
 * An unordered map where the keys are unboxed chars and the values are also unboxed chars. Null keys are not allowed. No allocation is
 * done except when growing the table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * Unordered sets and maps are not designed to provide especially fast iteration.
 * <p>
 * You can customize most behavior of this map by extending it. {@link #place(char)} can be overridden to change how hashCodes
 * are calculated (which can be useful for types like {@link StringBuilder} that don't implement hashCode()), and
 * {@link #locateKey(char)} can be overridden to change how equality is calculated.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are rehashed using Fibonacci
 * hashing, instead of the more common power-of-two mask, to better distribute poor hashCodes (see <a href=
 * "https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte
 * Skarupke's blog post</a>). Linear probing continues to work even when all hashCodes collide, just more slowly.
 *
 * @author Nathan Sweet
 * @author Tommy Ettinger
 */
public class CharCharMap implements Serializable {
    private static final long serialVersionUID = 0L;

    protected int size;

    protected char[] keyTable;
    protected char[] valueTable;
    protected boolean hasZeroValue;
    protected char zeroValue;
    protected float loadFactor;
    protected int threshold;

    protected int shift;

    /**
     * A bitmask used to confine hashcodes to the size of the table. Must be all 1 bits in its low positions, ie a power of two
     * minus 1.
     */
    protected int mask;

    public char defaultReturnValue = 0;
    /**
     * Used to establish the size of a hash table for sets, maps, and related code.
     * The table size will always be a power of two, and should be the next power of two that is at least equal
     * to {@code capacity / loadFactor}.
     *
     * @param capacity   the amount of items the hash table should be able to hold
     * @param loadFactor between 0.0 (exclusive) and 1.0 (inclusive); the fraction of how much of the table can be filled
     * @return the size of a hash table that can handle the specified capacity with the given loadFactor
     */
    static int tableSize (int capacity, float loadFactor) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
        }
        int tableSize = 1 << -Integer.numberOfLeadingZeros(Math.max(2, (int)Math.ceil(capacity / loadFactor)) - 1);
        if (tableSize > 1 << 30 || tableSize < 0) {
            throw new IllegalArgumentException("The required capacity is too large: " + capacity);
        }
        return tableSize;
    }

    /**
     * Creates a new map with an initial capacity of 51 and a load factor of 0.8.
     */
    public CharCharMap() {
        this(51, 0.8f);
    }

    /**
     * Creates a new map with a load factor of 0.8.
     *
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public CharCharMap(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    /**
     * Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
     * growing the backing table.
     *
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public CharCharMap(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0f || loadFactor > 1f) { throw new IllegalArgumentException("loadFactor must be > 0 and <= 1: " + loadFactor); }
        this.loadFactor = loadFactor;

        int tableSize = tableSize(initialCapacity, loadFactor);
        threshold = (int)(tableSize * loadFactor);
        mask = tableSize - 1;
        shift = Long.numberOfLeadingZeros(mask);

        keyTable = new char[tableSize];
        valueTable = new char[tableSize];
    }

    /**
     * Creates a new map identical to the specified map.
     */
    public CharCharMap(CharCharMap map) {
        this((int)(map.keyTable.length * map.loadFactor), map.loadFactor);
        System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
        System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
        size = map.size;
        defaultReturnValue = map.defaultReturnValue;
    }

    /**
     * Given two side-by-side arrays, one of keys, one of values, this constructs a map and inserts each pair of key and value into it.
     * If keys and values have different lengths, this only uses the length of the smaller array.
     * @param keys an array of keys
     * @param values an array of values
     */
    public CharCharMap(char[] keys, char[] values){
        this(Math.min(keys.length, values.length));
        putAll(keys, values);
    }

    /**
     * Explicitly implemented, does not interact with the {@link Object#clone()} method, and simply calls
     * {@link #CharCharMap(CharCharMap)} with this CharCharMap as its argument.
     * @return a deep copy of this CharCharMap
     */
    public CharCharMap clone() {
        return new CharCharMap(this);
    }

    /**
     * Returns an index &gt;= 0 and &lt;= {@link #mask} for the specified {@code item}.
     * <p>
     * The default behavior uses Fibonacci hashing; it simply gets the {@link Object#hashCode()}
     * of {@code item}, multiplies it by a specific char constant related to the golden ratio,
     * and makes an unsigned right shift by {@link #shift} before casting to int and returning.
     * This can be overridden to hash {@code item} differently, though all implementors must
     * ensure this returns results in the range of 0 to {@link #mask}, inclusive. If nothing
     * else is changed, then unsigned-right-shifting an int or char by {@link #shift} will also
     * restrict results to the correct range.
     *
     * @param item a non-null Object; its hashCode() method should be used by most implementations.
     */
    protected int place (char item) {
        return (int)(item * 0x9E3779B97F4A7C15L >>> shift);
    }

    /**
     * Returns the index of the key if already present, else {@code -1 - index} for the next empty index. This can be overridden
     * to compare for equality differently than {@code ==}.
     */
    protected int locateKey (char key) {
        char[] keyTable = this.keyTable;
        for (int i = place(key); ; i = i + 1 & mask) {
            char other = keyTable[i];
            if (other == 0) {
                return ~i; // Empty space is available.
            }
            if (other == key) {
                return i; // Same key was found.
            }
        }
    }

    /**
     * Returns the old value associated with the specified key, or this map's {@link #defaultReturnValue} if there was no prior value.
     */
    public char put (char key, char value) {
        if (key == 0) {
            char oldValue = defaultReturnValue;
            if (hasZeroValue) { oldValue = zeroValue; } else { size++; }
            hasZeroValue = true;
            zeroValue = value;
            return oldValue;
        }
        int i = locateKey(key);
        if (i >= 0) { // Existing key was found.
            char oldValue = valueTable[i];
            valueTable[i] = value;
            return oldValue;
        }
        i = ~i; // Empty space was found.
        keyTable[i] = key;
        valueTable[i] = value;
        if (++size >= threshold) { resize(keyTable.length << 1); }
        return defaultReturnValue;
    }

    /**
     * Returns the old value associated with the specified key, or the given {@code defaultValue} if there was no prior value.
     */
    public char putOrDefault (char key, char value, char defaultValue) {
        if (key == 0) {
            char oldValue = defaultValue;
            if (hasZeroValue) { oldValue = zeroValue; } else { size++; }
            hasZeroValue = true;
            zeroValue = value;
            return oldValue;
        }
        int i = locateKey(key);
        if (i >= 0) { // Existing key was found.
            char oldValue = valueTable[i];
            valueTable[i] = value;
            return oldValue;
        }
        i = ~i; // Empty space was found.
        keyTable[i] = key;
        valueTable[i] = value;
        if (++size >= threshold) { resize(keyTable.length << 1); }
        return defaultValue;
    }

    public void putAll (CharCharMap map) {
        ensureCapacity(map.size);
        if (map.hasZeroValue) {
            if (!hasZeroValue) { size++; }
            hasZeroValue = true;
            zeroValue = map.zeroValue;
        }
        char[] keyTable = map.keyTable;
        char[] valueTable = map.valueTable;
        char key;
        for (int i = 0, n = keyTable.length; i < n; i++) {
            key = keyTable[i];
            if (key != 0) { put(key, valueTable[i]); }
        }
    }

    /**
     * Given two side-by-side arrays, one of keys, one of values, this inserts each pair of key and value into this map with put().
     * @param keys an array of keys
     * @param values an array of values
     */
    public void putAll (char[] keys, char[] values) {
        putAll(keys, 0, values, 0, Math.min(keys.length, values.length));
    }

    /**
     * Given two side-by-side arrays, one of keys, one of values, this inserts each pair of key and value into this map with put().
     * @param keys an array of keys
     * @param values an array of values
     * @param length how many items from keys and values to insert, at-most
     */
    public void putAll (char[] keys, char[] values, int length) {
        putAll(keys, 0, values, 0, length);
    }

    /**
     * Given two side-by-side arrays, one of keys, one of values, this inserts each pair of key and value into this map with put().
     * @param keys an array of keys
     * @param keyOffset the first index in keys to insert
     * @param values an array of values
     * @param valueOffset the first index in values to insert
     * @param length how many items from keys and values to insert, at-most
     */
    public void putAll (char[] keys, int keyOffset, char[] values, int valueOffset, int length) {
        length = Math.min(length, Math.min(keys.length - keyOffset, values.length - valueOffset));
        ensureCapacity(length);
        for (int k = keyOffset, v = valueOffset, i = 0, n = length; i < n; i++, k++, v++) {
            put(keys[k], values[v]);
        }
    }

    /**
     * Skips checks for existing keys, doesn't increment size.
     */
    private void putResize (char key, char value) {
        char[] keyTable = this.keyTable;
        for (int i = place(key); ; i = i + 1 & mask) {
            if (keyTable[i] == 0) {
                keyTable[i] = key;
                valueTable[i] = value;
                return;
            }
        }
    }

    /**
     * Returns the value for the specified key, or {@link #defaultReturnValue} if the key is not in the map.
     *
     * @param key any {@code char}
     */
    public char get (char key) {
        if (key == 0) { return hasZeroValue ? zeroValue : defaultReturnValue; }
        int i = locateKey(key);
        return i < 0 ? defaultReturnValue : valueTable[i];
    }

    /**
     * Returns the value for the specified key, or the default value if the key is not in the map.
     */
    public char getOrDefault (char key, char defaultValue) {
        if (key == 0) { return hasZeroValue ? zeroValue : defaultValue; }
        int i = locateKey(key);
        return i < 0 ? defaultValue : valueTable[i];
    }

    public char remove (char key) {
        if (key == 0) {
            if (hasZeroValue) {
                hasZeroValue = false;
                --size;
                return zeroValue;
            }
            return defaultReturnValue;
        }
        int i = locateKey(key);
        if (i < 0) { return defaultReturnValue; }
        char[] keyTable = this.keyTable;
        char rem;
        char[] valueTable = this.valueTable;
        char oldValue = valueTable[i];
        int mask = this.mask, next = i + 1 & mask;
        while ((rem = keyTable[next]) != 0) {
            int placement = place(rem);
            if ((next - placement & mask) > (i - placement & mask)) {
                keyTable[i] = rem;
                valueTable[i] = valueTable[next];
                i = next;
            }
            next = next + 1 & mask;
        }
        keyTable[i] = 0;

        size--;
        return oldValue;
    }

    /**
     * Returns true if the map has one or more items.
     */
    public boolean notEmpty () {
        return size > 0;
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map
     */
    public int size () {
        return size;
    }

    /**
     * Returns true if the map is empty.
     */
    public boolean isEmpty () {
        return size == 0;
    }

    /**
     * Gets the default value, a {@code char} which is returned by {@link #get(char)} if the key is not found.
     * If not changed, the default value is 0.
     *
     * @return the current default value
     */
    public char defaultReturnValue() {
        return defaultReturnValue;
    }

    /**
     * Sets the default value, a {@code char} which is returned by {@link #get(char)} if the key is not found.
     * If not changed, the default value is 0. Note that {@link #getOrDefault(char, char)} is also available,
     * which allows specifying a "not-found" value per-call.
     *
     * @param defaultReturnValue may be any char; should usually be one that doesn't occur as a typical value
     */
    public void defaultReturnValue(char defaultReturnValue) {
        this.defaultReturnValue = defaultReturnValue;
    }

    /**
     * Reduces the size of the backing arrays to be the specified capacity / loadFactor, or less. If the capacity is already less,
     * nothing is done. If the map contains more items than the specified capacity, the next highest power of two capacity is used
     * instead.
     */
    public void shrink (int maximumCapacity) {
        if (maximumCapacity < 0) { throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity); }
        int tableSize = tableSize(maximumCapacity, loadFactor);
        if (keyTable.length > tableSize) { resize(tableSize); }
    }

    /**
     * Clears the map and reduces the size of the backing arrays to be the specified capacity / loadFactor, if they are larger.
     */
    public void clear (int maximumCapacity) {
        int tableSize = tableSize(maximumCapacity, loadFactor);
        if (keyTable.length <= tableSize) {
            clear();
            return;
        }
        size = 0;
        resize(tableSize);
    }

    public void clear () {
        if (size == 0) { return; }
        size = 0;
        Arrays.fill(keyTable, '\u0000');
    }

    /**
     * Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may
     * be an expensive operation.
     */
    public boolean containsValue (char value) {
        if (hasZeroValue && zeroValue == value) { return true; }
        char[] valueTable = this.valueTable;
        char[] keyTable = this.keyTable;
        for (int i = valueTable.length - 1; i >= 0; i--) {
            if (keyTable[i] != 0 && valueTable[i] == value) { return true; }
        }
        return false;
    }

    public boolean containsKey (char key) {
        if (key == 0) { return hasZeroValue; }
        return locateKey(key) >= 0;
    }

    /**
     * Returns the key for the specified value, or null if it is not in the map. Note this traverses the entire map and compares
     * every value, which may be an expensive operation.
     */
    public char findKey (char value, char defaultKey) {
        if (hasZeroValue && zeroValue == value) { return 0; }
        char[] valueTable = this.valueTable;
        char[] keyTable = this.keyTable;
        for (int i = valueTable.length - 1; i >= 0; i--) {
            if (keyTable[i] != 0 && valueTable[i] == value) { return keyTable[i]; }
        }

        return defaultKey;
    }

    /**
     * Increases the size of the backing array to accommodate the specified number of additional items / loadFactor. Useful before
     * adding many items to avoid multiple backing array resizes.
     */
    public void ensureCapacity (int additionalCapacity) {
        int tableSize = tableSize(size + additionalCapacity, loadFactor);
        if (keyTable.length < tableSize) { resize(tableSize); }
    }

    protected void resize (int newSize) {
        int oldCapacity = keyTable.length;
        threshold = (int)(newSize * loadFactor);
        mask = newSize - 1;
        shift = Long.numberOfLeadingZeros(mask);

        char[] oldKeyTable = keyTable;
        char[] oldValueTable = valueTable;

        keyTable = new char[newSize];
        valueTable = new char[newSize];

        if (size > 0) {
            for (int i = 0; i < oldCapacity; i++) {
                char key = oldKeyTable[i];
                if (key != 0) { putResize(key, oldValueTable[i]); }
            }
        }
    }

    public float getLoadFactor(){
        return loadFactor;
    }

    public void setLoadFactor(float loadFactor){
        if (loadFactor <= 0f || loadFactor > 1f) { throw new IllegalArgumentException("loadFactor must be > 0 and <= 1: " + loadFactor); }
        this.loadFactor = loadFactor;
        int tableSize = tableSize(size, loadFactor);
        if(tableSize - 1 != mask) {
            resize(tableSize);
        }
    }

    @Override
    public int hashCode () {
        int h = hasZeroValue ? zeroValue + size : size;
        char[] keyTable = this.keyTable;
        char[] valueTable = this.valueTable;
        for (int i = 0, n = keyTable.length; i < n; i++) {
            char key = keyTable[i];
            if (key != 0) {
                h += key * 31;
                key = valueTable[i];
                h += key;
            }
        }
        return h;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof CharCharMap)) { return false; }
        CharCharMap other = (CharCharMap)obj;
        if (other.size != size) { return false; }
        if (other.hasZeroValue != hasZeroValue || other.zeroValue != zeroValue) { return false; }
        char[] keyTable = this.keyTable;
        char[] valueTable = this.valueTable;
        for (int i = 0, n = keyTable.length; i < n; i++) {
            char key = keyTable[i];
            if (key != 0) {
                char value = valueTable[i];
                if (value != other.get(key)) { return false; }
            }
        }
        return true;
    }

    public String toString (String separator) {
        return toString(separator, false);
    }

    @Override
    public String toString () {
        return toString(", ", true);
    }

    protected String toString (String separator, boolean braces) {
        if (size == 0) { return braces ? "{}" : ""; }
        StringBuilder buffer = new StringBuilder(32);
        if (braces) { buffer.append('{'); }
        if (hasZeroValue) {
            buffer.append("0=").append(zeroValue);
            if (size > 1) { buffer.append(separator); }
        }
        char[] keyTable = this.keyTable;
        char[] valueTable = this.valueTable;
        int i = keyTable.length;
        while (i-- > 0) {
            char key = keyTable[i];
            if (key == 0) { continue; }
            buffer.append(key);
            buffer.append('=');
            char value = valueTable[i];
            buffer.append(value);
            break;
        }
        while (i-- > 0) {
            char key = keyTable[i];
            if (key == 0) { continue; }
            buffer.append(separator);
            buffer.append(key);
            buffer.append('=');
            char value = valueTable[i];
            buffer.append(value);
        }
        if (braces) { buffer.append('}'); }
        return buffer.toString();
    }

    public static class Entry {
        public char key;
        public char value;

        @Override
        public String toString () {
            return key + "=" + value;
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *                               required to, throw this exception if the entry has been
         *                               removed from the backing map.
         */
        public char getKey () {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.  If the mapping
         * has been removed from the backing map (by the iterator's
         * {@code remove} operation), the results of this call are undefined.
         *
         * @return the value corresponding to this entry
         */
        public char getValue () {
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value (optional operation).  (Writes through to the map.)  The
         * behavior of this call is undefined if the mapping has already been
         * removed from the map (by the iterator's {@code remove} operation).
         *
         * @param value new value to be stored in this entry
         * @return old value corresponding to the entry
         * @throws UnsupportedOperationException if the {@code put} operation
         *                                       is not supported by the backing map
         * @throws ClassCastException            if the class of the specified value
         *                                       prevents it from being stored in the backing map
         * @throws NullPointerException          if the backing map does not permit
         *                                       null values, and the specified value is null
         * @throws IllegalArgumentException      if some property of this value
         *                                       prevents it from being stored in the backing map
         * @throws IllegalStateException         implementations may, but are not
         *                                       required to, throw this exception if the entry has been
         *                                       removed from the backing map.
         */
        public char setValue (char value) {
            char old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public boolean equals ( Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Entry entry = (Entry)o;

            if (key != entry.key) { return false; }
            return value == entry.value;
        }

        @Override
        public int hashCode () {
            return (int)((key ^ key >>> 32) * 0x9E3779B97F4A7C15L + (value ^ value << 32) >>> 32);
        }
    }

    public char putIfAbsent(char key, char value) {
        char v = get(key);
        if (!containsKey(key)) {
            v = put(key, value);
        }
        return v;
    }
    public boolean replace(char key, char oldValue, char newValue) {
        char curValue = get(key);
        if (curValue != oldValue || !containsKey(key)) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    public char replace(char key, char value) {
        char curValue = get(key);
        if (containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }
}
