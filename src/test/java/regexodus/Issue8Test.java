package regexodus;

import org.junit.Assert;
import org.junit.Test;

public class Issue8Test {
    @Test
    public void testZeroLengthBranchStar() {
        Pattern pattern = Pattern.compile("(ab|)*c");
        boolean result = pattern.matches("aaaa");
        Assert.assertFalse(result);
    }
    @Test
    public void testZeroLengthGroupPlus() {
        Pattern pattern = Pattern.compile("()+d|abc");
        boolean result = pattern.matches("cab");
        Assert.assertFalse(result);
    }
    @Test
    public void testStartStar() {
        Pattern pattern = Pattern.compile("(^)*b{1,3}");
        boolean result = pattern.matches("c");
        Assert.assertFalse(result);
    }
}
