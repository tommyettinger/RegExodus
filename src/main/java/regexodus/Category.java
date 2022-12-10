package regexodus;

import regexodus.ds.CharCharMap;
import regexodus.ds.IntBitSet;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Unicode categories constructed from tightly compressed string and array literals instead of large resources.
 * Credit for the technique and much of the code goes to gagern, https://gist.github.com/gagern/89db1179766a702c564d
 * Also, the heavy amount of work that went into the Unicode DB for Node.JS (which the pre-processing stage for this
 * depends on) must be commended; that project is https://github.com/mathiasbynens/node-unicode-data
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
    public static final Category C=new Category(new int[]{0,1,2,3,4,5,8,6,7,11,10,9,12,13,14,15,16,23,27,24,44,19,25,20,31,32,33,34,37,39,50,52,56,57,61,90,17,18,21,22,28,29,30,36,46,48,60,65,71,87,95,104,117,26,40,41,42,43,45,49,51,54,55,58,59,62,63,66,68,73,77,79,85,96,102,107,116,125,127,136,141,149,153,162,185,191,192,193,199,215,251,284,349,366,367,378,398,664,670,715,1166,1813,8451,11173,28126}," 8i9. \u0083!(#& \" F \u0080 =!\\!$ @(H#(01!v [!N!j-N!>!0 I!\" ,?G 5*0 s +!#!1 & \"\"%!*!#!%(\"## '!U!$ (##!1 & # # #!\" '##!$\"\"'% \"'E+$ * $ 1 & # '!) $ $!\".%!-'& $ +!#!1 & # '!*!#!$'$## '!5+# (\"$ %\"# \" #\"#\"$\"-#'\"$ %!\"%\"-G$. $ 3 D\"+ $ %'# $$%!)'1 $ 3 ) '!* $ %'#'\" %!) #,. $ ? $ (#D!2 $ 5\"6 * \"!&\"\"#( \" +%)!$)`#JK# \" ' 6 \" 3!' \" (!)!%8e <#V < 0 .Kx \"$\"!\u007f %!& \" %!X %!; %!& \" %!0 _ %!d!:\"2%Q!(!\u0082\"C'. &*3&F). $ #)R!)%)%/!)%C'4$P+9 -#-#\"\"Y!'*Z#2%,\"b!c J!,%)%/!Eaf#L\"T(B\"0\"B'4!,(4$z {!(!=!(!+ \" \" \" 9!] 0 /!( 7!$ * ,$2$>/#!H .\":/;.p#\u00813,7\u0085!: r M q$L \"$\"!A'#-6&& & & & & & & & l42 C)y6-#O Q!S$4 R h)M \u0088\"\u0084\"@&|5t(w!*WA\")%A(P(-%T*8\"g ,#; @&/!)!S1I+(!(!(&& & B#n!)%\u0087)3#>\u0086~!k<&)'$2 ' \" # # m0}/O!^=/!2%? 7 %#' o#u\"(!(!(!$\"& &,#! ");

    /**
     * All private use characters; Unicode category Co.
     */
    public static final Category Co=new Category(new int[]{1792,6399,57344},"\"! ");
    
    /**
     * All unassigned characters; Unicode category Cn.
     */
    public static final Category Cn=new Category(new int[]{0,1,2,3,4,5,8,6,7,11,10,9,13,14,12,16,15,23,24,27,44,19,25,61,20,30,33,34,37,39,52,56,57,90,17,18,21,22,28,29,31,32,36,46,48,50,65,71,87,95,102,104,117,26,40,41,42,43,45,51,54,55,58,59,60,62,63,66,68,73,77,79,85,107,116,125,127,136,141,149,153,178,185,191,193,199,215,241,251,284,349,366,378,398,664,670,888,1166,1813,8815,11173,28126},"\u0080!(#& \" D } =![!$ ?(F#(*9 w 7!R,`!M!/ G!\" .>E 5*q +!#!1 & \"\"%!*!#!%(\"## '!U!$ (##!1 & # # #!\" '##!$\"\"'% \"'C+$ * $ 1 & # '!) $ $!\"-%!,'& $ +!#!1 & # '!*!#!$'$## '!5+# (\"$ %\"# \" #\"#\"$\",#'\"$ %!\"%\",E$- $ 2 B\"+ $ %'# $$%!)'1 $ 2 ) '!* $ %'#'\" %!) #.- $ > $ (#B!3 $ 5\"6 * \"!&\"\"#( \" +%)!$)_#9J# \" ' 6 \" 2!' \" (!)!%He <#V < / -Ju \"$\"!| %!& \" %!X %!; %!& \" %!/ ^ %!d!:\"3%P!(!\u007f\"A'- &*2&D)- $ #)Q!)%)%/ )%A'4$O+I ,#,#\"\"Y!'*Z#3%.\"b!c 9!.%)%0!Caf#K\"T(7\"/\"7'4!.(4$x y!(!=!(!+ \" \" \" I!\\ / 0!( 8!$ * R ,!F -\":0;-n#~2.8\u0082!: p L o$K \"$\"!@'#,6&& & & & & & & & j43 A)v6,#N P!S$4 Q h)L \u0085\"\u0081\"?&z5r(t!*W@\")%@(O(,%T*H\"g .#; ?&0!)!S1G+(!(!(&& & 7#l!)%\u0084)2#M#\u0083!i<&)'$3 ' \" # # k/{0N!]=0!3%> 8 %#' m!\" s\"(!(!(!$\"& &+'! ");

    /**
     * All control characters; Unicode category Cc.
     */
    public static final Category Cc=new Category(new int[]{0,31,32,96}," !#\"");

    /**
     * All format characters; Unicode category Cf.
     */
    public static final Category Cf=new Category(new int[]{0,4,2,50,5,9,23,27,173,193,250,467,1363,2045,3884,56976},"( ,$& ) # + . -!'!#!\"%/ *\"!");

    /**
     * All surrogate characters; Unicode category Cs.
     */
    public static final Category Cs=new Category(new int[]{2047,55296},"! ");

    /**
     * All letters; Unicode category L.
     */
    public static final Category L=new Category(new int[]{2,0,3,4,1,6,5,7,8,17,11,12,15,22,9,10,25,21,42,13,16,30,46,18,37,14,19,24,29,35,40,43,53,23,27,31,32,33,49,52,65,88,20,26,28,36,48,50,51,55,56,59,63,68,69,85,102,117,34,38,39,41,45,47,54,61,64,66,72,74,81,82,83,89,93,94,98,105,107,114,116,130,132,134,138,165,191,268,277,332,362,365,457,470,513,619,1164,2684,6591,8453,11171,20988},"H0'0N!*!&!%- 5 |&*,#(! !q# $\"\" !'!   ! : g t.u 8\"!'>dK&\"62M$ l !4$($* \"!)! <5I+!0D/$&!%1&!/!#!;;(/`J )a@#!:!(.4,&'\"$\"1 % !#\"#!)!9$  ,$*!.&&$\"1 % $ $ $D\" !J )(   1 % $ ##!:!4$;!+'\"$\"1 % $ ##!C$  4!7! &#  \"#$ ! $#$# #*A!@'   - ,#!B %$C!&'   - . ##!E! $4$7(   >\"!)!% . 0&%)#A ( !\"%S_ $3%S$ ! # A ! . $/!\"# !-\"E!b' =L#p21!)&&\"#!#$( &+3!78 !%!\"2 y \"\"% ! \"\"> \"\"D \"\"% ! \"\"9 R \"\"c[,)W\"&#\u007f\"4 0%e''(+ \",),),+  4PM!&!UI(#\"E !%V*5O<\"#+?&0Q-/Gh!k67%R<9$*?B=2 *=\"((2\" 2\" & $#!%vHx\"&\"8\"&\"' ! ! ! 5\"G % !#  %#\"\"&&+%  %Y!9!)+X!&!\". !##'! ! ! \" /\"\"%#&!@$\u00816 6 r'\"#$38 !%!\"Q(!)-/% % % % % % % %f!}$?#%$&W'  i \"%2 j7CF,~\u0082H\u0085#\u0080U^\"w#,*$16)5\"VO(\"X\"G\"(?+   \" -5P,FT&#! $+B*-KL(6<!)# .*# >;  '1-#!#F !#$\"#\"! !0 \"/( 3&\"&\"&/% % 2 3'o5\u00843-&N\u0083{\"m\\%3#%! . + # ! $ $ nZz:T\"@]*Y# s80'0+I#&\"&\"&\" =");

    /**
     * All upper-case letters; Unicode category Lu.
     */
    public static final Category Lu=new Category(new int[]{0,2,3,1,4,9,7,5,6,8,10,11,13,12,25,37,50,16,20,22,34,36,42,44,46,49,62,65,73,85,102,136,197,263,290,321,723,2203,2685,2890,22316,31054},";.>3!(4 ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \" ! ! ! ! ! ! ! \" ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#! ! $#! !#!!\"\"!#!!$#!#! ! !#! \" !#!!! !#$ ) \" \" \" ! ! ! ! ! ! ! \" ! ! ! ! ! ! ! ! \" \" !!! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! )#!#\" !\"! ! ! ! B ! $ % & !!! !#!1!)5 \"!$ ! ! ! ! ! ! ! ! ! ! ! ( \" !#\"09 ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! * ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#! ! ! ! ! ! \" ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \"/G/! ( D=E6\"!C ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! * ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! *&%'+&%&%'- ! ! ! %&<\",\",\",$-\"A ' $!\"!\" $$& ! ! !\"\"\"+#( : F80 !!\" ! ! !\"! \" %!! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! % ! ' I ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! 2 ! ! ! ! ! ! ! ! ! ! ! ! ! ? ! ! ! ! ! ! $ ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! + ! !#! ! ! ! ' ! \" ! $ ! ! ! ! ! ! ! ! ! !$!$! ! ! ! ! $ !\"! 7 H.@");

    /**
     * All lower-case letters; Unicode category Ll.
     */
    public static final Category Ll=new Category(new int[]{0,2,3,1,4,5,7,9,6,8,11,13,42,25,28,10,12,19,20,23,26,33,34,37,40,43,44,46,47,49,52,54,59,64,68,79,97,103,120,136,165,194,275,761,822,1066,2179,2732,2888,20289,30996},"D-@ ,3!&! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#! ! ! ! ! ! ! !#! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \" ! !!\" ! \" $#% \" $!\" \" ! ! \" !#! \" $ ! \"#\"!& \" \" ! ! ! ! ! ! ! !#! ! ! ! ! ! ! ! !#\" ! $ ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !(\" \"#! % ! ! ! !B!4I ! $ $!1 .6!#$!! ! ! ! ! ! ! ! ! ! ! !$! \" \"#><! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! / ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! \" ! ! ! ! ! !#! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! =8P,\"!K%N)F9A0!5E ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !)! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !)'%*&'&'%*&'&'+\"&'&'&'$!#& $!!#'\"\"#'&*!!#J $#$ . % % \"#'\"% ? O;\" $#! ! ! % !#!%( ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !#) ! % +7! ( R ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! 2 ! ! ! ! ! ! ! ! ! ! ! ! ! G ! ! ! ! ! !!! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !&! ! \" ! ! ! ! % ! \" !!! ! ! ! ! ! ! ! ! ! ( ( ! ! ! ! ! $ % ! : $ L,())CQ(+$M-H");

    /**
     * All title-case letters; Unicode category Lt.
     */
    public static final Category Lt=new Category(new int[]{0,7,3,9,13,16,39,48,453,7574},"( \" \" & )!#!#!$ % ' ");

    /**
     * All modifier letters; Unicode category Lm.
     */
    public static final Category Lm=new Category(new int[]{0,1,5,4,2,6,8,10,14,17,470,3,11,12,15,22,23,24,29,32,35,36,44,46,62,81,94,96,98,104,108,109,112,115,122,128,134,138,165,175,192,231,242,270,271,329,465,479,566,612,688,690,1237,1251,1755,3040,21511,28439},"R)\",.#& $ D % O I F!K!\" 3 ' # M T C P V > Q N\"G8( 45S ( )-W!J H * 6#% <!:$Y U\"L A 2!B&9 1 @!* 0 E ? /!=+' X 7!;");

    /**
     * All other letters (those without casing information); Unicode category Lo.
     */
    public static final Category Lo=new Category(new int[]{2,0,3,4,1,5,6,8,15,9,11,17,7,12,22,21,25,10,16,30,14,18,24,29,31,46,13,19,20,23,27,32,33,34,35,40,42,43,49,53,68,114,26,28,41,50,51,52,54,55,56,63,69,170,36,38,39,44,47,48,59,60,61,64,65,66,67,74,85,88,89,93,98,104,105,106,107,116,117,134,146,177,209,257,267,328,362,365,513,552,619,828,1083,1142,3064,6591,8453,11171,20988},"U!2!s!%\"r!{J%\"98 )V$ h !0$* \"!+! 73e-!0?./E6'1P< +^G#!;!')+4%,\"$\"/ & !#\"#!+!4$  ($*!)%%$\"/ & $ $ $?\" !< +'   / & $ ##!;!2$6!-,\"$\"/ & $ ##!8$  2!5! %#  \"#$ ! $#$# #*=!G,   . (#!> &$8!%,   . ) ##!@! $2$5'   C\"!+!& ) 0%&+#= ' !\"&\\Z $:%]$ ! # = ! ) $1!\"#6\"@!_, BK#mD/!+%%\"#!#$' %-:!Iu \"\"& ! \"\"C \"\"? \"\"& ! \"\"4 R \"\"aW(Iz\"2 0&c,,'- \"(+(+(-  2NL!HA O'#\"@ !&T*3M7\"#-E%0Q.1Oq95&R74$*E>BD *7I\" % $#!|\"~Q0.1& & & & & & & &y!P!%d)! f%!&D g58F(x\u007f`\u0082#< }HX)t%(*$b!MTU!i!#&   \" .3N(FS%#! $->*.JK'99#\"'*# C6  ,/( %#!#F !#$\"#\"! !0$#1'!(%\"%\"%1& &pA3\u0081:.%[\u0080w\"jH! ) - # ! $ $ lAv;S\"GL*n# ok) Y\"3#%\"%\"%\" B");

    /**
     * All letters that have a case, including lower-case (Ll), upper-case (Lu), and title-case (Lt) letters.
     */
    public static final Category Lc=new Category(new int[]{2,3,0,4,5,6,7,8,1,25,37,42,9,12,13,19,22,27,30,43,46,65,102,165,10,11,26,33,40,45,47,52,53,59,64,77,79,82,85,135,138,193,194,207,262,277,673,822,1034,2179,2684,2840,20289,30995},"5)&)A\"90 2 J !$K :I!!(#  \"&\"   \" / E H,7 *8<S* \"%\"!+! NF!$Q''+! 53B- ;6M!$!*!$!& \" \" \" 2!? % \"#  %#!!$$-%  %L\"$\"!, \"##&\" \" \" ! $$\"!!%#$\"@(R4 4 1!6&!#(.* \"%\"U=/1GC 0#! >!'3(#\"O+%''DT%.#P)&)7");

    /**
     * All marks; Unicode category M.
     */
    public static final Category M=new Category(new int[]{2,0,1,3,4,5,6,10,11,8,31,57,7,12,13,17,30,19,9,14,49,15,16,20,24,26,27,28,32,35,39,44,46,48,51,58,120,21,23,25,29,33,36,37,41,42,45,50,55,56,59,60,62,64,65,68,71,73,75,81,84,92,97,99,101,102,106,111,119,142,153,199,220,237,264,273,276,464,555,704,721,738,768,947,3071,20273,30165},"rcl&j? ! \" \" !Y'47/!a&#%#\" #J!*9]'R)'!G# )   $? D3 <P  / &(\"0 +! &#\"# '!(\":!# +! $%\"# $!*\"$!- +! ,    E\"F%  +! &#\"# ) (\"*!S$$  #'!L$C&   #)\"-\"0 +! &   #)\"-\"H#Q\" &   #'!(\"0 X!%% ! ,1\"T!#&.,_!#)-%Z\";! ! !%\"O1 \"&' ='!`18#%   #&$#3( !(#o s 0 0\"*\"V*'!@ D\"=!d(%(h$C2 ;#!46U$A6>). *-+.41f  7%!,!# g+ $p<t e!^*n%b\"v# 2I\"[\"k!$!%!8$%!\\\"B/:/3!>,9-N#A.K!W..!2\"@ B!  #\"&\" !M$&\"i, \"u!q5/5m");

    /**
     * All non-spacing marks; Unicode category Mn.
     */
    public static final Category Mn=new Category(new int[]{0,2,1,3,5,4,6,7,10,9,8,12,13,31,59,30,17,21,26,49,58,14,11,15,27,33,35,44,56,57,73,120,16,20,23,25,28,29,36,39,40,41,43,46,47,48,51,52,62,64,65,68,75,81,91,92,97,99,102,103,108,111,119,142,152,153,199,220,239,266,273,276,464,555,704,721,738,768,947,3071,20273,30165},"m]g%e;! !\"!\"! >(3A0 Z&#$#\"!#F -2W(.*( C#!*!!!%;!?5!-< ! $'$ %&6\"/ . $#) 1\"8 #\"4 $\"$\"#!% -\"% +\"4 $%!\"$ 1\"B$! . # !#) *\"+\"- P , N % 4!&!!#*\"+\"/ . # ' &\"1\"E\"4\"$#) 1\"/ > *!! V #&,'Y #*+$T\"D ! ! <,!%!\"&(!:( [#!$!\"#\"2\"$!0#5 #\"' @ j!n!/!/\"-\"R\"!&) #(( K!?\": ^!$\"( '!c\"# . !&! ! #'')# 3,!\"Q#3 !%! & I*,\"9##\"!!= !\"% !!.'#\"a!!+!&$ ' %\"b=!%k+$ %6o!_ X-i#\\\"q $)9\"U\"f % $ 2\"& `\"805 G'2(L!3 ###\"H S$#\"#\", ) M O !!#\"&\"! J\") d # $ p l707h");

    /**
     * All enclosing marks; Unicode category Me.
     */
    public static final Category Me=new Category(new int[]{2,0,1,3,1160,1567,5685,34188},"$\"&!%#  ' ");

    /**
     * All combining spacing marks; Unicode category Mc.
     */
    public static final Category Mc=new Category(new int[]{1,0,2,3,5,7,59,11,4,6,9,49,62,10,22,44,51,172,8,15,19,25,26,31,42,43,46,48,54,56,61,64,67,76,89,103,110,111,143,146,225,238,331,347,1818,2307,4919,21523,30708},"M!=!#\"*#\" 0 &\"% # '!/!&\"@!&\"*!\" < &!\"!% # '!C \" (\"\"\"'!8\",#, &!\"(# \" - / &\")\"\"\"'!9 A\"%%4 J ?!1 $!%!# 6 '\"#). #$#!'\"L!2%\" K##\"$ \"$H &!\"!-!\" *$G!+!)!\"(\" ,!7!$ #!>!#\"\"!( +%* 1!.!N P #!B 03F ;!+ $ #\"E # 5!:!\"!D!# )!I \" \" \"!O");

    /**
     * All numbers; Unicode category N.
     */
    public static final Category N=new Category(new int[]{9,5,7,0,2,3,119,6,199,4,8,14,19,23,71,87,135,1,10,12,15,18,21,26,29,31,33,39,40,48,50,59,79,97,104,105,109,110,111,116,121,129,139,155,166,183,189,230,240,269,301,321,344,407,413,631,720,727,778,882,1047,1386,1442,21271,29537},"= H1'#%$^ 0 ( V & !!D & & %!F3G  'B C'*5E A & .,Q . X,[$P \" - R I2L \" M / 0 \" \\#)!\" (>%)Y?@6W8]#Z#7*4$T%J 9\"$+: <+` N S!K ; ( - / U _ O");

    /**
     * All decimal digits; Unicode category Nd.
     */
    public static final Category Nd=new Category(new int[]{9,119,7,39,71,87,135,199,23,48,97,129,167,183,230,279,301,407,413,679,1575,1863,21271,35271},") 4 & ' 2 ! ! ! ! ! ! ! ! ! * ! $ / $ 5 # 0 + , \" - % & \" 7 3 # ' ( % 1 6 .");

    /**
     * All "letter numbers" such as Roman numerals; Unicode category Nl.
     */
    public static final Category Nl=new Category(new int[]{2,3,0,8,9,15,26,34,2672,3711,5870,22800,30380},"* ('!!)\"&#% ,$+");

    /**
     * All other kinds of number character; Unicode category No.
     */
    public static final Category No=new Category(new int[]{0,9,5,2,6,3,7,14,1,4,8,15,18,19,21,29,31,33,40,42,59,79,121,134,139,178,199,218,377,434,481,631,727,1078,1140,1173,1386,1686,2358,22474,30065},"9($ %#F\"<\"6#7$;$,*=!A-B!> E )\"&!:+3 @45.?/D C%8!0&#'1!2'H\"G");

    /**
     * Some whitespace characters; Unicode category Z. This has some notable missing characters, like newline, tab (both
     * horizontal and vertical), and carriage return; you may want {@link #Horizontal} and/or {@link #Vertical}, which
     * don't line up to Unicode categories but do match the correct sets of horizontal and vertical whitespace. There is
     * also the option of {@link #Space} as the fusion of both {@link #Horizontal} and {@link #Vertical}.
     */
    public static final Category Z=new Category(new int[]{0,1,6,10,30,32,48,128,2432,4001,5600},"% ' * (#$!\" & ) ");

    /**
     * Some space separator characters; Unicode category Zs.
     */
    public static final Category Zs=new Category(new int[]{0,10,32,37,48,128,2432,4001,5600},"\" % ( &!# $ ' ");

    /**
     * All line separator characters (well, character; there's only one, and it isn't in ASCII); Unicode category Zs.
     */
    public static final Category Zl=new Category(new int[]{0,8232},"! ");

    /**
     * All paragraph separator characters (well, character; there's only one); Unicode category Zp.
     */
    public static final Category Zp=new Category(new int[]{0,8233},"! ");

    /**
     * All punctuation (but not symbols); Unicode category P.
     */
    public static final Category P=new Category(new int[]{0,2,1,3,5,11,4,9,13,6,12,14,17,21,23,27,28,31,32,33,38,45,72,75,91,7,8,10,15,19,30,34,36,41,42,44,46,48,50,52,55,60,63,64,65,79,80,87,98,99,100,103,112,113,116,121,122,125,127,129,141,144,150,152,154,156,158,169,172,173,209,217,234,250,262,270,314,368,381,391,404,431,467,613,622,634,703,764,829,1086,20819,29699},"3!!$!#%\"&\"/!! 0 ! @ ) & %\"& & v ' r$B\"G ! # # 5\"-\"!\"+ #\"7#S C(h!H+2 j\"% \\ W X o ( m 8 %\"c+! 4#6 7&$\"T$d s:_ t 5\"M!6\"b!!!4;l\"g\"[)!$e)a#I&J\"L9* x.'=!*!%1\"<\"u#>\"y(N\"2'p-K#3\"w#!\"U ]D!1# q!$'#%, ( Q 8 {\"k!R % V$n#O\"A!! F\"E P*,\"Y#Z\",\"i z\"f'.?!(! $ !\"^!!$!#%\"&\"/!! 0 ! !)`");

    /**
     * All dash punctuation; Unicode category Pd.
     */
    public static final Category Pd=new Category(new int[]{0,1,5,3,11,20,32,38,45,52,112,170,242,476,1030,1373,2058,3586,3650,52625},"( / ) 2 . 0\"1 # &!\" - % * 3!' $ + ,");

    /**
     * All starting/opening "bracket-like" punctuation; Unicode category Ps.
     */
    public static final Category Ps=new Category(new int[]{0,2,4,3,32,51,16,18,26,30,31,33,34,39,40,56,65,81,171,216,405,454,635,1062,1087,1887,2431,3775,52514},". % $ ; ! 9 : \" - / & 6 ! * 8 ! ! ! ! ! ! 1 + ! ! ! ! 4 ! ! ! ! ! ! ! ! ! ! 0 ! , 7 ! ! ! ( 5 ! ! ! ! \" ! ! ! # < 3 ) ! ! ! ! ! ! ! \" ' ! ! 2 % $ \" # ");

    /**
     * All initial/opening "quote-like" punctuation; Unicode category Pi.
     */
    public static final Category Pi=new Category(new int[]{0,3,1,2,4,5,16,26,171,3529,8045},"( * !\"! ' ) # % ! & $ ");

    /**
     * All ending/closing "bracket-like" punctuation; Unicode category Pe.
     */
    public static final Category Pe=new Category(new int[]{0,2,3,4,32,52,1,16,18,30,31,33,34,41,56,65,81,171,218,405,480,635,1062,1087,1887,2474,3774,52511},"- % $ : ! 8 9 . ' 5 ! * 7 ! ! ! ! ! ! 0 + ! ! ! ! 3 ! ! ! ! ! ! ! ! ! ! / ! , 6 ! ! ! 4 ! ! ! ! # ! ! ! \"&; 2 ) ! ! ! ! ! ! ! # ( ! ! 1 % $ \" \" ");

    /**
     * All finalizing/closing "quote-like" punctuation; Unicode category Pf.
     */
    public static final Category Pf=new Category(new int[]{0,4,2,3,5,16,29,187,3529,8030},"' ) ! & ( \" $ # % ! ");

    /**
     * All connector punctuation, such as the underscore; Unicode category Pc.
     */
    public static final Category Pc=new Category(new int[]{0,1,2,20,25,95,240,8160,56799},"% '!# (!$\"& ");

    /**
     * All other punctuation; Unicode category Po.
     */
    public static final Category Po=new Category(new int[]{0,2,1,3,4,5,11,8,9,6,7,12,14,13,17,21,28,55,75,113,125,10,15,23,32,33,37,38,41,42,44,45,48,50,58,60,63,65,69,72,87,91,98,100,103,112,116,121,122,127,129,141,144,150,154,156,158,169,172,173,190,217,234,250,262,270,314,368,381,391,431,467,613,703,774,835,3227,21029,29699},"9!!!# ! !\"&\"$\"0 F ) 6\"' i ( g%= 1 # # ?\"/\"!\", #\"2#L >-^!1,8 `\"& S O P e - c I &\"Y,! 3 2$%\"M%Z h'j 4!G\"X!!!;%!#b\"]\"R)!%[)W#C$D\"E*+ k\"(*('###!$5! !(l#!\"3 T\"%!# #'!\"! #\"&$!(##! !+# f!B \\ n\"a!K & N%d#H\"<!! A\"@ J+.\"4#Q\".\"_ m)# 7 /\"##$!!#'!* !\"U!!!# ! !\"&\"$\"0 : #\"V");

    /**
     * All symbols (but not punctuation); Unicode category S.
     */
    public static final Category S=new Category(new int[]{0,2,1,3,5,6,14,7,11,9,31,13,16,4,10,12,15,28,32,8,17,20,22,23,29,30,33,36,104,158,198,21,25,26,27,35,38,40,42,45,47,48,54,59,62,63,65,70,77,82,88,92,101,113,118,119,127,130,131,133,134,137,140,155,194,207,208,213,226,230,244,246,248,251,267,319,354,357,358,366,373,375,402,459,499,570,571,574,615,724,753,1089,6593,20414,22161},"; ' 4!2 ! 1 ! ;-!\"# !## - * 2 s#++%%! !,V 0\"U ^ j!W!# #\"a ( 5\"h 3\"t\"'\"g X Z'[ b F > `!, !!#$? ! ! \\'!$!\"%#>\"z)y o m =:l).3{ !!/!&!&!&\"O & E!&!5*N\"!#!\"( !!%$! ! ! $ /\"$-%#! K\"$q$1#iA.QP7xGI#9(r7L$*#p#*!<e$n\"H@!R+cB() &\"+ 6\"'\"S\"f\"$):C89/8) ,*(D,k|M~Ju6.\"<\"=#(#w!d 0\"} ]0v\"T !!# _ ' 4!2 ! 1 ! Y%!%&\"!");

    /**
     * All math symbols; Unicode category Sm.
     */
    public static final Category Sm=new Category(new int[]{0,2,3,5,1,4,7,32,62,14,17,31,40,257,6,8,10,11,15,19,20,23,24,30,33,38,41,43,46,49,55,69,91,112,130,132,140,165,267,337,470,528,767,825,6716,53213},"; *!( ! < # 9 ' J I!L ) ,!)!D ,%& ?%.$# \" \" / '$\" ! 'F8$@ +6:#H 0 >&A G%\"712-B5(#+\"-=4\"#M K !!E *!( ! C &\"3");

    /**
     * All currency symbols; Unicode category Sc.
     */
    public static final Category Sc=new Category(new int[]{0,1,499,3,4,8,25,31,36,109,124,126,155,220,246,264,582,1258,2245,2460,21956,34681},"( +#1 * \"!\"!% . / 0 3 2'5 4 ) , -!$!&");

    /**
     * All modifier symbols; Unicode category Sk.
     */
    public static final Category Sk=new Category(new int[]{0,2,1,14,15,6,13,3,4,5,7,10,12,16,22,28,72,94,104,118,163,522,893,977,4253,7224,20551,30308},"1 ! 0 * ) ( 5'&&%%! !-3 $\"9 !!,!#!#!#\"8\";.+\"2\"7 $\":$6 ! 4 /");

    /**
     * All other symbols; Unicode category So.
     */
    public static final Category So=new Category(new int[]{0,2,1,3,5,7,9,11,6,10,16,29,30,4,8,12,13,14,22,26,31,33,38,47,15,19,20,21,23,25,27,35,39,42,43,45,53,54,59,63,65,68,77,80,82,88,104,110,128,131,133,134,158,166,182,198,207,208,213,230,247,248,255,267,269,319,337,354,358,374,392,487,513,516,574,723,724,753,866,978,1412,6593,21380,22161},"U # $ ! o _\"P\"X ' :\"] i e Q$! R Y A f!* !!#$; ! ! S%!$!\"(#W\"m&l k T5c&).p\"!#!\"' !\"%$! ! ! $ /\"8 !\"! F\")-##!\"!\"!(!,#\"! !,`%$9#(#K!+3@%I3)LJ<V!.!D&O!\\CBH^h72\"%6#4!N[$d\"7=!M0Z>'& 1\"0 2\"%\"b\"$&5?+,/+& *4'6*aqGsEn#'\"! j!r g - $\"1\"!");

    /**
     * All "programming word" characters, a weird group that includes all letters, all numbers, and the underscore '_'.
     * You may want {@link #Identifier}, {@link #IdentifierPart}, and/or {@link #IdentifierStart} for Java identifier
     * characters, which are a larger group and allow matching the initial character differently from the rest.
     * <br>
     * Accessible in regexes via "<code>\w</code>" when Unicode mode hasn't been explicitly disabled.
     */
    public static final Category Word=new Category(new int[]{2,3,0,4,5,6,1,9,7,8,10,11,12,15,13,14,17,18,19,25,21,22,48,30,42,23,35,37,40,16,20,27,31,33,38,52,26,28,39,43,45,46,50,53,54,55,57,58,64,65,69,73,85,88,116,24,29,32,34,41,44,47,49,51,56,59,62,63,66,68,72,74,75,77,79,82,83,89,93,100,101,102,105,107,114,115,122,128,132,134,138,165,249,268,282,321,332,362,365,457,470,619,631,727,1133,1164,6591,8453,11171,20988},"6')3$\" 36\")& \"#&   5 7 \203$+-#)\" \"1V &!! \"(\"   \" 2 k z #!{ ;!\"(<'\\ \" & & \"'D$!7*%S$p (!' 1!\"0O!o-K$\"!\"!H2?$*L> 0,/ w!' 1 (!&!4 % \"#!!)!&!!'\"$& #!+!$!\" \"!  $$&!4 % & & &!\" #$&! #\")! \")-,  )   4 % & #!'    !\"=!!'*%   (!&!4 % & #!)!&! ) $& #!' %+& $#  !#& \" &#&# #+$##  !!\"(\"-,/,   5 -#(   !)&  %!!''% ! (   5 ' #!)   !)&)\" !!' &/,   J   #%-!1 $   0#9 ) \"!%#\"$$ \" (('!&/N%/ '<& \" # 9 \" 5!# \" $!'!!A\"W&(2 \" \" \"$' :$2 0 :*\"OS(i!; \"%\"!8 \200 !!% \" !!< !!Y !!% \" !!/ ` !!d! *2#-0T!$#\205!= 3%g#*), %,>,2.,   &.l#\"$&!'('1 !'(U)8%R+7 +$++F!#,G$3(*B?$b E!*('/\"'. &Ph$'1).u.M''#6!))8! 0  B%| ~!$!;!$!( \" \" \" 7!C % \"#  %#!!$$,%  %V&!$%*(,C,$\"#+1\"$\"!' \"##(\" \" \" ! *!!%#$\" N\207aj4\206X\210I I x()*\"!; \"%\"!M)\"=9*% % % % % % % % @6\"\204 D/ #!#$T!&!  m !%8 n#!+@^-A'@( /A'</\177\212Q\215#\211eH!}#?4]$' tB)!q!C!)GJ$\"#$+_.R+'(9#\" 6!:.E#P-*(7 L*.!'(5#f3 !-!#+$!$!$*% % 8 .(v &!'(\214.5$6\213\202!rF%.#%+ , # \" & & sZ\2012c!K[+$-0-Q# y>')3(3,U#$!$!$! :");

    /**
     * All valid characters that can be used in a Java identifier.
     * <br>
     * Accessible in regexes via "<code>\pJ</code>" or "<code>\p{J}</code>" (J for Java).
     */
    public static final Category Identifier=new Category(new int[]{2,3,0,4,5,1,6,9,7,8,12,10,15,11,13,25,14,17,19,18,21,22,42,48,23,30,31,37,40,16,20,26,27,28,33,35,38,32,39,43,46,50,52,53,54,55,57,58,64,69,73,85,88,24,29,34,41,44,45,47,49,51,56,59,62,63,65,66,67,68,72,74,75,77,79,82,83,89,93,100,101,102,105,107,114,115,116,122,128,132,134,138,165,249,268,282,321,332,362,365,457,470,619,631,727,1133,1164,6591,8453,11171,20988},"\")&.'\"*')/$\" /$E!!$\")% \"#%   5 9 \204$-,#)\" \"3v %!! \"(\"   \" 2 k { #!| ;!\"(<(\" Y \" % % \"'?$!/\"$+&R$p (!' 3!\"1O!o,K$\"!72@$+L> 1*0 x!' 3 (!%!4 & \"#!!)!%!!'\"$% #!2 % \"!  $$%!4 & % % %!\" #$%! #\")! \"),*  )   4 & % #!'    !\"=!!' \")&   (!%!4 & % #!)!%! ) $% #!' &-% $#  !#% \" %#%# #-$##  !!\"(\",*(\"(*   5 ,#(   !)%  &!!''& ! (   5 ' #!)   !)%)\" !!' %0*   I   #&,!3 $   1#8 ) \"!&#\"$$ \" (('!%0N$, '<% \" # 8 \" 5!# \" $!'!!B\"U%(2 \" \" \"$' C$2 1 C+\"OR(i!; \"&\"!6 \201 !!& \" !!< !!E !!& \" !!0 ^ !!c! +2#,1S!$#\206!= /&g#+)* &*>*2.*   %.l#\"# !'('3 !'(T)6&Q-9 -$--F!#*G$/(+D@$` A!+('0\"'. %Ph$'3).u.M''#7!))6! 1  D&} \177!$!;!$!( \" \" \" 9!J & \"#  &#!!$$*&  &d%>\"A%!$&+(*#:1*$\"#-3\"$\"!' \"##(\" \" \" ! +!!&#$\" N\210_j4\207V\211H H y()+\"!; \"&\"!M)\"=8+& & & & & & & & :7\"\205 ?0 #!#$S!%!  m !&6 n#!-:\\,B':( 0B'<0\200\213b\216#\212eZ!~#@4[$' tD)!q!J!)GI$\"#$!\")].Q-'(8#\" 7!C.A#P,+(9 L+.!'(5#f/ !,!#-$!$!$+& & 6 .(w %!'(\215.5$7\214\203!rF&.#&- * # \" % % sW\2022a!KX*#,1,#%/ ?\"(# z)\"*')/$\" /*T#$!$!$! #%#%/");

    /**
     * All valid characters that can be used as the first character in a Java identifier (more than you might think).
     * <br>
     * Accessible in regexes via "<code>\p{Js}</code>".
     */
    public static final Category IdentifierStart=new Category(new int[]{2,0,3,4,1,5,6,8,7,12,17,25,15,22,10,11,9,16,21,42,29,40,13,18,30,46,14,19,23,26,31,37,20,24,27,32,33,35,36,43,49,52,55,65,88,28,41,51,53,56,59,63,67,68,85,34,38,39,45,47,48,50,54,61,64,66,69,74,79,81,82,83,89,93,94,98,102,105,107,114,116,130,132,134,138,165,191,268,277,332,362,365,457,470,513,619,1164,2680,6591,8453,11171,20988},"F!4+%! +5\"%!/!%!&- 8 |%/,#'! !q# $\"\" !(!   ! ; f t0u ?\"!(5(!K=%\"+!23F$ k !1$'$/ \"!*! 48L)!+C.$%!#<%!.!#!AA'.^@ *_P#!;!'01,%(\"$\"2 & !#\"#!*!:$  ,\"'$0%%$\"2 & $ $ $C\" !@ *'   2 & $ ##!;!1$1!'!)(\"$\"2 & $ ##!>$  1!7! %#  \"#$ ! $#$# #/<!N!)(   - ,#!B &$>!%(   - 0 ##!D! $1$7'   5\"!*!& 0 +%&*#< ' !\"&R[ $)(R$ ! # < ! 0 $.!\"# !-\"D!`( EM#p32!*%%\"#!#$' %)6!7? !&!\"3 y \"\"& ! \"\"5 \"\"C \"\"& ! \"\": Q \"\"aX,*V\"%#\177\"1 +&c#.') \",*,*,)  1OF!#$UL'#\"D !&b/8]4\"#)G%+J-.Ig!j97&Q4:$/GBE3 /E\"''3\" 3\" % $#!&vKx\"%\"?\"%\"( ! ! ! 8\"I & !#  &#\"\"%%)&  &T$@!4!:!*)#>T!%!\"0 !##(! ! ! \" .\"\"&#%!75\2019 9 r(\"#$6? !&!\"J'!*-.& & & & & & & &e!} =''#\"#%V(  h \"&3 i7>H,~\202K\205#\200UZ\"w#,/$29*8\"d5'\"l\"I\"'G)   \" --!'O,HS%#! $)B/-=M'94!*# 0/# 5A  (2-#!#H !#$\"#\"! !+ \".' 6%\"%\"%.& & 3 6(o8\2046-%\\\203{\"mY&6#&! 0 ) # ! $ $ nWz;S\"PN)J$+ =!(# s'!4+%! +)L#%\"%\"%\" #$#$+");

    /**
     * All valid characters that can be used as the second or later character in a Java identifier.
     * <br>
     * Accessible in regexes via "<code>\p{Jp}</code>".
     */
    public static final Category IdentifierPart=new Category(new int[]{2,3,0,4,5,1,6,9,7,8,12,10,15,11,13,25,14,17,19,18,21,22,42,48,23,30,31,37,40,16,20,26,27,28,33,35,38,32,39,43,46,50,52,53,54,55,57,58,64,69,73,85,88,24,29,34,41,44,45,47,49,51,56,59,62,63,65,66,67,68,72,74,75,77,79,82,83,89,93,100,101,102,105,107,114,115,116,122,128,132,134,138,165,249,268,282,321,332,362,365,457,470,619,631,727,1133,1164,6591,8453,11171,20988},"\")&.'\"*')/$\" /$E!!$\")% \"#%   5 9 \204$-,#)\" \"3v %!! \"(\"   \" 2 k { #!| ;!\"(<(\" Y \" % % \"'?$!/\"$+&R$p (!' 3!\"1O!o,K$\"!72@$+L> 1*0 x!' 3 (!%!4 & \"#!!)!%!!'\"$% #!2 % \"!  $$%!4 & % % %!\" #$%! #\")! \"),*  )   4 & % #!'    !\"=!!' \")&   (!%!4 & % #!)!%! ) $% #!' &-% $#  !#% \" %#%# #-$##  !!\"(\",*(\"(*   5 ,#(   !)%  &!!''& ! (   5 ' #!)   !)%)\" !!' %0*   I   #&,!3 $   1#8 ) \"!&#\"$$ \" (('!%0N$, '<% \" # 8 \" 5!# \" $!'!!B\"U%(2 \" \" \"$' C$2 1 C+\"OR(i!; \"&\"!6 \201 !!& \" !!< !!E !!& \" !!0 ^ !!c! +2#,1S!$#\206!= /&g#+)* &*>*2.*   %.l#\"# !'('3 !'(T)6&Q-9 -$--F!#*G$/(+D@$` A!+('0\"'. %Ph$'3).u.M''#7!))6! 1  D&} \177!$!;!$!( \" \" \" 9!J & \"#  &#!!$$*&  &d%>\"A%!$&+(*#:1*$\"#-3\"$\"!' \"##(\" \" \" ! +!!&#$\" N\210_j4\207V\211H H y()+\"!; \"&\"!M)\"=8+& & & & & & & & :7\"\205 ?0 #!#$S!%!  m !&6 n#!-:\\,B':( 0B'<0\200\213b\216#\212eZ!~#@4[$' tD)!q!J!)GI$\"#$!\")].Q-'(8#\" 7!C.A#P,+(9 L+.!'(5#f/ !,!#-$!$!$+& & 6 .(w %!'(\215.5$7\214\203!rF&.#&- * # \" % % sW\2022a!KX*#,1,#%/ ?\"(# z)\"*')/$\" /*T#$!$!$! #%#%/");

    /**
     * Horizontal whitespace characters; not an actual Unicode category but probably more useful because it contains
     * the horizontal tab character while Unicode's {@link #Z} category does not.
     * <br>
     * Accessible in regexes via "<code>\p{Zh}</code>" or "<code>\p{Gh}</code>" .
     */
    public static final Category Horizontal = new Category(new int[]{0,9,10,23,37,48,128,2432,4001,5600},"! # & ) '\"$ % ( ");

    /**
     * Vertical whitespace characters; not an actual Unicode category but probably more useful because it contains the
     * newline and carriage return characters while Unicode's {@link #Z} category does not.
     * <br>
     * Accessible in regexes via "<code>\p{Zv}</code>" or "<code>\p{Gv}</code>" .
     */
    public static final Category Vertical = new Category(new int[]{0,1,3,10,120,8099},"#\"$ %!");

    /**
     * Whitespace characters, both horizontal and vertical; not an actual Unicode category but acts like the combination
     * of {@link #Horizontal} (Zh in regexes) and {@link #Vertical} (Zv in regexes) in that it includes both the obscure
     * Unicode characters that are in the Unicode Z category but also the practical characters such as carriage return,
     * newline, and horizontal tab that are not classified as whitespace by Unicode but are by everyone else.
     * <br>
     * Accessible in regexes via "<code>\pG</code>" or "<code>\p{G}</code>" (G for Gap).
     */
    public static final Category Space=new Category(new int[]{0,1,4,6,9,10,19,27,30,48,101,2432,4001,5600},"$\"& * ' - +%(!# ) , ");

    private static final char[] upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZµÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞĀĂĄĆĈĊČĎĐĒĔĖĘĚĜĞĠĢĤĦĨĪĬĮĲĴĶĹĻĽĿŁŃŅŇŊŌŎŐŒŔŖŘŚŜŞŠŢŤŦŨŪŬŮŰŲŴŶŸŹŻŽſƁƂƄƆƇƉƊƋƎƏƐƑƓƔƖƗƘƜƝƟƠƢƤƦƧƩƬƮƯƱƲƳƵƷƸƼǄǅǇǈǊǋǍǏǑǓǕǗǙǛǞǠǢǤǦǨǪǬǮǱǲǴǶǷǸǺǼǾȀȂȄȆȈȊȌȎȐȒȔȖȘȚȜȞȠȢȤȦȨȪȬȮȰȲȺȻȽȾɁɃɄɅɆɈɊɌɎͅͰͲͶͿΆΈΉΊΌΎΏΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫςϏϐϑϕϖϘϚϜϞϠϢϤϦϨϪϬϮϰϱϴϵϷϹϺϽϾϿЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯѠѢѤѦѨѪѬѮѰѲѴѶѸѺѼѾҀҊҌҎҐҒҔҖҘҚҜҞҠҢҤҦҨҪҬҮҰҲҴҶҸҺҼҾӀӁӃӅӇӉӋӍӐӒӔӖӘӚӜӞӠӢӤӦӨӪӬӮӰӲӴӶӸӺӼӾԀԂԄԆԈԊԌԎԐԒԔԖԘԚԜԞԠԢԤԦԨԪԬԮԱԲԳԴԵԶԷԸԹԺԻԼԽԾԿՀՁՂՃՄՅՆՇՈՉՊՋՌՍՎՏՐՑՒՓՔՕՖႠႡႢႣႤႥႦႧႨႩႪႫႬႭႮႯႰႱႲႳႴႵႶႷႸႹႺႻႼႽႾႿჀჁჂჃჄჅჇჍᏸᏹᏺᏻᏼᏽᲀᲁᲂᲃᲄᲅᲆᲇᲈᲐᲑᲒᲓᲔᲕᲖᲗᲘᲙᲚᲛᲜᲝᲞᲟᲠᲡᲢᲣᲤᲥᲦᲧᲨᲩᲪᲫᲬᲭᲮᲯᲰᲱᲲᲳᲴᲵᲶᲷᲸᲹᲺᲽᲾᲿḀḂḄḆḈḊḌḎḐḒḔḖḘḚḜḞḠḢḤḦḨḪḬḮḰḲḴḶḸḺḼḾṀṂṄṆṈṊṌṎṐṒṔṖṘṚṜṞṠṢṤṦṨṪṬṮṰṲṴṶṸṺṼṾẀẂẄẆẈẊẌẎẐẒẔẛẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼẾỀỂỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰỲỴỶỸỺỼỾἈἉἊἋἌἍἎἏἘἙἚἛἜἝἨἩἪἫἬἭἮἯἸἹἺἻἼἽἾἿὈὉὊὋὌὍὙὛὝὟὨὩὪὫὬὭὮὯᾸᾹᾺΆιῈΈῊΉῘῙῚΊῨῩῪΎῬῸΌῺΏΩKÅℲⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅬⅭⅮⅯↃⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⰀⰁⰂⰃⰄⰅⰆⰇⰈⰉⰊⰋⰌⰍⰎⰏⰐⰑⰒⰓⰔⰕⰖⰗⰘⰙⰚⰛⰜⰝⰞⰟⰠⰡⰢⰣⰤⰥⰦⰧⰨⰩⰪⰫⰬⰭⰮⱠⱢⱣⱤⱧⱩⱫⱭⱮⱯⱰⱲⱵⱾⱿⲀⲂⲄⲆⲈⲊⲌⲎⲐⲒⲔⲖⲘⲚⲜⲞⲠⲢⲤⲦⲨⲪⲬⲮⲰⲲⲴⲶⲸⲺⲼⲾⳀⳂⳄⳆⳈⳊⳌⳎⳐⳒⳔⳖⳘⳚⳜⳞⳠⳢⳫⳭⳲꙀꙂꙄꙆꙈꙊꙌꙎꙐꙒꙔꙖꙘꙚꙜꙞꙠꙢꙤꙦꙨꙪꙬꚀꚂꚄꚆꚈꚊꚌꚎꚐꚒꚔꚖꚘꚚꜢꜤꜦꜨꜪꜬꜮꜲꜴꜶꜸꜺꜼꜾꝀꝂꝄꝆꝈꝊꝌꝎꝐꝒꝔꝖꝘꝚꝜꝞꝠꝢꝤꝦꝨꝪꝬꝮꝹꝻꝽꝾꞀꞂꞄꞆꞋꞍꞐꞒꞖꞘꞚꞜꞞꞠꞢꞤꞦꞨꞪꞫꞬꞭꞮꞰꞱꞲꞳꞴꞶꞸꞺꞼꞾꟂꟄꟅꟆꟇꟉꟵꭰꭱꭲꭳꭴꭵꭶꭷꭸꭹꭺꭻꭼꭽꭾꭿꮀꮁꮂꮃꮄꮅꮆꮇꮈꮉꮊꮋꮌꮍꮎꮏꮐꮑꮒꮓꮔꮕꮖꮗꮘꮙꮚꮛꮜꮝꮞꮟꮠꮡꮢꮣꮤꮥꮦꮧꮨꮩꮪꮫꮬꮭꮮꮯꮰꮱꮲꮳꮴꮵꮶꮷꮸꮹꮺꮻꮼꮽꮾꮿＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺẞᾈᾉᾊᾋᾌᾍᾎᾏᾘᾙᾚᾛᾜᾝᾞᾟᾨᾩᾪᾫᾬᾭᾮᾯᾼῌῼ".toCharArray();
    private static final char[] lower = "abcdefghijklmnopqrstuvwxyzμàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþāăąćĉċčďđēĕėęěĝğġģĥħĩīĭįĳĵķĺļľŀłńņňŋōŏőœŕŗřśŝşšţťŧũūŭůűųŵŷÿźżžsɓƃƅɔƈɖɗƌǝəɛƒɠɣɩɨƙɯɲɵơƣƥʀƨʃƭʈưʊʋƴƶʒƹƽǆǆǉǉǌǌǎǐǒǔǖǘǚǜǟǡǣǥǧǩǫǭǯǳǳǵƕƿǹǻǽǿȁȃȅȇȉȋȍȏȑȓȕȗșțȝȟƞȣȥȧȩȫȭȯȱȳⱥȼƚⱦɂƀʉʌɇɉɋɍɏιͱͳͷϳάέήίόύώαβγδεζηθικλμνξοπρστυφχψωϊϋσϗβθφπϙϛϝϟϡϣϥϧϩϫϭϯκρθεϸϲϻͻͼͽѐёђѓєѕіїјљњћќѝўџабвгдежзийклмнопрстуфхцчшщъыьэюяѡѣѥѧѩѫѭѯѱѳѵѷѹѻѽѿҁҋҍҏґғҕҗҙқҝҟҡңҥҧҩҫҭүұҳҵҷҹһҽҿӏӂӄӆӈӊӌӎӑӓӕӗәӛӝӟӡӣӥӧөӫӭӯӱӳӵӷӹӻӽӿԁԃԅԇԉԋԍԏԑԓԕԗԙԛԝԟԡԣԥԧԩԫԭԯաբգդեզէըթժիլխծկհձղճմյնշոչպջռսվտրցւփքօֆⴀⴁⴂⴃⴄⴅⴆⴇⴈⴉⴊⴋⴌⴍⴎⴏⴐⴑⴒⴓⴔⴕⴖⴗⴘⴙⴚⴛⴜⴝⴞⴟⴠⴡⴢⴣⴤⴥⴧⴭᏰᏱᏲᏳᏴᏵвдосттъѣꙋაბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰჱჲჳჴჵჶჷჸჹჺჽჾჿḁḃḅḇḉḋḍḏḑḓḕḗḙḛḝḟḡḣḥḧḩḫḭḯḱḳḵḷḹḻḽḿṁṃṅṇṉṋṍṏṑṓṕṗṙṛṝṟṡṣṥṧṩṫṭṯṱṳṵṷṹṻṽṿẁẃẅẇẉẋẍẏẑẓẕṡạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹỻỽỿἀἁἂἃἄἅἆἇἐἑἒἓἔἕἠἡἢἣἤἥἦἧἰἱἲἳἴἵἶἷὀὁὂὃὄὅὑὓὕὗὠὡὢὣὤὥὦὧᾰᾱὰάιὲέὴήῐῑὶίῠῡὺύῥὸόὼώωkåⅎⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹⅺⅻⅼⅽⅾⅿↄⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩⰰⰱⰲⰳⰴⰵⰶⰷⰸⰹⰺⰻⰼⰽⰾⰿⱀⱁⱂⱃⱄⱅⱆⱇⱈⱉⱊⱋⱌⱍⱎⱏⱐⱑⱒⱓⱔⱕⱖⱗⱘⱙⱚⱛⱜⱝⱞⱡɫᵽɽⱨⱪⱬɑɱɐɒⱳⱶȿɀⲁⲃⲅⲇⲉⲋⲍⲏⲑⲓⲕⲗⲙⲛⲝⲟⲡⲣⲥⲧⲩⲫⲭⲯⲱⲳⲵⲷⲹⲻⲽⲿⳁⳃⳅⳇⳉⳋⳍⳏⳑⳓⳕⳗⳙⳛⳝⳟⳡⳣⳬⳮⳳꙁꙃꙅꙇꙉꙋꙍꙏꙑꙓꙕꙗꙙꙛꙝꙟꙡꙣꙥꙧꙩꙫꙭꚁꚃꚅꚇꚉꚋꚍꚏꚑꚓꚕꚗꚙꚛꜣꜥꜧꜩꜫꜭꜯꜳꜵꜷꜹꜻꜽꜿꝁꝃꝅꝇꝉꝋꝍꝏꝑꝓꝕꝗꝙꝛꝝꝟꝡꝣꝥꝧꝩꝫꝭꝯꝺꝼᵹꝿꞁꞃꞅꞇꞌɥꞑꞓꞗꞙꞛꞝꞟꞡꞣꞥꞧꞩɦɜɡɬɪʞʇʝꭓꞵꞷꞹꞻꞽꞿꟃꞔʂᶎꟈꟊꟶᎠᎡᎢᎣᎤᎥᎦᎧᎨᎩᎪᎫᎬᎭᎮᎯᎰᎱᎲᎳᎴᎵᎶᎷᎸᎹᎺᎻᎼᎽᎾᎿᏀᏁᏂᏃᏄᏅᏆᏇᏈᏉᏊᏋᏌᏍᏎᏏᏐᏑᏒᏓᏔᏕᏖᏗᏘᏙᏚᏛᏜᏝᏞᏟᏠᏡᏢᏣᏤᏥᏦᏧᏨᏩᏪᏫᏬᏭᏮᏯａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚßᾀᾁᾂᾃᾄᾅᾆᾇᾐᾑᾒᾓᾔᾕᾖᾗᾠᾡᾢᾣᾤᾥᾦᾧᾳῃῳ".toCharArray();
    private static final CharCharMap upperToLower = new CharCharMap(upper.length);
    private static final CharCharMap lowerToUpper = new CharCharMap(upper.length);

    static {
        for (int i = upper.length - 1; i >= 0; i--) {
            upperToLower.put(upper[i], lower[i]);
            lowerToUpper.put(lower[i], upper[i]);
        }
    }

    private static final char[] openers =
            new char[]{'(','<','[','{','༺','༼','᚛','⁅','⁽','₍','⌈','⌊','〈','❨','❪','❬','❮','❰','❲','❴','⟅','⟦',
                    '⟨','⟪','⟬','⟮','⦃','⦅','⦇','⦉','⦋','⦍','⦏','⦑','⦓','⦕','⦗','⧘','⧚','⧼','⸢','⸤','⸦','⸨',
                    '〈','《','「','『','【','〔','〖','〘','〚','〝','﴿','︗','︵','︷','︹','︻','︽','︿','﹁',
                    '﹃','﹇','﹙','﹛','﹝','（','［','｛','｟','｢'},
    closers =
            new char[]{')','>',']','}','༻','༽','᚜','⁆','⁾','₎','⌉','⌋','〉','❩','❫','❭','❯','❱','❳','❵','⟆','⟧',
                    '⟩','⟫','⟭','⟯','⦄','⦆','⦈','⦊','⦌','⦎','⦐','⦒','⦔','⦖','⦘','⧙','⧛','⧽','⸣','⸥','⸧','⸩',
                    '〉','》','」','』','】','〕','〗','〙','〛','〞','﴾','︘','︶','︸','︺','︼','︾','﹀','﹂',
                    '﹄','﹈','﹚','﹜','﹞','）','］','｝','｠','｣'};

    private static final CharCharMap openBrackets = new CharCharMap(openers, closers),
            closingBrackets = new CharCharMap(closers, openers);

    /**
     * Returns the given char c's lower-case representation, if it has one, otherwise returns it verbatim.
     * This is currently the same as {@link #caseDown(char)}, but this method may change in the future to more precisely
     * support case folding instead of caseDown()'s case mapping. Future code is encouraged to use
     * {@link #caseDown(char)} and {@link #caseUp(char)} for case mapping, and this method only when case folding is
     * preferred (such as for case-insensitive comparisons). Like caseDown(), this will prefer the char with the lower
     * codepoint if there are multiple possible conversions to lower-case.
     * @param c any char; this should only return a case-folded different char for upper-case letters
     * @return the single-char case-folded version of c, of it has one, otherwise c
     */
    public static char caseFold(char c)
    {
        if(upperToLower.containsKey(c))
        {
            return upperToLower.get(c);
        }
        return c;
    }

    /**
     * Returns the given char c's lower-case representation, if it has one, otherwise returns it verbatim.
     * This is currently the same as {@link #caseFold(char)}, but this method may change in the future if the subtle
     * distinction between case folding and case mapping is needed by this library. This is the counterpart to
     * {@link #caseUp(char)}, but because of the complexities of... language, calling caseDown() and then caseUp() will
     * not always return the original character. This has to do with how some characters, like lower-case s, have
     * multiple upper-case conversions possible, and the same in the other direction. This method and caseUp() both will
     * prefer the character that has a lower codepoint value if there are multiple possible conversions.
     * @param c any char; this should only return a lower-cased different char for upper-case letters
     * @return the single-char lower-case version of c, of it has one, otherwise c
     */
    public static char caseDown(char c)
    {
        if(upperToLower.containsKey(c))
        {
            return upperToLower.get(c);
        }
        return c;
    }

    /**
     * The counterpart to {@link #caseDown(char)} that returns the given char c's upper-case representation, if it has
     * one, otherwise it returns it verbatim. This has dubiously correct behavior for digraphs and ligature chars, but
     * they tend to be rare or even discouraged in practice. If there are multiple possible upper-case conversions from
     * the given character, this prefers the character with the lower codepoint value.
     * @param c any char; this should only return a case-folded different char for lower-case letters
     * @return the single-char upper-case version of c, if it has one, otherwise c
     */
    public static char caseUp(char c)
    {
        if(lowerToUpper.containsKey(c))
        {
            return lowerToUpper.get(c);
        }
        return c;
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
        {
            return openBrackets.get(c);
        }
        else if(closingBrackets.containsKey(c))
        {
            return closingBrackets.get(c);
        }
        return c;
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

        superCategories = new LinkedHashMap<String, Category>(16);
        superCategories.put("C", C);
        superCategories.put("L", L);
        superCategories.put("M", M);
        superCategories.put("N", N);
        superCategories.put("Z", Z);
        superCategories.put("P", P);
        superCategories.put("S", S);
        superCategories.put("J", Identifier);
        superCategories.put("G", Space);

        categories = new LinkedHashMap<String, Category>(64);
        categories.put("C", C);
        categories.put("L", L);
        categories.put("M", M);
        categories.put("N", N);
        categories.put("Z", Z);
        categories.put("P", P);
        categories.put("S", S);
        categories.put("J", Identifier);
        categories.put("G", Space);
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
        categories.put("Zh", Horizontal);
        categories.put("Zv", Vertical);
        categories.put("Gh", Horizontal);
        categories.put("Gv", Vertical);
        categories.put("Js", IdentifierStart);
        categories.put("Jp", IdentifierPart);
    }
}
