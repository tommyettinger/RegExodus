package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c{∅} = ∅
 * δ(∅) = ∅
 */
public class Empty extends RegEx {

    @Override
    public RegEx derive(char c) {
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
    public int matches(char[] chars, int first, int last, int len) {
        return last;
    }

    @Override
    public RegEx shareParent(Group newParent) {
        if(newParent == null)
            return new Empty();
        else
            return newParent.empty;
    }
}