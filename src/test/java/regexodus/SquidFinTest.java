package regexodus;

/**
 * Created by Tommy Ettinger on 5/18/2016.
 */
public class SquidFinTest {
    public static void main(String[] args)
    {
        SquidFin flg = SquidFin.ENGLISH;
        SquidFin.RNG rng = new SquidFin.RNG(0xBABABADAL);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 10, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.LOVECRAFT;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 3, 9, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.GREEK_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.GREEK_AUTHENTIC;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.FRENCH;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 12, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.RUSSIAN_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.25));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.RUSSIAN_AUTHENTIC;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.25));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.JAPANESE_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 13, new String[]{",", ",", ",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.12));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.SWAHILI;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?"}, 0.12));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.SOMALI;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?"}, 0.12));
        }


        rng.setState(0xf00df00L);
        flg = SquidFin.ENGLISH.mix(SquidFin.FRENCH.removeAccents(), 0.5);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.RUSSIAN_ROMANIZED.mix(SquidFin.ENGLISH, 0.35);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 10, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.22));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.FRENCH.mix(SquidFin.GREEK_ROMANIZED, 0.55);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.22));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.ENGLISH.mix(SquidFin.GREEK_AUTHENTIC, 0.25);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.ENGLISH.addAccents(0.5, 0.15);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        rng.setState(0xf00df00L);
        flg = SquidFin.FRENCH.mix(SquidFin.JAPANESE_ROMANIZED, 0.65);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "...", "..."}, 0.17));
        }

        rng.setState(0xf00df00L);
        flg = SquidFin.RUSSIAN_ROMANIZED.mix(SquidFin.JAPANESE_ROMANIZED, 0.75);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.2));
        }

        rng.setState(0xf00df00L);
        flg = SquidFin.ENGLISH.addModifiers(SquidFin.Modifier.NO_DOUBLES);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        rng.setState(0xf00df00L);
        flg = SquidFin.JAPANESE_ROMANIZED.addModifiers(SquidFin.Modifier.DOUBLE_CONSONANTS);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.SOMALI.mix(SquidFin.JAPANESE_ROMANIZED, 0.3).mix(SquidFin.SWAHILI, 0.1);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.15));
        }

        rng.setState(0xf00df00L);
        flg = SquidFin.FANTASY_NAME;
        System.out.print(flg.word(rng, true, rng.nextInt(2, 4)));
        for (int i = 1; i < 10; i++) {
            System.out.print(", " + flg.word(rng, true, rng.nextInt(2, 4)));
        }
        System.out.println("...");

        rng.setState(0xf00df00L);
        flg = SquidFin.FANCY_FANTASY_NAME;
        System.out.print(flg.word(rng, true, rng.nextInt(2, 4)));
        for (int i = 1; i < 10; i++) {
            System.out.print(", " + flg.word(rng, true, rng.nextInt(2, 4)));
        }
        System.out.println("...");
        System.out.println('"' + SquidFin.ENGLISH.sentence(rng, 4, 7, new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        System.out.println('"' + SquidFin.JAPANESE_ROMANIZED.sentence(rng, 4, 7, new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        System.out.println('"' + SquidFin.FRENCH.sentence(rng, 5, 8, new String[]{" -", ",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.1) + "\",");
        System.out.println('"' + SquidFin.GREEK_ROMANIZED.sentence(rng, 5, 8, new String[]{",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.15) + "\",");
        System.out.println('"' + SquidFin.GREEK_AUTHENTIC.sentence(rng, 5, 8, new String[]{",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.15) + "\",");
        System.out.println('"' + SquidFin.RUSSIAN_ROMANIZED.sentence(rng, 4, 7, new String[]{" -", ",", ",", ",", ";"}, new String[]{"!", "!", ".", "...", ".", "?"}, 0.22) + "\",");
        System.out.println('"' + SquidFin.RUSSIAN_AUTHENTIC.sentence(rng, 4, 7, new String[]{" -", ",", ",", ",", ";"}, new String[]{"!", "!", ".", "...", ".", "?"}, 0.22) + "\",");
        System.out.println('"' + SquidFin.LOVECRAFT.sentence(rng, 4, 7, new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        System.out.println('"' + SquidFin.SWAHILI.sentence(rng, 4, 7, new String[]{",", ",", ";"}, new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = SquidFin.FRENCH.mix(SquidFin.JAPANESE_ROMANIZED, 0.65);
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.17) + "\",");
        flg = SquidFin.ENGLISH.addAccents(0.5, 0.15);
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.17) + "\",");
        flg = SquidFin.RUSSIAN_AUTHENTIC.mix(SquidFin.GREEK_AUTHENTIC, 0.5).mix(SquidFin.FRENCH, 0.35);
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.2) + "\",");
        flg = SquidFin.FANCY_FANTASY_NAME;
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.2) + "\",");
        flg = SquidFin.SWAHILI.mix(SquidFin.JAPANESE_ROMANIZED, 0.35); //.mix(SquidFin.FRENCH, 0.35)
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = SquidFin.SWAHILI.mix(SquidFin.JAPANESE_ROMANIZED, 0.32).mix(SquidFin.FANCY_FANTASY_NAME, 0.25);
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = SquidFin.SOMALI.mix(SquidFin.JAPANESE_ROMANIZED, 0.3).mix(SquidFin.SWAHILI, 0.15);
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");

        rng.setState(0xf00df00L);
        flg = SquidFin.HINDI_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?"}, 0.12));
        }
        rng.setState(0xf00df00L);
        flg = SquidFin.JAPANESE_ROMANIZED.addModifiers(SquidFin.Modifier.DOUBLE_CONSONANTS, SquidFin.Modifier.LISP);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

    }
}
