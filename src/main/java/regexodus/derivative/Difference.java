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
    public RegEx derive(char c) {
        return new Difference(left.derive(c), right.derive(c));
    }

    @Override
    public boolean isNullable() {
        return left.isNullable() && !right.isNullable();
    }
}
