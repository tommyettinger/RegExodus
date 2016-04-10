package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L*) = (D_cL).L*
 * δ(L*) = eps
 */
public class Repetition extends RegEx {
    private RegEx inside;

    public Repetition(RegEx inside) {
        this.inside = inside;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        return new Sequence(inside.derive(c, idx).shareParent(parent), this).shareParent(parent);
    }

    @Override
    public boolean emptySuccess() {
        return true;
    }

    @Override
    public int kind() {
        return REPETITION;
    }

    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        inside.reset();
        midway = false;
    }

}
