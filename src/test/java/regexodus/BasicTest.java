package regexodus;

import org.junit.Test;

import java.util.Random;

/**
 * Created by Tommy Ettinger on 3/28/2016.
 */
public class BasicTest {
    static Random r = new Random(0x1337CAFE);
    static char[] tmp = new char[10];
    static final int STR_LEN = 10;
    static String[] strings = new String[500000];
    static char[][] chars = new char[500000][STR_LEN];

    private static char[] exampleString()
    {
        for (int i = 0; i < STR_LEN; i++) {
            tmp[i] = (char) ('0' + r.nextInt(10));
        }
        return tmp;
    }
    static {
        for (int i = 0; i < 500000; i++) {
            System.arraycopy(exampleString(), 0, chars[i], 0, STR_LEN);
            strings[i] = new String(chars[i]);
        }
    }
    //@Test
    public void testASCII()
    {
        String[] strings = new String[100000];
        for (int i = 0; i < 100000; i++) {
            strings[i] = new String(exampleString());
        }
        Matcher p = new Pattern("[0-9a-fA-F]+").matcher();
        java.util.regex.Pattern jup = java.util.regex.Pattern.compile("[0-9a-fA-F]+");
        long time = System.currentTimeMillis(), ctr = 0;
        for (int i = 0; i < 100000; i++) {
            p.setTarget(strings[i]);
            if(p.matches())
                ctr++;
        }
        //System.out.println(p.matches("1337ca7CAFE"));
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(ctr);
        time = System.currentTimeMillis();
        ctr = 0;

        for (int i = 0; i < 100000; i++) {
            if(jup.matcher(strings[i]).matches())
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
    @Test
    public void testMatcherProblems()
    {
        //Matcher p = new Pattern("[0-9a-fA-F]+").matcher();
        Pattern p = new Pattern("\\w\\w\\w({a}\\w\\w){\\a}"), p2 = new Pattern("\\w\\w\\w({a}..){\\a}");

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
        //Matcher p = new Pattern("({a}.){\\a}+").matcher();
        Matcher p = new Pattern("000").matcher();
        long time = System.currentTimeMillis(), ctr = 0;
        for (int i = 0; i < 500000; i++) {
            p.setTarget(chars[i], 0, STR_LEN);
            if(p.find())
                ctr++;
        }
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(ctr);
    }

    @Test
    public void testJUSpeed()
    {
        //java.util.regex.Pattern jup = java.util.regex.Pattern.compile("[0-9a-fA-F]+");
        //java.util.regex.Pattern jup = java.util.regex.Pattern.compile("(.)\\1+");
        java.util.regex.Pattern jup = java.util.regex.Pattern.compile("000");
        long time = System.currentTimeMillis(), ctr = 0;

        for (int i = 0; i < 500000; i++) {
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
