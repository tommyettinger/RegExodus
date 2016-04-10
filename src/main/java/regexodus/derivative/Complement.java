package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(~L) = ~(D_c(L))
 * δ(~L) = ~(δ(L))
 */
public class Complement extends RegEx {
    private RegEx lang;

    private Complement(RegEx lang) {
        this.lang = lang;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        return new Complement(lang.derive(c, idx).shareParent(parent)).shareParent(parent);
    }

    @Override
    public boolean emptySuccess() {
        return !lang.emptySuccess();
    }

    @Override
    public int kind() {
        return COMPLEMENT;
    }

    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        lang.reset();
        midway = false;
    }
}
