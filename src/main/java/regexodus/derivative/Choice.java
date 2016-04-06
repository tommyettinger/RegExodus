package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L1 U L2) = D_c(L1) U D_c(L2)
 * δ(L1 U L2) = δ(L1) U δ(L2)
 */
public class Choice extends RegEx {
    public RegEx left, right;

    public Choice(RegEx left, RegEx right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public RegEx derive(char c) {
        return new Choice(left.derive(c), right.derive(c));
    }

    @Override
    public boolean isNullable() {
        return left.isNullable() || right.isNullable();
    }
}
