package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c{c} = eps
 * δ(c) = ∅
 * Not in the original.
 */
public class Any extends RegEx {
    @Override
    public RegEx derive(char[] c, int idx) {
        return (parent == null) ?
                        new Blank() :
                        parent.blank;
    }

    @Override
    public boolean emptySuccess() {
        return false;
    }

    @Override
    public int kind() {
        return ANY;
    }

    @Override
    public void reset() {

    }
}
