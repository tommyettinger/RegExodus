package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * Not in the original.
 */
public class Container extends RegEx{
    RegEx lang;
    public Container(RegEx lang, Group parent)
    {
        this.lang = lang;
        this.parent = parent;
    }
    @Override
    public RegEx derive(char[] c, int idx) {
        RegEx re = lang.derive(c, idx);
        if (re.emptySuccess()) {
            if (parent.matchStart < 0)
                parent.matchStart = idx;
            parent.matchEnd = idx + 1;
        }
        return new Container(re, parent);
    }
        /*
        RegEx re = lang.derive(c, idx);
        if(re.kind() != EMPTY && parent != null)
        {
            parent.matchEnd = idx + 1;
            if(parent.matchStart < 0)
                parent.matchStart = idx;

            return new Container(re, parent);
        }
        return re;
        */

    @Override
    public boolean emptySuccess() {
        return lang.emptySuccess();
    }

    @Override
    public int kind() {
        return CONTAINER;
    }

    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        lang.reset();
        midway = false;
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
        if(parent == null)
            return got;
        if(got)
        {
            parent.matchStart = first;
            return true;
        }
        parent.matchStart = parent.matchEnd = -1;
        return false;
    }*/

    @Override
    public RegEx shareParent(Group newParent) {
        return this;
    }
}
