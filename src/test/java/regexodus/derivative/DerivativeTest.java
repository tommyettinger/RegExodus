package regexodus.derivative;

import org.junit.Test;
import regexodus.Category;

/**
 * Testing the derivative-based engine, which it looks like may be unsuited.
 * Created by Tommy Ettinger on 4/7/2016.
 */
public class DerivativeTest {
    @Test
    public void testPrimitive()
    {
        RegEx r = new Primitive('a');
        System.out.println(r.matches(""));
        System.out.println(r.matches("a"));
        System.out.println(r.matches("b"));
        System.out.println(r.matches("aa"));
        System.out.println(r.matches("ab"));
        System.out.println(r.matches("ba"));
    }

    @Test
    public void testPrimitiveRepetition()
    {
        RegEx r = new Repetition(new Primitive('a'));

        System.out.println(r.matches(""));
        System.out.println(r.matches("a"));
        System.out.println(r.matches("b"));
        System.out.println(r.matches("aa"));
        System.out.println(r.matches("ab"));
        System.out.println(r.matches("ba"));
    }

    @Test
    public void testCategory()
    {
        RegEx r = new Sequence(new UnicodeCategory(Category.Ll), new Any());
        System.out.println(r.matches(""));
        System.out.println(r.matches("a"));
        System.out.println(r.matches("b"));
        System.out.println(r.matches("aa"));
        System.out.println(r.matches("ab"));
        System.out.println(r.matches("ba"));
    }

    private void printGroupMatch(RegEx r, Group g, String text)
    {
        System.out.println(r.matches(text) + ": " + g.contents(text));
    }
    @Test
    public void testGroupCategory()
    {
        Group g = new Group(new UnicodeCategory(Category.Ll));
        RegEx r = new Sequence(g, new Any());
        printGroupMatch(r, g, "");
        printGroupMatch(r, g, "a");
        printGroupMatch(r, g, "b");
        printGroupMatch(r, g, "aa");
        printGroupMatch(r, g, "ab");
        printGroupMatch(r, g, "ba");
    }

    @Test
    public void testGroupRepetition()
    {
        Group g = new Group(new Sequence(new Primitive('a'), new Repetition(new Primitive('a'))));
        RegEx r = new Sequence(g, new Any());

        printGroupMatch(r, g, "aaa");

        printGroupMatch(r, g, "a");
        printGroupMatch(r, g, "c");
        printGroupMatch(r, g, "ac");
        printGroupMatch(r, g, "bc");
        printGroupMatch(r, g, "aac");
        printGroupMatch(r, g, "abc");
        printGroupMatch(r, g, "bac");
        printGroupMatch(r, g, "aabc");
        printGroupMatch(r, g, "abac");
        printGroupMatch(r, g, "babc");
        printGroupMatch(r, g, "aaa");
    }
}
