package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * Not in the original.
 */
public class Group extends RegEx {
    public RegEx lang;
    public RegEx empty, blank;
    public int matchStart, matchEnd;
    public Group(RegEx lang) {
        this.lang = lang.shareParent(this);
        empty = new Empty().shareParent(this);
        blank = new Blank().shareParent(this);
        matchStart = matchEnd = -1;
    }

    @Override
    public RegEx derive(char c) {
        return lang.derive(c);
    }

    @Override
    public boolean emptySuccess() {
        return lang.emptySuccess();
    }

    @Override
    public int matches(char[] chars, int first, int last, int len)
    {
        int l;
        if(first >= len)
            l = emptySuccess() ? last : -1;
        else
            l = derive(chars[first]).matches(chars, 1 + first, last + 1, len);

        if(l >= 0)
        {
            matchStart = first;
            matchEnd = first + l;
            return l;
        }
        matchStart = matchEnd = -1;
        return -1;
    }
    @Override
    public int kind() {
        return GROUP;
    }
}
