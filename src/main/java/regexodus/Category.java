package regexodus;

import regexodus.ds.CharCharMap;
import regexodus.ds.IntBitSet;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Unicode categories constructed from tightly compressed string and array literals instead of large resources.
 * Credit for the technique and much of the code goes to gagern,
 * <a href="https://gist.github.com/gagern/89db1179766a702c564d">from this Gist</a>.
 * Also, the heavy amount of work that went into the Unicode DB for Node.JS (which the pre-processing stage for this
 * depends on) must be commended; that project is
 * <a href="https://github.com/mathiasbynens/node-unicode-data">node-unicode-data</a>.
 */
public class Category {
    public final int length;
    private int n;
    private final char[] cal;
    final Block[] blocks;
    private Category()
    {
        length = 0;
        cal = new char[0];
        blocks = new Block[0];
    }
    private Category(int[] directory, String data)
    {
        n = data.length();
        int j = 0, len = 0;
        cal = new char[n];
        for (int i = 0; i < n; ++i) {
            cal[i] = (char) (j += directory[data.charAt(i) - 32]);
            if((i & 1) == 1) len += 1 + j - cal[i-1];
        }
        length = len;
        blocks = makeBlocks();
    }

    public char[] contents()
    {
        int k = 0;
        char[] con = new char[length];
        for (int i = 0; i < n - 1; i += 2)
            for (char e = cal[i]; e <= cal[i+1]; ++e)
                con[k++] = e;
        return con;
    }

    private Block[] makeBlocks() {
        int k = 0;
        Block[] bls = new Block[256];
        IntBitSet[] bss = new IntBitSet[256];
        int e, e2, eb, e2b;
        for (int i = 0; i < n - 1; i += 2) {
            e = cal[i];
            e2 = cal[i+1];
            eb = e >>> 8;
            e2b = e2 >>> 8;
            if(bss[eb] == null) bss[eb] = new IntBitSet();
            if(eb == e2b)
            {
                bss[eb].set(e & 0xff, e2 & 0xff);
                continue;
            }
            bss[eb++].set(e & 0xff, 255);
            while (eb != e2b) {
                if(bss[eb] == null) bss[eb] = new IntBitSet();
                bss[eb++].set(0, 255);
            }
            if(bss[e2b] == null) bss[e2b] = new IntBitSet();
            bss[e2b].set(0, e2 & 0xff);
        }
        for (int i = 0; i < 256; i++) {
            if(bss[i] == null)
                bls[i] = new Block();
            else
                bls[i] = new Block(bss[i]);
        }
        return bls;
    }

    public boolean contains(char checking) {
        for (int i = 0; i < n - 1; i += 2) {
            if (checking >= cal[i] && checking <= cal[i + 1])
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Category{" +
                cal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (length != category.length) return false;
        if (n != category.n) return false;
        if(category.cal == null || !Arrays.equals(cal, category.cal)) return false;
        return Arrays.equals(blocks, category.blocks);

    }

    @Override
    public int hashCode() {
        int result = length;
        result = 31 * result + n;
        result = 31 * result + Arrays.hashCode(cal);
        result = 31 * result + Arrays.hashCode(blocks);
        return result;
    }

    /**
     * All control, format, surrogate, private use, and unassigned characters; Unicode category C.
     */
    public static final Category C = new Category(new int[]{0,1,2,3,4,5,8,6,7,11,9,10,12,14,13,23,15,16,27,25,32,20,24,31,34,44,19,21,33,37,50,56,60,87,90,17,28,29,30,36,39,43,49,52,57,61,63,71,95,104,127,18,22,26,40,42,45,46,48,51,54,55,58,59,66,68,73,76,78,79,81,96,102,107,117,126,136,141,162,167,185,191,193,199,207,215,349,350,367,378,398,446,535,667,670,715,1813,8451,11173,29294}," 7g4- \u007f!(#& \" ; z H![!$ ?(D#(1/ r J!@!h.@!>!1 E!\" ,$4(c n *!#!/ & \"\"%!+!#!%(\"## '!U!$ (##!/ & # # #!\" '##!$\"\"'% \"'S*$ + $ / & # '!) $ $!\"-%!.'& $ *!#!/ & # '!+!#!$'$## '!:*# (\"$ %\"# \" #\"#\"$\".#'\"$ %!\"%\".T$- $ 6 C!+ $ %'# $!\"!%!)'/ $ 6 ) '!+ $ %'#%# %!) $)- $ K $ (#C!2 $ :\"3 + \"!&\"\"#( \" *%)!$)_#FG# \" ' 3 \" 6!' \" & )!%7b =#V = 1 -Gs \"$\"!y %!& \" %!W %!8 %!& \" %!1 ^ %!a!<\"2%A!(!~\"B'/&3&;)- $ #)P!)%)%0 ,%B'9$O*4 .#.#\"\"I!'+X#2%,\"N!` F!,%)%0!4Zd o(M\"1\"N$9!,(9$|!(!H!(!* \" \" \" 4!\\ 1 0!( 5!$ + ,$2$>0#!D -\"8-8-m#};,5\u0080!< w$Y \"$\"!L'#.3&& & & & & & & & R<2 B)u3f A!Q$9 P A&J \u0083\"?&v:p(t!# \" *5@\")%L(O(.%j+7\"e ,#8 ?&0!)!Q/E*(!(!(&& & M#R!)%\u0082)6#>\u0081x!i=&)'$2 ' \" # # k0{!]'\"7I%K 5 %#' l#q\"(!(!(!$\"& &,#! ");

    /**
     * All control, format, surrogate, private use, and unassigned characters; Unicode category C.
     */
    public static final Category Other = C;

    /**
     * Returns true if c is an ISO control character, in the ranges {@code 0x00 to 0x1F} or {@code 0x7F to 0x9F}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode letter
     */
    public static boolean isISOControl(char c) {
        return c < 0x1F || (c >= 0x7F && c <= 0x9F);
    }

    /**
     * All private use characters; Unicode category Co.
     */
    public static final Category Co = new Category(new int[]{1792,6399,57344},"\"! ");

    /**
     * All private use characters; Unicode category Co.
     */
    public static final Category PrivateUse = Co;

    /**
     * All unassigned characters; Unicode category Cn.
     */
    public static final Category Cn = new Category(new int[]{0,1,2,3,4,5,8,6,7,11,9,10,13,14,23,12,27,16,25,15,20,24,32,34,44,19,21,31,33,37,56,61,87,90,17,28,29,30,36,39,43,50,52,57,60,63,71,95,102,104,127,18,22,26,40,42,45,46,48,49,51,54,55,58,59,66,68,73,78,79,81,107,117,126,136,141,167,185,191,199,207,215,238,271,349,350,378,398,446,535,667,670,888,1813,8815,11173,29294},"|!(#& \" : w G!\\!$ >(C#(+s ?!P,L!I!1 D!\" /$6 #$r *!#!. & \"\"%!+!#!%(\"## '!U!$ (##!. & # # #!\" '##!$\"\"'% \"'S*$ + $ . & # '!) $ $!\"-%!,'& $ *!#!. & # '!+!#!$'$## '!9*# (\"$ %\"# \" #\"#\"$\",#'\"$ %!\"%\",T$- $ 5 B!+ $ %'# $!\"!%!)'. $ 5 ) '!+ $ %'#%# %!) $)- $ J $ (#B!0 $ 9\"2 + \"!&\"\"#( \" *%)!$)`#EF# \" ' 2 \" 5!' \" & )!%;c =#V = 1 -Fo \"$\"!v %!& \" %!W %!7 %!& \" %!1 _ %!b!<\"0%@!(!{\"A'.&2&:)- $ #)O!)%)%0%A'8$N*6 ,#,#\"\"H!'+X#0%/\"M!a E!/%)%3!6Zd l(?\"1\"M$8!/(8$y!(!G!(!* \" \" \" 6!] 1 3!( 4!$ + P ,!C -\"7-7-k#z:/4}!< u$Y \"$\"!K'#,2&& & & & & & & & R<0 A)q2f @!Q$8 O @&[ \u0080\">&t9m(p!# \" *4L\")%K(N(,%h+;\"e /#7 >&3!)!Q.D*(!(!(&& & ?#R!)%\u007f)5#I#~!g=&)'$0 ' \" # # i3x!^'\";H%J 4 %#' j!\" n\"(!(!(!$\"& &*'! ");

    /**
     * All unassigned characters; Unicode category Cn.
     */
    public static final Category Unassigned = Cn;

    /**
     * All control characters; Unicode category Cc.
     */
    public static final Category Cc = new Category(new int[]{0,31,32,96}," !#\"");

    /**
     * All control characters; Unicode category Cc.
     */
    public static final Category Control = Cc;

    /**
     * All format characters; Unicode category Cf.
     */
    public static final Category Cf = new Category(new int[]{0,4,2,50,1,5,9,23,27,81,173,193,250,385,1363,2045,3884,56976},"* .%' + # -$) 0 /!(!#!\"&1 ,\"!");

    /**
     * All format characters; Unicode category Cf.
     */
    public static final Category Format = Cf;

    /**
     * All surrogate characters; Unicode category Cs.
     */
    public static final Category Cs = new Category(new int[]{2047,55296},"! ");

    /**
     * All surrogate characters; Unicode category Cs.
     */
    public static final Category Surrogate = Cs;

    /**
     * All letters; Unicode category L.
     */
    public static final Category L = new Category(new int[]{2,0,3,4,1,6,5,7,8,17,11,15,22,10,12,9,25,21,42,13,16,18,30,14,37,19,23,24,29,32,35,40,46,53,27,31,43,49,55,59,65,88,26,28,33,36,41,48,50,51,52,63,66,68,69,85,102,117,20,34,38,39,45,47,56,64,72,74,81,82,83,89,93,94,98,105,107,114,116,130,134,138,165,191,228,268,277,332,362,365,457,470,513,619,2684,6591,8453,11171,22156},"H0'0O!*!&!%, 6 z&*+#(! !o# $\"\" !'!   ! 9 e q/r 8\"!'?bJ&\"@2M$ j !4$($* \"!)! <6I.!0=-$&!%1&!-!#!;;(-%: &5NGA#!9!(/4+&'\"$\"1 % !#\"#!)!7$  +$*!/&&$\"1 % $ $ $=\" !Z )(   1 % $ ##!9!4$;!.'\"$\"1 % $ ##!C$  4!5! &#  \"#$ ! $#$# #*:!A'   , +#!B \"!\"$C!&'   , / ##!=$ $4$5(   ?\"!)!% / 0&%)#: ( !\"%G_ $3%G$ ! # : ! / $-!\"# !,\"L!a' >K#n21!)&&\"#!#$( &.3!58 !%!\"2 w \"\"% ! \"\"? \"\"= \"\"% ! \"\"7 ` \"\"T\\+)W\"&#}\"4 0%c''()75+)+.  4QM!&!UI(#\"L !%V*6P<\"#.D&0F,-Rf!i@5'F<7$*DB>2 *>\"-%2\" 2\" & $#!%sHv\"&\"8\"&\"' ! ! ! 6\"R % !#  %#\"\"&&.%  %Y!7!).X!&!\"/ !##'! ! ! \" -\"\"%#&!A$~t'\"#$38 !%!\"F(!),-% % % % % % % %d!{$D#%$&W'  g \"%2 h5CE+|\u007fH\u0082U^\"u#+*$1@)6\"VP(\"X\"T\"$ ! ',+   \" ,6Q+ES&#! $.B*,JK(@<!)# /*# ?;  '1,#!#E !#$\"#\"! !0 \"-( 3&\"&\"&-% % 2 3'm6\u00813,&O\u0080y\"k]%3#%! / . # ! $ $ l[x9S\"AN*Y# p80'0.I#&\"&\"&\" >");

    /**
     * All letters; Unicode category L.
     */
    public static final Category Letter = L;

    /**
     * Returns true if c is a Unicode letter, in category {@link #L}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode letter
     */
    public static boolean isLetter(char c) {
        return L.contains(c);
    }

    /**
     * All upper-case letters; Unicode category Lu.
     */
    public static final Category Lu = new Category(new int[]{0,2,3,1,4,7,9,5,6,8,10,11,13,25,12,37,49,16,20,22,34,36,42,47,50,62,65,73,85,102,136,197,263,290,321,723,2196,2685,2890,22316,31054},":-=3!(4 ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \" ! ! ! ! ! ! ! \" ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#! ! $#! !#!!\"\"!#!!$#!#! ! !#! \" !#!!! !#$ ) \" \" \" ! ! ! ! ! ! ! \" ! ! ! ! ! ! ! ! \" \" !!! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! )#!#\" !\"! ! ! ! A ! $ & % !!! !#!1!)5 \"!$ ! ! ! ! ! ! ! ! ! ! ! ( \" !#\"80 ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! * ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#! ! ! ! ! ! \" ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \"/F/! ( C<D %6\"!B ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! * ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! *%&'+%&%&'. ! ! ! &%;\",\",\",$.\"@ ' $!\"!\" $$% ! ! !\"\"\"+#( 9 E70 !!\" ! ! !\"! \" &!! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! & ! ' H ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! 2 ! ! ! ! ! ! ! ! ! ! ! ! ! > ! ! ! ! ! ! $ ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! + ! !#! ! ! ! ' ! \" ! $ ! ! ! ! ! ! ! ! ! !$!$! ! ! ! ! ! ! !\"! !#$ ( ! ! ! - G-?");

    /**
     * All upper-case letters; Unicode category Lu.
     */
    public static final Category UppercaseLetter = Lu;

    /**
     * Returns true if c is a Unicode upper-case letter, in category {@link #Lu}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode upper-case letter
     */
    public static boolean isUpperCase(char c) {
        return Lu.contains(c);
    }

    /**
     * All lower-case letters; Unicode category Ll.
     */
    public static final Category Ll = new Category(new int[]{0,2,3,1,4,5,7,9,6,8,11,13,42,25,28,47,10,12,19,20,23,26,27,33,34,37,40,43,49,52,54,59,64,68,79,97,103,118,136,165,194,275,761,822,1066,2179,2732,2888,20289,30996},"C-? ,4!&! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#! ! ! ! ! ! ! !#! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \" ! !!\" ! \" $#% \" $!\" \" ! ! \" !#! \" $ ! \"#\"!& \" \" ! ! ! ! ! ! ! !#! ! ! ! ! ! ! ! !#\" ! $ ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !(\" \"#! % ! ! ! !A!5H ! $ $!2 .8!#$!! ! ! ! ! ! ! ! ! ! ! !$! \" \"#=/! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! 0 ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \" ! ! ! ! ! !#! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! <:O,\"!J%M)! E;@1!7D ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !)! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !)'%*&'&'%*&'&'+\"&'&'&'$!#& $!!#'\"\"#'&*!!#I $#$ . % % \"#'\"% > N/! $#! ! ! % !#!%( ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#) ! % +9! ( Q ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! 3 ! ! ! ! ! ! ! ! ! ! ! ! ! F ! ! ! ! ! !!! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !&! ! \" ! ! ! ! % ! \" !!! ! ! ! ! ! ! ! ! ! ( ( ! ! ! ! ! ! ! % ! \" $ ! ! ! ! ! 6 $ K,())BP(+$L-G");

    /**
     * All lower-case letters; Unicode category Ll.
     */
    public static final Category LowercaseLetter = Ll;

    /**
     * Returns true if c is a Unicode lower-case letter, in category {@link #Ll}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode lower-case letter
     */
    public static boolean isLowerCase(char c) {
        return Ll.contains(c);
    }

    /**
     * All title-case letters; Unicode category Lt.
     */
    public static final Category Lt = new Category(new int[]{0,7,3,9,13,16,39,48,453,7574},"( \" \" & )!#!#!$ % ' ");

    /**
     * All title-case letters; Unicode category Lt.
     */
    public static final Category TitlecaseLetter = Lt;

    /**
     * All modifier letters; Unicode category Lm.
     */
    public static final Category Lm = new Category(new int[]{0,1,4,5,2,6,8,10,14,17,470,3,11,12,15,22,23,24,29,32,35,36,44,46,62,81,94,96,98,104,106,108,109,115,122,128,134,138,161,165,168,175,192,231,242,270,271,465,479,566,612,688,690,1237,1251,1755,3040,21511,28439},"S)#,.\"& $ D % P K G!M!# 3 ' \" F H U C Q W ? R O#I8( 45T ( )-X!L J * 6\"% <!:$Z V#N A 2!B&9 1 >$\"!* 0 E @ /!=+' Y 7!;");

    /**
     * All modifier letters; Unicode category Lm.
     */
    public static final Category ModifierLetter = Lm;

    /**
     * All other letters (those without casing information); Unicode category Lo.
     */
    public static final Category Lo = new Category(new int[]{2,0,3,4,1,5,6,8,9,11,15,7,17,22,12,21,25,10,16,18,14,30,23,24,29,31,32,40,46,13,19,27,34,35,42,43,49,53,55,68,114,26,28,33,41,50,51,52,60,63,69,170,20,36,38,39,44,47,48,54,56,59,64,65,66,67,74,85,88,89,93,98,104,105,106,107,116,117,134,146,177,209,257,267,328,362,365,513,552,619,828,1083,1142,3064,6591,8453,11171,21012},"S!2!r!%\"q!zI%\"<9 (U$ g !0$) \"!,! 85d.!0:-/C7'1&6 %3;PE#!>!'(,4%+\"$\"/ & !#\"#!,!4$  *$)!(%%$\"/ & $ $ $:\" !T ,'   / & $ ##!>!2$7!.+\"$\"/ & $ ##!9$  2!3! %#  \"#$ ! $#$# #)6!E+   - *#!? \"!\"$9!%+   - ( ##!:$ $2$3'   ;\"!,!& ( 0%&,#6 ' !\"&]Y $=%P$ ! # 6 ! ( $1!\"#7\"K!^+ AJ#lB/!,%%\"#!#$' %.=!Ht \"\"& ! \"\"; \"\": \"\"& ! \"\"4 \\ \"\"`V*Hy\"2 0&b++',43*,*.  2NL!G@ O'#\"K !&R)5M8\"#.C%0F-1Op<3+F84$)C?AB )8H\" % $#!{\"}F0-1& & & & & & & &x![!%c(! e%!&B f39D*w~_\u0081 |GW(s%*)$a!MRS!h!#&   \" -5N*DQ%#! $.?)-IJ'<<#\"')# ;7  +/* %#!#D !#$\"#\"! !0$#1'!*%\"%\"%1& &o@5\u0080=-%Z\u007fv\"iG! ( . # ! $ $ k@u>Q\"EL)m# nj( X\"5#%\"%\"%\" A");

    /**
     * All other letters (those without casing information); Unicode category Lo.
     */
    public static final Category OtherLetter = Lo;

    /**
     * All letters that have a case, including lower-case (Ll), upper-case (Lu), and title-case (Lt) letters.
     */
    public static final Category Lc = new Category(new int[]{2,0,3,4,5,6,7,1,25,37,42,8,9,10,12,13,19,22,30,65,102,165,11,26,27,33,40,43,45,52,53,59,61,64,77,79,82,85,123,135,138,193,194,207,262,277,673,822,1034,2179,2684,2840,20289,30995},"3(&(?!61 2 J \"$K 7I\"\"'#  !&!   ! 0 D H,5 )-:S) !%!\"*\" NE\"$Q-%*\" 3;A. 94M\"$\")\"$\"& ! ! ! 2\"= % !#  %#\"\"$$.%  %L!$!\", !##&! ! ! \" $$!\"\"%#$!>'RF\"4&\"#'/) !%!U<08GB 1#\" @\"' ! &('#!O*%++CT%/#P(&(5");

    /**
     * All letters that have a case, including lower-case (Ll), upper-case (Lu), and title-case (Lt) letters.
     */
    public static final Category CasedLetter = Lc;

    /**
     * All marks; Unicode category M.
     */
    public static final Category M = new Category(new int[]{2,0,1,3,6,4,5,10,8,11,13,31,7,12,57,17,30,19,9,49,14,15,16,20,23,24,26,27,28,32,35,39,44,46,48,50,51,56,60,21,25,29,33,36,37,41,42,43,45,55,58,59,62,63,65,68,71,73,74,81,84,92,97,99,101,102,106,111,118,119,142,153,199,220,237,264,273,276,464,555,704,721,738,768,947,3071,20273,30165},"scm$k@ ! \" \" !Y'37/!a$#&#\" #K!+:]'S('!H# (   %@ F(O8 =Q  / $)\"0 .! $#\"# '!)\";!# .! %&\"# %!+\"%!- .! ,    G\"8&  .! $#\"# ( )\"+!F%%  #'!M%E! $   #(\"-\"0 .! $   #(\"-\"6!*#E\" $   #'!)\"0 X!&& ! ,1\"T!#$*,_!#(-$Z\"<! ! !&\"C1 \"$' >'!`19#&   #$%#4) !)#p t#I 0\"+\"V+'!A  !d\">!e)&)i%R2 <#!30C%B6?(* +-.*31g  7&!,!# hUq=u f!^+o&b\"w# 2J\"[\"l!%!&!9%&!\\\"D/;/4!?,:-P#B*L!W**!2\"A D!  #\"$\" !N%$\"j, \"v!r5/5n");

    /**
     * All marks; Unicode category M.
     */
    public static final Category Mark = M;

    /**
     * All non-spacing marks; Unicode category Mn.
     */
    public static final Category Mn = new Category(new int[]{0,2,1,3,5,4,6,7,10,8,9,31,12,13,59,17,21,26,30,49,15,56,58,11,14,23,27,33,35,43,44,73,16,20,25,28,29,36,39,40,41,46,47,48,50,51,52,57,60,62,63,65,68,74,81,91,92,97,99,102,103,108,111,118,119,142,152,153,199,220,239,266,273,276,464,555,704,721,738,768,947,3071,20273,30165},"o^i%g>! !\"!\"! ?(3A/ [&#$#\"!#E +1X(.)( B#!)!!!%>!P)=9!+5 ! $'$ %&7\"2 . $#* 0\": #\"6 $\"$\"#!% +\"% ,\"6 $%!\"$ 0\"9$! . # !#* )\",\"+ Q - M % 5 !!&!!#)\",\"2 . # ' &\"0\"D\"6\"$#* 0\"2 ? )!! W #&-'Z #),&U\"C ! ! 5-!%!\"&(!<( \\#!$!\"#\"1\"$!/#8 #\"' @ l!p!2\"+\"+\"S\"!&* #(( I!! _\"< `!$\"( '!e\"# . !&! ! #''*# 3-!4L#3 !%! & H)-\";##\"!!O !\"% !!.'#\"c!!,!&$ ' %\"dRm,$ %7q!a Y+k#]\"s $*;\"V\"h % $ 1\"& b\":/8 F'1(J!3 ###\"G T$#\"#\"- * K N !!#\"&\"! =\"* f # $ r n4/4j");

    /**
     * All non-spacing marks; Unicode category Mn.
     */
    public static final Category NonspacingMark = Mn;

    /**
     * All enclosing marks; Unicode category Me.
     */
    public static final Category Me = new Category(new int[]{2,0,1,3,1160,1567,5685,34188},"$\"&!%#  ' ");

    /**
     * All enclosing marks; Unicode category Me.
     */
    public static final Category EnclosingMark = Me;

    /**
     * All combining spacing marks; Unicode category Mc.
     */
    public static final Category Mc = new Category(new int[]{1,0,2,3,5,7,59,11,4,6,9,49,62,10,15,22,31,51,172,8,19,25,26,29,42,43,44,46,48,54,56,61,64,67,76,89,103,110,111,130,143,146,225,238,331,347,1657,2307,4919,21523,30708},"O!>!#\"*#\" 1 &\"% # '!:!&\"A!&\"*!\" = &!\"!% # '!D \" (\"\"\"'!8\",#, &!\"(# \" - 7!. &\")\"\"\"'!9 B\"%%4 L @!2 $!%!# 6 '\"#)/ #$#!'\"N!0!G!3%\" M##\"$ \"$J &!\"!-!\" *$I!+!)!\"(\" ,!0!$ #!?!#\"\"!( +%* 2!/!P R #!C 1.H <!+ $ #\"F # 5!;!\"!E!# )!K \" \" \"!Q");

    /**
     * All combining spacing marks; Unicode category Mc.
     */
    public static final Category SpacingMark = Mc;

    /**
     * All numbers; Unicode category N.
     */
    public static final Category N = new Category(new int[]{9,5,7,0,2,3,119,6,199,4,8,14,19,23,71,87,135,1,10,12,15,18,21,26,29,31,33,39,40,48,50,59,79,97,104,105,109,110,111,116,121,129,139,155,166,183,189,230,240,269,301,321,344,407,413,631,720,727,778,882,1047,1386,1442,21271,29537},"= H1'#%$^ 0 ( V & !!D & & %!F3G  'B C'*5E A & .,Q . X,[$P \" - R I2L \" M / 0 \" \\#)!\" (>%)Y?@6W8]#Z#7*4$T%J 9\"$+: <+` N S!K ; ( - / U _ O");

    /**
     * All numbers; Unicode category N.
     */
    public static final Category Number = N;

    /**
     * All decimal digits; Unicode category Nd.
     */
    public static final Category Nd = new Category(new int[]{9,119,7,39,71,87,135,199,23,48,97,129,167,183,230,279,301,407,413,679,1575,1863,21271,35271},") 4 & ' 2 ! ! ! ! ! ! ! ! ! * ! $ / $ 5 # 0 + , \" - % & \" 7 3 # ' ( % 1 6 .");

    /**
     * All decimal digits; Unicode category Nd.
     */
    public static final Category DecimalNumber = Nd;

    /**
     * Returns true if c is a Unicode decimal digit, in category {@link #Nd}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode decimal digit
     */
    public static boolean isDigit(char c) {
        return Nd.contains(c);
    }

    /**
     * All "letter numbers" such as Roman numerals; Unicode category Nl.
     */
    public static final Category Nl = new Category(new int[]{2,3,0,8,9,15,26,34,2672,3711,5870,22800,30380},"* ('!!)\"&#% ,$+");

    /**
     * All "letter numbers" such as Roman numerals; Unicode category Nl.
     */
    public static final Category LetterNumber = Nl;

    /**
     * All other kinds of number character; Unicode category No.
     */
    public static final Category No = new Category(new int[]{0,9,5,2,6,3,7,14,1,4,8,15,18,19,21,29,31,33,40,42,59,79,121,134,139,178,199,218,377,434,481,631,727,1078,1140,1173,1386,1686,2358,22474,30065},"9($ %#F\"<\"6#7$;$,*=!A-B!> E )\"&!:+3 @45.?/D C%8!0&#'1!2'H\"G");

    /**
     * All other kinds of number character; Unicode category No.
     */
    public static final Category OtherNumber = No;

    /**
     * Some whitespace characters; Unicode category Z. This has some notable missing characters, like newline, tab (both
     * horizontal and vertical), and carriage return; you may want {@link #Horizontal} and/or {@link #Vertical}, which
     * don't line up to Unicode categories but do match the correct sets of horizontal and vertical whitespace. There is
     * also the option of {@link #Space} as the fusion of both {@link #Horizontal} and {@link #Vertical}.
     */
    public static final Category Z = new Category(new int[]{0,1,6,10,30,32,48,128,2432,4001,5600},"% ' * (#$!\" & ) ");

    /**
     * Some whitespace characters; Unicode category Z. This has some notable missing characters, like newline, tab (both
     * horizontal and vertical), and carriage return; you may want {@link #Horizontal} and/or {@link #Vertical}, which
     * don't line up to Unicode categories but do match the correct sets of horizontal and vertical whitespace. There is
     * also the option of {@link #Space} as the fusion of both {@link #Horizontal} and {@link #Vertical}.
     */
    public static final Category Separator = Z;

    /**
     * Some space separator characters; Unicode category Zs.
     */
    public static final Category Zs = new Category(new int[]{0,10,32,37,48,128,2432,4001,5600},"\" % ( &!# $ ' ");

    /**
     * Some space separator characters; Unicode category Zs.
     */
    public static final Category SpaceSeparator = Zs;

    /**
     * All line separator characters (well, character; there's only one, and it isn't in ASCII); Unicode category Zl.
     */
    public static final Category Zl = new Category(new int[]{0,8232},"! ");

    /**
     * All line separator characters (well, character; there's only one, and it isn't in ASCII); Unicode category Zl.
     */
    public static final Category LineSeparator = Zl;

    /**
     * All paragraph separator characters (well, character; there's only one); Unicode category Zp.
     */
    public static final Category Zp = new Category(new int[]{0,8233},"! ");

    /**
     * All paragraph separator characters (well, character; there's only one); Unicode category Zp.
     */
    public static final Category ParagraphSeparator = Zp;

    /**
     * All punctuation (but not symbols); Unicode category P.
     */
    public static final Category P = new Category(new int[]{0,2,1,3,11,5,4,9,13,6,12,14,17,21,23,27,28,31,32,33,38,45,72,75,91,125,7,8,10,15,19,29,30,34,36,41,42,44,46,48,50,52,55,60,63,64,65,79,80,87,98,99,100,103,112,113,116,121,122,127,129,141,144,150,152,154,158,161,169,172,209,217,234,250,262,270,314,368,381,391,404,420,467,613,622,634,703,764,829,1086,20819,29699},"3!!%!#$\"&\"/!! 0 ! B ) & $\"& & v ' r%D\"I ! # # 5\"-\"!\"+ !!7#U E(h!J+2 j\"$ ] Y Z o ( m 8 $\"d+! 4#6 7&%\"V%e s;` t 5\"O!6\"b!!!4<l\"g\"\\)!%c\"$)?!9#K&L\"N:* x.'>!*!$1\"=\"u#@\"y(P\"2'p-M#3\"w#!\"W ^F!1#$q!%'#$, ( S 8 {\"k!T $ X%n#Q\"C!! H\"G R*,\"9#[\",\"i z\"f'.A!(! % !\"_!!%!#$\"&\"/!! 0 ! !)a");

    /**
     * All punctuation (but not symbols); Unicode category P.
     */
    public static final Category Punctuation = P;

    /**
     * All dash punctuation; Unicode category Pd.
     */
    public static final Category Pd = new Category(new int[]{0,1,5,3,11,20,29,32,38,45,52,112,170,242,447,1030,1373,2058,3586,3650,52625},") 0 * 3 / 1\"2 # '!\" & . % + 4!( $ , -");

    /**
     * All dash punctuation; Unicode category Pd.
     */
    public static final Category DashPunctuation = Pd;

    /**
     * All starting/opening "bracket-like" punctuation; Unicode category Ps.
     */
    public static final Category Ps = new Category(new int[]{0,2,4,3,32,51,16,18,19,26,30,31,33,34,39,40,56,65,81,171,216,405,429,635,1062,1087,1887,2431,3775,52514},"/ % $ < ! : ; \" . 0 & 7 ! + 9 ! ! ! ! ! ! 2 , ! ! ! ! 5 ! ! ! ! ! ! ! ! ! ! 1 ! - 8 ! ! ! ) ( ! ! ! 6 ! ! ! ! \" ! ! ! # = 4 * ! ! ! ! ! ! ! \" ' ! ! 3 % $ \" # ");

    /**
     * All starting/opening "bracket-like" punctuation; Unicode category Ps.
     */
    public static final Category OpenPunctuation = Ps;

    /**
     * All initial/opening "quote-like" punctuation; Unicode category Pi.
     */
    public static final Category Pi = new Category(new int[]{0,3,1,2,4,5,16,26,171,3529,8045},"( * !\"! ' ) # % ! & $ ");

    /**
     * All initial/opening "quote-like" punctuation; Unicode category Pi.
     */
    public static final Category InitialPunctuation = Pi;

    /**
     * All ending/closing "bracket-like" punctuation; Unicode category Pe.
     */
    public static final Category Pe = new Category(new int[]{0,2,3,4,32,52,1,16,18,30,31,33,34,41,45,56,65,81,171,218,405,429,635,1062,1087,1887,2474,3774,52511},"- % $ ; ! 9 : / ' 6 ! * 8 ! ! ! ! ! ! 1 + ! ! ! ! 4 ! ! ! ! ! ! ! ! ! ! 0 ! , 7 ! ! ! . ! ! ! 5 ! ! ! ! # ! ! ! \"&< 3 ) ! ! ! ! ! ! ! # ( ! ! 2 % $ \" \" ");

    /**
     * All ending/closing "bracket-like" punctuation; Unicode category Pe.
     */
    public static final Category ClosePunctuation = Pe;

    /**
     * All finalizing/closing "quote-like" punctuation; Unicode category Pf.
     */
    public static final Category Pf = new Category(new int[]{0,4,2,3,5,16,29,187,3529,8030},"' ) ! & ( \" $ # % ! ");

    /**
     * All finalizing/closing "quote-like" punctuation; Unicode category Pf.
     */
    public static final Category FinalPunctuation = Pf;

    /**
     * All connector punctuation, such as the underscore; Unicode category Pc.
     */
    public static final Category Pc = new Category(new int[]{0,1,2,20,25,95,240,8160,56799},"% '!# (!$\"& ");

    /**
     * All connector punctuation, such as the underscore; Unicode category Pc.
     */
    public static final Category ConnectorPunctuation = Pc;

    /**
     * All other punctuation; Unicode category Po.
     */
    public static final Category Po = new Category(new int[]{0,2,1,3,4,5,11,8,9,6,7,12,14,125,13,17,21,28,55,75,113,10,15,23,29,32,33,37,38,41,42,44,45,48,50,58,60,63,65,69,72,87,91,98,100,103,112,116,121,122,127,129,141,144,150,154,158,161,169,172,190,217,234,250,262,270,314,368,381,391,429,467,613,703,774,835,3227,21029,29699},":!!!# ! !\"&\"$\"1 G ) 6\"' i ( g%> 2 # # @\"0\"!\", !!3#M ?.^!2,9 `\"& T P Q e . c J &\"Z,! 4 3$%\"N%[ h'j -!H\"X!!!<%!#b\"]\"S)!%Y\"&)8!-#D$E\"F*+ k\"(*('###!$5! !(l#!\"4 U\"%!# #'!\"! #\"&$!(##! !+#!f!C \\ n\"a!L & O%d#I\"=!! B\"A K+/\"-#R\"/\"_ m)# 7 0\"##$!!#'!* !\"V!!!# ! !\"&\"$\"1 ; #\"W");

    /**
     * All other punctuation; Unicode category Po.
     */
    public static final Category OtherPunctuation = Po;

    /**
     * All symbols (but not punctuation); Unicode category S.
     */
    public static final Category S = new Category(new int[]{0,2,1,3,5,6,14,7,11,16,9,10,13,15,31,32,4,12,17,23,28,8,20,22,30,33,36,45,104,137,158,198,21,25,27,29,37,38,40,42,47,48,54,59,62,63,64,70,77,82,88,92,99,113,118,119,127,128,130,131,133,134,140,155,194,207,208,213,226,230,244,246,248,254,267,319,354,357,358,362,366,373,375,382,402,459,570,574,615,724,753,1089,6593,20414,22161},": ' 2!/ ! 4 ! :0!\"# !## 0 . / u#,,%%! !)V -\"U ^ j!W!# #\"a ( 6\"h 5\"= o\"'\"g X ['\\ b G ? `!) !!#$@ ! ! ]'!$!\"%#?\"z*y p m >9l*+5{ !!1!&!&!&\"O & F!&!6/N\"!#!\"( !!%$! ! ! $ 1\"$0%#! K\"$r$4#i3+QP3x;I#8(t3L$.#q#.!<e$n\"HA!R,cB-$ &\", 7\"'\"S\"f\"$*9D+ 281C* ).(E)k|M~Jv7+\"<\">#(#w!d -\"} =)s-Y ;#T !!# _ ' 2!/ ! 4 ! Z%!%&\"!");

    /**
     * All symbols (but not punctuation); Unicode category S.
     */
    public static final Category Symbol = S;

    /**
     * All math symbols; Unicode category Sm.
     */
    public static final Category Sm = new Category(new int[]{0,2,3,5,1,4,7,32,62,14,17,31,40,257,6,8,10,11,15,19,20,23,24,30,33,38,41,43,46,49,55,69,91,112,130,132,140,165,267,337,470,528,767,825,6716,53213},"; *!( ! < # 9 ' J I!L ) ,!)!D ,%& ?%.$# \" \" / '$\" ! 'F8$@ +6:#H 0 >&A G%\"712-B5(#+\"-=4\"#M K !!E *!( ! C &\"3");

    /**
     * All math symbols; Unicode category Sm.
     */
    public static final Category MathSymbol = Sm;

    /**
     * Returns true if c is a Unicode math symbol, in category {@link #Sm}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode math symbol
     */
    public static boolean isMath(char c) {
        return Sm.contains(c);
    }

    /**
     * All currency symbols; Unicode category Sc.
     */
    public static final Category Sc = new Category(new int[]{0,1,499,3,4,8,25,32,36,109,124,126,155,220,246,264,582,1258,2245,2460,21956,34680},"( +#1 * \"!\"!% . / 0 3 2'5 4 ) , -!$!&");

    /**
     * All currency symbols; Unicode category Sc.
     */
    public static final Category CurrencySymbol = Sc;

    /**
     * Returns true if c is a Unicode currency symbol, in category {@link #Sc}, or false otherwise.
     * @param c the char to check
     * @return true if c is a Unicode currency symbol
     */
    public static boolean isCurrency(char c) {
        return Sc.contains(c);
    }

    /**
     * All modifier symbols; Unicode category Sk.
     */
    public static final Category Sk = new Category(new int[]{0,2,1,14,6,13,15,16,3,4,5,7,10,12,22,28,72,94,104,118,163,522,892,977,1283,4253,5941,20551,30308},"1 ! 0 + * ) 5(%%$$! !'3 &\"8 : !!-!#!#!#\"9\"<.,\"2\"7 &\";'6 ! 4 /");

    /**
     * All modifier symbols; Unicode category Sk.
     */
    public static final Category ModifierSymbol = Sk;

    /**
     * All other symbols; Unicode category So.
     */
    public static final Category So = new Category(new int[]{0,2,1,3,5,7,9,10,11,6,15,16,30,4,8,12,13,14,22,23,29,31,33,38,47,128,17,19,20,21,25,26,27,37,39,42,43,45,46,53,54,59,63,65,71,77,80,82,88,104,110,131,133,134,158,166,182,198,207,208,213,230,247,248,255,267,269,319,337,354,358,374,392,485,513,516,574,723,724,753,866,978,1412,6593,21191,22161},"W # $ ! q a\"9\"Z ( <\"_ k g S$! T [ C h!+ !!#$= ! ! U%!$!\")#Y\"o&n m V6e&'.r\"!#!\"( !\"%$! ! ! $ /\"* !\"! I\"'-##!\"!\"!)!,#\"! !,b%$;#)#N!4?B%L3'OM3X!.!G&R!^EDK`j82\"%7#5!Q]$f\"8>!P0\\@*$ 1\"0 2\"%\"d\"$&6A' :,/4& +5(7+csJuHp#(\"! l!t*9 F!i - $\"1\"!");

    /**
     * All other symbols; Unicode category So.
     */
    public static final Category OtherSymbol = So;

    /**
     * All "programming word" characters, an odd group that includes all letters, all numbers, and the underscore '_'.
     * You may want {@link #Identifier}, {@link #IdentifierPart}, and/or {@link #IdentifierStart} for Java identifier
     * characters, which are a larger group and allow matching the initial character differently from the rest.
     * <br>
     * Accessible in regexes via "<code>\w</code>" when Unicode mode hasn't been explicitly disabled.
     */
    public static final Category Word = new Category(new int[]{2,3,0,4,5,6,1,9,7,8,10,11,15,12,13,14,21,18,19,22,25,17,48,23,30,42,35,37,40,16,27,31,33,38,53,26,28,39,45,50,52,55,57,58,65,66,69,73,74,85,88,116,20,24,29,32,34,41,43,44,47,49,51,54,56,59,62,63,64,68,72,76,77,79,82,83,89,93,100,101,102,105,107,114,115,122,128,134,138,165,228,268,321,332,362,365,457,470,533,619,631,727,1133,6591,8453,11171,22156},"6')4$\" 46\")& \"#&   3 8 \u0080$+,#)\" \"1S &!! \"(\"   \" 2 j x #!y ;!\"(<'[ \" & & \"'C$!8*%O$o (!' 1!\"5K!n,B$\"!\"!F2>$*%7 $'P v!' 1 (!&!0 % \"#!!)!&!!'\"$& #!+!$!\" \"!  $$&!0 % & & &!\" #$&! #\")! \"),-  )   0 % & #!'    !\"=!!'*%   (!&!0 % & #!)!&! ) $& #!' %+& $#  !#& \" &#&# #+$##  !!\"(\",-/-   3 ,!)   !)&  !\"!!!''% ! (   3 ' #!)   !)&(& !!'  .-   G   #%,!1 $   5#7 ) \"!%#\"$$ \" (('!&/J%/ '<& \" # 7 \" 3!# \" % '!!@\"U&(2 \" \" \"$' :$2 5 :*\"KO(h!; \"%\"!9 } !!% \" !!< !!W !!% \" !!/ ` !!M! *2#,5Q!$#\u0083!= 4%P#*)0*0-2.-   &.k#\"$&!'('1  *(R)9%N+8 +$++E!#-Z$4(*A>$b D!*('/\"'. ,Gg#'1).t.I''#6!*%9! 5  A%\u0082!$!;!$!( \" \" \" 8!H % \"#  %#!!$$-%  %S&!$%*(-H-$\"#+1\"$\"!' \"##(\" \" \" ! *!!%#$\" J\u0085ai0\u0084V\u0086z()*\"!; \"%\"!I)\"=7*% % % % % % % % ?6\"\u0081 C/ #!#$Q!&!  l !%9 m#!+?],@'?( /@'</|\u0087L\u008aeF!{#>0\\$' sA)!p!M!& \" (3B$\"#$+^.N+'(7#\" 6!:.D#d,*(8 _*.!'(3#f4 !,!#+$!$!$*% % 9 .(u &!'(\u0089.3$6\u0088\u007f!qE%.#%+ - # \" & & rX~2c!BY+$,5,L# wT')4(4-R#$!$!$! :");

    /**
     * Returns true if c is a "word-like" character, meaning a letter, a number, or the underscore, or false otherwise.
     * @param c the char to check
     * @return true if c is a "word-like" character
     */
    public static boolean isWord(char c) {
        return Word.contains(c);
    }

    /**
     * All valid characters that can be used in a Java identifier.
     * <br>
     * Accessible in regexes via "<code>\pJ</code>" or "<code>\p{J}</code>" (J for Java).
     */
    public static final Category J = new Category(new int[]{2,3,0,4,5,1,6,9,7,8,10,12,15,13,11,25,19,21,18,22,14,17,23,42,48,16,30,37,40,26,27,28,31,32,33,35,38,53,39,50,55,57,58,66,69,73,74,85,88,20,24,29,34,41,43,44,45,47,49,51,52,54,56,59,62,63,64,65,67,68,72,76,77,79,82,83,89,93,100,101,102,105,107,114,115,116,122,128,134,138,165,228,268,321,332,362,365,457,470,533,619,631,727,1133,6591,8453,11171,22156},"\")&-'\"+')/$\" /$A!!$\")% \"#%   3 : \u0081$.,#)\" \"2u %!! \"(\"   \" 0 j y #!z ;!\"(<(\" W \" % % \"'=$!/\"$*&M$o (!' 2!\"5J!n,E$\"!80>$*&6 $'N w!' 2 (!%!1 & \"#!!)!%!!'\"$% #!0 % \"!  $$%!1 & % % %!\" #$%! #\")! \"),+  )   1 & % #!'    !\"9!!' \")&   (!%!1 & % #!)!%! ) $% #!' &.% $#  !#% \" %#%# #.$##  !!\"(\",+(\"(+   3 ,!)   !)%  !\"!!!''& ! (   3 ' #!)   !)%(% !!'  -+   G   #&,!2 $   5#6 ) \"!&#\"$$ \" (('!%4I$, '<% \" # 6 \" 3!# \" & '!!B\"R%(0 \" \" \"$' C$0 5 C*\"JM(h!; \"&\"!7 ~ !!& \" !!< !!A !!& \" !!4 ^ !!K! *0#,5O!$#\u0084!9 /&N#*)1*1+0-+   %-k#\"# !'('2  *(P)7&L.: .$..F!#+V$/(*D>$` ?!*('4\"'- ,Gg#'2)-t-H''#8!*&7! 5  D&\u0083!$!;!$!( \" \" \" :!\\ & \"#  &#!!$$+&  &d%Q\"?%!$&*(+#A9+$\"#.2\"$\"!' \"##(\" \" \" ! *!!&#$\" I\u0086_i1\u0085S\u0087{()*\"!; \"&\"!H)\"96*& & & & & & & & @8\"\u0082 =4 #!#$O!%!  l !&7 m#!.@Z,B'@( 4B'<4}\u0088c\u008beX!|#>1Y$' sD)!p!K!% \" (3E$\"#$!\")[-L.'(6#\" 8!C-?#b,*(: ]*-!'(3#f/ !,!#.$!$!$*& & 7 -(v %!'(\u008a-3$8\u0089\u0080!qF&-#&. + # \" % % rT\u007f0a!EU+#,5,#%/ =\"(# x)\"+')/$\" /+P#$!$!$! #%#%/");

    /**
     * All valid characters that can be used in a Java identifier.
     * <br>
     * Accessible in regexes via "<code>\pJ</code>" or "<code>\p{J}</code>" (J for Java).
     */
    public static final Category Identifier = J;

    /**
     * All valid characters that can be used as the first character in a Java identifier (more than you might think).
     * <br>
     * Accessible in regexes via "<code>\p{Js}</code>".
     */
    public static final Category Js = new Category(new int[]{2,0,3,4,1,5,6,8,7,12,17,22,25,10,15,11,9,16,18,21,42,29,40,13,30,14,23,32,19,26,37,55,24,27,31,35,36,41,46,49,59,65,66,88,20,28,33,43,51,52,53,63,68,85,34,38,39,45,47,48,50,56,64,67,69,74,79,81,82,83,89,93,94,98,102,105,107,114,116,130,134,138,165,191,228,268,277,332,362,365,457,470,513,619,2680,6591,8453,11171,22156},"D!5,%! ,6\"%!/!%!&+ 8 z%/.#'! !o# $\"\" !(!   ! < d q0r >\"!(6(!I=%\",!34D$ i !1$'$/ \"!*! 58K)!,;-$%!#:%!-!#!@@'-&: %2EHR#!<!'01.%(\"$\"3 & !#\"#!*!9$  .\"'$0%%$\"3 & $ $ $;\" !L *'   3 & $ ##!<!1$1!'!)(\"$\"3 & $ ##!B$  1!2! %#  \"#$ ! $#$# #/:!E!)(   + .#!A \"!\"$B!%(   + 0 ##!;$ $1$2'   6\"!*!& 0 ,%&*#: ' !\"&HZ $)(H$ ! # : ! 0 $-!\"# !+\"N!^( CM#n43!*%%\"#!#$' %)7!2> !&!\"4 w \"\"& ! \"\"6 \"\"; \"\"& ! \"\"9 ] \"\"JW.*U\"%#}\"1 ,&a#-'*92.*.)  1PD!#$TK'#\"N !&`/8\\5\"#)O%,?+-Qe!hF2(?59$/OAC4 /C\"-&4\" 4\" % $#!&sIv\"%\">\"%\"( ! ! ! 8\"Q & !#  &#\"\"%%)&  &_$L!5!9!*)#;J!%!\"0 !##(! ! ! \" -\"\"&#%!26~t(\"#$7> !&!\"?'!*+-& & & & & & & &c!{ =''#\"#%U(  f \"&4 g2BG.|\u007fI\u0082TY\"u#./$3F*8\"b6'\"j\"J\"$ ! (+.   \" ++!'P.GS%#! $)A/+=M'F5!*# 0/# 6@  (3+#!#G !#$\"#\"! !, \"-' 7%\"%\"%-& & 4 7(m8\u00817+%[\u0080y\"kX&7#&! 0 ) # ! $ $ lVx<S\"RE)?$, =!(# p'!5,%! ,)K#%\"%\"%\" #$#$,");

    /**
     * All valid characters that can be used as the first character in a Java identifier (more than you might think).
     * <br>
     * Accessible in regexes via "<code>\p{Js}</code>".
     */
    public static final Category IdentifierStart = Js;

    /**
     * Returns true if c is allowed as the start of a Java identifier, meaning a letter, a currency symbol, a
     * connecting punctuation character such as the underscore, or a "letter number", or false otherwise.
     * @param c the char to check
     * @return true if c is allowed as the start of a Java identifier
     */
    public static boolean isJavaIdentifierStart(char c) {
        return IdentifierStart.contains(c);
    }

    /**
     * All valid characters that can be used as the second or later character in a Java identifier.
     * This is identical to {@link #J}.
     * <br>
     * Accessible in regexes via "<code>\p{Jp}</code>".
     */
    public static final Category Jp = J;

    /**
     * All valid characters that can be used as the second or later character in a Java identifier.
     * This is identical to {@link #Identifier}.
     * <br>
     * Accessible in regexes via "<code>\p{Jp}</code>".
     */
    public static final Category IdentifierPart = Identifier;

    /**
     * Returns true if c is allowed as a later part of a Java identifier, meaning a letter, a number, a currency symbol,
     * a connecting punctuation character such as the underscore, or a "letter number", or false otherwise.
     * @param c the char to check
     * @return true if c is allowed as a later part of a Java identifier
     */
    public static boolean isJavaIdentifierPart(char c) {
        return IdentifierPart.contains(c);
    }

    /**
     * Horizontal whitespace characters; not an actual Unicode category but probably more useful because it contains
     * the horizontal tab character while Unicode's {@link #Z} category does not.
     * <br>
     * Accessible in regexes via "<code>\p{Zh}</code>" or "<code>\p{Gh}</code>" .
     */
    public static final Category Gh = new Category(new int[]{0,9,10,23,37,48,128,2432,4001,5600},"! # & ) '\"$ % ( ");

    /**
     * Horizontal whitespace characters; not an actual Unicode category but probably more useful because it contains
     * the horizontal tab character while Unicode's {@link #Z} category does not.
     * <br>
     * Accessible in regexes via "<code>\p{Zh}</code>" or "<code>\p{Gh}</code>" .
     */
    public static final Category Horizontal = Gh;

    /**
     * Vertical whitespace characters; not an actual Unicode category but probably more useful because it contains the
     * newline and carriage return characters while Unicode's {@link #Z} category does not.
     * <br>
     * Accessible in regexes via "<code>\p{Zv}</code>" or "<code>\p{Gv}</code>" .
     */
    public static final Category Gv = new Category(new int[]{0,1,3,10,120,8099},"#\"$ %!");
    /**
     * Vertical whitespace characters; not an actual Unicode category but probably more useful because it contains the
     * newline and carriage return characters while Unicode's {@link #Z} category does not.
     * <br>
     * Accessible in regexes via "<code>\p{Zv}</code>" or "<code>\p{Gv}</code>" .
     */
    public static final Category Vertical = Gv;
    /**
     * Whitespace characters, both horizontal and vertical; not an actual Unicode category but acts like the combination
     * of {@link #Horizontal} (Zh in regexes) and {@link #Vertical} (Zv in regexes) in that it includes both the obscure
     * Unicode characters that are in the Unicode Z category but also the practical characters such as carriage return,
     * newline, and horizontal tab that are not classified as whitespace by Unicode but are by everyone else.
     * This is likely different from the regex {@code \s} because it includes some control characters used as whitespace
     * (such as the carriage return).
     * <br>
     * Accessible in regexes via "<code>\pG</code>" or "<code>\p{G}</code>" (G for Gap).
     */
    public static final Category G = new Category(new int[]{0,1,4,6,9,10,19,27,30,48,101,2432,4001,5600},"$\"& * ' - +%(!# ) , ");
    /**
     * Whitespace characters, both horizontal and vertical; not an actual Unicode category but acts like the combination
     * of {@link #Horizontal} (Zh in regexes) and {@link #Vertical} (Zv in regexes) in that it includes both the obscure
     * Unicode characters that are in the Unicode Z category but also the practical characters such as carriage return,
     * newline, and horizontal tab that are not classified as whitespace by Unicode but are by everyone else.
     * This is likely different from the regex {@code \s} because it includes some control characters used as whitespace
     * (such as the carriage return).
     * <br>
     * Accessible in regexes via "<code>\pG</code>" or "<code>\p{G}</code>" (G for Gap).
     */
    public static final Category Space = G;

    /**
     * Returns true if c is a whitespace character, including space, tab, newline, carriage return, and more Unicode space characters, or false otherwise.
     * @param c the char to check
     * @return true if c is a whitespace character
     */
    public static boolean isWhitespace(char c) {
        return Space.contains(c);
    }

    private static final char[]
            openers =
            new char[]{'(','<','[','{','༺','༼','᚛','‚','„','⁅','⁽','₍','⌈','⌊','〈','❨','❪','❬','❮','❰','❲','❴','⟅','⟦',
                    '⟨','⟪','⟬','⟮','⦃','⦅','⦇','⦉','⦋','⦍','⦏','⦑','⦓','⦕','⦗','⧘','⧚','⧼','⸢','⸤','⸦','⸨',
                    '⹂','⹕','⹗','⹙','⹛','〈','《','「','『','【','〔','〖','〘','〚','〝','﴿',
                    '︗','︵','︷','︹','︻','︽','︿','﹁','﹃','﹇','﹙','﹛','﹝','（','［','｛','｟','｢'},
            closers =
            new char[]{')','>',']','}','༻','༽','᚜','‛','‟','⁆','⁾','₎','⌉','⌋','〉','❩','❫','❭','❯','❱','❳','❵','⟆','⟧',
                    '⟩','⟫','⟭','⟯','⦄','⦆','⦈','⦊','⦌','⦎','⦐','⦒','⦔','⦖','⦘','⧙','⧛','⧽','⸣','⸥','⸧','⸩',
                    '〟','⹖','⹘','⹚','⹜','〉','》','」','』','】','〕','〗','〙','〛','〞','﴾',
                    '︘','︶','︸','︺','︼','︾','﹀','﹂','﹄','﹈','﹚','﹜','﹞','）','］','｝','｠','｣'};

    /**
     * An immutable String showing all chars, in a specific order, that are considered "opening" brackets, braces,
     * parentheses, quotation marks, and so on. This matches the exact size and order used by {@link #CLOSING_BRACKETS},
     * and you can use the same index in the two Strings to get an opening and closing pair.
     */
    public static final String OPENING_BRACKETS = String.valueOf(openers);
    /**
     * An immutable String showing all chars, in a specific order, that are considered "closing" brackets, braces,
     * parentheses, quotation marks, and so on. This matches the exact size and order used by {@link #OPENING_BRACKETS},
     * and you can use the same index in the two Strings to get an opening and closing pair.
     */
    public static final String CLOSING_BRACKETS = String.valueOf(closers);

    private static final CharCharMap openBrackets = new CharCharMap(openers, closers),
            closingBrackets = new CharCharMap(closers, openers);

    /**
     * Returns the given char c's lower-case representation, if it has one, otherwise returns it verbatim.
     * This is currently the same as {@link #caseDown(char)}, but this method may change in the future to more precisely
     * support case folding instead of caseDown()'s case mapping. Future code is encouraged to use
     * {@link #caseDown(char)} and {@link #caseUp(char)} for case mapping, and this method only when case folding is
     * preferred (such as for some case-insensitive comparisons). Case-insensitive comparisons are easier to do with
     * {@link #caseUp(char)} on both compared chars; that technique works for every alphabet except Georgian. This
     * method will currently act exactly how {@link Character#toLowerCase(char)} acts on Java 25 on a PC.
     * @param c any char; this should only return a case-folded different char for upper-case letters
     * @return the single-char case-folded version of c, of it has one, otherwise c
     */
    public static char caseFold(char c)
    {
        return Casing.allToLower[c];
    }

    /**
     * Returns the given char c's lower-case representation, if it has one, otherwise returns it verbatim.
     * This is currently the same as {@link #caseFold(char)}, but this method may change in the future if the subtle
     * distinction between case folding and case mapping is needed by this library. This is the counterpart to
     * {@link #caseUp(char)}, but because of the complexities of... language, calling caseDown() and then caseUp() will
     * not always return the original character. This has to do with how some characters, like lower-case s, have
     * multiple upper-case conversions possible, and the same in the other direction. This method will
     * act exactly how {@link Character#toLowerCase(char)} acts on Java 25 on a PC.
     * @param c any char; this should only return a case-changed different char for upper-case letters
     * @return the single-char lower-case version of c, of it has one, otherwise c
     */
    public static char caseDown(char c)
    {
        return Casing.allToLower[c];
    }

    /**
     * The counterpart to {@link #caseDown(char)} that returns the given char c's upper-case representation, if it has
     * one, otherwise it returns it verbatim. This has dubiously correct behavior for digraphs and ligature chars, but
     * they tend to be rare or even discouraged in practice. Case-insensitive comparisons are easier to do with
     * caseUp() on both compared chars; this technique works for every alphabet except Georgian. This method will
     * act exactly how {@link Character#toUpperCase(char)} acts on Java 25 on a PC.
     * @param c any char; this should only return a case-changed different char for lower-case letters
     * @return the single-char upper-case version of c, if it has one, otherwise c
     */
    public static char caseUp(char c)
    {
        return Casing.allToUpper[c];
    }

    /**
     * Finds the matching closing or opening bracket when given an opening or closing bracket as the char c. If c is not
     * a bracket character this recognizes, then this will return c verbatim; you can check if the return value of this
     * method is equal to c to determine if a matching bracket char is possible. This does recognize '&lt;' as opening
     * and '&gt;' as closing, despite those two not being in Unicode's categories of opening or closing brackets,
     * because more programmers should find that behavior useful and matching always should need to be specified anyway
     * (you won't have '&lt;' or '&gt;' change meaning unless you're expecting a matching bracket).
     * @param c any char; if it is a bracket this will different behavior than non-bracket characters
     * @return a char; if c is a bracket this will return its opening or closing counterpart, otherwise returns c
     */
    public static char matchBracket(char c)
    {
        if(openBrackets.containsKey(c))
            return openBrackets.get(c);
        return closingBrackets.getOrDefault(c, c);
    }

    public static String reverseWithBrackets(CharSequence s)
    {
        char[] c = new char[s.length()];
        for (int i = c.length - 1, r = 0; i >= 0; i--, r++) {
            c[r] = matchBracket(s.charAt(i));
        }
        return String.valueOf(c);
    }

    public static boolean reverseEqual(CharSequence left, CharSequence right)
    {
        if(left == null) return right == null;
        if(right == null) return false;
        if(left.length() != right.length()) return false;
        for (int l = 0, r = right.length() - 1; r >= 0; r--, l++) {
            if(left.charAt(l) != right.charAt(r)) return false;
        }
        return true;
    }

    public static boolean reverseBracketEqual(CharSequence left, CharSequence right)
    {
        if(left == null) return right == null;
        if(right == null) return false;
        if(left.length() != right.length()) return false;
        for (int l = 0, r = right.length() - 1; r >= 0; r--, l++) {
            if(left.charAt(l) != matchBracket(right.charAt(r))) return false;
        }
        return true;
    }

    public static final LinkedHashMap<String, Category> categories;
    public static final LinkedHashMap<String, Category> superCategories;
    static {

        superCategories = new LinkedHashMap<String, Category>(32);
        superCategories.put("C", C);
        superCategories.put("L", L);
        superCategories.put("M", M);
        superCategories.put("N", N);
        superCategories.put("Z", Z);
        superCategories.put("P", P);
        superCategories.put("S", S);
        superCategories.put("J", J);
        superCategories.put("G", G);

//        superCategories.put("Other", Other);
//        superCategories.put("Letter", Letter);
//        superCategories.put("Mark", Mark);
//        superCategories.put("Number", Number);
//        superCategories.put("Separator", Separator);
//        superCategories.put("Punctuation", Punctuation);
//        superCategories.put("Symbol", Symbol);
//        superCategories.put("Identifier", Identifier);
//        superCategories.put("Space", Space);

        categories = new LinkedHashMap<String, Category>(128);
        categories.put("C", C);
        categories.put("L", L);
        categories.put("M", M);
        categories.put("N", N);
        categories.put("Z", Z);
        categories.put("P", P);
        categories.put("S", S);
        categories.put("J", J);
        categories.put("G", G);
        categories.put("Cc", Cc);
        categories.put("Cf", Cf);
        categories.put("Co", Co);
        categories.put("Cn", Cn);
        categories.put("Cs", Cs);
        categories.put("Lu", Lu);
        categories.put("Ll", Ll);
        categories.put("Lt", Lt);
        categories.put("Lm", Lm);
        categories.put("Lo", Lo);
        categories.put("Lc", Lc);
        categories.put("Mn", Mn);
        categories.put("Me", Me);
        categories.put("Mc", Mc);
        categories.put("Nd", Nd);
        categories.put("Nl", Nl);
        categories.put("No", No);
        categories.put("Zs", Zs);
        categories.put("Zl", Zl);
        categories.put("Zp", Zp);
        categories.put("Pd", Pd);
        categories.put("Ps", Ps);
        categories.put("Pi", Pi);
        categories.put("Pe", Pe);
        categories.put("Pf", Pf);
        categories.put("Pc", Pc);
        categories.put("Po", Po);
        categories.put("Sm", Sm);
        categories.put("Sc", Sc);
        categories.put("Sk", Sk);
        categories.put("So", So);
        categories.put("Zh", Gh);
        categories.put("Zv", Gv);
        categories.put("Gh", Gh);
        categories.put("Gv", Gv);
        categories.put("Js", Js);
        categories.put("Jp", Jp);

        categories.put("Other", Other);
        categories.put("PrivateUse", PrivateUse);
        categories.put("Unassigned", Unassigned);
        categories.put("Control", Control);
        categories.put("Format", Format);
        categories.put("Surrogate", Surrogate);
        categories.put("Letter", Letter);
        categories.put("UppercaseLetter", UppercaseLetter);
        categories.put("LowercaseLetter", LowercaseLetter);
        categories.put("TitlecaseLetter", TitlecaseLetter);
        categories.put("ModifierLetter", ModifierLetter);
        categories.put("OtherLetter", OtherLetter);
        categories.put("CasedLetter", CasedLetter);
        categories.put("Mark", Mark);
        categories.put("NonspacingMark", NonspacingMark);
        categories.put("EnclosingMark", EnclosingMark);
        categories.put("SpacingMark", SpacingMark);
        categories.put("Number", Number);
        categories.put("DecimalNumber", DecimalNumber);
        categories.put("LetterNumber", LetterNumber);
        categories.put("OtherNumber", OtherNumber);
        categories.put("Separator", Separator);
        categories.put("SpaceSeparator", SpaceSeparator);
        categories.put("LineSeparator", LineSeparator);
        categories.put("ParagraphSeparator", ParagraphSeparator);
        categories.put("Punctuation", Punctuation);
        categories.put("DashPunctuation", DashPunctuation);
        categories.put("OpenPunctuation", OpenPunctuation);
        categories.put("InitialPunctuation", InitialPunctuation);
        categories.put("ClosePunctuation", ClosePunctuation);
        categories.put("FinalPunctuation", FinalPunctuation);
        categories.put("ConnectorPunctuation", ConnectorPunctuation);
        categories.put("OtherPunctuation", OtherPunctuation);
        categories.put("Symbol", Symbol);
        categories.put("MathSymbol", MathSymbol);
        categories.put("CurrencySymbol", CurrencySymbol);
        categories.put("ModifierSymbol", ModifierSymbol);
        categories.put("OtherSymbol", OtherSymbol);
        categories.put("Identifier", Identifier);
        categories.put("Space", Space);
        categories.put("Horizontal", Horizontal);
        categories.put("Vertical", Vertical);
        categories.put("IdentifierStart", IdentifierStart);
        categories.put("IdentifierPart", IdentifierPart);
    }
}
