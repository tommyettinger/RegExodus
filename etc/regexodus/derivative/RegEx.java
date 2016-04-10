package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 */
public abstract class RegEx {

    static final int EMPTY = 0;
    static final int GROUP = 1;
    static final int BLANK = 2;
    static final int PRIMITIVE = 3;
    static final int COMPLEMENT = 4;
    static final int CHOICE = 5;
    static final int INTERSECTION = 6;
    static final int DIFFERENCE = 7;
    static final int SEQUENCE = 8;
    static final int REPETITION = 9;
    static final int RANGE = 10;
    static final int CATEGORY = 11;
    static final int ANY = 12;
    static final int CONTAINER = 13;

    Group parent = null;

    protected abstract RegEx derive(char[] c, int idx);

    protected abstract boolean emptySuccess();

    protected abstract int kind();
    boolean midway = false;
    protected abstract void reset();
    RegEx shareParent(Group newParent)
    {
        parent = newParent;
        return this;
    }

    public boolean matches(String s) {
        reset();
        return matches(s.toCharArray(), 0, s.length());
//            return derive(s.charAt(0)).matches(s.substring(1, len));
    }
    boolean matches(char[] chars, int first, int len)
    {
        if(parent != null)
            parent.matchEnd = first;

        if(first >= len)
            return emptySuccess();
        else
            return derive(chars, first).matches(chars, 1 + first, len);
    }
}
