package regexodus;

import org.junit.Test;
/**
 * Unit test for Finnegan.
 */
public class FinneganTest
{
    @Test
    public void testFinnegan()
    {
        Finnegan fin = Finnegan.ENGLISH;
        fin.setSeed(0xf00df00l);

        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(5, 10, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17, 40));
        }
        fin = Finnegan.LOVECRAFT;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(3, 9, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15, 30));
        }
        fin = Finnegan.GREEK_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2, 50));
        }
        fin = Finnegan.GREEK_AUTHENTIC;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2, 50));
        }
        fin = Finnegan.FRENCH;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 12, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17, 30));
        }
        fin = Finnegan.RUSSIAN_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 13, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.25, 30));
        }
        fin = Finnegan.RUSSIAN_AUTHENTIC;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 13, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.25, 30));
        }

        fin = Finnegan.ENGLISH.mix(Finnegan.FRENCH, 0.5);
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        fin = Finnegan.FRENCH.mix(Finnegan.GREEK_ROMANIZED, 0.55);
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.22));
        }
        fin = Finnegan.ENGLISH.mix(Finnegan.GREEK_AUTHENTIC, 0.25);
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        fin = Finnegan.ENGLISH.addAccents(0.5, 0.15);
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        fin = Finnegan.RUSSIAN_ROMANIZED.mix(Finnegan.ENGLISH, 0.4);
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 10, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.22));
        }
        fin = Finnegan.RUSSIAN_AUTHENTIC.mix(Finnegan.ENGLISH, 0.4);
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(4, 10, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.22));
        }

        fin = Finnegan.JAPANESE_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(5, 8, new String[]{",", ",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.12, 30));
        }

        fin = Finnegan.FANTASY_NAME;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(3, 7, new String[]{",", ",", ",", ",", ";", ";"},
                    new String[]{"~"} /*{".", ".", ".", "!", "!", "?", "...", "..."}*/, 0.2, 30));
        }
        fin = Finnegan.FANCY_FANTASY_NAME;
        for (int i = 0; i < 40; i++) {
            System.out.println(fin.sentence(3, 7, new String[]{",", ",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "!", "?", "...", "..."}, 0.2, 30));
        }
        System.out.println(Finnegan.ENGLISH.word(0xbababadal, false));

    }
}
