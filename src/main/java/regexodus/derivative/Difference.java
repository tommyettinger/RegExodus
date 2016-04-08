package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L1 - L2) = D_c(L1) ∩ D_c(L2)
 * δ(L1 - L2) = δ(L1) - δ(L2)
 */
public class Difference extends RegEx {
    public RegEx left, right;

    public Difference(RegEx left, RegEx right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        return new Difference(
                left.derive(c, idx).shareParent(parent),
                right.derive(c, idx).shareParent(parent)
        ).shareParent(parent);
    }

    @Override
    public boolean emptySuccess() {
        return left.emptySuccess() && !right.emptySuccess();
    }

    @Override
    public int kind() {
        return DIFFERENCE;
    }

    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        left.reset();
        right.reset();
        midway = false;
    }
}
