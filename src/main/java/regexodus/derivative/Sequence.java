package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L1.L2) = D_c(L1.L2) U (δ(L1).D_c(L2))
 * δ(L1 U L2) = δ(L1) U δ(L2)
 */
public class Sequence extends RegEx {
    public RegEx first, second;

    public Sequence(RegEx first, RegEx second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public RegEx derive(char[] c, int idx) {
        if(first.kind() == EMPTY)
            return parent == null ? new Empty() : parent.empty;
        if (first.emptySuccess())
            return new Choice(
                    new Sequence(
                            first.derive(c, idx).shareParent(parent), second.shareParent(parent)).shareParent(parent),
                    second.derive(c, idx).shareParent(parent)).shareParent(parent);
        else
            return new Sequence(first.derive(c, idx).shareParent(parent), second.shareParent(parent)).shareParent(parent);
    }

    @Override
    public boolean emptySuccess() {
        return first.emptySuccess() && second.emptySuccess();
    }

    @Override
    public int kind() {
        return SEQUENCE;
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
