package regexodus.derivative;

import regexodus.Category;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * Not in the original.
 */
public class UnicodeCategory extends RegEx {
    private Category cat;

    public UnicodeCategory(Category c) {
        this.cat = c;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        return this.cat.contains(c[idx]) ?
                (parent == null) ?
                        new Blank() :
                        parent.blank :
                (parent == null) ?
                        new Empty() :
                        parent.empty;
    }

    @Override
    public boolean emptySuccess() {
        return false;
    }

    @Override
    public int kind() {
        return CATEGORY;
    }

    @Override
    public void reset() {
    }

}
