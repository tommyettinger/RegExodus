package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c{eps} = ∅
 * δ(eps) = eps
 */
public class Blank extends RegEx {

    @Override
    public RegEx derive(char[] c, int idx) {
        return parent == null ? new Empty() : parent.empty;
    }

    @Override
    public boolean emptySuccess() {
        return true;
    }

    @Override
    public int kind() {
        return BLANK;
    }

    @Override
    public void reset() {

    }
}