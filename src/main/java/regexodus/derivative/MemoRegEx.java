package regexodus.derivative;

import java.util.HashMap;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * This particular section is based on work by Daniel Spiewak:
 * https://github.com/djspiewak/derivative-combinators
 */
public abstract class MemoRegEx extends RegEx {

    protected HashMap<Character, RegEx> derivations = new HashMap<Character, RegEx>(32);
    public RegEx derive(char[] c, int idx)
    {
        RegEx got = derivations.get(c[idx]);
        if(got != null)
            return got;
        got = innerDerive(c, idx);
        derivations.put(c[idx], got);
        return got;
    }

    protected abstract RegEx innerDerive(char[] c, int idx);
}
