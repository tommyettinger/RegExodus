package regexodus.ds;

import java.util.BitSet;

/**
 * Created by Tommy Ettinger on 3/30/2016.
 */
public class CompressedCharBitSet extends BitSet {
    public CharArrayList data;
    public CompressedCharBitSet() {
        data = new CharArrayList();
    }

    /**
     * Constructs a CompressedCharBitSet that includes all bits between start and end, inclusive.
     * @param start inclusive
     * @param end inclusive
     */
    public CompressedCharBitSet(int start, int end) {
        data = new CharArrayList(4);
        data.add(start);
        data.add(end - start);
    }
    public CompressedCharBitSet(char[] chars) {
        data = new CharArrayList(chars);
    }
    public CompressedCharBitSet(String s) {
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

    public CompressedCharBitSet get(int fromIndex, int toIndex) {
        return new CompressedCharBitSet(fromIndex, toIndex - 1);
    }

    public int length() {
        return super.length();
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public boolean intersects(CompressedCharBitSet set) {
        return super.intersects(set);
    }

    public int cardinality() {
        return super.cardinality();
    }

    public void and(CompressedCharBitSet set) {
        super.and(set);
    }

    public void or(CompressedCharBitSet set) {
        super.or(set);
    }

    public void xor(CompressedCharBitSet set) {
        super.xor(set);
    }

    public void andNot(CompressedCharBitSet set) {
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

    public static CharArrayList union(CharArrayList left, CharArrayList right)
    {
        if(left.isEmpty())
            return right;
        if(right.isEmpty())
            return left;
        CharArrayList packing = new CharArrayList(64);
        boolean on = false, onLeft = false, onRight = false;
        int idx = 0, skip = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0,
                leftLength = left.size(), rightLength = right.size();
        while ((elemLeft < leftLength || elemRight < rightLength) && idx <= 0xffff) {
            if (elemLeft >= leftLength) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left.getChar(elemLeft);
                if(onLeft) totalLeft++;
            }
            if(elemRight >= rightLength) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right.getChar(elemRight);
                if(onRight) totalRight++;
            }
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft || onRight)) {
                    if(on) skip--;
                    packing.add(skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft || onRight)) {
                    if(on) skip--;
                    packing.add(skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                skip += totalRight - idx;
                idx = totalRight;
                if(on != (onLeft || onRight)) {
                    if(on) skip--;
                    packing.add(skip);
                    skip = 0;
                    on = !on;
                }
                elemRight++;
            }
        }
        return packing;
    }

    public static CharArrayList intersection(CharArrayList left, CharArrayList right)
    {
        if(left.isEmpty() || right.isEmpty())
            return new CharArrayList();
        CharArrayList packing = new CharArrayList(64);
        boolean on = false, onLeft = false, onRight = false;
        int idx = 0, skip = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0,
                leftLength = left.size(), rightLength = right.size();
        while ((elemLeft < leftLength || elemRight < rightLength) && idx <= 0xffff) {
            if (elemLeft >= leftLength) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left.getChar(elemLeft);
                if(onLeft) totalLeft++;
            }
            if(elemRight >= rightLength) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right.getChar(elemRight);
                if(onRight) totalRight++;
            }
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft || onRight)) {
                    if(on) skip--;
                    packing.add(skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft || onRight)) {
                    if(on) skip--;
                    packing.add(skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                skip += totalRight - idx;
                idx = totalRight;
                if(on != (onLeft || onRight)) {
                    if(on) skip--;
                    packing.add(skip);
                    skip = 0;
                    on = !on;
                }
                elemRight++;
            }
        }
        return packing;
    }

    public static CharArrayList insert(CharArrayList left, char insertion)
    {
        if(left.isEmpty())
            return new CharArrayList(insertion, '\0');
        return union(left, new CharArrayList(insertion, '\0'));
    }


}
