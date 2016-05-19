package regexodus;

import java.io.Serializable;
import java.util.*;

/**
 * A text generator for producing sentences and/or words in nonsense languages that fit a theme. This does not use an
 * existing word list as a basis for its output, so it may or may not produce existing words occasionally, but you can
 * safely assume it won't generate a meaningful sentence except in the absolute unlikeliest of cases.
 * Created by Tommy Ettinger on 11/29/2015.
 * @author Tommy Ettinger
 */

public class SquidFin implements Serializable {
    public static class RNG implements Serializable {
        private static final long serialVersionUID = 4378460257281186371L;

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
         * Shuffle an array using the "inside-out" Fisher-Yates algorithm. DO NOT give the same array for both elements and
         * dest, since the prior contents of dest are rearranged before elements is used, and if they refer to the same
         * array, then you can end up with bizarre bugs where one previously-unique item shows up dozens of times. If
         * possible, create a new array with the same length as elements and pass it in as dest; the returned value can be
         * assigned to whatever you want and will have the same items as the newly-formed array.
         * <br>
         * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_.22inside-out.22_algorithm
         * @param elements an array of T; will not be modified
         * @param <T> can be any non-primitive type.
         * @param dest Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
         *             randomPortion method of this class to fill the smaller dest. MUST NOT be the same array as elements!
         * @return {@code dest} after modifications
         */
    /* This method has this prototype to be compatible with GWT. */
        public <T> T[] shuffle(T[] elements, T[] dest)
        {
            if (dest.length != elements.length)
                return randomPortion(elements, dest);
            for (int i = 0; i < elements.length; i++)
            {
                int r = nextInt(i + 1);
                if(r != i)
                    dest[i] = dest[r];
                dest[r] = elements[i];
            }
            return dest;
        }

        /**
         * Shuffle a {@link List} using the Fisher-Yates algorithm.
         * @param elements a List of T; will not be modified
         * @param <T> can be any non-primitive type.
         * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
         */
        public <T> ArrayList<T> shuffle(List<T> elements)
        {
            ArrayList<T> al = new ArrayList<T>(elements);
            int n = al.size();
            for (int i = 0; i < n; i++)
            {
                Collections.swap(al, i + nextInt(n - i), i);
            }
            return al;
        }

        /**
         * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
         * it can, and then returns output. Will only use a given position in the given data at most once; does this by
         * generating random indices for data's elements, but only as much as needed, assigning the copied section to output
         * and not modifying data.
         * <br>
         * Based on http://stackoverflow.com/a/21460179 , credit to Vincent van der Weele; modifications were made to avoid
         * copying or creating a new generic array (a problem on GWT).
         * @param data an array of T; will not be modified.
         * @param output an array of T that will be overwritten; should always be instantiated with the portion length
         * @param <T> can be any non-primitive type.
         * @return an array of T that has length equal to output's length and may contain unchanged elements (null if output
         * was empty) if data is shorter than output
         */
        public <T> T[] randomPortion(T[] data, T[] output)
        {
            int length = data.length;
            int[] mapping = new int[length];
            for (int i = 0; i < length; i++) {
                mapping[i] = i;
            }

            for (int i = 0; i < output.length && length > 0; i++) {
                int r = nextInt(length);

                output[i] = data[mapping[r]];

                mapping[r] = length-1;
                length--;
            }

            return output;
        }

        /**
         * Gets a random portion of a List and returns it as a new List. Will only use a given position in the given
         * List at most once; does this by shuffling a copy of the List and getting a section of it.
         * @param data a List of T; will not be modified.
         * @param count the non-negative number of elements to randomly take from data
         * @param <T> can be any non-primitive type
         * @return a List of T that has length equal to the smaller of count or data.length
         */
        public <T> List<T> randomPortion(List<T> data, int count)
        {
            return shuffle(data).subList(0, Math.min(count, data.size()));
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
    private static final long serialVersionUID = -2396642435461186352L;
    public final String[] openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants,
            vowelSplitters, closingSyllables;
    public boolean clean;
    public final LinkedHashMap<Integer, Double> syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    public final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency;
    protected final Pattern[] sanityChecks;
    public ArrayList<Modifier> modifiers;
    public static final RNG srng = new RNG();
    protected static final Pattern repeats = Pattern.compile("(.)\\1+");
    //, diacritics = Pattern.compile("[\u0300-\u036F\u1DC0-\u1DFF]+");
    public static final Pattern[]
            vulgarChecks = new Pattern[]
            {
                    Pattern.compile("[SsξCcсςС][hнН].*[dtтτТΤf]"),
                    Pattern.compile("([PpрρРΡ][hнН])|[KkкκКΚFfDdCcсςС].{1,4}[KkкκКΚCcсςСxхжχХЖΧ]"), // lots of these end in a 'k' sound, huh
                    Pattern.compile("[BbъыбвβЪЫБВΒ]..?.?[cсςС][hнН]"),
                    Pattern.compile("[WwшщψШЩHhнН]..?[rяЯ]"),
                    Pattern.compile("[TtтτТΤ]..?[tтτТΤ]"),
                    Pattern.compile("([PpрρРΡ][hнН])|[Ff]..?[rяЯ][tтτТΤ]"),
                    Pattern.compile("([Ssξ][hнН])|[j][iτιΙ].?[sξzΖ]"),
                    Pattern.compile("[AaаαАΑΛ][NnийИЙΝ]..?[SsξlιζzΖ]"),
                    Pattern.compile("[AaаαАΑΛ][sξ][sξ]"),
                    Pattern.compile(".[uμυν][hнН]?[nийИЙΝ]+[tтτТΤ]"),
                    Pattern.compile("[NnFf]..?g"), // might as well remove two possible slurs with one check
                    Pattern.compile("[PpрρРΡ][eеёзξεЕЁЗΞΕΣioоюσοОЮΟuμυν][eеёзξεЕЁЗΞΕΣoоюσοОЮΟs]"), // the grab bag of juvenile words
                    Pattern.compile("[MmмМΜ]..?[rяЯ].?d"), // should pick up the #1 obscenity from Spanish and French
                    Pattern.compile("[Gg][hнН]?[aаαАΑΛeеёзξεЕЁЗΞΕΣ][yуλγУΥeеёзξεЕЁЗΞΕΣ]") // could be inappropriate for random text
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
                    Pattern.compile("[þðďđḍ]").replacer("d"),
                    Pattern.compile("[ĝğġģ]").replacer("g"),
                    Pattern.compile("[ĥħḥ]").replacer("h"),
                    Pattern.compile("[ĵȷ]").replacer("j"),
                    Pattern.compile("ķ").replacer("k"),
                    Pattern.compile("[ĺļľŀłḷḹļ]").replacer("l"),
                    Pattern.compile("ṃ").replacer("m"),
                    Pattern.compile("[ñńņňŋṅṇ]").replacer("n"),
                    Pattern.compile("[ŕŗřṛṝŗŕ]").replacer("r"),
                    Pattern.compile("[śŝşšșṣ]").replacer("s"),
                    Pattern.compile("[ţťțṭ]").replacer("t"),
                    Pattern.compile("[ŵẁẃẅ]").replacer("w"),
                    Pattern.compile("[ýÿŷỳ]").replacer("y"),
                    Pattern.compile("[źżž]").replacer("z"),
                    Pattern.compile("[ÇĆĈĊČ]").replacer("C"),
                    Pattern.compile("[ÞÐĎĐḌ]").replacer("D"),
                    Pattern.compile("[ĜĞĠĢ]").replacer("G"),
                    Pattern.compile("[ĤĦḤ]").replacer("H"),
                    Pattern.compile("Ĵ").replacer("J"),
                    Pattern.compile("Ķ").replacer("K"),
                    Pattern.compile("[ĹĻĽĿŁḶḸĻ]").replacer("L"),
                    Pattern.compile("Ṃ").replacer("M"),
                    Pattern.compile("[ÑŃŅŇŊṄṆ]").replacer("N"),
                    Pattern.compile("[ŔŖŘṚṜŖŔ]").replacer("R"),
                    Pattern.compile("[ŚŜŞŠȘṢ]").replacer("S"),
                    Pattern.compile("[ŢŤȚṬ]").replacer("T"),
                    Pattern.compile("[ŴẀẂẄ]").replacer("W"),
                    Pattern.compile("[ÝŸŶỲ]").replacer("Y"),
                    Pattern.compile("[ŹŻŽ]").replacer("Z"),

            };

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
                                    'f'
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
                                    'k', 'ķ',
                            },
                            new char[]{
                                    'l', 'ĺ', 'ļ', 'ľ', 'ŀ', 'ł',
                            },
                            new char[]{
                                    'm',
                            },
                            new char[]{
                                    'n', 'ñ', 'ń', 'ņ', 'ň', 'ŋ',
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
                                    't', 'ţ', 'ť', 'ț',
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

    /*
     * Removes accented characters from a string; if the "base" characters are non-English anyway then the result won't
     * be an ASCII string, but otherwise it probably will be.
     * <br>
     * Credit to user hashable from http://stackoverflow.com/a/1215117
     *
     * @param str a string that may contain accented characters
     * @return a string with all accented characters replaced with their (possibly ASCII) counterparts
     *
    public String removeAccents(String str) {
        String alteredString = Normalizer.normalize(str, Normalizer.Form.NFD);
        alteredString = diacritics.matcher(alteredString).replaceAll("");
        alteredString = alteredString.replace('æ', 'a');
        alteredString = alteredString.replace('œ', 'o');
        alteredString = alteredString.replace('Æ', 'A');
        alteredString = alteredString.replace('Œ', 'O');
        return alteredString;
    }*/

    /**
     * Removes accented Latin-script characters from a string; if the "base" characters are non-English anyway then the
     * result won't be an ASCII string, but otherwise it probably will be.
     *
     * @param str a string that may contain accented Latin-script characters
     * @return a string with all accented characters replaced with their (possibly ASCII) counterparts
     */
    public CharSequence removeAccents(CharSequence str) {
        CharSequence alteredString = str;
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
    public static final SquidFin LOVECRAFT = new SquidFin(
            new String[]{"a", "i", "o", "e", "u", "a", "i", "o", "e", "u", "ia", "ai", "aa", "ei"},
            new String[]{},
            new String[]{"s", "t", "k", "n", "y", "p", "k", "l", "g", "gl", "th", "sh", "ny", "ft", "hm", "zvr", "cth"},
            new String[]{"h", "gl", "gr", "nd", "mr", "vr", "kr"},
            new String[]{"l", "p", "s", "t", "n", "k", "g", "x", "rl", "th", "gg", "gh", "ts", "lt", "rk", "kh", "sh", "ng", "shk"},
            new String[]{"aghn", "ulhu", "urath", "oigor", "alos", "'yeh", "achtal", "urath", "ikhet", "adzek"},
            new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2}, 0.4, 0.31, 0.07, 0.04, null, true);
    /**
     * Imitation English; may seem closer to Dutch in some generated text, and is not exactly the best imitation.
     * Should seem pretty fake to many readers; does not filter out dictionary words but does perform basic vulgarity
     * filtering. If you want to avoid generating other words, you can subclass SquidFin and modify word() .
     * <br>
     * Mont tiste frot; mousation hauddes?
     * Lily wrely stiebes; flarrousseal gapestist.
     */
    public static final SquidFin ENGLISH = new SquidFin(
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
    public static final SquidFin GREEK_ROMANIZED = new SquidFin(
            new String[]{"a", "a", "a", "o", "o", "o", "e", "e", "i", "i", "i", "au", "ai", "ai", "oi", "oi", "ia", "io", "ou", "ou", "eo", "ei"},
            new String[]{"ui", "ei"},
            new String[]{"rh", "s", "z", "t", "t", "k", "ch", "n", "th", "kth", "m", "p", "ps", "b", "l", "kr", "g", "phth"},
            new String[]{"lph", "pl", "l", "l", "kr", "nch", "nx", "ps"},
            new String[]{"s", "p", "t", "ch", "n", "m", "s", "p", "t", "ch", "n", "m", "b", "g", "st", "rst", "rt", "sp", "rk", "ph", "x", "z", "nk", "ng", "th"},
            new String[]{"os", "os", "is", "us", "um", "eum", "ium", "iam", "us", "um", "es", "anes", "eros", "or", "ophon", "on", "otron"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3, null, true);
    /**
     * Imitation ancient Greek, using the original Greek alphabet. People may try to translate it and get gibberish.
     * Make sure the font you use to render this supports the Greek alphabet! In the GDX display module, the "smooth"
     * fonts support all the Greek you need for this.
     * <br>
     * Ψυιλασ αλορ; αιπεομαρτα λε λιασπα...
     */
    public static final SquidFin GREEK_AUTHENTIC = new SquidFin(
            new String[]{"α", "α", "α", "ο", "ο", "ο", "ε", "ε", "ι", "ι", "ι", "αυ", "αι", "αι", "οι", "οι", "ια", "ιο", "ου", "ου", "εο", "ει"},
            new String[]{"υι", "ει"},
            new String[]{"ρ", "σ", "ζ", "τ", "τ", "κ", "χ", "ν", "θ", "κθ", "μ", "π", "ψ", "β", "λ", "κρ", "γ", "φθ"},
            new String[]{"λφ", "πλ", "λ", "λ", "κρ", "γχ", "γξ", "ψ"},
            new String[]{"σ", "π", "τ", "χ", "ν", "μ", "σ", "π", "τ", "χ", "ν", "μ", "β", "γ", "στ", "ρστ", "ρτ", "σπ", "ρκ", "φ", "ξ", "ζ", "γκ", "γγ", "θ"},
            new String[]{"ος", "ος", "ις", "υς", "υμ", "ευμ", "ιυμ", "ιαμ", "υς", "υμ", "ες", "ανες", "ερος", "ορ", "οφον", "ον", "οτρον"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3, null, true);

    /**
     * Imitation modern French, using (too many of) the accented vowels that are present in the language. Translating it
     * will produce gibberish if it produces anything at all. In the GDX display module, the "smooth" and "unicode"
     * fonts support all the accented characters you need for this.
     * <br><br>
     * Fa veau, ja ri avé re orçe jai braï aisté.
     */
    public static final SquidFin FRENCH = new SquidFin(
            new String[]{"a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "ai", "oi", "oui", "au", "œu", "ou"
            },
            new String[]{
                    "ai", "aie", "aou", "eau", "oi", "oui", "oie", "eu", "eu",
                    "à", "â", "ai", "aî", "aï", "aie", "aou", "aoû", "au", "ay", "e", "é", "ée", "è",
                    "ê", "eau", "ei", "eî", "eu", "eû", "i", "î", "ï", "o", "ô", "oe", "oê", "oë", "œu",
                    "oi", "oie", "oï", "ou", "oû", "oy", "u", "û", "ue",
                    "a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
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
    public static final SquidFin RUSSIAN_ROMANIZED = new SquidFin(
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
     * In the GDX display module, the "smooth" fonts support all the Cyrillic alphabet you need for this.
     * <br>
     * Жыдотуф руц пйцас, гогутяр шыскучэбаб - гйчапофёглор гюнуз ъсказюжин.
     */
    public static final SquidFin RUSSIAN_AUTHENTIC = new SquidFin(
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
    public static final SquidFin JAPANESE_ROMANIZED = new SquidFin(
            new String[]{"a", "a", "a", "a", "e", "e", "i", "i", "i", "i", "o", "o", "o", "u", "ou", "u", "ai", "ai"},
            new String[]{},
            new String[]{"k", "ky", "s", "sh", "t", "ts", "ch", "n", "ny", "h", "f", "hy", "m", "my", "y", "r", "ry", "g",
                    "gy", "z", "j", "d", "b", "by", "p", "py",
                    "k", "t", "n", "s", "k", "t", "d", "s", "sh", "sh", "g", "r", "b",
                    "k", "t", "n", "s", "k", "t", "b", "s", "sh", "sh", "g", "r", "b",
                    "k", "t", "n", "s", "k", "t", "z", "s", "sh", "sh", "ch", "ry", "ts"
            },
            new String[]{"k", "ky", "s", "sh", "t", "ts", "ch", "n", "ny", "h", "f", "hy", "m", "my", "y", "r", "ry", "g",
                    "gy", "z", "j", "d", "b", "by", "p", "py",
                    "k", "t", "d", "s", "k", "t", "d", "s", "sh", "sh", "y", "j", "p", "r", "d",
                    "k", "t", "b", "s", "k", "t", "b", "s", "sh", "sh", "y", "j", "p", "r", "d",
                    "k", "t", "z", "s", "f", "g", "z", "b", "d", "ts",
                    "nn", "nn", "nn", "nd", "nz", "mm", "kk", "kk", "tt", "ss", "ssh", "tch"},
            new String[]{"n"},
            new String[]{},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 4, 5, 4, 3}, 0.3, 0.9, 0.0, 0.0, japaneseSanityChecks, true);

    /**
     * Swahili is one of the more commonly-spoken languages in sub-Saharan Africa, and serves mainly as a shared language
     * that is often learned after becoming fluent in one of many other (vaguely-similar) languages of the area. An
     * example sentence in Swahili, that this might try to imitate aesthetically, is "Mtoto mdogo amekisoma," meaning
     * "The small child reads it" (where it is a book). A notable language feature used here is the redoubling of words,
     * which is used in Swahili to emphasize or alter the meaning of the doubled word; here, it always repeats exactly
     * and can't make minor changes like a real language might. This generates things like "gata-gata", "hapi-hapi", and
     * "mimamzu-mimamzu", always separating with a hyphen here.
     * <br>
     * As an aside, please try to avoid the ugly stereotypes that fantasy media often assigns to speakers of African-like
     * languages when using this or any of the generators. Many fantasy tropes come from older literature written with
     * major cultural biases, and real-world cultural elements can be much more interesting to players than yet another
     * depiction of a "jungle savage" with stereotypical traits. Consider drawing from existing lists of real-world
     * technological discoveries, like https://en.wikipedia.org/wiki/History_of_science_and_technology_in_Africa , for
     * inspiration when world-building; though some groups may not have developed agriculture by early medieval times,
     * their neighbors may be working iron and studying astronomy just a short distance away.
     * <br>
     * Kondueyu; ma mpiyamdabota mise-mise nizakwaja alamsa amja, homa nkajupomba.
     */
    public static final SquidFin SWAHILI = new SquidFin(
            new String[]{"a", "i", "o", "e", "u",
                    "a", "a", "i", "o", "o", "e", "u",
                    "a", "a", "i", "o", "o", "u",
                    "a", "a", "i", "i", "o",
                    "a","a","a","a","a",
                    "a", "i", "o", "e", "u",
                    "a", "a", "i", "o", "o", "e", "u",
                    "a", "a", "i", "o", "o", "u",
                    "a", "a", "i", "i", "o",
                    "a","a","a","a","a",
                    "aa", "aa", "ue", "uo", "ii", "ea"},
            new String[]{},
            new String[]{
                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "nb", "nj", "ns", "nz",
                    "nb", "nch", "nj", "ns", "ny", "nz",
                    "nb", "nch", "nf", "ng", "nj", "nk", "np", "ns", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nv", "nw", "nz",

                    "mb", "ms", "my", "mz",
                    "mb", "mch","ms", "my", "mz",
                    "mb", "mch", "mk", "mp", "ms", "my", "mz",
                    "mb", "mch", "md", "mk", "mp", "ms", "mt", "my", "mz",
                    "mb", "mch", "md", "mf", "mg", "mj", "mk", "mp", "ms", "mt", "mv", "mw", "my", "mz",
                    "sh", "sh", "sh", "ny", "kw",
                    "dh", "th", "sh", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
            },
            new String[]{
                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "nb", "nj", "ns", "nz",
                    "nb", "nch", "nj", "ns", "ny", "nz",
                    "nb", "nch", "nf", "ng", "nj", "nk", "np", "ns", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nw", "nz",

                    "mb", "ms", "my", "mz",
                    "mb", "mch","ms", "my", "mz",
                    "mb", "mch", "mk", "mp", "ms", "my", "mz",
                    "mb", "mch", "md", "mk", "mp", "ms", "mt", "my", "mz",
                    "mb", "mch", "md", "mf", "mg", "mj", "mk", "mp", "ms", "mt", "mw", "my", "mz",
                    "sh", "sh", "sh", "ny", "kw",
                    "dh", "th", "sh", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
                    "ng", "ng", "ng", "ng", "ng"
            },
            new String[]{""},
            new String[]{"a-@2a", "a-@2a", "a-@3a","a-@2a", "a-@2a", "a-@3a","i-@2i", "i-@2i", "i-@3i",
                    "e-@2e", "e-@2e", "e-@3e", "u-@2u", "u-@2u", "u-@3u",
            },
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{1, 7, 6, 4, 2}, 0.2, 1.0, 0.0, 0.25, null, true);

    /**
     * Imitation Somali, using the Latin alphabet. Due to uncommon word structure, unusual allowed combinations of
     * letters, and no common word roots with most familiar languages, this may seem like an unidentifiable or "alien"
     * language to most readers. However, it's based on the Latin writing system for the Somali language (probably
     * closest to the northern dialect), which due to the previously mentioned properties, makes it especially good for
     * mixing with other languages to make letter combinations that seem strange to appear. It is unlikely that this
     * particular generated language style will be familiar to readers, so it probably won't have existing stereotypes
     * associated with the text. One early comment this received was, "it looks like a bunch of letters semi-randomly
     * thrown together", which is probably a typical response (the comment was made by someone fluent in German and
     * English, and most Western European languages are about as far as you can get from Somali).
     * <br>
     * Libor cat naqoxekh dhuugad gisiqir?
     */
    public static final SquidFin SOMALI = new SquidFin(
            new String[]{"a", "a", "a", "a", "a", "a", "a", "aa", "aa", "aa",
                    "e", "e", "ee",
                    "i", "i", "i", "i", "ii",
                    "o", "o", "o", "oo",
                    "u", "u", "u", "uu", "uu",
                    },
            new String[]{},
            new String[]{"b", "t", "j", "x", "kh", "d", "r", "s", "sh", "dh", "c", "g", "f", "q", "k", "l", "m",
                    "n", "w", "h", "y",
                    "x", "g", "b", "d", "s", "m", "dh", "n", "r",
                    "g", "b", "s", "dh",
            },
            new String[]{
                    "bb", "gg", "dd", "bb", "dd", "rr", "ddh", "cc", "gg", "ff", "ll", "mm", "nn",
                    "bb", "gg", "dd", "bb", "dd", "gg",
                    "bb", "gg", "dd", "bb", "dd", "gg",
                    "cy", "fk", "ft", "nt", "rt", "lt", "qm", "rdh", "rsh", "lq",
                    "my", "gy", "by", "lkh", "rx", "md", "bd", "dg", "fd", "mf",
                    "dh", "dh", "dh", "dh",
            },
            new String[]{
                    "b", "t", "j", "x", "kh", "d", "r", "s", "sh", "c", "g", "f", "q", "k", "l", "m", "n", "h",
                    "x", "g", "b", "d", "s", "m", "q", "n", "r",
                    "b", "t", "j", "x", "kh", "d", "r", "s", "sh", "c", "g", "f", "q", "k", "l", "m", "n", "h",
                    "x", "g", "b", "d", "s", "m", "q", "n", "r",
                    "b", "t", "j", "x", "kh", "d", "r", "s", "sh", "c", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "q", "n", "r",
                    "b", "t", "x", "kh", "d", "r", "s", "sh", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "r", "n",
                    "b", "t", "kh", "d", "r", "s", "sh", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "r", "n",
                    "b", "t", "d", "r", "s", "sh", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "r", "n",
            },
            new String[]{"aw", "ow", "ay", "ey", "oy", "ay", "ay"},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 4, 5, 4, 1}, 0.25, 0.3, 0.0, 0.08, null, true);
    /**
     * Imitation Hindi, romanized to use the Latin alphabet using accented glyphs similar to the IAST standard.
     * You can get this to produce actual IAST glyphs by calling removeModifiers() on this, but most fonts do not
     * support the glyphs that needs. If the modifier that does this is not removed, then the IAST standard glyphs
     * {@code "ṛṝḷḹḍṭṅṇṣṃḥ"} become {@code "ŗŕļĺđţńņşĕĭ"}, with the nth glyph in the first string being substituted
     * with the nth glyph in the second string.
     * <br>
     * Darvāga yar; ghađhinopŕauka āĕrdur, conśaigaijo śabhodhaĕđū jiviđaudu.
     */
    public static final SquidFin HINDI_ROMANIZED = new SquidFin(
            new String[]{
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "aṃ", "aṃ", "aṃ", "aṃ", "aṃ", "āṃ", "āṃ", "iṃ", "iṃ", "iṃ", "īṃ", "īṃ",
                    "uṃ", "uṃ", "ūṃ", "aiṃ", "aiṃ", "oṃ", "oṃ", "oṃ", "auṃ",
                    //"aḥ", "aḥ", "aḥ", "aḥ", "aḥ", "āḥ", "āḥ", "iḥ", "iḥ", "iḥ", "īḥ", "īḥ",
                    //"uḥ", "uḥ", "ūḥ", "aiḥ", "aiḥ", "oḥ", "oḥ", "oḥ", "auḥ",
            },
            new String[]{"a'","i'","u'", "o'", "a'","i'","u'", "o'",
            },
            new String[]{
                    "k", "k", "k", "k", "k", "k", "k", "k", "kṛ", "kṝ", "kḷ",
                    "c", "c", "c", "c", "c", "c", "cṛ", "cṝ", "cḷ",
                    "ṭ", "t", "t", "t", "t", "t", "t", "t", "t", "t", "tṛ", "tṝ", "tṛ", "tṝ",
                    "p", "p", "p", "p", "p", "p", "p", "p", "p", "p", "pṛ", "pṝ", "pḷ", "pḹ", "pṛ", "pṝ", "p", "p",
                    "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "khṛ", "khṝ", "khḷ", "khḹ",
                    "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "chṛ", "chṝ", "chḷ", "chḹ",
                    "ṭh", "th", "th", "th", "th", "th", "th", "th", "th", "th", "thṛ", "thṝ", "thḷ", "thḹ",
                    "ph", "ph", "ph", "ph", "ph", "ph", "ph", "phṛ", "phṝ", "phḷ", "phḹ",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh",
                    "ṅ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh",
                    "ṅ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ṣ", "s",
                    "g", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh", "n", "m", "v", "s",
                    "g", "ḍ", "d", "b", "g", "d", "b", "dh", "bh", "n", "m", "v",
                    "g", "ḍ", "d", "b", "g", "d", "b", "dh", "bh", "n", "m", "v",
            },
            new String[]{
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "kṛ", "kṛ", "kṛ", "kṛ", "kṛ", "nkṛ", "rkṛ",
                    "kṝ", "kṝ", "kṝ", "kṝ", "kṝ", "nkṝ", "rkṝ",
                    "kḷ", "kḷ", "kḷ", "kḷ", "kḷ", "nkḷ", "rkḷ",

                    "c", "c", "c", "c", "c", "c", "cṛ", "cṝ", "cḷ",
                    "ṭ", "t", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "tṛ", "tṛ", "tṛ", "tṛ", "tṛ", "ntṛ", "rtṛ",
                    "tṝ", "tṝ", "tṝ", "tṝ", "tṝ", "ntṝ", "rtṝ",
                    "tṛ", "tṛ", "tṛ", "tṛ", "tṛ", "ntṛ", "rtṛ",
                    "tṝ", "tṝ", "tṝ", "tṝ", "tṝ", "ntṝ", "rtṝ",

                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "pṛ", "pṛ", "pṛ", "pṛ", "pṛ", "npṛ", "rpṛ",
                    "pṝ", "pṝ", "pṝ", "pṝ", "pṝ", "npṝ", "rpṝ",
                    "pḷ", "pḷ", "pḷ", "pḷ", "pḷ", "npḷ", "rpḷ",
                    "pḹ", "pḹ", "pḹ", "pḹ", "pḹ", "npḹ", "rpḹ",
                    "pṛ", "pṛ", "pṛ", "pṛ", "pṛ", "npṛ", "rpṛ",
                    "pṝ", "pṝ", "pṝ", "pṝ", "pṝ", "npṝ", "rpṝ",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",

                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "khṛ", "khṛ", "khṛ", "khṛ", "khṛ", "nkhṛ", "rkhṛ",
                    "khṝ", "khṝ", "khṝ", "khṝ", "khṝ", "nkhṝ", "rkhṝ",
                    "khḷ", "khḷ", "khḷ", "khḷ", "khḷ", "nkhḷ", "rkhḷ",
                    "khḹ", "khḹ", "khḹ", "khḹ", "khḹ", "nkhḹ", "rkhḹ",

                    "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "chṛ", "chṝ", "chḷ", "chḹ",
                    "ṭh", "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "thṛ", "thṛ", "thṛ", "thṛ", "thṛ", "nthṛ", "rthṛ",
                    "thṝ", "thṝ", "thṝ", "thṝ", "thṝ", "nthṝ", "rthṝ",
                    "thḷ", "thḷ", "thḷ", "thḷ", "thḷ", "nthḷ", "rthḷ",
                    "thḹ", "thḹ", "thḹ", "thḹ", "thḹ", "nthḹ", "rthḹ",

                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "phṛ", "phṛ", "phṛ", "phṛ", "phṛ", "nphṛ", "rphṛ",
                    "phṝ", "phṝ", "phṝ", "phṝ", "phṝ", "nphṝ", "rphṝ",
                    "phḷ", "phḷ", "phḷ", "phḷ", "phḷ", "nphḷ", "rphḷ",
                    "phḹ", "phḹ", "phḹ", "phḹ", "phḹ", "nphḹ", "rphḹ",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "g", "g", "g", "g", "g", "ng", "rg",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "g", "g", "g", "g", "g", "ng", "rg",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",
            },
            new String[]{"t", "d", "m", "r", "dh", "b", "t", "d", "m", "r", "dh", "bh", "nt", "nt", "nk", "ṣ"},
            new String[]{"it", "it", "ati", "adva", "aṣ", "arma", "ardha", "abi", "ab", "aya"},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{1, 2, 3, 3, 1}, 0.15, 0.75, 0.0, 0.12, null, true)
            .addModifiers(Modifier.replacementTable("ṛṝḷḹḍṭṅṇṣṃḥ", "ŗŕļĺđţńņşĕĭ"));

    /**
     * A mix of four different languages, using only ASCII characters, that is meant for generating single words for
     * creature or place names in fantasy settings.
     * <br>
     * Adeni, Sainane, Caneros, Sune, Alade, Tidifi, Muni, Gito, Lixoi, Bovi...
     */
    public static final SquidFin FANTASY_NAME = GREEK_ROMANIZED.mix(
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
    public static final SquidFin FANCY_FANTASY_NAME = FANTASY_NAME.addAccents(0.47, 0.07);

            /**
             * Zero-arg constructor for a SquidFin; produces a SquidFin equivalent to SquidFin.ENGLISH .
             */
    public SquidFin() {
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
    public SquidFin(String[] openingVowels, String[] midVowels, String[] openingConsonants,
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
    public SquidFin(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                    String[] midConsonants, String[] closingConsonants, String[] closingSyllables, String[] vowelSplitters,
                    int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                    double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency,
                    Pattern[] sane, boolean clean) {
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
        sanityChecks = sane;
        modifiers = new ArrayList<Modifier>(16);
    }

    private SquidFin(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                     String[] midConsonants, String[] closingConsonants, String[] closingSyllables,
                     String[] vowelSplitters, LinkedHashMap<Integer, Double> syllableFrequencies,
                     double vowelStartFrequency, double vowelEndFrequency, double vowelSplitFrequency,
                     double syllableEndFrequency, Pattern[] sanityChecks, boolean clean,
                     List<Modifier> modifiers) {
        this.openingVowels = copyStrings(openingVowels);
        this.midVowels = copyStrings(midVowels);
        this.openingConsonants = copyStrings(openingConsonants);
        this.midConsonants = copyStrings(midConsonants);
        this.closingConsonants = copyStrings(closingConsonants);
        this.closingSyllables = copyStrings(closingSyllables);
        this.vowelSplitters = copyStrings(vowelSplitters);
        this.syllableFrequencies = new LinkedHashMap<Integer, Double>(syllableFrequencies);
        this.vowelStartFrequency = vowelStartFrequency;
        this.vowelEndFrequency = vowelEndFrequency;
        this.vowelSplitFrequency = vowelSplitFrequency;
        this.syllableEndFrequency = syllableEndFrequency;
        for (Double freq : this.syllableFrequencies.values()) {
            totalSyllableFrequency += freq;
        }
        if (sanityChecks == null)
            this.sanityChecks = null;
        else {
            this.sanityChecks = new Pattern[sanityChecks.length];
            System.arraycopy(sanityChecks, 0, this.sanityChecks, 0, sanityChecks.length);
        }
        this.clean = clean;
        this.modifiers = new ArrayList<Modifier>(modifiers);
    }

    protected boolean checkAll(CharSequence testing, Pattern[] checks)
    {
        CharSequence fixed = removeAccents(testing);
        for (int i = 0; i < checks.length; i++) {
            if(checks[i].matcher(fixed).find())
                return false;
        }
        return true;
    }
    /**
     * Generate a word from this SquidFin, using and changing the current seed.
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(boolean capitalize)
    {
        return word(srng, capitalize);
    }
    /**
     * Generate a word from this SquidFin using the specified RNG.
     *
     * @param rng        the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(RNG rng, boolean capitalize) {
        while(true) {
            StringBuilder sb = new StringBuilder(20);
            double syllableChance = rng.nextDouble(totalSyllableFrequency);
            int syllables = 1, i = 0;
            for (Map.Entry<Integer, Double> kv : syllableFrequencies.entrySet()) {
                if (syllableChance < kv.getValue()) {
                    syllables = kv.getKey();
                    break;
                } else
                    syllableChance -= kv.getValue();
            }
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                sb.append(rng.getRandomElement(midConsonants));
                i++;
            } else {
                sb.append(rng.getRandomElement(openingConsonants));
            }

            for (; i < syllables - 1; i++) {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                sb.append(rng.getRandomElement(midConsonants));
            }
            if (rng.nextDouble() < syllableEndFrequency) {
                String close = rng.getRandomElement(closingSyllables);
                if((close.contains("@1") && syllables == 1) ||
                        (close.contains("@2") && syllables == 2) ||
                        (close.contains("@3") && syllables == 3) )
                {
                    sb.append(close.replaceAll("@\\d", sb.toString()));
                }
                else if(!close.contains("@"))
                    sb.append(close);
                else if (rng.nextDouble() < vowelEndFrequency) {
                    sb.append(rng.getRandomElement(midVowels));
                    if (rng.nextDouble() < vowelSplitFrequency) {
                        sb.append(rng.getRandomElement(vowelSplitters));
                        sb.append(rng.getRandomElement(midVowels));
                    }
                }
            } else {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                if (rng.nextDouble() >= vowelEndFrequency) {
                    sb.append(rng.getRandomElement(closingConsonants));
                    if (rng.nextDouble() < syllableEndFrequency) {
                        String close = rng.getRandomElement(closingSyllables);
                        if((close.contains("@1") && syllables == 1) ||
                                (close.contains("@2") && syllables == 2) ||
                                (close.contains("@3") && syllables == 3) )
                        {
                            sb.append(close.replaceAll("@\\d", sb.toString()));
                        }
                        else if(!close.contains("@"))
                            sb.append(close);
                    }
                }
            }
            if(sanityChecks != null && !checkAll(sb, sanityChecks))
                continue;

            for(Modifier mod : modifiers)
            {
                sb = mod.modify(rng, sb);
            }

            if (capitalize)
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

            if(clean && !checkAll(sb, vulgarChecks))
                continue;
            return sb.toString();
        }
    }/**
     * Generate a word from this SquidFin using the specified RNG.
     *
     * @param rng        the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(RNG rng, boolean capitalize, int approxSyllables) {
        if(approxSyllables <= 0)
        {
            String finished = rng.getRandomElement(openingVowels);
            if(capitalize) return finished.substring(0, 1).toUpperCase();
            else return finished.substring(0, 1);
        }
        while(true) {
            StringBuilder sb = new StringBuilder(20);
            int i = 0;
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                sb.append(rng.getRandomElement(midConsonants));
                i++;
            } else {
                sb.append(rng.getRandomElement(openingConsonants));
            }

            for (; i < approxSyllables - 1; i++) {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                sb.append(rng.getRandomElement(midConsonants));
            }
            if (rng.nextDouble() < syllableEndFrequency) {
                String close = rng.getRandomElement(closingSyllables);
                if((close.contains("@1") && approxSyllables == 1) || (close.contains("@2") && approxSyllables == 2) ||
                        (close.contains("@3") && approxSyllables == 3) )
                {
                    sb.append(close.replaceAll("@\\d", sb.toString()));
                }
                else if(!close.contains("@"))
                    sb.append(close);
                else if (rng.nextDouble() < vowelEndFrequency) {
                    sb.append(rng.getRandomElement(midVowels));
                    if (rng.nextDouble() < vowelSplitFrequency) {
                        sb.append(rng.getRandomElement(vowelSplitters));
                        sb.append(rng.getRandomElement(midVowels));
                    }
                }
            } else {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                if (rng.nextDouble() >= vowelEndFrequency) {
                    sb.append(rng.getRandomElement(closingConsonants));
                    if (rng.nextDouble() < syllableEndFrequency) {
                        String close = rng.getRandomElement(closingSyllables);
                        if((close.contains("@1") && approxSyllables == 1) ||
                                (close.contains("@2") && approxSyllables == 2) ||
                                (close.contains("@3") && approxSyllables == 3) )
                        {
                            close = close.replaceAll("@\\d", sb.toString());
                            sb.append(close);
                        }
                        else if(!close.contains("@"))
                            sb.append(close);
                    }
                }
            }

            if(sanityChecks != null && !checkAll(sb, sanityChecks))
                continue;

            for(Modifier mod : modifiers)
            {
                sb = mod.modify(rng, sb);
            }

            if (capitalize)
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

            if(clean && !checkAll(sb, vulgarChecks))
                continue;
            return sb.toString();
        }
    }

    /**
     * Generate a sentence from this SquidFin, using and changing the current seed.
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords)
    {
        return sentence(srng, minWords, maxWords, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", ".", "!", "?", "..."}, 0.2);
    }

    /**
     * Generate a sentence from this SquidFin, using and changing the current seed.
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
        return sentence(srng, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency);
    }
    /**
     * Generate a sentence from this SquidFin using the specific RNG.
     *
     * @param rng                     the RNG to use for the randomized string building
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
    public String sentence(RNG rng, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency) {
        if (minWords < 1)
            minWords = 1;
        if (minWords > maxWords)
            maxWords = minWords;
        if (midPunctuationFrequency > 1.0) {
            midPunctuationFrequency = 1.0 / midPunctuationFrequency;
        }
        StringBuilder sb = new StringBuilder(12 * maxWords);
        sb.append(word(rng, true));
        for (int i = 1; i < minWords; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(rng, false));
        }
        for (int i = minWords; i < maxWords && rng.nextInt(2 * maxWords) > i; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(rng, false));
        }
        sb.append(rng.getRandomElement(endPunctuation));
        return sb.toString();
    }
    /**
     * Generate a sentence from this SquidFin that fits in the given length limit..
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
        return sentence(srng, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency, maxChars);
    }
    /**
     * Generate a sentence from this SquidFin using the specific RNG that fits in the given length limit.
     *
     * @param rng                     the RNG to use for the randomized string building
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
    public String sentence(RNG rng, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency, int maxChars) {
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
        String next = word(rng, true);
        while (next.length() >= maxChars - 1 && frustration < 50) {
            next = word(rng, true);
            frustration++;
        }
        if(frustration >= 50) return "!";
        sb.append(next);
        for (int i = 1; i < minWords && frustration < 50 && sb.length() < maxChars - 7; i++) {
            if (rng.nextDouble() < midPunctuationFrequency && sb.length() < maxChars - 3) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            next = word(rng, false);
            while (sb.length() + next.length() >= maxChars - 2 && frustration < 50) {
                next = word(rng, false);
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
            next = word(rng, false);
            while (sb.length() + next.length() >= maxChars - 2 && frustration < 50) {
                next = word(rng, false);
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

    protected String[] merge1000(RNG rng, String[] me, String[] other, double otherInfluence) {
        if(other.length <= 0 && me.length <= 0)
            return new String[]{};
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * otherInfluence);
        int idx = 0;
        if (other.length > 0) {
            String[] tmp = new String[other.length];
            rng.shuffle(other, tmp);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        }
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
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


    protected String[] accentVowels(RNG rng, String[] me, double influence) {
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * influence);
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
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

    protected String[] accentConsonants(RNG rng, String[] me, double influence) {
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * influence);
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
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

    protected String[] accentBoth(RNG rng, String[] me, double vowelInfluence, double consonantInfluence) {
        String[] ret = new String[1000];
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
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

    public SquidFin mix(SquidFin other, double otherInfluence) {
        otherInfluence = Math.max(0.0, Math.min(otherInfluence, 1.0));
        double myInfluence = 1.0 - otherInfluence;

        RNG rng = new RNG((hashCode() & 0xffffffffL) | ((other.hashCode() & 0xffffffffL) << 32)
                ^ Double.doubleToLongBits(otherInfluence));

        String[] ov = merge1000(rng, openingVowels, other.openingVowels, otherInfluence),
                mv = merge1000(rng, midVowels, other.midVowels, otherInfluence),
                oc = merge1000(rng, openingConsonants, other.openingConsonants, otherInfluence *
                        Math.max(0.0, Math.min(1.0, (1.0 - other.vowelStartFrequency + vowelStartFrequency)))),
                mc = merge1000(rng, midConsonants, other.midConsonants, otherInfluence),
                cc = merge1000(rng, closingConsonants, other.closingConsonants, otherInfluence *
                        Math.max(0.0, Math.min(1.0, (1.0 - other.vowelEndFrequency + vowelEndFrequency)))),
                cs = merge1000(rng, closingSyllables, other.closingSyllables, otherInfluence *
                        Math.max(0.0, Math.min(1.0, (other.syllableEndFrequency - syllableEndFrequency)))),
                splitters = merge1000(rng, vowelSplitters, other.vowelSplitters, otherInfluence);

        LinkedHashMap<Integer, Double> freqs = new LinkedHashMap<Integer, Double>(syllableFrequencies);
        for (Map.Entry<Integer, Double> kv : other.syllableFrequencies.entrySet()) {
            if (freqs.containsKey(kv.getKey()))
                freqs.put(kv.getKey(), kv.getValue() + freqs.get(kv.getKey()));
            else
                freqs.put(kv.getKey(), kv.getValue());
        }
        List<Modifier> mods = new ArrayList<Modifier>((int)(Math.ceil(modifiers.size() * myInfluence) +
                Math.ceil(other.modifiers.size() * otherInfluence)));
        mods.addAll(rng.randomPortion(modifiers, (int)Math.ceil(modifiers.size() * myInfluence)));
        mods.addAll(rng.randomPortion(other.modifiers, (int)Math.ceil(other.modifiers.size() * otherInfluence)));
        SquidFin finished = new SquidFin(ov, mv, oc, mc, cc, cs, splitters, freqs,
                vowelStartFrequency * myInfluence + other.vowelStartFrequency * otherInfluence,
                vowelEndFrequency * myInfluence + other.vowelEndFrequency * otherInfluence,
                vowelSplitFrequency * myInfluence + other.vowelSplitFrequency * otherInfluence,
                syllableEndFrequency * myInfluence + other.syllableEndFrequency * otherInfluence,
                (sanityChecks == null) ? other.sanityChecks : sanityChecks, true, mods);
        return finished;
    }

    public SquidFin addAccents(double vowelInfluence, double consonantInfluence) {
        vowelInfluence = Math.max(0.0, Math.min(vowelInfluence, 1.0));
        consonantInfluence = Math.max(0.0, Math.min(consonantInfluence, 1.0));

        RNG rng = new RNG((hashCode() & 0xffffffffL) ^
                ((Double.doubleToLongBits(vowelInfluence) & 0xffffffffL) | (Double.doubleToLongBits(consonantInfluence) << 32)));
        String[] ov = accentVowels(rng, openingVowels, vowelInfluence),
                mv = accentVowels(rng, midVowels, vowelInfluence),
                oc = accentConsonants(rng, openingConsonants, consonantInfluence),
                mc = accentConsonants(rng, midConsonants, consonantInfluence),
                cc = accentConsonants(rng, closingConsonants, consonantInfluence),
                cs = accentBoth(rng, closingSyllables, vowelInfluence, consonantInfluence);


        SquidFin finished = new SquidFin(ov, mv, oc, mc, cc, cs, vowelSplitters, syllableFrequencies,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean, modifiers);
        return finished;
    }
    static String[] copyStrings(String[] start)
    {
        String[] next = new String[start.length];
        System.arraycopy(start, 0, next, 0, start.length);
        return next;
    }
    public SquidFin removeAccents() {

        String[] ov = copyStrings(openingVowels),
                mv = copyStrings(midVowels),
                oc = copyStrings(openingConsonants),
                mc = copyStrings(midConsonants),
                cc = copyStrings(closingConsonants),
                cs = copyStrings(closingSyllables);
        for (int i = 0; i < ov.length; i++) {
            ov[i] = removeAccents(openingVowels[i]).toString();
        }
        for (int i = 0; i < mv.length; i++) {
            mv[i] = removeAccents(midVowels[i]).toString();
        }
        for (int i = 0; i < oc.length; i++) {
            oc[i] = removeAccents(openingConsonants[i]).toString();
        }
        for (int i = 0; i < mc.length; i++) {
            mc[i] = removeAccents(midConsonants[i]).toString();
        }
        for (int i = 0; i < cc.length; i++) {
            cc[i] = removeAccents(closingConsonants[i]).toString();
        }
        for (int i = 0; i < cs.length; i++) {
            cs[i] = removeAccents(closingSyllables[i]).toString();
        }

        return new SquidFin(ov, mv, oc, mc, cc, cs, vowelSplitters, syllableFrequencies,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean, modifiers);
    }

    /**
     * Adds the specified Modifier objects from a Collection to a copy of this SquidFin and returns it.
     * You can obtain a Modifier with the static constants in the SquidFin.Modifier nested class, the
     * SquidFin.modifier() method, or Modifier's constructor.
     * @param mods an array or vararg of Modifier objects
     * @return a copy of this with the Modifiers added
     */
    public SquidFin addModifiers(Collection<Modifier> mods)
    {
        SquidFin next = copy();
        next.modifiers.addAll(mods);
        return next;
    }

    /**
     * Adds the specified Modifier objects to a copy of this SquidFin and returns it.
     * You can obtain a Modifier with the static constants in the SquidFin.Modifier nested class, the
     * SquidFin.modifier() method, or Modifier's constructor.
     * @param mods an array or vararg of Modifier objects
     * @return a copy of this with the Modifiers added
     */
    public SquidFin addModifiers(Modifier... mods)
    {
        SquidFin next = copy();
        Collections.addAll(next.modifiers, mods);
        return next;
    }

    /**
     * Creates a copy of this SquidFin with no modifiers.
     * @return a copy of this SquidFin with modifiers removed.
     */
    public SquidFin removeModifiers()
    {
        SquidFin next = copy();
        next.modifiers.clear();
        return next;
    }

    public static Modifier modifier(String pattern, String replacement)
    {
        return new Modifier(pattern, replacement);
    }
    public static Modifier modifier(String pattern, String replacement, double chance)
    {
        return new Modifier(pattern, replacement, chance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SquidFin that = (SquidFin) o;

        if (clean != that.clean) return false;
        if (Double.compare(that.totalSyllableFrequency, totalSyllableFrequency) != 0) return false;
        if (Double.compare(that.vowelStartFrequency, vowelStartFrequency) != 0) return false;
        if (Double.compare(that.vowelEndFrequency, vowelEndFrequency) != 0) return false;
        if (Double.compare(that.vowelSplitFrequency, vowelSplitFrequency) != 0) return false;
        if (Double.compare(that.syllableEndFrequency, syllableEndFrequency) != 0) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(openingVowels, that.openingVowels)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(midVowels, that.midVowels)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(openingConsonants, that.openingConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(midConsonants, that.midConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(closingConsonants, that.closingConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(vowelSplitters, that.vowelSplitters)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(closingSyllables, that.closingSyllables)) return false;
        if (!syllableFrequencies.equals(that.syllableFrequencies)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(sanityChecks, that.sanityChecks)) return false;
        return modifiers != null ? modifiers.equals(that.modifiers) : that.modifiers == null;
    }

    @Override
    public int hashCode() {
        long result;
        long temp;
        result = hash64(openingVowels);
        result = 31 * result + hash64(midVowels);
        result = 31 * result + hash64(openingConsonants);
        result = 31 * result + hash64(midConsonants);
        result = 31 * result + hash64(closingConsonants);
        result = 31 * result + hash64(vowelSplitters);
        result = 31 * result + hash64(closingSyllables);
        result = 31 * result + (clean ? 1 : 0);
        result = 31 * result + syllableFrequencies.hashCode();
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
        result = 31 * result + (modifiers != null ? modifiers.hashCode() : 0);
        return (int)(result ^ (result >>> 32));
    }

    @Override
    public String toString() {
        return "SquidFin{" +
                "openingVowels=" + Arrays.toString(openingVowels) +
                ", midVowels=" + Arrays.toString(midVowels) +
                ", openingConsonants=" + Arrays.toString(openingConsonants) +
                ", midConsonants=" + Arrays.toString(midConsonants) +
                ", closingConsonants=" + Arrays.toString(closingConsonants) +
                ", vowelSplitters=" + Arrays.toString(vowelSplitters) +
                ", closingSyllables=" + Arrays.toString(closingSyllables) +
                ", clean=" + clean +
                ", syllableFrequencies=" + syllableFrequencies +
                ", totalSyllableFrequency=" + totalSyllableFrequency +
                ", vowelStartFrequency=" + vowelStartFrequency +
                ", vowelEndFrequency=" + vowelEndFrequency +
                ", vowelSplitFrequency=" + vowelSplitFrequency +
                ", syllableEndFrequency=" + syllableEndFrequency +
                ", sanityChecks=" + Arrays.toString(sanityChecks) +
                ", modifiers=" + modifiers +
                '}';
    }

    public SquidFin copy()
    {
        return new SquidFin(openingVowels, midVowels, openingConsonants, midConsonants,
                closingConsonants, closingSyllables, vowelSplitters, syllableFrequencies, vowelStartFrequency,
                vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency, sanityChecks, clean, modifiers);
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

    public static class Modifier implements Serializable
    {
        private static final long serialVersionUID = 1734863678490422371L;
        public final Alteration[] alterations;
        public Modifier()
        {
            this("sh?", "th");
        }
        public Modifier(String pattern, String replacement)
        {
            alterations = new Alteration[]{new Alteration(pattern, replacement)};
        }

        public Modifier(String pattern, String replacement, double chance)
        {
            alterations = new Alteration[]{new Alteration(pattern, replacement, chance)};
        }

        public Modifier(Alteration... alts)
        {
            alterations = (alts == null) ? new Alteration[0] : alts;
        }

        public StringBuilder modify(RNG rng, StringBuilder sb)
        {
            Matcher m;
            Replacer.StringBuilderBuffer tb, working = Replacer.wrap(sb);
            String tmp;
            boolean found;
            for(Alteration alt : alterations) {
                tmp = working.toString();
                tb = Replacer.wrap(new StringBuilder(tmp.length()));
                m = alt.replacer.getPattern().matcher(tmp);

                found = false;
                while (true) {
                    //tb.append(m.prefix());
                    if (rng.nextDouble() < alt.chance) {
                        if(!Replacer.replaceStep(m, alt.replacer.getSubstitution(), tb))
                            break;
                        found = true;
                    } else {
                        if(!m.find())
                            break;
                        found = true;
                        m.getGroup(MatchResult.PREFIX, tb);
                        m.getGroup(MatchResult.MATCH, tb);
                        m.setTarget(m, MatchResult.SUFFIX);
                    }
                }
                if (found) {
                    m.getGroup(MatchResult.TARGET, tb);
                    //tb.append(m.suffix());
                    working = tb;
                }
            }
            return working.toStringBuilder();
        }

        /**
         * For a character who always pronounces 's', 'ss', and 'sh' as 'th'.
         */
        public static final Modifier LISP = new Modifier("[tţťțṭ]?[sśŝşšș]+h?", "th");

        /**
         * For a character who always lengthens 's' and 'z' sounds not starting a word.
         */
        public static final Modifier HISS = new Modifier("(.)([sśŝşšșzźżž])+", "$1$2$2$2");

        /**
         * For a character who has a 20% chance to repeat a starting consonant or vowel.
         */
        public static final Modifier STUTTER = new Modifier(
                new Alteration("^([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоу]+)", "$1-$1", 0.2),
                new Alteration("^([aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųαοειυаеёийъыэюяоу]+)", "$1-$1", 0.2));

        /**
         * For a language that has a 40% chance to repeat a single Latin vowel (a, e, o, or a variant on one of them
         * like æ or ö).
         */
        public static final Modifier DOUBLE_VOWELS = new Modifier(
                "([aàáâãäåæāăąǻǽeèéêëēĕėęěòóôõöøōŏőœǿ])([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳ]|$)", "$1$1$2", 0.4);


        /**
         * For a language that has a 50% chance to repeat a single consonant.
         */
        public static final Modifier DOUBLE_CONSONANTS = new Modifier("([aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоу])" +
                "([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоуqwhjx])" +
                "([aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоу]|$)", "$1$2$2$3", 0.5);

        /**
         * For a language that never repeats the same letter twice in a row.
         */
        public static final Modifier NO_DOUBLES = new Modifier("(.)\\1", "$1");

        /**
         * Creates a Modifier that will replace the nth char in initial with the nth char in change. Expects initial and
         * change to be the same length, but will use the lesser length if they are not equal-length. Because of the
         * state of the text at the time modifiers are run, only lower-case letters need to be searched for.
         * @param initial a String containing lower-case letters or other symbols to be swapped out of a text
         * @param change a String containing characters that will replace occurrences of characters in initial
         * @return a Modifier that can be added to a SquidFin with its addModifiers() method
         */
        public static Modifier replacementTable(String initial, String change)
        {
            Alteration[] alts = new Alteration[Math.min(initial.length(), change.length())];
            for (int i = 0; i < alts.length; i++) {
                //literal string syntax; avoids sensitive escaping issues and also doesn't need a character class,
                // which is slightly slower and has some odd escaping cases.
                alts[i] = new Alteration("\\Q" + initial.charAt(i), change.substring(i, i+1));
            }
            return new Modifier(alts);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Modifier modifier = (Modifier) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(alterations, modifier.alterations);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(alterations);
        }

        @Override
        public String toString() {
            return "Modifier{" +
                    "alterations=" + Arrays.toString(alterations) +
                    '}';
        }
    }

    public static class Alteration implements Serializable
    {
        private static final long serialVersionUID = -2138854697837563188L;
        public Replacer replacer;
        public double chance;
        public Alteration()
        {
            this("[tţťțṭ]?[sśŝşšș]+h?", "th");
        }
        public Alteration(String pattern, String replacement)
        {
            replacer = Pattern.compile(pattern).replacer(replacement);
            chance = 1.0;
        }
        public Alteration(String pattern, String replacement, double chance)
        {
            replacer = Pattern.compile(pattern).replacer(replacement);
            this.chance = chance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Alteration that = (Alteration) o;

            if (Double.compare(that.chance, chance) != 0) return false;
            return replacer.equals(that.replacer);

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = replacer.hashCode();
            temp = Double.doubleToLongBits(chance);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Alteration{" +
                    "replacer=" + replacer +
                    ", chance=" + chance +
                    '}';
        }
    }

}
