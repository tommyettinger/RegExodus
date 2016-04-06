package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c{c} = eps
 * D_c{c'} = ∅ if c ≠ c
 * δ(c) = ∅
 */
public class Primitive extends RegEx {
    public char c;

    public Primitive(char c) {
        this.c = c;
    }

    @Override
    public RegEx derive(char c) {
        return (c == this.c) ? new Blank() : new Empty();
    }

    @Override
    public boolean isNullable() {
        return false;
    }
}
