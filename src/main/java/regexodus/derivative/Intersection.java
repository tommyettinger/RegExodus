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
    public RegEx derive(char[] c, int idx) {
        return new Intersection(
                first.derive(c, idx).shareParent(parent),
                second.derive(c, idx).shareParent(parent)
        ).shareParent(parent);
    }

    @Override
    public boolean emptySuccess() {
        return first.emptySuccess() && second.emptySuccess();
    }

    @Override
    public int kind() {
        return INTERSECTION;
    }

    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        first.reset();
        second.reset();
        midway = false;
    }

}
