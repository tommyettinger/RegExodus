package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * Not in the original.
 */
public class Range extends RegEx {
    public char start, end;

    public Range(char start, char end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        return (c[idx] >= start && c[idx] <= end) ?
                (parent == null) ?
                        new Blank() :
                        parent.blank :
                (parent == null) ?
                        new Empty() :
                        parent.empty;
    }

    @Override
    public boolean emptySuccess() {
        return false;
    }

    @Override
    public int kind() {
        return RANGE;
    }

    @Override
    public void reset() {
    }

}
