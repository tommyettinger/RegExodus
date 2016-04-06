package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L1 ∩ L2) = D_c(L1) ∩ D_c(L2)
 * δ(L1 ∩ L2) = δ(L1) ∩ δ(L2)
 */
public class Intersection extends RegEx {
    public RegEx first, second;

    public Intersection(RegEx first, RegEx second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public RegEx derive(char c) {
        return new Intersection(first.derive(c), second.derive(c));
    }

    @Override
    public boolean isNullable() {
        return first.isNullable() && second.isNullable();
    }
}
