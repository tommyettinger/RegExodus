package regexodus;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Just a quick test.
 * Created by Tommy Ettinger on 3/28/2016.
 */
public class BasicTest {
    private static Random r = new Random(0x1337CAFE);
    private static char[] tmp = new char[10];
    private static final int STR_LEN = 10, STRING_COUNT = 10000;
    private static String[] strings = new String[STRING_COUNT];
    private static char[][] chars = new char[STRING_COUNT][STR_LEN];

    private static char[] exampleString()
    {
        for (int i = 0; i < STR_LEN; i++) {
            tmp[i] = (char) (33 + r.nextInt(94));
        }
        return tmp;
    }
    private static String exampleASCII()
    {
        for (int i = 0; i < STR_LEN; i++) {
            tmp[i] = (char) (33 + r.nextInt(94));
        }
        return new String(tmp);
    }
    static {
        for (int i = 0; i < STRING_COUNT; i++) {
            System.arraycopy(exampleString(), 0, chars[i], 0, STR_LEN);
            strings[i] = new String(chars[i]);
        }
    }
    @Test
    public void testASCII() {

        String[] strings = new String[100000];
        for (int i = 0; i < 100000; i++) {
            strings[i] = exampleASCII();
        }
        Pattern p1 = new Pattern("([0-9a-f])\\1", "i"), p2 = Pattern.compile("([0-9A-F])\\1", "i");
        Matcher m1 = p1.matcher(), m2 = p2.matcher();
        Replacer r1 = p1.replacer("$1$1$1$1"), r2 = p2.replacer("$1$1$1$1");
        long ctr = 0;
        boolean found;
        /*
        for (int i = 0; i < 100000; i++) {
            m1.setTarget(strings[i]);
            m2.setTarget(strings[i]);
            found = m1.find();
            if(found) ctr++;
            Assert.assertEquals(found, m2.find());
            //System.out.println(r1.replace(strings[i]) + ";;;" + r2.replace(strings[i]));
        }*/
        System.out.println(r1.replace("aaB") + ";;;" + r2.replace("aaB"));
        m1.setTarget("aaB");
        m2.setTarget("aaB");
        System.out.println(m1.find() + "---" + m2.find());
        System.out.println(r1.replace("AAB") + ";;;" + r2.replace("AAB"));
        m1.setTarget("AAB");
        m2.setTarget("AAB");
        System.out.println(m1.find() + "---" + m2.find());
        System.out.println(r1.replace("aaB") + ";;;" + r2.replace("AAB"));
        m1.setTarget("aaB");
        m2.setTarget("AAB");
        System.out.println(m1.find() + "---" + m2.find());
        System.out.println(r1.replace("AAB") + ";;;" + r2.replace("aaB"));
        m1.setTarget("AAB");
        m2.setTarget("aaB");
        System.out.println(m1.find() + "---" + m2.find());


        /*
        Assert.assertEquals(p, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertTrue(p.matches("1337ca7CAFE"));
        Assert.assertTrue(p3.matches("[0-9a-fA-F]"));
        */
        System.out.println(Pattern.compile("(\\Q[]\\){)$^(\\E)").matches("[]\\){)$^("));
        System.out.println(Pattern.compile("\\m11").matches("\13")); //decimal is nicer than octal, huh
        System.out.println(Pattern.compile("[\\v]{3}").matches("\u2028\r\n"));
        System.out.println(Pattern.compile("[\\h]{5}").matches("\u1680\u00A0 \u2009\t"));
        System.out.println(Pattern.compile("\\bDorothy\\b", "ui").replacer("Nadanno")
                .replace("Dorothy lived in the midst of the great Kansas prairies"));
        System.out.println(Pattern.compile("\\b(?:(?:\\Qciploû\\E)|(?:\\Qlaounctouige\\E))\\b", "ui").matches("ciploû"));
        System.out.println(Pattern.compile("\\((?:[\\s,]*(?:[^\\s,]+)|(?:\\((?:[\\s,]*[^\\s,]+)*\\)))*\\)").matches("(ciploû (+ 1 2))"));
        System.out.println(Pattern.compile("(z\\p{Ps}\\*)\\QABC\\E{\\:@/1}").matches("z(*ABC*)Z"));
        System.out.println(Pattern.compile("([\\(\\[\\{]){\\:/1}").matches("()"));

    }
    @Test
    public void testReplace()
    {
        String[] strings = new String[10000];

        for (int i = 0; i < 10000; i++) {
            strings[i] = exampleASCII();
        }
        Replacer br = new Pattern("([\\p{Ps}\\p{Pe}])").replacer("${1}${:1}"),
                r1 = Pattern.compile("([0-9a-f])\\1", "i").replacer("  "),
                r2 = Pattern.compile("([0-9A-F])\\1", "i").replacer("  ");
        StringBuilder sb = new StringBuilder(5000), sb2 = new StringBuilder(5000), sb3 = new StringBuilder(5000);
        boolean found;
        for (int i = 0; i < 10000; i++) {
            r1.replace(strings[i], sb);
            r2.replace(strings[i], sb2);
            Assert.assertEquals(sb.toString(), sb2.toString());
            br.replace(strings[i], sb3);
        }
        //System.out.println(sb.toString());
        //System.out.println(sb2.toString());
        System.out.println(sb3);

        /*
        Assert.assertEquals(p, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertTrue(p.matches("1337ca7CAFE"));
        Assert.assertTrue(p3.matches("[0-9a-fA-F]"));
        */
    }
    //@Test
    public void testMatcherProblems()
    {
        //Matcher p = new Pattern("[0-9a-fA-F]+").matcher();
        Pattern p = new Pattern("\\w\\w\\d(\\w\\d)\\1"), p2 = new Pattern("\\w\\w\\d(\\w\\d)\\1");

        Term t = p.root0;
        while (t != null) {
            System.out.println(t);
            t = t.next;
        }
        t = p2.root0;
        while (t != null) {
            System.out.println(t);
            t = t.next;
        }
        System.out.println(p);
        if(p.matcher("1a2a1a1").find()) System.out.println("1a2a1a1");
        if(p.matcher("1a2a2a2").find()) System.out.println("1a2a2a2");
        if(p.matcher("1a2a3a3").find()) System.out.println("1a2a3a3");
        System.out.println(p2);
        if(p2.matcher("1a2a1a1").find()) System.out.println("1a2a1a1");
        if(p2.matcher("1a2a2a2").find()) System.out.println("1a2a2a2");
        if(p2.matcher("1a2a3a3").find()) System.out.println("1a2a3a3");

        Pattern swear0 = Pattern.compile("[AaаαАΑΛ][NnийИЙΝ]..?[SsξlιζzΖ]");
        java.util.regex.Pattern swear1 = java.util.regex.Pattern.compile("[AaаαАΑΛ][NnийИЙΝ]..?[SsξlιζzΖ]");
        System.out.println(swear0.matcher("Annals").find());
        System.out.println(swear1.matcher("Annals").find());


    }
    @Test
    public void testMatcherSpeed()
    {
        //Matcher p = new Pattern("[0-9a-fA-F]+").matcher();
        Pattern p = new Pattern("\\p{Lu}\\p{Ll}\\PP\\p{P}"); //"(.)\\1+"
        //Matcher p = new Pattern("000").matcher();
        long time = System.currentTimeMillis(), ctr = 0;
        for (int i = 0; i < STRING_COUNT; i++) {
            if(p.matcher(chars[i], 0, STR_LEN).find())
                ctr++;
        }
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(ctr);
    }

    @Test
    public void testJUSpeed()
    {
        //java.util.regex.Pattern jup = java.util.regex.Pattern.compile("[0-9a-fA-F]+");
        java.util.regex.Pattern jup = java.util.regex.Pattern.compile("\\p{Lu}\\p{Ll}\\PP\\p{P}"); //"(.)\\1+"
        //java.util.regex.Pattern jup = java.util.regex.Pattern.compile("000");
        long time = System.currentTimeMillis(), ctr = 0;

        for (int i = 0; i < STRING_COUNT; i++) {
            if(jup.matcher(strings[i]).find())
                ctr++;
        }
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(ctr);
        /*
        Assert.assertEquals(p, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertTrue(p.matches("1337ca7CAFE"));
        Assert.assertTrue(p3.matches("[0-9a-fA-F]"));
        */
    }
}
