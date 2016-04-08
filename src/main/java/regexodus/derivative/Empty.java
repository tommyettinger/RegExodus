package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c{∅} = ∅
 * δ(∅) = ∅
 */
public class Empty extends RegEx {

    @Override
    public RegEx derive(char[] c, int idx) {
        return this;
        /*
        if(parent == null)
            return new Empty().shareParent(parent);
        else
            return parent.empty;
        */
    }

    @Override
    public boolean emptySuccess() {
        return false;
    }

    @Override
    public int kind() {
        return EMPTY;
    }

    @Override
    public boolean matches(char[] chars, int first, int len) {
        return false;
    }

    @Override
    public RegEx shareParent(Group newParent) {
        if(newParent == null)
            return new Empty();
        else
            return newParent.empty == null ? new Empty() : newParent.empty;
    }
    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
    }
}