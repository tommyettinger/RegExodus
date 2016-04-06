package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L*) = (D_cL).L*
 * δ(L*) = eps
 */
public class Repetition extends RegEx {
    public RegEx inside;

    public Repetition(RegEx inside) {
        this.inside = inside;
    }

    @Override
    public RegEx derive(char c) {
        return new Sequence(inside.derive(c), this);
    }

    @Override
    public boolean isNullable() {
        return true;
    }
}
