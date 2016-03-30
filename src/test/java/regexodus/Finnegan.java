package regexodus;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A text generator for producing sentences and/or words in nonsense languages that fit a theme. This does not use an
 * existing word list as a basis for its output, so it may or may not produce existing words occasionally, but you can
 * safely assume it won't generate a meaningful sentence except in the absolute unlikeliest of cases. The name comes
 * from the novel Finnegan's Wake by James Joyce, which employed vaguely similar creative manipulation of language.
 * <br>
 *     "Thaw! The last word in stolentelling!"
 * <br>
 *     -- James Joyce, Finnegan's Wake
 * <br>
 *     "bababadalgharaghtakamminarronnkonnbronntonnerronntuonnthunntrovarrhounawnskawntoohoohoordenenthurnuk!"
 * <br>
 *     -- James Joyce, Finnegan's Wake
 * <br>
 * CC0 Licensed, originally by Tommy Ettinger. This class is meant to be copied into your own source code, not included
 * as a library. There is absolutely no need to attribute me if you use this, and you can change it without obstacle.
 */
public class Finnegan implements Serializable {

    public class RNG implements Serializable {
        private static final long serialVersionUID = 4378460257281186370L;

        /**
         * 2 raised to the 53, - 1.
         */
        private static final long DOUBLE_MASK = (1L << 53) - 1;
        /**
         * 2 raised to the -53.
         */
        private static final double NORM_53 = 1. / (1L << 53);

        public long state;

        public RNG() {
            state = Double.doubleToLongBits(Math.random());
        }

        public RNG(long seed) {
            state = seed;
        }

        /**
         * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
         *
         * @return any long, all 64 bits are random
         */
        public long nextLong() {
            long z = (state += 0x9E3779B97F4A7C15L);
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }

        /**
         * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
         *
         * @return any int, all 32 bits are random
         */
        public int nextInt() {
            return (int) nextLong();
        }

        /**
         * Exclusive on the upper bound n.  The lower bound is 0.
         *
         * @param n the upper bound; should be positive
         * @return a random int less than n and at least equal to 0
         */
        public int nextInt(final int n) {
            if (n <= 0) throw new IllegalArgumentException();
            final int bits = nextInt() >>> 1;
            return bits % n;
        }

        /**
         * Inclusive lower, exclusive upper.
         *
         * @param lower the lower bound, inclusive, can be positive or negative
         * @param upper the upper bound, exclusive, should be positive, must be greater than lower
         * @return a random int at least equal to lower and less than upper
         */
        public int nextInt(final int lower, final int upper) {
            if (upper - lower <= 0) throw new IllegalArgumentException();
            return lower + nextInt(upper - lower);
        }

        /**
         * Exclusive on the upper bound n. The lower bound is 0.
         *
         * @param n the upper bound; should be positive
         * @return a random long less than n
         */
        public long nextLong(final long n) {
            if (n <= 0) throw new IllegalArgumentException();
            //for(;;) {
            final long bits = nextLong() >>> 1;
            return bits % n;
            //long value = bits % n;
            //value = (value < 0) ? -value : value;
            //if ( bits - value + ( n - 1 ) >= 0 ) return value;
            //}
        }

        /**
         * Inclusive lower, exclusive upper.
         *
         * @param lower the lower bound, inclusive, can be positive or negative
         * @param upper the upper bound, exclusive, should be positive, must be greater than lower
         * @return a random long at least equal to lower and less than upper
         */
        public long nextLong(final long lower, final long upper) {
            if (upper - lower <= 0) throw new IllegalArgumentException();
            return lower + nextLong(upper - lower);
        }

        /**
         * Gets a uniform random double in the range [0.0,1.0)
         *
         * @return a random double at least equal to 0.0 and less than 1.0
         */
        public double nextDouble() {
            return (nextLong() & DOUBLE_MASK) * NORM_53;
        }

        /**
         * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
         * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
         *
         * @param outer the exclusive outer bound, can be negative
         * @return a random double between 0.0 (inclusive) and outer (exclusive)
         */
        public double nextDouble(final double outer) {
            return nextDouble() * outer;
        }

        public <T> T getRandomElement(T[] array) {
            if (array.length < 1) {
                return null;
            }
            return array[nextInt(array.length)];
        }

        /**
         * Shuffle an array using the Fisher-Yates algorithm.
         *
         * @param elements an array of T; will not be modified
         * @param <T>      can be any non-primitive type.
         * @return a shuffled copy of elements
         */
        public <T> T[] shuffle(T[] elements) {
            T[] array = elements.clone();
            int n = array.length;
            for (int i = 0; i < n; i++) {
                int r = i + nextInt(n - i);
                T t = array[r];
                array[r] = array[i];
                array[i] = t;
            }
            return array;
        }

        public long getState() {
            return state;
        }

        public void setState(long state) {
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RNG rng = (RNG) o;
            return state == rng.state;
        }

        @Override
        public int hashCode() {
            return (int) (state ^ (state >>> 32));
        }

        @Override
        public String toString() {
            return "RNG{" +
                    "state=" + state +
                    '}';
        }
    }

    private static final long serialVersionUID = -2578460257281186352L;
    public final String[] openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants,
            vowelSplitters, closingSyllables;
    public boolean clean;
    public final LinkedHashMap<Integer, Double> syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    public final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency;
    protected final Pattern[] sanityChecks;
    protected static final Pattern repeats = Pattern.compile("(.)\\1+");
    public static final Pattern[]
            vulgarChecks = new Pattern[]
            {
                    Pattern.compile("[SsξCcсςС][HhнН].*[DdtTтτТΤf]"),
                    Pattern.compile("([PpрρРΡ][HhнН])|[KkкκКΚFfDdCcсςС].{1,4}[KkкκКΚCcсςСxхжχХЖΧ]"), // lots of these end in a 'k' sound, huh
                    Pattern.compile("[BbъыбвβЪЫБВΒ]..?.?[CcсςС][HhнН]"),
                    Pattern.compile("[WwшщψШЩHhнН]..?[RrяЯ]"),
                    Pattern.compile("[TtтτТΤ]..?[tTтτТΤ]"),
                    Pattern.compile("([PpрρРΡ][HhнН])|[Ff]..?[RrяЯ][tTтτТΤ]"),
                    Pattern.compile("([Ssξ][HhнН])|[jJ][iτιΙ].?[sξzΖ]"),
                    Pattern.compile("[AaаαАΑΛ][NnийИЙΝ]..?[SsξlιζzΖ]"),
                    Pattern.compile("[AaаαАΑΛ][sξ][sξ]"),
                    Pattern.compile(".[uμυν][HhнН]?[nийИЙΝ]+[tTтτТΤ]"),
                    Pattern.compile("[NnFf]..?[Gg]"), // might as well remove two possible slurs with one check
                    Pattern.compile("[PpрρРΡ][eеёзξεЕЁЗΞΕΣioоюσοОЮΟuμυν][eеёзξεЕЁЗΞΕΣoоюσοОЮΟs]"), // the grab bag of juvenile words
                    Pattern.compile("[MmмМΜ]..?[RrяЯ].?d"), // should pick up the #1 obscenity from Spanish and French
                    Pattern.compile("[Gg][HhнН]?[aAаαАΑΛeEеёзξεЕЁЗΞΕΣ][yYуλγУΥeEеёзξεЕЁЗΞΕΣ]") // could be inappropriate for random text
            },
            englishSanityChecks = new Pattern[]
                    {
                            Pattern.compile("[AEIOUaeiou]{3}"),
                            Pattern.compile("(\\w)\\1\\1"),
                            Pattern.compile("(.)\\1(.)\\2"),
                            Pattern.compile("[Aa][ae]"),
                            Pattern.compile("[Uu][umlkj]"),
                            Pattern.compile("[Ii][iyqkhrl]"),
                            Pattern.compile("[Oo][c]"),
                            Pattern.compile("[Yy][aeiou]{2}"),
                            Pattern.compile("[Rr][aeiouy]+[xrhp]"),
                            Pattern.compile("[Qq]u[yu]"),
                            Pattern.compile("[^oai]uch"),
                            Pattern.compile("[^tcsz]hh"),
                            Pattern.compile("[Hh][tcszi]h"),
                            Pattern.compile("[Tt]t[^aeiouy]{2}"),
                            Pattern.compile("[IYiy]h[^aeiouy ]"),
                            Pattern.compile("[szSZrlRL][^aeiou][rlsz]"),
                            Pattern.compile("[UIuiYy][wy]"),
                            Pattern.compile("^[UIui][ae]"),
                            Pattern.compile("q$")
                    },
            japaneseSanityChecks  = new Pattern[]
                    {
                            Pattern.compile("[AEIOUaeiou]{3}"),
                            Pattern.compile("(\\w)\\1\\1"),
                            Pattern.compile("[Tt]s[^u]"),
                            Pattern.compile("[Ff][^u]"),
                            Pattern.compile("[Yy][^auo]"),
                            Pattern.compile("[Tt][ui]"),
                            Pattern.compile("[SsZzDd]i"),
                            Pattern.compile("[Hh]u"),
                    };
    public static final Replacer[]
            accentFinders = new Replacer[]
            {
                    Pattern.compile("[àáâãäåæāăąǻǽ]").replacer("a"),
                    Pattern.compile("[èéêëēĕėęě]").replacer("e"),
                    Pattern.compile("[ìíîïĩīĭįı]").replacer("i"),
                    Pattern.compile("[òóôõöøōŏőœǿ]").replacer("o"),
                    Pattern.compile("[ùúûüũūŭůűų]").replacer("u"),
                    Pattern.compile("[ÀÁÂÃÄÅÆĀĂĄǺǼ]").replacer("A"),
                    Pattern.compile("[ÈÉÊËĒĔĖĘĚ]").replacer("E"),
                    Pattern.compile("[ÌÍÎÏĨĪĬĮI]").replacer("I"),
                    Pattern.compile("[ÒÓÔÕÖØŌŎŐŒǾ]").replacer("O"),
                    Pattern.compile("[ÙÚÛÜŨŪŬŮŰŲ]").replacer("U"),

                    Pattern.compile("[çćĉċč]").replacer("c"),
                    Pattern.compile("[þðďđ]").replacer("d"),
                    Pattern.compile("[ſƒ]").replacer("f"),
                    Pattern.compile("[ĝğġģ]").replacer("g"),
                    Pattern.compile("[ĥħ]").replacer("h"),
                    Pattern.compile("[ĵȷ]").replacer("j"),
                    Pattern.compile("[ķĸ]").replacer("k"),
                    Pattern.compile("[ℓĺļľŀł]").replacer("l"),
                    Pattern.compile("[ñńņňŉŋ]").replacer("n"),
                    Pattern.compile("[ŕŗř]").replacer("r"),
                    Pattern.compile("[śŝşšș]").replacer("s"),
                    Pattern.compile("[ţťŧț]").replacer("t"),
                    Pattern.compile("[ŵẁẃẅ]").replacer("w"),
                    Pattern.compile("[ýÿŷỳ]").replacer("y"),
                    Pattern.compile("[źżž]").replacer("z"),
                    Pattern.compile("[ÇĆĈĊČ]").replacer("C"),
                    Pattern.compile("[ÞÐĎĐ]").replacer("D"),
                    Pattern.compile("Ƒ").replacer("F"),
                    Pattern.compile("[ĜĞĠĢ]").replacer("G"),
                    Pattern.compile("[ĤĦ]").replacer("H"),
                    Pattern.compile("Ĵ").replacer("J"),
                    Pattern.compile("Ķ").replacer("K"),
                    Pattern.compile("[ĹĻĽĿŁ]").replacer("L"),
                    Pattern.compile("[ÑŃŅŇŊ]").replacer("N"),
                    Pattern.compile("[ŔŖŘ]").replacer("R"),
                    Pattern.compile("[ŚŜŞŠȘ]").replacer("S"),
                    Pattern.compile("[ŢŤŦȚ]").replacer("T"),
                    Pattern.compile("[ŴẀẂẄ]").replacer("W"),
                    Pattern.compile("[ÝŸŶỲ]").replacer("Y"),
                    Pattern.compile("[ŹŻŽ]").replacer("Z"),

            };

    public RNG rng;
    public static final char[][] accentedVowels = new char[][]{
            new char[]{
                    'à', 'á', 'â', 'ã', 'ä', 'å', 'æ', 'ā', 'ă', 'ą', 'ǻ', 'ǽ'
            },
            new char[]{
                    'è', 'é', 'ê', 'ë', 'ē', 'ĕ', 'ė', 'ę', 'ě'
            },
            new char[]{
                    'ì', 'í', 'î', 'ï', 'ĩ', 'ī', 'ĭ', 'į', 'ı',
            },
            new char[]{
                    'ò', 'ó', 'ô', 'õ', 'ö', 'ø', 'ō', 'ŏ', 'ő', 'œ', 'ǿ'
            },
            new char[]{
                    'ù', 'ú', 'û', 'ü', 'ũ', 'ū', 'ŭ', 'ů', 'ű', 'ų'
            }
    },
            accentedConsonants = new char[][]
                    {
                            new char[]{
                                    'b'
                            },
                            new char[]{
                                    'c', 'ç', 'ć', 'ĉ', 'ċ', 'č',
                            },
                            new char[]{
                                    'd', 'þ', 'ð', 'ď', 'đ',
                            },
                            new char[]{
                                    'f', 'ſ', 'ƒ',
                            },
                            new char[]{
                                    'g', 'ĝ', 'ğ', 'ġ', 'ģ',
                            },
                            new char[]{
                                    'h', 'ĥ', 'ħ',
                            },
                            new char[]{
                                    'j', 'ĵ', 'ȷ',
                            },
                            new char[]{
                                    'k', 'ķ', 'ĸ',
                            },
                            new char[]{
                                    'l', 'ℓ', 'ĺ', 'ļ', 'ľ', 'ŀ', 'ł',
                            },
                            new char[]{
                                    'm',
                            },
                            new char[]{
                                    'n', 'ñ', 'ń', 'ņ', 'ň', 'ŉ', 'ŋ',
                            },
                            new char[]{
                                    'p',
                            },
                            new char[]{
                                    'q',
                            },
                            new char[]{
                                    'r', 'ŕ', 'ŗ', 'ř',
                            },
                            new char[]{
                                    's', 'ś', 'ŝ', 'ş', 'š', 'ș',
                            },
                            new char[]{
                                    't', 'ţ', 'ť', 'ŧ', 'ț',
                            },
                            new char[]{
                                    'v',
                            },
                            new char[]{
                                    'w', 'ŵ', 'ẁ', 'ẃ', 'ẅ',
                            },
                            new char[]{
                                    'x',
                            },
                            new char[]{
                                    'y', 'ý', 'ÿ', 'ŷ', 'ỳ',
                            },
                            new char[]{
                                    'z', 'ź', 'ż', 'ž',
                            },
                    };

    /**
     * Removes accented Latin-script characters from a string; if the "base" characters are non-English anyway then the
     * result won't be an ASCII string, but otherwise it probably will be.
     *
     * @param str a string that may contain accented Latin-script characters
     * @return a string with all accented characters replaced with their (possibly ASCII) counterparts
     */
    public String removeAccents(String str) {
        String alteredString = str;
        for (int i = 0; i < accentFinders.length; i++) {
            alteredString = accentFinders[i].replace(alteredString);
        }
        return alteredString;
    }

    /**
     * Ia! Ia! Cthulhu Rl'yeh ftaghn! Useful for generating cultist ramblings or unreadable occult texts.
     * <br>
     * Zvrugg pialuk, ya'as irlemrugle'eith iposh hmo-es nyeighi, glikreirk shaivro'ei!
     */
    public static final Finnegan LOVECRAFT = new Finnegan(
            new String[]{"a", "i", "o", "e", "u", "a", "i", "o", "e", "u", "ia", "ai", "aa", "ei"},
            new String[]{},
            new String[]{"s", "t", "k", "n", "y", "p", "k", "l", "g", "gl", "th", "sh", "ny", "ft", "hm", "zvr", "cth"},
            new String[]{"h", "gl", "gr", "nd", "mr", "vr", "kr"},
            new String[]{"l", "p", "s", "t", "n", "k", "g", "x", "rl", "th", "gg", "gh", "ts", "lt", "rk", "kh", "sh", "ng", "shk"},
            new String[]{"aghn", "ulhu", "urath", "oigor", "alos", "'yeh", "achtal", "urath", "ikhet", "adzek"},
            new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2}, 0.4, 0.31, 0.07, 0.04, null, true);
    /**
     * Imitation English; may seem closer to Dutch in some generated text, and is not exactly the best imitation.
     * Should seem pretty fake to many readers; does not filter out dictionary words. If you want to
     * avoid generating certain words, you can subclass Finnegan and modify the word() method.
     * <br>
     * Mont tiste frot; mousation hauddes?
     * Lily wrely stiebes; flarrousseal gapestist.
     */
    public static final Finnegan ENGLISH = new Finnegan(
            new String[]{
                    "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                    "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                    "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                    "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                    "au", "ai", "ai", "ou", "ea", "ie", "io", "ei",
            },
            new String[]{"u", "u", "oa", "oo", "oo", "oo", "ee", "ee", "ee", "ee",},
            new String[]{
                    "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gl", "gr", "h", "j", "k", "l", "m", "n",
                    "p", "pl", "pr", "qu", "r", "s", "sh", "sk", "st", "sp", "sl", "sm", "sn", "t", "tr", "th", "thr", "v", "w", "y", "z",
                    "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gr", "h", "j", "k", "l", "m", "n",
                    "p", "pl", "pr", "r", "s", "sh", "st", "sp", "sl", "t", "tr", "th", "w", "y",
                    "b", "br", "c", "ch", "d", "dr", "f", "g", "h", "j", "l", "m", "n",
                    "p", "r", "s", "sh", "st", "sl", "t", "tr", "th",
                    "b", "d", "f", "g", "h", "l", "m", "n",
                    "p", "r", "s", "sh", "t", "th",
                    "b", "d", "f", "g", "h", "l", "m", "n",
                    "p", "r", "s", "sh", "t", "th",
                    "r", "s", "t", "l", "n",
                    "str", "spr", "spl", "wr", "kn", "kn", "gn",
            },
            new String[]{"x", "cst", "bs", "ff", "lg", "g", "gs",
                    "ll", "ltr", "mb", "mn", "mm", "ng", "ng", "ngl", "nt", "ns", "nn", "ps", "mbl", "mpr",
                    "pp", "ppl", "ppr", "rr", "rr", "rr", "rl", "rtn", "ngr", "ss", "sc", "rst", "tt", "tt", "ts", "ltr", "zz"
            },
            new String[]{"b", "rb", "bb", "c", "rc", "ld", "d", "ds", "dd", "f", "ff", "lf", "rf", "rg", "gs", "ch", "lch", "rch", "tch",
                    "ck", "ck", "lk", "rk", "l", "ll", "lm", "m", "rm", "mp", "n", "nk", "nch", "nd", "ng", "ng", "nt", "ns", "lp", "rp",
                    "p", "r", "rn", "rts", "s", "s", "s", "s", "ss", "ss", "st", "ls", "t", "t", "ts", "w", "wn", "x", "ly", "lly", "z",
                    "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "t", "w",
            },
            new String[]{"ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ay", "ay", "ey", "oy", "ay", "ay", "ey", "oy",
                    "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                    "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                    "ily", "ily", "ily", "adly", "owly", "oorly", "ardly", "iedly",
            },
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{7, 8, 4, 1}, 0.22, 0.1, 0.0, 0.25, englishSanityChecks, true);
    /**
     * Imitation ancient Greek, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Psuilas alor; aipeomarta le liaspa...
     */
    public static final Finnegan GREEK_ROMANIZED = new Finnegan(
            new String[]{"a", "a", "a", "o", "o", "o", "e", "e", "i", "i", "i", "au", "ai", "ai", "oi", "oi", "ia", "io", "ou", "ou", "eo", "ei"},
            new String[]{"ui", "ei"},
            new String[]{"rh", "s", "z", "t", "t", "k", "ch", "n", "th", "kth", "m", "p", "ps", "b", "l", "kr", "g", "phth"},
            new String[]{"lph", "pl", "l", "l", "kr", "nch", "nx", "ps"},
            new String[]{"s", "p", "t", "ch", "n", "m", "s", "p", "t", "ch", "n", "m", "b", "g", "st", "rst", "rt", "sp", "rk", "ph", "x", "z", "nk", "ng", "th"},
            new String[]{"os", "os", "is", "us", "um", "eum", "ium", "iam", "us", "um", "es", "anes", "eros", "or", "ophon", "on", "otron"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3, null, true);
    /**
     * Imitation ancient Greek, using the original Greek alphabet. People may try to translate it and get gibberish.
     * Make sure the font you use to render this supports the Greek alphabet!
     * <br>
     * Ψυιλασ αλορ; αιπεομαρτα λε λιασπα...
     */
    public static final Finnegan GREEK_AUTHENTIC = new Finnegan(
            new String[]{"α", "α", "α", "ο", "ο", "ο", "ε", "ε", "ι", "ι", "ι", "αυ", "αι", "αι", "οι", "οι", "ια", "ιο", "ου", "ου", "εο", "ει"},
            new String[]{"υι", "ει"},
            new String[]{"ρ", "σ", "ζ", "τ", "τ", "κ", "χ", "ν", "θ", "κθ", "μ", "π", "ψ", "β", "λ", "κρ", "γ", "ϕθ"},
            new String[]{"λϕ", "πλ", "λ", "λ", "κρ", "γχ", "γξ", "ψ"},
            new String[]{"σ", "π", "τ", "χ", "ν", "μ", "σ", "π", "τ", "χ", "ν", "μ", "β", "γ", "στ", "ρστ", "ρτ", "σπ", "ρκ", "ϕ", "ξ", "ζ", "γκ", "γγ", "θ"},
            new String[]{"ος", "ος", "ις", "υς", "υμ", "ευμ", "ιυμ", "ιαμ", "υς", "υμ", "ες", "ανες", "ερος", "ορ", "οϕον", "ον", "οτρον"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3, null, true);

    /**
     * Imitation modern French, using (too many of) the accented vowels that are present in the language. Translating it
     * will produce gibberish if it produces anything at all.
     * <br>
     * Fa veau, ja ri avé re orçe jai braï aisté.
     */
    public static final Finnegan FRENCH = new Finnegan(
            new String[]{"a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "ai", "oi", "oui", "au", "œu", "ou"
            },
            new String[]{
                    "ai", "aie", "aou", "eau", "oi", "oui", "oie", "eu", "eu",
                    "à", "â", "ai", "aî", "aï", "aie", "aou", "aoû", "au", "ay", "e", "é", "ée", "è",
                    "ê", "eau", "ei", "eî", "eu", "eû", "i", "î", "ï", "o", "ô", "oe", "oê", "oë", "œu",
                    "oi", "oie", "oï", "ou", "oû", "oy", "u", "û", "ue"
            },
            new String[]{"tr", "ch", "m", "b", "b", "br", "j", "j", "j", "j", "g", "t", "t", "t", "c", "d", "f", "f", "h", "n", "l", "l",
                    "s", "s", "s", "r", "r", "r", "v", "v", "p", "pl", "pr", "bl", "br", "dr", "gl", "gr"},
            new String[]{"cqu", "gu", "qu", "rqu", "nt", "ng", "ngu", "mb", "ll", "nd", "ndr", "nct", "st",
                    "xt", "mbr", "pl", "g", "gg", "ggr", "gl",
                    "m", "m", "mm", "v", "v", "f", "f", "f", "ff", "b", "b", "bb", "d", "d", "dd", "s", "s", "s", "ss", "ss", "ss",
                    "cl", "cr", "ng", "ç", "ç", "rç"},
            new String[]{},
            new String[]{"e", "e", "e", "e", "e", "é", "é", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es", "es",
                    "e", "e", "e", "e", "e", "é", "é", "er", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es",
                    "e", "e", "e", "e", "e", "é", "é", "é", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es",
                    "ent", "em", "en", "en", "aim", "ain", "an", "oin", "ien", "iere", "ors", "anse",
                    "ombs", "ommes", "ancs", "ends", "œufs", "erfs", "ongs", "aps", "ats", "ives", "ui", "illes",
                    "aen", "aon", "am", "an", "eun", "ein", "age", "age", "uile", "uin", "um", "un", "un", "un",
                    "aille", "ouille", "eille", "ille", "eur", "it", "ot", "oi", "oi", "oi", "aire", "om", "on", "on",
                    "im", "in", "in", "ien", "ien", "ion", "il", "eil", "oin", "oint", "iguïté", "ience", "incte",
                    "ang", "ong", "acré", "eau", "ouche", "oux", "oux", "ect", "ecri", "agne", "uer", "aix", "eth", "ut", "ant",
                    "anc", "anc", "anche", "ioche", "eaux", "ive", "eur", "ancois", "ecois"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{18, 7, 2}, 0.35, 1.0, 0.0, 0.55, null, true);

    /**
     * Imitation modern Russian, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Zhydotuf ruts pitsas, gogutiar shyskuchebab - gichapofeglor giunuz ieskaziuzhin.
     */
    public static final Finnegan RUSSIAN_ROMANIZED = new Finnegan(
            new String[]{"a", "e", "e", "i", "i", "o", "u", "ie", "y", "e", "iu", "ia", "y", "a", "a", "o", "u"},
            new String[]{},
            new String[]{"b", "v", "g", "d", "k", "l", "p", "r", "s", "t", "f", "kh", "ts",
                    "b", "v", "g", "d", "k", "l", "p", "r", "s", "t", "f", "kh", "ts",
                    "b", "v", "g", "d", "k", "l", "p", "r", "s", "t", "f",
                    "zh", "m", "n", "z", "ch", "sh", "shch",
                    "br", "sk", "tr", "bl", "gl", "kr", "gr"},
            new String[]{"bl", "br", "pl", "dzh", "tr", "gl", "gr", "kr"},
            new String[]{"b", "v", "g", "d", "zh", "z", "k", "l", "m", "n", "p", "r", "s", "t", "f", "kh", "ts", "ch", "sh",
                    "v", "f", "sk", "sk", "sk", "s", "b", "d", "d", "n", "r", "r"},
            new String[]{"odka", "odna", "usk", "ask", "usky", "ad", "ar", "ovich", "ev", "ov", "of", "agda", "etsky", "ich", "on", "akh", "iev", "ian"},
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{4, 5, 6, 5, 3, 1}, 0.1, 0.2, 0.0, 0.12, englishSanityChecks, true);


    /**
     * Imitation modern Russian, using the authentic Cyrillic alphabet used in Russia and other countries.
     * Make sure the font you use to render this supports the Cyrillic alphabet!
     * <br>
     * Жыдотуф руц пйцас, гогутяр шыскучэбаб - гйчапофёглор гюнуз ъсказюжин.
     */
    public static final Finnegan RUSSIAN_AUTHENTIC = new Finnegan(
            new String[]{"а", "е", "ё", "и", "й", "о", "у", "ъ", "ы", "э", "ю", "я", "ы", "а", "а", "о", "у"},
            new String[]{},
            new String[]{"б", "в", "г", "д", "к", "л", "п", "р", "с", "т", "ф", "х", "ц",
                    "б", "в", "г", "д", "к", "л", "п", "р", "с", "т", "ф", "х", "ц",
                    "б", "в", "г", "д", "к", "л", "п", "р", "с", "т", "ф",
                    "ж", "м", "н", "з", "ч", "ш", "щ",
                    "бр", "ск", "тр", "бл", "гл", "кр", "гр"},
            new String[]{"бл", "бр", "пл", "дж", "тр", "гл", "гр", "кр"},
            new String[]{"б", "в", "г", "д", "ж", "з", "к", "л", "м", "н", "п", "р", "с", "т", "ф", "х", "ц", "ч", "ш",
                    "в", "ф", "ск", "ск", "ск", "с", "б", "д", "д", "н", "р", "р"},
            new String[]{"одка", "одна", "уск", "аск", "ускы", "ад", "ар", "овйч", "ев", "ов", "оф", "агда", "ёцкы", "йч", "он", "ах", "ъв", "ян"},
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{4, 5, 6, 5, 3, 1}, 0.1, 0.2, 0.0, 0.12, null, true);

    /**
     * Imitation Japanese, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Narurehyounan nikase keho...
     */
    public static final Finnegan JAPANESE_ROMANIZED = new Finnegan(
            new String[]{"a", "a", "a", "a", "e", "e", "i", "i", "i", "i", "o", "o", "o", "u", "ou", "u", "ai", "ai"},
            new String[]{},
            new String[]{"k", "ky", "s", "sh", "t", "ts", "ch", "n", "ny", "h", "f", "hy", "m", "my", "y", "r", "ry", "g",
                    "gy", "z", "j", "d", "b", "by", "p", "py",
                    "k", "t", "n", "s", "k", "t", "n", "s", "sh", "sh", "g", "r", "b",
                    "k", "t", "n", "s", "k", "t", "n", "s", "sh", "sh", "g", "r", "b",
                    "k", "t", "n", "s", "k", "t", "n", "s", "sh", "sh", "ch", "ry", "ts"
            },
            new String[]{"k", "ky", "s", "sh", "t", "ts", "ch", "n", "ny", "h", "f", "hy", "m", "my", "y", "r", "ry", "g",
                    "gy", "z", "j", "d", "b", "by", "p", "py",
                    "k", "t", "n", "s", "k", "t", "n", "s", "sh", "sh", "y", "j", "p", "r", "d",
                    "k", "t", "n", "s", "k", "t", "n", "s", "sh", "sh", "y", "j", "p", "r", "d",
                    "k", "t", "n", "s", "f", "g", "z", "b", "d", "ts",
                    "nn", "nn", "nn", "nd", "nz", "", "mm", "kk", "kk", "tt", "ss", "ssh", "tch"},
            new String[]{"n"},
            new String[]{},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 4, 5, 4, 3}, 0.3, 0.9, 0.0, 0.0, japaneseSanityChecks, true);

    /**
     * A mix of four different languages, using only ASCII characters, that is meant for generating single words for
     * creature or place names in fantasy settings.
     * <br>
     * Adeni, Sainane, Caneros, Sune, Alade, Tidifi, Muni, Gito, Lixoi, Bovi...
     */
    public static final Finnegan FANTASY_NAME = GREEK_ROMANIZED.mix(
            RUSSIAN_ROMANIZED.mix(
                    FRENCH.removeAccents().mix(
                            JAPANESE_ROMANIZED, 0.5), 0.85), 0.925);
    /**
     * A mix of four different languages with some accented characters added onto an ASCII base, that can be good for
     * generating single words for creature or place names in fantasy settings that should have a "fancy" feeling from
     * having unnecessary accents added primarily for visual reasons.
     * <br>
     * Askieno, Blarcīnũn, Mēmida, Zizhounkô, Blęrinaf, Zemĭ, Mónazôr, Renerstă, Uskus, Toufounôr...
     */
    public static final Finnegan FANCY_FANTASY_NAME = FANTASY_NAME.addAccents(0.47, 0.07);

    /**
     * Zero-arg constructor for a Finnegan; produces a Finnegan equivalent to Finnegan.ENGLISH .
     */
    public Finnegan() {
        this(
                new String[]{
                        "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                        "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                        "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                        "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                        "au", "ai", "ai", "ou", "ea", "ie", "io", "ei",
                },
                new String[]{"u", "u", "oa", "oo", "oo", "oo", "ee", "ee", "ee", "ee",},
                new String[]{
                        "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gl", "gr", "h", "j", "k", "l", "m", "n",
                        "p", "pl", "pr", "qu", "r", "s", "sh", "sk", "st", "sp", "sl", "sm", "sn", "t", "tr", "th", "thr", "v", "w", "y", "z",
                        "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gr", "h", "j", "k", "l", "m", "n",
                        "p", "pl", "pr", "r", "s", "sh", "st", "sp", "sl", "t", "tr", "th", "w", "y",
                        "b", "br", "c", "ch", "d", "dr", "f", "g", "h", "j", "l", "m", "n",
                        "p", "r", "s", "sh", "st", "sl", "t", "tr", "th",
                        "b", "d", "f", "g", "h", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "b", "d", "f", "g", "h", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "r", "s", "t", "l", "n",
                        "str", "spr", "spl", "wr", "kn", "kn", "gn",
                },
                new String[]{"x", "cst", "bs", "ff", "lg", "g", "gs",
                        "ll", "ltr", "mb", "mn", "mm", "ng", "ng", "ngl", "nt", "ns", "nn", "ps", "mbl", "mpr",
                        "pp", "ppl", "ppr", "rr", "rr", "rr", "rl", "rtn", "ngr", "ss", "sc", "rst", "tt", "tt", "ts", "ltr", "zz"
                },
                new String[]{"b", "rb", "bb", "c", "rc", "ld", "d", "ds", "dd", "f", "ff", "lf", "rf", "rg", "gs", "ch", "lch", "rch", "tch",
                        "ck", "ck", "lk", "rk", "l", "ll", "lm", "m", "rm", "mp", "n", "nk", "nch", "nd", "ng", "ng", "nt", "ns", "lp", "rp",
                        "p", "r", "rn", "rts", "s", "s", "s", "s", "ss", "ss", "st", "ls", "t", "t", "ts", "w", "wn", "x", "ly", "lly", "z",
                        "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "t", "w",
                },
                new String[]{"ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                        "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                        "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                        "ay", "ay", "ey", "oy", "ay", "ay", "ey", "oy",
                        "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                        "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                        "ily", "ily", "ily", "adly", "owly", "oorly", "ardly", "iedly",
                },
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{7, 8, 4, 1}, 0.22, 0.1, 0.0, 0.25, englishSanityChecks, true);
    }

    /**
     * This is a very complicated constructor! Maybe look at the calls to this to initialize static members of this
     * class, LOVECRAFT and GREEK_ROMANIZED.
     *
     * @param openingVowels        String array where each element is a vowel or group of vowels that may appear at the start
     *                             of a word or in the middle; elements may be repeated to make them more common
     * @param midVowels            String array where each element is a vowel or group of vowels that may appear in the
     *                             middle of the word; all openingVowels are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param openingConsonants    String array where each element is a consonant or consonant cluster that can appear
     *                             at the start of a word; elements may be repeated to make them more common
     * @param midConsonants        String array where each element is a consonant or consonant cluster than can appear
     *                             between vowels; all closingConsonants are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param closingConsonants    String array where each element is a consonant or consonant cluster than can appear
     *                             at the end of a word; elements may be repeated to make them more common
     * @param closingSyllables     String array where each element is a syllable starting with a vowel and ending in
     *                             whatever the word should end in; elements may be repeated to make them more common
     * @param vowelSplitters       String array where each element is a mark that goes between vowels, so if "-" is in this,
     *                             then "a-a" may be possible; elements may be repeated to make them more common
     * @param syllableLengths      int array where each element is a possible number of syllables a word can use; closely
     *                             tied to syllableFrequencies
     * @param syllableFrequencies  double array where each element corresponds to an element in syllableLengths and
     *                             represents how often each syllable count should appear relative to other counts; there
     *                             is no need to restrict the numbers to add up to any other number
     * @param vowelStartFrequency  a double between 0.0 and 1.0 that determines how often words start with vowels;
     *                             higher numbers yield more words starting with vowels
     * @param vowelEndFrequency    a double between 0.0 and 1.0 that determines how often words end with vowels; higher
     *                             numbers yield more words ending in vowels
     * @param vowelSplitFrequency  a double between 0.0 and 1.0 that, if vowelSplitters is not empty, determines how
     *                             often a vowel will be split into two vowels separated by one of those splitters
     * @param syllableEndFrequency a double between 0.0 and 1.0 that determines how often an element of
     *                             closingSyllables is used instead of ending normally
     */
    public Finnegan(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                    String[] midConsonants, String[] closingConsonants, String[] closingSyllables, String[] vowelSplitters,
                    int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                    double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency) {
        this(openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants, closingSyllables,
                vowelSplitters, syllableLengths, syllableFrequencies, vowelStartFrequency, vowelEndFrequency,
                vowelSplitFrequency, syllableEndFrequency, englishSanityChecks, true);
    }

    /**
     * This is a very complicated constructor! Maybe look at the calls to this to initialize static members of this
     * class, LOVECRAFT and GREEK_ROMANIZED.
     *
     * @param openingVowels        String array where each element is a vowel or group of vowels that may appear at the start
     *                             of a word or in the middle; elements may be repeated to make them more common
     * @param midVowels            String array where each element is a vowel or group of vowels that may appear in the
     *                             middle of the word; all openingVowels are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param openingConsonants    String array where each element is a consonant or consonant cluster that can appear
     *                             at the start of a word; elements may be repeated to make them more common
     * @param midConsonants        String array where each element is a consonant or consonant cluster than can appear
     *                             between vowels; all closingConsonants are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param closingConsonants    String array where each element is a consonant or consonant cluster than can appear
     *                             at the end of a word; elements may be repeated to make them more common
     * @param closingSyllables     String array where each element is a syllable starting with a vowel and ending in
     *                             whatever the word should end in; elements may be repeated to make them more common
     * @param vowelSplitters       String array where each element is a mark that goes between vowels, so if "-" is in this,
     *                             then "a-a" may be possible; elements may be repeated to make them more common
     * @param syllableLengths      int array where each element is a possible number of syllables a word can use; closely
     *                             tied to syllableFrequencies
     * @param syllableFrequencies  double array where each element corresponds to an element in syllableLengths and
     *                             represents how often each syllable count should appear relative to other counts; there
     *                             is no need to restrict the numbers to add up to any other number
     * @param vowelStartFrequency  a double between 0.0 and 1.0 that determines how often words start with vowels;
     *                             higher numbers yield more words starting with vowels
     * @param vowelEndFrequency    a double between 0.0 and 1.0 that determines how often words end with vowels; higher
     *                             numbers yield more words ending in vowels
     * @param vowelSplitFrequency  a double between 0.0 and 1.0 that, if vowelSplitters is not empty, determines how
     *                             often a vowel will be split into two vowels separated by one of those splitters
     * @param syllableEndFrequency a double between 0.0 and 1.0 that determines how often an element of
     *                             closingSyllables is used instead of ending normally
     * @param sane true to perform sanity checks for pronounce-able sounds to most English speakers, replacing many
     *             words that are impossible to say; slows down generation slightly, irrelevant for non-Latin alphabets
     * @param clean true to perform vulgarity/obscenity checks on the word, replacing it if it is too close to a
     *              common English vulgarity, obscenity, or slur/epithet; slows down generation slightly
     */
    public Finnegan(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                    String[] midConsonants, String[] closingConsonants, String[] closingSyllables, String[] vowelSplitters,
                    int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                    double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency,
                    Pattern[] sane, boolean clean) {
        rng = new RNG(hash64(openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants,
                closingSyllables, vowelSplitters) ^ hash64(syllableLengths)
                ^ (hash64(syllableFrequencies) << 31) ^
                Double.doubleToLongBits(vowelStartFrequency + 19.0 * (vowelEndFrequency + 19.0 * (vowelSplitFrequency
                        + 19.0 * syllableEndFrequency))));
        this.openingVowels = openingVowels;
        this.midVowels = new String[openingVowels.length + midVowels.length];
        System.arraycopy(midVowels, 0, this.midVowels, 0, midVowels.length);
        System.arraycopy(openingVowels, 0, this.midVowels, midVowels.length, openingVowels.length);
        this.openingConsonants = openingConsonants;
        this.midConsonants = new String[midConsonants.length + closingConsonants.length];
        System.arraycopy(midConsonants, 0, this.midConsonants, 0, midConsonants.length);
        System.arraycopy(closingConsonants, 0, this.midConsonants, midConsonants.length, closingConsonants.length);
        this.closingConsonants = closingConsonants;
        this.vowelSplitters = vowelSplitters;
        this.closingSyllables = closingSyllables;

        this.syllableFrequencies = new LinkedHashMap<Integer, Double>(syllableLengths.length);
        for (int i = 0; i < syllableLengths.length && i < syllableFrequencies.length; i++) {
            this.syllableFrequencies.put(syllableLengths[i], syllableFrequencies[i]);
        }
        for (Double freq : this.syllableFrequencies.values()) {
            totalSyllableFrequency += freq;
        }
        if (vowelStartFrequency > 1.0)
            this.vowelStartFrequency = 1.0 / vowelStartFrequency;
        else
            this.vowelStartFrequency = vowelStartFrequency;
        if (vowelEndFrequency > 1.0)
            this.vowelEndFrequency = 1.0 / vowelEndFrequency;
        else
            this.vowelEndFrequency = vowelEndFrequency;
        if (vowelSplitters.length == 0)
            this.vowelSplitFrequency = 0.0;
        else if (vowelSplitFrequency > 1.0)
            this.vowelSplitFrequency = 1.0 / vowelSplitFrequency;
        else
            this.vowelSplitFrequency = vowelSplitFrequency;
        if (closingSyllables.length == 0)
            this.syllableEndFrequency = 0.0;
        else if (syllableEndFrequency > 1.0)
            this.syllableEndFrequency = 1.0 / syllableEndFrequency;
        else
            this.syllableEndFrequency = syllableEndFrequency;
        this.clean = clean;
        this.sanityChecks = sane;
    }

    protected boolean checkAll(String testing, Pattern[] checks)
    {
        String fixed = removeAccents(testing);
        for (int i = 0; i < checks.length; i++) {
            if(checks[i].matcher(fixed).find())
                return false;
        }
        return true;
    }
    /**
     * Generate a word from this Finnegan, using and changing the current seed.
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(boolean capitalize)
    {
        return word(rng.state, capitalize);
    }
    /**
     * Generate a word from this Finnegan using the specified seed.
     *
     * @param seed        the RNG seed to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(long seed, boolean capitalize) {
        rng.state = seed;
        String finished = "thaw"; // the last word in stolentelling
        while(true) {
            StringBuilder sb = new StringBuilder(20);
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                sb.append(rng.getRandomElement(midConsonants));
            } else {
                sb.append(rng.getRandomElement(openingConsonants));
            }

            double syllableChance = rng.nextDouble(totalSyllableFrequency);
            int syllables = 1;
            for (Map.Entry<Integer, Double> kv : syllableFrequencies.entrySet()) {
                if (syllableChance < kv.getValue()) {
                    syllables = kv.getKey();
                    break;
                } else
                    syllableChance -= kv.getValue();
            }
            for (int i = 0; i < syllables - 1; i++) {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                sb.append(rng.getRandomElement(midConsonants));
            }
            if (rng.nextDouble() < syllableEndFrequency) {
                sb.append(rng.getRandomElement(closingSyllables));
            } else {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                if (rng.nextDouble() >= vowelEndFrequency) {
                    sb.append(rng.getRandomElement(closingConsonants));
                    if (rng.nextDouble() < syllableEndFrequency) {
                        sb.append(rng.getRandomElement(closingSyllables));
                    }
                }
            }
            if (capitalize)
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            finished = sb.toString();
            if(sanityChecks != null && !checkAll(finished, sanityChecks))
                continue;
            else if(clean && !checkAll(finished, vulgarChecks))
                continue;
            break;
        }
        return finished;
    }

    /**
     * Generate a sentence from this Finnegan, using and changing the current seed.
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords)
    {
        return sentence(rng.state, minWords, maxWords, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", ".", "!", "?", "..."}, 0.2);
    }

    /**
     * Generate a sentence from this Finnegan, using and changing the current seed.
     *
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency)
    {
        return sentence(rng.state, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency);
    }
    /**
     * Generate a sentence from this Finnegan using the specific starting RNG seed.
     *
     * @param seed                    the RNG seed to use for the randomized string building
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(long seed, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency) {
        rng.state = seed;
        if (minWords < 1)
            minWords = 1;
        if (minWords > maxWords)
            maxWords = minWords;
        if (midPunctuationFrequency > 1.0) {
            midPunctuationFrequency = 1.0 / midPunctuationFrequency;
        }
        StringBuilder sb = new StringBuilder(12 * maxWords);
        sb.append(word(true));
        for (int i = 1; i < minWords; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(false));
        }
        for (int i = minWords; i < maxWords && rng.nextInt(2 * maxWords) > i; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(false));
        }
        sb.append(rng.getRandomElement(endPunctuation));
        return sb.toString();
    }
    /**
     * Generate a sentence from this Finnegan that fits in the given length limit..
     *
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @param maxChars the longest string length this can produce; should be at least {@code 6 * minWords}
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency, int maxChars) {
        return sentence(rng.state, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency, maxChars);
    }
    /**
     * Generate a sentence from this Finnegan using the specific starting RNG seed that fits in the given length limit.
     *
     * @param seed                    the RNG seed to use for the randomized string building
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @param maxChars the longest string length this can produce; should be at least {@code 6 * minWords}
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(long seed, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency, int maxChars) {
        rng.state = seed;
        if (minWords < 1)
            minWords = 1;
        if (minWords > maxWords)
            maxWords = minWords;
        if (midPunctuationFrequency > 1.0) {
            midPunctuationFrequency = 1.0 / midPunctuationFrequency;
        }
        if(maxChars < 4)
            return "!";
        if(maxChars <= 5 * minWords) {
            minWords = 1;
            maxWords = 1;
        }
        int frustration = 0;
        StringBuilder sb = new StringBuilder(maxChars);
        String next = word(true);
        while (next.length() >= maxChars - 1 && frustration < 50) {
            next = word(true);
            frustration++;
        }
        if(frustration >= 50) return "!";
        sb.append(next);
        for (int i = 1; i < minWords && frustration < 50 && sb.length() < maxChars - 7; i++) {
            if (rng.nextDouble() < midPunctuationFrequency && sb.length() < maxChars - 3) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            next = word(false);
            while (sb.length() + next.length() >= maxChars - 2 && frustration < 50) {
                next = word(false);
                frustration++;
            }
            if(frustration >= 50) break;
            sb.append(' ');
            sb.append(next);
        }
        for (int i = minWords; i < maxWords && sb.length() < maxChars - 7 && rng.nextInt(2 * maxWords) > i && frustration < 50; i++) {
            if (rng.nextDouble() < midPunctuationFrequency && sb.length() < maxChars - 3) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            next = word(false);
            while (sb.length() + next.length() >= maxChars - 2 && frustration < 50) {
                next = word(false);
                frustration++;
            }
            if(frustration >= 50) break;
            sb.append(' ');
            sb.append(next);
        }
        next = rng.getRandomElement(endPunctuation);
        if(sb.length() + next.length() >= maxChars)
            next = ".";
        sb.append(next);
        if(sb.length() > maxChars)
            return "!";
        return sb.toString();
    }

    protected String[] merge1000(String[] me, String[] other, double otherInfluence) {
        if(other.length <= 0 && me.length <= 0)
            return new String[]{};
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * otherInfluence);
        int idx = 0;
        if (other.length > 0) {
            String[] tmp = rng.shuffle(other);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        }
        if (me.length > 0) {
            String[] tmp = rng.shuffle(me);
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        }
        else
        {
            for (; idx < 1000; idx++) {
                ret[idx] = other[idx % other.length];
            }
        }
        return ret;
    }


    protected String[] accentVowels(String[] me, double influence) {
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * influence);
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = rng.shuffle(me);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length]
                        .replace('a', accentedVowels[0][rng.nextInt(accentedVowels[0].length)])
                        .replace('e', accentedVowels[1][rng.nextInt(accentedVowels[1].length)])
                        .replace('i', accentedVowels[2][rng.nextInt(accentedVowels[2].length)])
                        .replace('o', accentedVowels[3][rng.nextInt(accentedVowels[3].length)])
                        .replace('u', accentedVowels[4][rng.nextInt(accentedVowels[4].length)]);
                matcher = repeats.matcher(ret[idx]);
                if (matcher.find()) {
                    ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                }
            }
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        } else
            return new String[]{};
        return ret;
    }

    protected String[] accentConsonants(String[] me, double influence) {
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * influence);
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = rng.shuffle(me);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length]
                        //0
                        .replace('c', accentedConsonants[1][rng.nextInt(accentedConsonants[1].length)])
                        .replace('d', accentedConsonants[2][rng.nextInt(accentedConsonants[2].length)])
                        .replace('f', accentedConsonants[3][rng.nextInt(accentedConsonants[3].length)])
                        .replace('g', accentedConsonants[4][rng.nextInt(accentedConsonants[4].length)])
                        .replace('h', accentedConsonants[5][rng.nextInt(accentedConsonants[5].length)])
                        .replace('j', accentedConsonants[6][rng.nextInt(accentedConsonants[6].length)])
                        .replace('k', accentedConsonants[7][rng.nextInt(accentedConsonants[7].length)])
                        .replace('l', accentedConsonants[8][rng.nextInt(accentedConsonants[8].length)])
                        //9
                        .replace('n', accentedConsonants[10][rng.nextInt(accentedConsonants[10].length)])
                        //11
                        //12
                        .replace('r', accentedConsonants[13][rng.nextInt(accentedConsonants[13].length)])
                        .replace('s', accentedConsonants[14][rng.nextInt(accentedConsonants[14].length)])
                        .replace('t', accentedConsonants[15][rng.nextInt(accentedConsonants[15].length)])
                        //16
                        .replace('w', accentedConsonants[17][rng.nextInt(accentedConsonants[17].length)])
                        //18
                        .replace('y', accentedConsonants[19][rng.nextInt(accentedConsonants[19].length)])
                        .replace('z', accentedConsonants[20][rng.nextInt(accentedConsonants[20].length)]);

                matcher = repeats.matcher(ret[idx]);
                if (matcher.find()) {
                    ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                }
            }
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        } else
            return new String[]{};
        return ret;
    }

    protected String[] accentBoth(String[] me, double vowelInfluence, double consonantInfluence) {
        String[] ret = new String[1000];
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = rng.shuffle(me);
            for (idx = 0; idx < 1000; idx++) {
                boolean subVowel = rng.nextDouble() < vowelInfluence, subCon = rng.nextDouble() < consonantInfluence;
                if (subVowel && subCon) {
                    ret[idx] = tmp[idx % tmp.length]
                            .replace('a', accentedVowels[0][rng.nextInt(accentedVowels[0].length)])
                            .replace('e', accentedVowels[1][rng.nextInt(accentedVowels[1].length)])
                            .replace('i', accentedVowels[2][rng.nextInt(accentedVowels[2].length)])
                            .replace('o', accentedVowels[3][rng.nextInt(accentedVowels[3].length)])
                            .replace('u', accentedVowels[4][rng.nextInt(accentedVowels[4].length)])

                            //0
                            .replace('c', accentedConsonants[1][rng.nextInt(accentedConsonants[1].length)])
                            .replace('d', accentedConsonants[2][rng.nextInt(accentedConsonants[2].length)])
                            .replace('f', accentedConsonants[3][rng.nextInt(accentedConsonants[3].length)])
                            .replace('g', accentedConsonants[4][rng.nextInt(accentedConsonants[4].length)])
                            .replace('h', accentedConsonants[5][rng.nextInt(accentedConsonants[5].length)])
                            .replace('j', accentedConsonants[6][rng.nextInt(accentedConsonants[6].length)])
                            .replace('k', accentedConsonants[7][rng.nextInt(accentedConsonants[7].length)])
                            .replace('l', accentedConsonants[8][rng.nextInt(accentedConsonants[8].length)])
                            //9
                            .replace('n', accentedConsonants[10][rng.nextInt(accentedConsonants[10].length)])
                            //11
                            //12
                            .replace('r', accentedConsonants[13][rng.nextInt(accentedConsonants[13].length)])
                            .replace('s', accentedConsonants[14][rng.nextInt(accentedConsonants[14].length)])
                            .replace('t', accentedConsonants[15][rng.nextInt(accentedConsonants[15].length)])
                            //16
                            .replace('w', accentedConsonants[17][rng.nextInt(accentedConsonants[17].length)])
                            //18
                            .replace('y', accentedConsonants[19][rng.nextInt(accentedConsonants[19].length)])
                            .replace('z', accentedConsonants[20][rng.nextInt(accentedConsonants[20].length)]);

                    matcher = repeats.matcher(ret[idx]);
                    if (matcher.find()) {
                        ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                    }
                } else if (subVowel) {
                    ret[idx] = tmp[idx % tmp.length]
                            .replace('a', accentedVowels[0][rng.nextInt(accentedVowels[0].length)])
                            .replace('e', accentedVowels[1][rng.nextInt(accentedVowels[1].length)])
                            .replace('i', accentedVowels[2][rng.nextInt(accentedVowels[2].length)])
                            .replace('o', accentedVowels[3][rng.nextInt(accentedVowels[3].length)])
                            .replace('u', accentedVowels[4][rng.nextInt(accentedVowels[4].length)]);

                    matcher = repeats.matcher(ret[idx]);
                    if (matcher.find()) {
                        ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                    }
                } else if (subCon) {
                    ret[idx] = tmp[idx % tmp.length]
                            //0
                            .replace('c', accentedConsonants[1][rng.nextInt(accentedConsonants[1].length)])
                            .replace('d', accentedConsonants[2][rng.nextInt(accentedConsonants[2].length)])
                            .replace('f', accentedConsonants[3][rng.nextInt(accentedConsonants[3].length)])
                            .replace('g', accentedConsonants[4][rng.nextInt(accentedConsonants[4].length)])
                            .replace('h', accentedConsonants[5][rng.nextInt(accentedConsonants[5].length)])
                            .replace('j', accentedConsonants[6][rng.nextInt(accentedConsonants[6].length)])
                            .replace('k', accentedConsonants[7][rng.nextInt(accentedConsonants[7].length)])
                            .replace('l', accentedConsonants[8][rng.nextInt(accentedConsonants[8].length)])
                            //9
                            .replace('n', accentedConsonants[10][rng.nextInt(accentedConsonants[10].length)])
                            //11
                            //12
                            .replace('r', accentedConsonants[13][rng.nextInt(accentedConsonants[13].length)])
                            .replace('s', accentedConsonants[14][rng.nextInt(accentedConsonants[14].length)])
                            .replace('t', accentedConsonants[15][rng.nextInt(accentedConsonants[15].length)])
                            //16
                            .replace('w', accentedConsonants[17][rng.nextInt(accentedConsonants[17].length)])
                            //18
                            .replace('y', accentedConsonants[19][rng.nextInt(accentedConsonants[19].length)])
                            .replace('z', accentedConsonants[20][rng.nextInt(accentedConsonants[20].length)]);

                    matcher = repeats.matcher(ret[idx]);
                    if (matcher.find()) {
                        ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                    }
                } else ret[idx] = tmp[idx % tmp.length];

            }
        } else
            return new String[]{};
        return ret;
    }

    public Finnegan mix(Finnegan other, double otherInfluence) {
        otherInfluence = Math.max(0.0, Math.min(otherInfluence, 1.0));
        double myInfluence = 1.0 - otherInfluence;
        long oldState = rng.state;
        rng.state = (hashCode() & 0xffffffffL) | ((other.hashCode() & 0xffffffffL) << 32)
                ^ Double.doubleToLongBits(otherInfluence);
        String[] ov = merge1000(openingVowels, other.openingVowels, otherInfluence),
                mv = merge1000(midVowels, other.midVowels, otherInfluence),
                oc = merge1000(openingConsonants, other.openingConsonants, otherInfluence),
                mc = merge1000(midConsonants, other.midConsonants, otherInfluence),
                cc = merge1000(closingConsonants, other.closingConsonants, otherInfluence),
                cs = merge1000(closingSyllables, other.closingSyllables, otherInfluence),
                splitters = merge1000(vowelSplitters, other.vowelSplitters, otherInfluence);

        LinkedHashMap<Integer, Double> freqs = new LinkedHashMap<Integer, Double>(syllableFrequencies);
        for (Map.Entry<Integer, Double> kv : other.syllableFrequencies.entrySet()) {
            if (freqs.containsKey(kv.getKey()))
                freqs.put(kv.getKey(), kv.getValue() + freqs.get(kv.getKey()));
            else
                freqs.put(kv.getKey(), kv.getValue());
        }
        int[] lens = new int[freqs.size()];
        double[] odds = new double[freqs.size()];
        int i = 0;
        for (Map.Entry<Integer, Double> kv : freqs.entrySet()) {
            lens[i] = kv.getKey();
            odds[i++] = kv.getValue();
        }
        Finnegan finished = new Finnegan(ov, mv, oc, mc, cc, cs, splitters, lens, odds,
                vowelStartFrequency * myInfluence + other.vowelStartFrequency * otherInfluence,
                vowelEndFrequency * myInfluence + other.vowelEndFrequency * otherInfluence,
                vowelSplitFrequency * myInfluence + other.vowelSplitFrequency * otherInfluence,
                syllableEndFrequency * myInfluence + other.syllableEndFrequency * otherInfluence,
                (sanityChecks == null) ? other.sanityChecks : sanityChecks, true);
        finished.rng.state = rng.state;
        rng.state = oldState;
        return finished;
    }

    public Finnegan addAccents(double vowelInfluence, double consonantInfluence) {
        vowelInfluence = Math.max(0.0, Math.min(vowelInfluence, 1.0));
        consonantInfluence = Math.max(0.0, Math.min(consonantInfluence, 1.0));
        long oldState = rng.state;
        rng.state = (hashCode() & 0xffffffffL) ^ ((Double.doubleToLongBits(vowelInfluence) & 0xffffffffL) |
                (Double.doubleToLongBits(consonantInfluence) << 32));
        String[] ov = accentVowels(openingVowels, vowelInfluence),
                mv = accentVowels(midVowels, vowelInfluence),
                oc = accentConsonants(openingConsonants, consonantInfluence),
                mc = accentConsonants(midConsonants, consonantInfluence),
                cc = accentConsonants(closingConsonants, consonantInfluence),
                cs = accentBoth(closingSyllables, vowelInfluence, consonantInfluence);
        int[] lens = new int[syllableFrequencies.size()];
        double[] odds = new double[syllableFrequencies.size()];
        int i = 0;
        for (Map.Entry<Integer, Double> kv : syllableFrequencies.entrySet()) {
            lens[i] = kv.getKey();
            odds[i++] = kv.getValue();
        }
        Finnegan finished = new Finnegan(ov, mv, oc, mc, cc, cs, vowelSplitters, lens, odds,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean);
        finished.rng.state = rng.state;
        rng.state = oldState;
        return finished;
    }
    public Finnegan removeAccents() {

        String[] ov = openingVowels.clone(),
                mv = midVowels.clone(),
                oc = openingConsonants.clone(),
                mc = midConsonants.clone(),
                cc = closingConsonants.clone(),
                cs = closingSyllables.clone();
        for (int i = 0; i < ov.length; i++) {
            ov[i] = removeAccents(openingVowels[i]);
        }
        for (int i = 0; i < mv.length; i++) {
            mv[i] = removeAccents(midVowels[i]);
        }
        for (int i = 0; i < oc.length; i++) {
            oc[i] = removeAccents(openingConsonants[i]);
        }
        for (int i = 0; i < mc.length; i++) {
            mc[i] = removeAccents(midConsonants[i]);
        }
        for (int i = 0; i < cc.length; i++) {
            cc[i] = removeAccents(closingConsonants[i]);
        }
        for (int i = 0; i < cs.length; i++) {
            cs[i] = removeAccents(closingSyllables[i]);
        }
        int[] lens = new int[syllableFrequencies.size()];
        double[] odds = new double[syllableFrequencies.size()];
        int i = 0;
        for (Map.Entry<Integer, Double> kv : syllableFrequencies.entrySet()) {
            lens[i] = kv.getKey();
            odds[i++] = kv.getValue();
        }
        Finnegan finished =  new Finnegan(ov, mv, oc, mc, cc, cs, vowelSplitters, lens, odds,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean);
        finished.rng.state = rng.state;
        return finished;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Finnegan finnegan = (Finnegan) o;

        if (clean != finnegan.clean) return false;
        if (Double.compare(finnegan.totalSyllableFrequency, totalSyllableFrequency) != 0) return false;
        if (Double.compare(finnegan.vowelStartFrequency, vowelStartFrequency) != 0) return false;
        if (Double.compare(finnegan.vowelEndFrequency, vowelEndFrequency) != 0) return false;
        if (Double.compare(finnegan.vowelSplitFrequency, vowelSplitFrequency) != 0) return false;
        if (Double.compare(finnegan.syllableEndFrequency, syllableEndFrequency) != 0) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(openingVowels, finnegan.openingVowels)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(midVowels, finnegan.midVowels)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(openingConsonants, finnegan.openingConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(midConsonants, finnegan.midConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(closingConsonants, finnegan.closingConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(vowelSplitters, finnegan.vowelSplitters)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(closingSyllables, finnegan.closingSyllables)) return false;
        if (syllableFrequencies != null ? !syllableFrequencies.equals(finnegan.syllableFrequencies) : finnegan.syllableFrequencies != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(sanityChecks, finnegan.sanityChecks) && (rng != null ? rng.equals(finnegan.rng) : finnegan.rng == null);

    }

    @Override
    public int hashCode() {
        long result, temp;
        result = hash64(openingVowels);
        result = 31 * result + hash64(midVowels);
        result = 31 * result + hash64(openingConsonants);
        result = 31 * result + hash64(midConsonants);
        result = 31 * result + hash64(closingConsonants);
        result = 31 * result + hash64(vowelSplitters);
        result = 31 * result + hash64(closingSyllables);
        result = 31 * result + (clean ? 1 : 0);
        result = 31 * result + (syllableFrequencies != null ? syllableFrequencies.hashCode() : 0);
        temp = Double.doubleToLongBits(totalSyllableFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vowelStartFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vowelEndFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vowelSplitFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(syllableEndFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (sanityChecks != null ? sanityChecks.length + 1 : 0);
        result = 31 * result + (rng != null ? rng.hashCode() : 0);
        return (int)result;
    }

    @Override
    public String toString() {
        return "Finnegan{" +
                "openingVowels=" + Arrays.toString(openingVowels) +
                ", midVowels=" + Arrays.toString(midVowels) +
                ", openingConsonants=" + Arrays.toString(openingConsonants) +
                ", midConsonants=" + Arrays.toString(midConsonants) +
                ", closingConsonants=" + Arrays.toString(closingConsonants) +
                ", vowelSplitters=" + Arrays.toString(vowelSplitters) +
                ", closingSyllables=" + Arrays.toString(closingSyllables) +
                ", syllableFrequencies=" + syllableFrequencies +
                ", totalSyllableFrequency=" + totalSyllableFrequency +
                ", vowelStartFrequency=" + vowelStartFrequency +
                ", vowelEndFrequency=" + vowelEndFrequency +
                ", vowelSplitFrequency=" + vowelSplitFrequency +
                ", syllableEndFrequency=" + syllableEndFrequency +
                ", sane=" + Arrays.toString(sanityChecks) +
                ", clean=" + clean +
                ", RNG=" + rng +
                '}';
    }

    public long getSeed() {
        return rng.state;
    }

    public void setSeed(long seed) {
        rng.state = seed;
    }

    static long hash64(char[] data)
    {
        if(data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 1099511628211L;
            h ^= data[i] >>> 8;
            h *= 1099511628211L;
        }
        return h;
    }
    static long hash64(int[] data)
    {
        if(data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 1099511628211L;
            h ^= (data[i] >>> 8) & 0xff;
            h *= 1099511628211L;
            h ^= (data[i] >>> 16) & 0xff;
            h *= 1099511628211L;
            h ^= data[i] >>> 24;
            h *= 1099511628211L;
        }
        return h;
    }
    static long hash64(double[] data)
    {
        if(data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = Double.doubleToRawLongBits(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }
    static long hash64(String s)
    {
        if(s == null)
            return 0;
        return hash64(s.toCharArray());
    }
    static long hash64(String[] data)
    {
        if(data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash64(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }
    static long hash64(String[]... data)
    {
        if(data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash64(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

}