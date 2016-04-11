/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */

package regexodus;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;

/**
 * A handle for a precompiled regular expression; core operations should be identical to java.util.regex.Pattern .
 * Pattern should be no different.
 * <br>
 * To match a regular expression <code>myExpr</code> against a text <code>myString</code> one should first
 * create a Pattern object:<pre>
 * Pattern p = new Pattern(myExpr);
 * </pre>
 * or <pre>
 * Pattern p = Pattern.compile(myExpr);
 * </pre>
 * then obtain a Matcher object:<pre>
 * Matcher matcher=p.matcher(myText);
 * </pre>
 * The latter is an automaton that actually performs a search. It provides the following methods:
 * <li> search for matching substrings : matcher.find() or matcher.findAll();
 * <li> test whether the text matches the whole pattern : matcher.matches();
 * <li> test whether the text matches the beginning of the pattern : matcher.matchesPrefix();
 * <li> search with custom options : matcher.find(int options)
 * <p>
 * <b>Flags</b><br>
 * Flags (see REFlags interface) change the meaning of some regular expression elements at compiletime.
 * These flags may be passed both as string(see Pattern(String,String)) and as bitwise OR of:
 * <li><b>REFlags.IGNORE_CASE</b> - enables case insensitivity
 * <li><b>REFlags.MULTILINE</b> - forces "^" and "$" to match both at the start and the end of line;
 * <li><b>REFlags.DOTALL</b> - forces "." to match eols('\r' and '\n' in ASCII);
 * <li><b>REFlags.IGNORE_SPACES</b> - literal spaces in expression are ignored for better readability;
 * <li><b>REFlags.UNICODE</b> - the predefined classes('\w','\d',etc) are referenced to Unicode;
 * <li><b>REFlags.XML_SCHEMA</b> - permits XML Schema regular expressions syntax extensions.
 * <p>
 * <b>Multithreading</b><br>
 * Pattern instances are not thread-safe, and neither are Matcher objects.
 *
 * @see REFlags
 * @see Matcher
 * @see Matcher#setTarget(java.lang.String)
 * @see Matcher#setTarget(java.lang.String, int, int)
 * @see Matcher#setTarget(char[], int, int)
 * @see Matcher#setTarget(java.io.Reader, int)
 * @see MatchResult
 * @see MatchResult#group(int)
 * @see MatchResult#start(int)
 * @see MatchResult#end(int)
 * @see MatchResult#length(int)
 * @see MatchResult#charAt(int, int)
 * @see MatchResult#prefix()
 * @see MatchResult#suffix()
 */

public class Pattern implements Serializable, REFlags {
    String stringRepr;

    // tree entry
    Term root, root0;

    // required number of memory slots
    int memregs;

    // required number of iteration counters
    int counters;

    // number of lookahead groups
    int lookaheads;

    HashMap<String, Integer> namedGroupMap;

    boolean caseless = false;

    protected Pattern() throws PatternSyntaxException {
    }

    /**
     * Compiles an expression with default flags.
     *
     * @param regex the Perl5-compatible regular expression string.
     * @throws PatternSyntaxException if the argument doesn't correspond to perl5 regex syntax.
     * @see Pattern#Pattern(java.lang.String, java.lang.String)
     * @see Pattern#Pattern(java.lang.String, int)
     */
    public Pattern(String regex) throws PatternSyntaxException {
        this(regex, DEFAULT);
    }

    /**
     * Compiles a regular expression using Perl5-style flags.
     * The flag string should consist of letters 'i','m','s','x','u','X'(the case is significant) and a hyphen or plus.
     * The meaning of letters:
     * <ul>
     * <li><b>i</b> - case insensitivity, corresponds to REFlags.IGNORE_CASE;
     * <li><b>m</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to REFlags.MULTILINE flag;
     * <li><b>s</b> - single line treatment('.' matches \r's and \n's),corresponds to REFlags.DOTALL;
     * <li><b>x</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to REFlags.IGNORE_SPACES.
     * <li><b>u</b> - predefined classes are regarded as belonging to Unicode, corresponds to REFlags.UNICODE; this may yield some performance penalty.
     * <li><b>X</b> - compatibility with XML Schema, corresponds to REFlags.XML_SCHEMA.
     * <li><b>-</b> - turn off the specified flags; normally has no effect unless something adds the flags.
     * <li><b>+</b> - turn on the specified flags; normally is no different from just using the letters.
     * </ul>
     *
     * @param regex the Perl5-compatible regular expression string.
     * @param flags the Perl5-compatible flags.
     * @throws PatternSyntaxException if the argument doesn't correspond to perl5 regex syntax.
     *                                see REFlags
     */
    public Pattern(String regex, String flags) throws PatternSyntaxException {
        internalCompile(regex, parseFlags(flags));
    }

    /**
     * Compiles a regular expression using REFlags.
     * The <code>flags</code> parameter is a bitwise OR of the following values:
     * <ul>
     * <li><b>REFlags.IGNORE_CASE</b> - case insensitivity, corresponds to '<b>i</b>' letter;
     * <li><b>REFlags.MULTILINE</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to '<b>m</b>';
     * <li><b>REFlags.DOTALL</b> - single line treatment('.' matches \r's and \n's),corresponds to '<b>s</b>';
     * <li><b>REFlags.IGNORE_SPACES</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to '<b>x</b>'.
     * <li><b>REFlags.UNICODE</b> - predefined classes are regarded as belonging to Unicode, corresponds to '<b>u</b>'; this may yield some performance penalty.
     * <li><b>REFlags.XML_SCHEMA</b> - compatibility with XML Schema, corresponds to '<b>X</b>'.
     * </ul>
     *
     * @param regex the Perl5-compatible regular expression string.
     * @param flags the Perl5-compatible flags.
     * @throws PatternSyntaxException if the argument doesn't correspond to perl5 regex syntax.
     *                                see REFlags
     */
    private Pattern(String regex, int flags) throws PatternSyntaxException {
        internalCompile(regex, flags);
    }


    //java.util.regex.* compatibility

    /**
     * Compiles the given String into a Pattern that can be used to match text.
     * The syntax is normal for Java, including backslashes as part of regex syntax, like the digit shorthand "\d",
     * escaped twice to "\\d" (so the double-quoted String itself doesn't try to interpret the backslash).
     * @param regex a String in normal Java regular expression format
     * @return a newly constructed Pattern object that can be used to match text that fits the given regular expression
     * @throws PatternSyntaxException
     */
    public static Pattern compile(String regex) throws PatternSyntaxException{
        return new Pattern(regex, DEFAULT);
    }
    //java.util.regex.* compatibility

    /**
     * Compiles the given String into a Pattern that can be used to match text.
     * The syntax is normal for Java, including backslashes as part of regex syntax, like the digit shorthand "\d",
     * escaped twice to "\\d" (so the double-quoted String itself doesn't try to interpret the backslash).
     * <br>
     * This variant allows flags to be passed as an int constructed via bitwise OR from REFlags constants. You may prefer
     * the variant that takes a String for clarity.
     * @param regex a String in normal Java regular expression format
     * @param flags integer flags that are constructed via bitwise OR from the flag constants in REFlags.
     * @return a newly constructed Pattern object that can be used to match text that fits the given regular expression
     * @throws PatternSyntaxException
     */
    public static Pattern compile(String regex,int flags) throws PatternSyntaxException{
        return new Pattern(regex, flags);
    }
    //java.util.regex.* compatibility
    /**
     * Compiles the given String into a Pattern that can be used to match text.
     * The syntax is normal for Java, including backslashes as part of regex syntax, like the digit shorthand "\d",
     * escaped twice to "\\d" (so the double-quoted String itself doesn't try to interpret the backslash).
     * <br>
     * This variant allows flags to be passed as an String.
     * The flag string should consist of letters 'i','m','s','x','u','X'(the case is significant) and a hyphen or plus.
     * The meaning of letters:
     * <ul>
     * <li><b>i</b> - case insensitivity, corresponds to REFlags.IGNORE_CASE;
     * <li><b>m</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to REFlags.MULTILINE flag;
     * <li><b>s</b> - single line treatment('.' matches \r's and \n's),corresponds to REFlags.DOTALL;
     * <li><b>x</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to REFlags.IGNORE_SPACES.
     * <li><b>u</b> - predefined classes are regarded as belonging to Unicode, corresponds to REFlags.UNICODE; this may yield some performance penalty.
     * <li><b>X</b> - compatibility with XML Schema, corresponds to REFlags.XML_SCHEMA.
     * <li><b>-</b> - turn off the specified flags; normally has no effect unless something adds the flags.
     * <li><b>+</b> - turn on the specified flags; normally is no different from just using the letters.
     * </ul>
     *
     * @param regex a String in normal Java regular expression format
     * @param flags integer flags that are constructed via bitwise OR from the flag constants in REFlags.
     * @return a newly constructed Pattern object that can be used to match text that fits the given regular expression
     * @throws PatternSyntaxException
     */
    public static Pattern compile(String regex,String flags) throws PatternSyntaxException{
        return new Pattern(regex, flags);
    }


    private void internalCompile(String regex, int flags) throws PatternSyntaxException {
        stringRepr = regex;
        caseless = (flags & IGNORE_CASE) == IGNORE_CASE;
        Term.makeTree(regex, flags, this);
    }

    /**
     * How many capturing groups does this expression include?
     */
    public int groupCount() {
        return memregs;
    }

    /**
     * Get numeric id for a group name.
     *
     * @return <code>null</code> if no such name found.
     * @see MatchResult#group(java.lang.String)
     * @see MatchResult#isCaptured(java.lang.String)
     */
    public Integer groupId(String name) {
        return (namedGroupMap.get(name));
    }

    /**
     * A shorthand for Pattern.matcher(String).matches().<br>
     *
     * @param s the target
     * @return true if the entire target matches the pattern
     * @see Matcher#matches()
     * @see Matcher#matches(String)
     */
    public boolean matches(String s) {
        return matcher(s).matches();
    }

    /**
     * A shorthand for Pattern.matcher(String).matchesPrefix().<br>
     *
     * @param s the target
     * @return true if the entire target matches the beginning of the pattern
     * @see Matcher#matchesPrefix()
     */
    public boolean startsWith(String s) {
        return matcher(s).matchesPrefix();
    }

    /**
     * Returns a target-less matcher.
     * Don't forget to supply a target.
     */
    public Matcher matcher() {
        return new Matcher(this);
    }

    /**
     * Returns a matcher for a specified string.
     */
    public Matcher matcher(String s) {
        Matcher m = new Matcher(this);
        m.setTarget(s);
        return m;
    }

    /**
     * Returns a matcher for a specified region.
     */
    public Matcher matcher(char[] data, int start, int end) {
        Matcher m = new Matcher(this);
        m.setTarget(data, start, end);
        return m;
    }

    /**
     * Returns a matcher for a match result (in a performance-friendly way).
     * <code>groupId</code> parameter specifies which group is a target.
     *
     * @param groupId which group is a target; either positive integer(group id), or one of MatchResult.MATCH,MatchResult.PREFIX,MatchResult.SUFFIX,MatchResult.TARGET.
     */
    public Matcher matcher(MatchResult res, int groupId) {
        Matcher m = new Matcher(this);
        if (res instanceof Matcher) {
            m.setTarget((Matcher) res, groupId);
        } else {
            m.setTarget(res.targetChars(), res.start(groupId) + res.targetStart(), res.length(groupId));
        }
        return m;
    }

    /**
     * Just as above, yet with symbolic group name.
     *
     * @throws NullPointerException if there is no group with such name
     */
    public Matcher matcher(MatchResult res, String groupName) {
        Integer id = res.pattern().groupId(groupName);
        if (id == null) throw new IllegalArgumentException("group not found:" + groupName);
        int group = id;
        return matcher(res, group);
    }

    /**
     * Returns a matcher taking a text stream as target.
     * <b>Note that this is not a true POSIX-style stream matching</b>, i.e. the whole length of the text is preliminary read and stored in a char array.
     *
     * @param text   a text stream
     * @param length the length to read from a stream; if <code>len</code> is <code>-1</code>, the whole stream is read in.
     * @throws IOException indicates an IO problem
     */
    @GwtIncompatible
    public Matcher matcher(Reader text, int length) throws IOException {
        Matcher m = new Matcher(this);
        m.setTarget(text, length);
        return m;
    }

    /**
     * Returns a replacer of a pattern by specified perl-like expression.
     * Such replacer will substitute all occurrences of a pattern by an evaluated expression
     * ("$&" and "$0" will substitute by the whole match, "$1" will substitute by group#1, etc).
     * Example:<pre>
     * String text="The quick brown fox jumped over the lazy dog";
     * Pattern word=new Pattern("\\w+");
     * System.out.println(word.replacer("[$&]").replace(text));
     * //prints "[The] [quick] [brown] [fox] [jumped] [over] [the] [lazy] [dog]"
     * Pattern swap=new Pattern("(fox|dog)(.*?)(fox|dog)");
     * System.out.println(swap.replacer("$3$2$1").replace(text));
     * //prints "The quick brown dog jumped over the lazy fox"
     * Pattern scramble=new Pattern("(\\w+)(.*?)(\\w+)");
     * System.out.println(scramble.replacer("$3$2$1").replace(text));
     * //prints "quick The fox brown over jumped lazy the dog"
     * </pre>
     *
     * @param expr a perl-like expression, the "$&" and "${&}" standing for whole match, the "$N" and "${N}" standing for group#N, and "${Foo}" standing for named group Foo.
     * @see Replacer
     */
    public Replacer replacer(String expr) {
        return new Replacer(this, expr);
    }

    /**
     * Returns a replacer will substitute all occurrences of a pattern
     * through applying a user-defined substitution model.
     *
     * @param model a Substitution object which is in charge for match substitution
     * @see Replacer
     */
    public Replacer replacer(Substitution model) {
        return new Replacer(this, model);
    }

    /**
     * Tokenizes a text by an occurrences of the pattern.
     * Note that a series of adjacent matches are regarded as a single separator.
     * The same as new RETokenizer(Pattern,String);
     *
     * @see RETokenizer
     * @see RETokenizer#RETokenizer(regexodus.Pattern, java.lang.String)
     */
    public RETokenizer tokenizer(String text) {
        return new RETokenizer(this, text);
    }

    /**
     * Tokenizes a specified region by an occurrences of the pattern.
     * Note that a series of adjacent matches are regarded as a single separator.
     * The same as new RETokenizer(Pattern,char[],int,int);
     *
     * @see RETokenizer
     * @see RETokenizer#RETokenizer(regexodus.Pattern, char[], int, int)
     */
    public RETokenizer tokenizer(char[] data, int off, int len) {
        return new RETokenizer(this, data, off, len);
    }

    /**
     * Tokenizes a specified region by an occurrences of the pattern.
     * Note that a series of adjacent matches are regarded as a single separator.
     * The same as new RETokenizer(Pattern,Reader,int);
     *
     * @see RETokenizer
     * @see RETokenizer#RETokenizer(regexodus.Pattern, java.io.Reader, int)
     */
    @GwtIncompatible
    public RETokenizer tokenizer(Reader in, int length) throws IOException {
        return new RETokenizer(this, in, length);
    }

    public String toString() {
        return stringRepr;
    }

    /**
     * Returns a less or more readable representation of a bytecode for the pattern.
     */
    public String toString_d() {
        return root.toStringAll();
    }

    private static int parseFlags(String flags) throws PatternSyntaxException {
        boolean enable = true;
        int len = flags.length();
        int result = DEFAULT;
        for (int i = 0; i < len; i++) {
            char c = flags.charAt(i);
            switch (c) {
                case '+':
                    enable = true;
                    break;
                case '-':
                    enable = false;
                    break;
                default:
                    int flag = getFlag(c);
                    if (enable) result |= flag;
                    else result &= (~flag);
            }
        }
        return result;
    }

    static int parseFlags(char[] data, int start, int len) throws PatternSyntaxException {
        boolean enable = true;
        int result = DEFAULT;
        for (int i = 0; i < len; i++) {
            char c = data[start + i];
            switch (c) {
                case '+':
                    enable = true;
                    break;
                case '-':
                    enable = false;
                    break;
                default:
                    int flag = getFlag(c);
                    if (enable) result |= flag;
                    else result &= (~flag);
            }
        }
        return result;
    }

    private static int getFlag(char c) throws PatternSyntaxException {
        switch (c) {
            case 'i':
                return IGNORE_CASE;
            case 'm':
                return MULTILINE;
            case 's':
                return DOTALL;
            case 'x':
                return IGNORE_SPACES;
            case 'u':
                return UNICODE;
            case 'X':
                return XML_SCHEMA;
        }
        throw new PatternSyntaxException("unknown flag: " + c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern = (Pattern) o;

        if (memregs != pattern.memregs) return false;
        if (counters != pattern.counters) return false;
        if (lookaheads != pattern.lookaheads) return false;
        if (stringRepr != null ? !stringRepr.equals(pattern.stringRepr) : pattern.stringRepr != null) return false;
        return root != null ? root.equals(pattern.root) : pattern.root == null && (root0 != null ? root0.equals(pattern.root0) : pattern.root0 == null && (namedGroupMap != null ? namedGroupMap.equals(pattern.namedGroupMap) : pattern.namedGroupMap == null));

    }

    @Override
    public int hashCode() {
        int result = stringRepr != null ? stringRepr.hashCode() : 0;
        result = 31 * result + (root != null ? root.hashCode() : 0);
        result = 31 * result + (root0 != null ? root0.hashCode() : 0);
        result = 31 * result + memregs;
        result = 31 * result + counters;
        result = 31 * result + lookaheads;
        result = 31 * result + (namedGroupMap != null ? namedGroupMap.hashCode() : 0);
        return result;
    }
}