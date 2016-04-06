package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 */
public abstract class RegEx {
    public abstract RegEx derive(char c);

    public abstract boolean isNullable();

    public boolean matches(CharSequence s) {
        int len = s.length();
        if (len == 0)
            return isNullable();
        else
            return derive(s.charAt(0)).matches(s.subSequence(1, len));
    }
}
