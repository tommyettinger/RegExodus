package regexodus;

import org.junit.Assert;
import org.junit.Test;
import regexodus.ds.IntBitSet;

import java.util.Arrays;
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
            tmp[i] = (char) (65 + r.nextInt(26));
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
        Matcher m = Pattern.compile("({=NAME}a+)").matcher("aa a aaa aaa");
        m.find();
        System.out.println(m.group("NAME"));
        System.out.println();
        m.flush();
        for (String s : m.foundStrings())
        {
            System.out.println(s);
        }
        m = Pattern.compile("\\p{Js}\\p{Jp}*").matcher("... uh, $1 please? That's 1 dollar.");
        for (String s : m.foundStrings())
        {
            System.out.println(s);
        }
        System.out.println(new Replacer(new Pattern("\\b(\\d+)\\b"),new PerlSubstitution("'$1'")).replace("abc 123 def"));
        System.out.println(new Replacer(new Pattern("\\b(\\d+)\\b"),new ChanceSubstitution("OOBAWOOBA", 0.5, 10)).replace("12 34 56 78"));
        System.out.println(new Replacer(new Pattern("\\b(\\d+)\\b"),new ChanceSubstitution("'$1'", 0.5, 10)).replace("12 34 56 78"));
        System.out.println(new Replacer(new Pattern("\\b(\\d+)\\b"),new ChanceSubstitution("$1$1$1", 0.5, 10)).replace("12 34 56 78"));

        Pattern classGroup = Pattern.compile("(?[[\\pJ]-[\\pS]])");
        System.out.println(classGroup.matches("$"));
        System.out.println(classGroup.matches("a"));
        System.out.println(classGroup.matches("2"));

        System.out.println(Category.Nl.contents());
        System.out.println(Category.IdentifierStart.contains('Ⅹ'));
        System.out.println(Category.IdentifierPart.contains('Ⅹ'));

        System.out.println(Pattern.compile("\\pL\\p{InBasicLatin}\\P{BasicLatin}\\p{Greek}").matches("buζζ"));
        System.out.println(Pattern.compile("\\p{InHiragana}+").matches("ひらがな"));

        String sentence = "Hiragana (ひらがな) is one of two kana\n" +
                "writing systems in use in modern Japan,\n" +
                "the other being katakana (カタカナ).";

        //splits on newlines, underscores, and chinese/japanese characters
        Pattern regularSplitter = Pattern.compile(
                "(?<=\n)|(?=\n)|(?<=_)|(?=_)|" + "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|"
                        + "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|"
                        + "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|"
                        + "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

        //additionally splits on words, so that each word can be arranged individually
        Pattern regularSplitterMultiline = Pattern.compile("(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|" + "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" + "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" + "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|"
        + "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
        for(String s : regularSplitter.split(sentence)){
            System.out.println(s);
        }
        System.out.println();
        for(String s : regularSplitterMultiline.split(sentence)){
            System.out.println(s);
        }
//        for (MatchIterator it = regularSplitter.matcher(sentence).findAll(); it.hasNext(); ) {
//            MatchResult mr = it.next();
//            System.out.println(mr.group(0));
//        }

        java.util.regex.Pattern jurSplitter = java.util.regex.Pattern.compile(
                "(?<=\n)|(?=\n)|(?<=_)|(?=_)|" + "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" + "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" + "(?<=\\p{InCJKUnifiedIdeographs})|(?=\\p{InCJKUnifiedIdeographs})|" + "(?<=\\p{InCJKSymbolsAndPunctuation})|(?=\\p{InCJKSymbolsAndPunctuation})");
//        java.util.regex.Pattern jurSplitter = java.util.regex.Pattern.compile(
//                "(?<=\n)|(?=\n)|(?<=_)|(?=_)|" + "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" + "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" + "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" + "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
        for(String s : jurSplitter.split(sentence)){
            System.out.println(s);
        }
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

    @Test
    public void testReplacementTable()
    {
        String[] data = new String[]{"alpha", "beta", "gamma", "pi", "phi", "omega", "zed", "pillow", "cc", "dd", "pihigammaddic"};
        Replacer rep = Replacer.makeTable("alpha", "A", "beta", "B", "gamma", "G", "pi", "P", "phi", "F", "omega", "O");
        for(String datum : data)
        {
            System.out.println(rep.replace(datum));
        }

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
        java.util.regex.Pattern swear1 = java.util.regex.Pattern.compile("[AaаαАΑΛ][NnийИЙΝ]..?[SsξLlιζzΖ]");
        System.out.println(swear0.matcher("Annals").find());
        System.out.println(swear1.matcher("Annals").find());


    }
    @Test
    public void testMatcherSpeed()
    {
        //Matcher p = new Pattern("[0-9a-fA-F]+").matcher();
        Pattern p = new Pattern("\\p{Lu}\\p{Ll}\\PP\\p{P}"); //"(.)\\1+"
        //Matcher p = new Pattern("000").matcher();
        long time = System.nanoTime(), ctr = 0;
        for (int i = 0; i < STRING_COUNT; i++) {
            if(p.matcher(chars[i], 0, STR_LEN).find())
                ctr++;
        }
        System.out.println(System.nanoTime() - time);
        System.out.println(ctr);
    }

    @Test
    public void testJUSpeed()
    {
        //java.util.regex.Pattern jup = java.util.regex.Pattern.compile("[0-9a-fA-F]+");
        java.util.regex.Pattern jup = java.util.regex.Pattern.compile("\\p{Lu}\\p{Ll}\\PP\\p{P}"); //"(.)\\1+"
        //java.util.regex.Pattern jup = java.util.regex.Pattern.compile("000");
        long time = System.nanoTime(), ctr = 0;

        for (int i = 0; i < STRING_COUNT; i++) {
            if(jup.matcher(strings[i]).find())
                ctr++;
        }
        System.out.println(System.nanoTime() - time);
        System.out.println(ctr);
        /*
        Assert.assertEquals(p, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertTrue(p.matches("1337ca7CAFE"));
        Assert.assertTrue(p3.matches("[0-9a-fA-F]"));
        */
    }
    @Test
    public void testToStringIssues()
    {
        System.out.println(CharacterClass.stringValue0(
                new IntBitSet(new int[]{0, 0x03ff0000, 0x87fffffe, 0x07fffffe, 0, 0, 0, 0})));
        System.out.println(CharacterClass.stringValue0(
                new IntBitSet(new int[]{0, 0xFFFFFFFE, 0x87ffffff, 0x07fffffe, 0, 0, -1, -1})));
    }
    @Test
    public void testBrackets()
    {
        Matcher m = Pattern.compile("({=bracket}\\p{Ps}).+?{\\:/bracket}").matcher("(+ [1 2 3] 10)");
        System.out.println(m.find());
        System.out.println(Arrays.toString(m.groups()));
        m = Pattern.compile("^((?:L(?=[^M]*((?(2)\\2)M)[^R]*(R(?(3)\\3))))+)").matcher("LLLMMMRRR"); //\d*{\mm}\d*{\rr}$
        System.out.println(m.find());
        System.out.println(Arrays.toString(m.groups()));
        m = Pattern.compile("^((?:L(?=([^M]*)({mm}M(?(3)\\3))([^R]*)({rr}R(?(5)\\5))(.*)))+)").matcher("LLLLLMMMMMRRRRR"); //\d*{\mm}\d*{\rr}$
        System.out.println(m.find());
        System.out.println(m.group("mm"));
        System.out.println(m.group(3));
        System.out.println(Arrays.toString(m.groups()));
        // "^(\\((?>[^\\(\\)]+|(?1))*\\))+$"
        // ^((?:L(?=[^M]*(\2?+M)[^R]*(\3?+R)))+)\d+\2\d+\3$
        Pattern tok = Pattern.compile("({=remove};(\\V*))" +
                "|({=code}(?:#({=remove}~)?({=mode}[^\\h\\v\\(\\)\\[\\]\\{\\}\"';#~]+)?)?({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
                "|({=remove}({=bracket}(?:~+)/)(?:[\\d\\D]*?){\\/bracket})" +
                "|({=open}(?:#({=remove}~)?({=mode}[^\\h\\v\\(\\)\\[\\]\\{\\}\"';#~]+)?)?({=bracket}[\\(\\[\\{]))" +
                "|({=close}({=bracket}[\\)\\]\\}]))" +
                "|({=contents}\\.+)" +
                "|({=contents}[^\\h\\v,.\\(\\)\\[\\]\\{\\}\"';#~]+)"
        );
        MatchIterator mi;
        MatchResult mr;
        m = tok.matcher("(+\n1 2; whee! \r\n3)");
        mi = m.findAll();
        while (mi.hasNext())
        {
            mr = mi.next();
            if(mr.isCaptured("remove"))
                continue;
            System.out.println(mr.group("contents"));
            System.out.println(mr.group("mode"));
            System.out.println(mr.group("bracket"));
        }
        System.out.println("\n");
        m = tok.matcher("(=\n  (count 'hey \\@ buddy')\n  9)\n\n#~'this should be ignored' #yes'but this is real'" +
                "\n 'is there anything here?' ~~/ ; woo ; /~~ 'no? ok then... ~/ ohoho /~'");
        mi = m.findAll();
        while (mi.hasNext())
        {
            mr = mi.next();
            if(mr.isCaptured("remove"))
                continue;
            System.out.println(mr.group("contents"));
            System.out.println(mr.group("mode"));
            System.out.println(mr.group("bracket"));
        }
        System.out.println("\n");
        m = tok.matcher("#infix('hey \\@ buddy'.length() == 9)");
        mi = m.findAll();
        while (mi.hasNext())
        {
            mr = mi.next();
            if(mr.isCaptured("remove"))
                continue;
            System.out.println(mr.group("contents"));
            System.out.println(mr.group("mode"));
            System.out.println(mr.group("bracket"));
        }
        
    }
    @Test
    public void testCase() {
        Matcher m = Pattern.compile("({=word}\\pL+)\\PL+((?#){\\!word})").matcher("I love that THAT exists");
        System.out.println(m.find());
        System.out.println(Arrays.toString(m.groups()));
        m = Pattern.compile("({=initial}\\pL)({=rest}[^\\.\\?\\!]+)({=tail}[\\.\\?\\!]+)")
                .matcher("no way I love waffles... they're like the best.");
        System.out.println(m.replaceAll("${!initial}${rest}${tail}"));

        String sentence = "the quick brown fox jumps over the lazy dog.";
        char[] sc = sentence.toCharArray();
        for (int i = 0; i < sc.length; i++) {
            sc[i] = Category.caseUp(sc[i]);
        }
        Assert.assertEquals("Case should both be upper-case",
                sentence.toUpperCase(),
                String.valueOf(sc));
        for (int i = 0; i < sc.length; i++) {
            sc[i] = Category.caseDown(sc[i]);
        }
        Assert.assertEquals("Case should both be lower-case",
                sentence.toLowerCase(),
                String.valueOf(sc));
    }

    @Test
    public void testChanceReplace()
    {
        String[] strings = new String[10000];

        for (int i = 0; i < 10000; i++) {
            strings[i] = exampleASCII();
        }
        Replacer r1 = new Pattern("\\w").replacer(new ChanceSubstitution("!", 0.25, 0)),
                r2 = new Pattern("\\w", "i").replacer(new ChanceSubstitution("!", 0.25, 0));
        StringBuilder sb = new StringBuilder(5000), sb2 = new StringBuilder(5000);
        boolean found;
        for (int i = 0; i < 10000; i++) {
            r1.replace(strings[i], sb);
            r2.replace(strings[i], sb2);
            Assert.assertEquals(sb.toString(), sb2.toString());
        }
        //System.out.println(sb.toString());
        //System.out.println(sb2.toString());
        System.out.println(sb);

        /*
        Assert.assertEquals(p, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertTrue(p.matches("1337ca7CAFE"));
        Assert.assertTrue(p3.matches("[0-9a-fA-F]"));
        */
    }
    @Test
    public void testSerialize()
    {
        Pattern p = new Pattern("[a-z]", "iu"), p2;
        String ser = p.serializeToString();
        p2 = Pattern.deserializeFromString(ser);
        Assert.assertTrue(p.equals(p2));
        Matcher m = p.matcher("A");
        System.out.println(m);
        Assert.assertTrue(m.matches());
        Replacer r = p.replacer("!");
        System.out.println(r);
        System.out.println(r.replace("hey"));
    }

    @Test
    public void testCompatibility(){
        boolean b;
        regexodus.Pattern p;
        java.util.regex.Pattern jp;

        p = regexodus.Pattern.compile("^<<");
        Assert.assertTrue(p.matcher("<<setBgMusic SongOfYarn.mp3>>").find());
        jp = java.util.regex.Pattern.compile("^<<");
        Assert.assertTrue(jp.matcher("<<setBgMusic SongOfYarn.mp3>>").find());

        p = regexodus.Pattern.compile("[a-zA-Z0-9_:.]+");
        Matcher m = p.matcher("<<setBgMusic SongOfYarn.mp3>>");
        Assert.assertTrue(m.find());
        System.out.println(m.group());

        jp = java.util.regex.Pattern.compile("[a-zA-Z0-9_:.]+");
        java.util.regex.Matcher jm = jp.matcher("<<setBgMusic SongOfYarn.mp3>>");
        Assert.assertTrue(jm.find());
        System.out.println(jm.group());

        p = regexodus.Pattern.compile("\\G\\W*(\\w+)");
        m = p.matcher("Why don't we sing the Song of Yarn?");
        b = m.find();
        System.out.println(m.group(1));
        Assert.assertTrue(b);
        b = m.find();
        System.out.println(b ? m.group(1) : "not found");
        b = m.find();
        System.out.println(b ? m.group(1) : "not found");
        b = m.find();
        System.out.println(b ? m.group(1) : "not found");
        b = m.find();
        System.out.println(b ? m.group(1) : "not found");
        Assert.assertTrue(m.find());

        jp = java.util.regex.Pattern.compile("\\G\\W*(\\w+)");
        jm = jp.matcher("Why don't we sing the Song of Yarn?");
        b = jm.find();
        System.out.println(jm.group(1));
        Assert.assertTrue(b);
        b = jm.find();
        System.out.println(b ? jm.group(1) : "not found");
        b = jm.find();
        System.out.println(b ? jm.group(1) : "not found");
        b = jm.find();
        System.out.println(b ? jm.group(1) : "not found");
        b = jm.find();
        System.out.println(b ? jm.group(1) : "not found");
        Assert.assertTrue(jm.find());

        p = regexodus.Pattern.compile("(<=|lte(?!\\w))");
        Assert.assertTrue(p.matcher("A <= B").find());
        jp = java.util.regex.Pattern.compile("(<=|lte(?!\\w))");
        Assert.assertTrue(jp.matcher("A <= B").find());
    }

    @Test
    public void testEquals() {
        String[] patternTexts = new String[]{"[ghjkGHJK]{3}","(\\p{L})\\1\\1","[Ii][iyq]","[Yy]([aiu])\\1","[Rr][uy]+[rh]","[Qq]u[yu]","[^oaei]uch","[Hh][tcszi]?h","[Tt]t[^aeiouy]{2}","[Yy]h([^aeiouy]|$)","([xqy])\\1$","[qi]y$","[szSZrlRL]+?[^aeiouytdfgkcpbmnslrv][rlsz]","[UIuiYy][wy]","^[UIui]e","[AEIOUaeiou]{3}","^([^aeioyl])\\1"};
        for (int i = 1; i < patternTexts.length; i++) {
            String pt = patternTexts[i];
            Pattern pNo = Pattern.compile(patternTexts[i-1]);
            Pattern p0 = Pattern.compile(pt);
            Pattern p1 = Pattern.compile(pt);
            Pattern p2 = Pattern.compile(pt, "i");
            Assert.assertNotEquals(pNo, p0);
            Assert.assertNotEquals(pNo, p1);
            Assert.assertEquals(p0, p1);
            Assert.assertNotEquals(p0, p2);
            p0.matches("aaa");
            Assert.assertNotEquals(pNo, p0);
            Assert.assertNotEquals(pNo, p1);
            Assert.assertEquals(p0, p1);
            Assert.assertNotEquals(p0, p2);
        }
    }

    @Test
    public void testAppendReplacement() {
        String format = "test%d";
        Matcher matcher = Pattern.compile("test").matcher(format);
        TextBuffer buffer = Replacer.wrap(new StringBuilder());
        while (Replacer.replaceStep(matcher, new PerlSubstitution("AAAAAAAAAAAAAAAA"), buffer));
        matcher.getGroup(MatchResult.TARGET, buffer);
        System.out.println("Result: " + buffer);
    }

    @Test
    public void testDoormat() {
        System.out.println(Doormat.format("My name is %s, I have %d toes, my padding is %010d, escape on the nose %%", "Regexodus", 12, 10));
        System.out.println(Doormat.format("Floats? %f Fun. %010f Fantastic. %8.2f Flabbergasting!", 1.2345f, 123.456f, 1.2345));
    }
}
