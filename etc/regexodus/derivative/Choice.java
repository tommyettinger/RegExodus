package regexodus.derivative;

/**
 * Ported to Java from http://blog.errstr.com/2013/01/22/implementing-a-more-powerful-regex/
 * D_c(L1 U L2) = D_c(L1) U D_c(L2)
 * δ(L1 U L2) = δ(L1) U δ(L2)
 * Incorporates techniques used by Daniel Spiewak in his Scala code, https://github.com/djspiewak/derivative-combinators
 */
public class Choice extends MemoRegEx {
    private RegEx left;
    private RegEx right;
    private int es = 0;

    public Choice(RegEx left, RegEx right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public RegEx innerDerive(char[] c, int idx) {
        int l = left.kind(), r = right.kind();
        if(l == EMPTY && r == EMPTY)
            return parent == null ? new Empty() : parent.empty;
        else if(l == EMPTY)
            return right.derive(c, idx).shareParent(parent);
        else if(r == EMPTY)
            return left.derive(c, idx).shareParent(parent);
        else
            return new Choice(
                left.derive(c, idx).shareParent(parent),
                right.derive(c, idx).shareParent(parent)
            ).shareParent(parent);
    }

    @Override
    public boolean emptySuccess() {
        if(es != 0) return es > 0;
        return (es = (left.emptySuccess() || right.emptySuccess()) ? 1 : -1) > 0;
    }

    @Override
    public int kind() {
        return CHOICE;
    }


    @Override
    public void reset() {
        if(midway)
            return;
        midway = true;
        left.reset();
        right.reset();
        midway = false;
    }

    /*
    @Override
    public boolean emptySuccess() {
        return left.emptySuccess() || right.emptySuccess();
    }
     */
}
