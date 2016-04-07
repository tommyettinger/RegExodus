package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 */
public abstract class RegEx {

    public static final int EMPTY = 0, GROUP = 1, BLANK = 2, PRIMITIVE = 3, COMPLEMENT = 4, CHOICE = 5,
            INTERSECTION = 6, DIFFERENCE = 7, SEQUENCE = 8, REPETITION = 9, RANGE = 10, CATEGORY = 11;

    public Group parent = null;

    public abstract RegEx derive(char c);

    public abstract boolean emptySuccess();

    public abstract int kind();

    public RegEx shareParent(Group newParent)
    {
        parent = newParent;
        return this;
    }

    public boolean matches(String s) {
        return matches(s.toCharArray(), 0, 0, s.length()) >= 0;
//            return derive(s.charAt(0)).matches(s.substring(1, len));
    }
    public int matches(char[] chars, int first, int last, int len)
    {
        if(first >= len)
            return emptySuccess() ? last : -1;
        else
            return derive(chars[first]).matches(chars, 1 + first, last + 1, len);
    }
}
