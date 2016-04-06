package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c{∅} = ∅
 * δ(∅) = ∅
 */
public class Empty extends RegEx {
    @Override
    public RegEx derive(char c) {
        return new Empty();
    }

    @Override
    public boolean isNullable() {
        return false;
    }
}