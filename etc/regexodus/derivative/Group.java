package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * Not in the original.
 */
public class Group extends RegEx {
    private RegEx lang;
    public RegEx empty, blank;
    public int matchStart, matchEnd;
    public Group(RegEx lang) {
        this.lang = lang.shareParent(this);
        empty = new Empty().shareParent(this);
        blank = new Blank().shareParent(this);
        matchStart = -1;
        matchEnd = -1;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        RegEx re = lang.derive(c, idx);

        if (re.emptySuccess()) {
            if (matchStart < 0)
                matchStart = idx;
            matchEnd = idx + 1;
        }
        return new Container(re, this);
    }

    @Override
    public boolean emptySuccess() {
        return lang.emptySuccess();
    }

    /*
    @Override
    public boolean matches(char[] chars, int first, int len)
    {
        boolean got;
        if(first >= len)
            got = emptySuccess();
        else
            got = derive(chars, first).matches(chars, 1 + first, len);

        if(got)
        {
            matchStart = first;
            return true;
        }
        matchStart = matchEnd = -1;
        return false;
    }
     */
    @Override
    public int kind() {
        return GROUP;
    }

    public String contents(String original)
    {
        if(matchStart >= 0 && matchEnd >= matchStart)
        {
            return original.substring(matchStart, matchEnd);
        }
        return "";
    }

    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        lang.reset();
        matchEnd = -1;
        matchStart = -1;
        midway = false;
    }

}
