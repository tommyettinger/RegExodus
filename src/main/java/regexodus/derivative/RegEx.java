package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 */
public abstract class RegEx {

    public static final int EMPTY = 0, GROUP = 1, BLANK = 2, PRIMITIVE = 3, COMPLEMENT = 4, CHOICE = 5,
            INTERSECTION = 6, DIFFERENCE = 7, SEQUENCE = 8, REPETITION = 9, RANGE = 10, CATEGORY = 11, ANY = 12,
            CONTAINER = 13;

    public Group parent = null;

    public abstract RegEx derive(char[] c, int idx);

    public abstract boolean emptySuccess();

    public abstract int kind();
    protected boolean midway = false;
    public abstract void reset();
    public RegEx shareParent(Group newParent)
    {
        parent = newParent;
        return this;
    }

    public boolean matches(String s) {
        reset();
        return matches(s.toCharArray(), 0, s.length());
//            return derive(s.charAt(0)).matches(s.substring(1, len));
    }
    public boolean matches(char[] chars, int first, int len)
    {
        if(parent != null)
            parent.matchEnd = first;

        if(first >= len)
            return emptySuccess();
        else
            return derive(chars, first).matches(chars, 1 + first, len);
    }
}
