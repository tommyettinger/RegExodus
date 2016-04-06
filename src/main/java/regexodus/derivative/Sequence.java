package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L1.L2) = D_c(L1.L2) U (δ(L1).D_c(L2))
 * δ(L1 U L2) = δ(L1) U δ(L2)
 */
public class Sequence extends RegEx {
    public RegEx first, second;

    public Sequence(RegEx first, RegEx second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public RegEx derive(char c) {
        if (first.isNullable())
            return new Choice(new Sequence(first.derive(c), second), second.derive(c));
        else
            return new Sequence(first.derive(c), second);
    }

    @Override
    public boolean isNullable() {
        return first.isNullable() && second.isNullable();
    }
}
