package regexodus.ds;

import java.util.BitSet;

/**
 * Created by Tommy Ettinger on 3/30/2016.
 */
public class CharBitSet extends BitSet {
    public CharArrayList data;
    public CharBitSet() {
        data = new CharArrayList();
    }

    /**
     * Constructs a CharBitSet that includes all bits between start and end, inclusive.
     * @param start inclusive
     * @param end inclusive
     */
    public CharBitSet(int start, int end) {
        data = new CharArrayList(4);
        data.add(start);
        data.add(end - start);
    }
    public CharBitSet(char[] chars) {
        data = new CharArrayList(chars);
    }
    public CharBitSet(String s) {
        data = new CharArrayList(s);
    }

    public void flip(int bitIndex) {
        super.flip(bitIndex);
    }

    public void flip(int fromIndex, int toIndex) {
        super.flip(fromIndex, toIndex);
    }

    public void set(int bitIndex) {

    }

    public void set(int bitIndex, boolean value) {
        super.set(bitIndex, value);
    }

    public void set(int fromIndex, int toIndex) {
        super.set(fromIndex, toIndex);
    }

    public void set(int fromIndex, int toIndex, boolean value) {
        super.set(fromIndex, toIndex, value);
    }

    public void clear(int bitIndex) {
        super.clear(bitIndex);
    }

    public void clear(int fromIndex, int toIndex) {
        super.clear(fromIndex, toIndex);
    }

    public void clear() {
        super.clear();
    }

    public boolean get(int bitIndex) {
        return super.get(bitIndex);
    }

    public CharBitSet get(int fromIndex, int toIndex) {
        return new CharBitSet(fromIndex, toIndex - 1);
    }

    public int length() {
        return super.length();
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public boolean intersects(CharBitSet set) {
        return super.intersects(set);
    }

    public int cardinality() {
        return super.cardinality();
    }

    public void and(CharBitSet set) {
        super.and(set);
    }

    public void or(CharBitSet set) {
        super.or(set);
    }

    public void xor(CharBitSet set) {
        super.xor(set);
    }

    public void andNot(CharBitSet set) {
        super.andNot(set);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public int size() {
        return super.size();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public Object clone() {
        return super.clone();
    }

}
