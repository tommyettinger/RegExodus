package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(~L) = ~(D_c(L))
 * δ(~L) = ~(δ(L))
 */
public class Complement extends RegEx {
    public RegEx lang;

    public Complement(RegEx lang) {
        this.lang = lang;
    }

    @Override
    public RegEx derive(char c) {
        return new Complement(lang.derive(c));
    }

    @Override
    public boolean isNullable() {
        return !lang.isNullable();
    }
}
