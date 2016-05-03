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
import java.io.Writer;

/**
 * <b>The Replacer class</b> suggests some methods to replace occurrences of a pattern
 * either by a result of evaluation of a perl-like expression, or by a plain string,
 * or according to a custom substitution model, provided as a Substitution interface implementation.<br>
 * A Replacer instance may be obtained either using Pattern.replacer(...) method, or by constructor:<pre>
 * Pattern p=new Pattern("\\w+");
 * Replacer perlExpressionReplacer=p.replacer("[$&amp;]");
 * //or another way to do the same
 * Substitution myOwnModel=new Substitution(){
 *    public void appendSubstitution(MatchResult match,TextBuffer tb){
 *       tb.append('[');
 *       match.getGroup(MatchResult.MATCH,tb);
 *       tb.append(']');
 *    }
 * }
 * Replacer myVeryOwnReplacer=new Replacer(p,myOwnModel);
 * </pre>
 * The second method is much more verbose, but gives more freedom.
 * To perform a replacement call replace(someInput):<pre>
 * System.out.print(perlExpressionReplacer.replace("All your base "));
 * System.out.println(myVeryOwnReplacer.replace("are belong to us"));
 * //result: "[All] [your] [base] [are] [belong] [to] [us]"
 * </pre>
 * This code was mostly written in 2001, I hope the reference isn't too outdated...
 * @see Substitution
 * @see PerlSubstitution
 * @see Replacer#Replacer(regexodus.Pattern, regexodus.Substitution)
 */

public class Replacer implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    private Pattern pattern;
    private Substitution substitution;

    /**
     * Unlikely to be used directly.
     * @param pattern a regexodus.Pattern that determines what should be replaced
     * @param substitution an implementation of the Substitution interface, which allows custom replacement behavior
     */
    public Replacer(Pattern pattern, Substitution substitution) {
        this.pattern = pattern;
        this.substitution = substitution;
    }

    /**
     * Constructs a Replacer from a Pattern and a String to replace occurrences of the Pattern with.
     * @param pattern a regexodus.Pattern that determines what should be replaced
     * @param substitution a String that will be used to replace occurrences of the Pattern
     */
    public Replacer(Pattern pattern, String substitution) {
        this(pattern, substitution, true);
    }

    public Replacer(Pattern pattern, String substitution, boolean isPerlExpr) {
        this.pattern = pattern;
        this.substitution = isPerlExpr ? new PerlSubstitution(substitution) :
                new DummySubstitution(substitution);
    }

    public void setSubstitution(String s, boolean isPerlExpr) {
        substitution = isPerlExpr ? new PerlSubstitution(s) :
                new DummySubstitution(s);
    }

    /**
     * Takes all instances in text of the Pattern this was constructed with, and replaces them with substitution.
     * @param text a String, StringBuilder, or other CharSequence that may contain the text to replace
     * @return the post-replacement text
     */
    public String replace(CharSequence text) {
        TextBuffer tb = wrap(new StringBuilder(text.length()));
        replace(pattern.matcher(text), substitution, tb);
        return tb.toString();
    }

    public String replace(char[] chars, int off, int len) {
        TextBuffer tb = wrap(new StringBuilder(len));
        replace(pattern.matcher(chars, off, len), substitution, tb);
        return tb.toString();
    }

    public String replace(MatchResult res, int group) {
        TextBuffer tb = wrap(new StringBuilder());
        replace(pattern.matcher(res, group), substitution, tb);
        return tb.toString();
    }

    @GwtIncompatible
    public String replace(Reader text, int length) throws IOException {
        TextBuffer tb = wrap(new StringBuilder(length >= 0 ? length : 0));
        replace(pattern.matcher(text, length), substitution, tb);
        return tb.toString();
    }

    /**
     * Takes all occurrences of the pattern this was constructed with in text and replaces them with the substitution.
     * Appends the replaced text into sb.
     * @param text a String, StringBuilder, or other CharSequence that may contain the text to replace
     * @param sb the StringBuilder to append the result into
     * @return the number of individual replacements performed; the results are applied to sb
     */
    public int replace(CharSequence text, StringBuilder sb) {
        return replace(pattern.matcher(text), substitution, wrap(sb));
    }

    /**
     */
    public int replace(char[] chars, int off, int len, StringBuilder sb) {
        return replace(chars, off, len, wrap(sb));
    }

    /**
     */
    public int replace(MatchResult res, int group, StringBuilder sb) {
        return replace(res, group, wrap(sb));
    }

    /**
     */
    public int replace(MatchResult res, String groupName, StringBuilder sb) {
        return replace(res, groupName, wrap(sb));
    }

    @GwtIncompatible
    public int replace(Reader text, int length, StringBuilder sb) throws IOException {
        return replace(text, length, wrap(sb));
    }

    /**
     */
    public int replace(CharSequence text, TextBuffer dest) {
        return replace(pattern.matcher(text), substitution, dest);
    }

    /**
     */
    private int replace(char[] chars, int off, int len, TextBuffer dest) {
        return replace(pattern.matcher(chars, off, len), substitution, dest);
    }

    /**
     */
    private int replace(MatchResult res, int group, TextBuffer dest) {
        return replace(pattern.matcher(res, group), substitution, dest);
    }

    /**
     */
    private int replace(MatchResult res, String groupName, TextBuffer dest) {
        return replace(pattern.matcher(res, groupName), substitution, dest);
    }

    @GwtIncompatible
    private int replace(Reader text, int length, TextBuffer dest) throws IOException {
        return replace(pattern.matcher(text, length), substitution, dest);
    }

    /**
     * Replaces all occurrences of a matcher's pattern in a matcher's target
     * by a given substitution appending the result to a buffer.<br>
     * The substitution starts from current matcher's position, current match
     * not included.
     */
    public static int replace(Matcher m, Substitution substitution, TextBuffer dest) {
        boolean firstPass = true;
        int c = 0;
        while (m.find()) {
            if (m.end() == 0 && !firstPass) continue;  //allow to replace at "^"
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            c++;
            m.setTarget(m, MatchResult.SUFFIX);
            firstPass = false;
        }
        m.getGroup(MatchResult.TARGET, dest);
        return c;
    }

    /**
     * Replaces the first n occurrences of a matcher's pattern, where n is equal to count,
     * in a matcher's target by a given substitution, appending the result to a buffer.
     * <br>
     * The substitution starts from current matcher's position, current match not included.
     */
    public static int replace(Matcher m, Substitution substitution, TextBuffer dest, int count) {
        boolean firstPass = true;
        int c = 0;
        while (c < count && m.find()) {
            if (m.end() == 0 && !firstPass) continue;  //allow to replace at "^"
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            c++;
            m.setTarget(m, MatchResult.SUFFIX);
            firstPass = false;
        }
        m.getGroup(MatchResult.TARGET, dest);
        return c;
    }

    @GwtIncompatible
    private static int replace(Matcher m, Substitution substitution, Writer out) throws IOException {
        try {
            return replace(m, substitution, wrap(out));
        } catch (WriteException e) {
            throw e.reason;
        }
    }

    @GwtIncompatible
    public void replace(CharSequence text, Writer out) throws IOException {
        replace(pattern.matcher(text), substitution, out);
    }

    @GwtIncompatible
    public void replace(char[] chars, int off, int len, Writer out) throws IOException {
        replace(pattern.matcher(chars, off, len), substitution, out);
    }

    @GwtIncompatible
    public void replace(MatchResult res, int group, Writer out) throws IOException {
        replace(pattern.matcher(res, group), substitution, out);
    }

    @GwtIncompatible
    public void replace(MatchResult res, String groupName, Writer out) throws IOException {
        replace(pattern.matcher(res, groupName), substitution, out);
    }

    @GwtIncompatible
    public void replace(Reader in, int length, Writer out) throws IOException {
        replace(pattern.matcher(in, length), substitution, out);
    }

    private static class DummySubstitution implements Substitution {
        String str;

        DummySubstitution(String s) {
            str = s;
        }

        public void appendSubstitution(MatchResult match, TextBuffer res) {
            if (str != null) res.append(str);
        }
    }

    public static TextBuffer wrap(final StringBuilder sb) {
        return new TextBuffer() {
            public void append(char c) {
                sb.append(c);
            }

            public void append(char[] chars, int start, int len) {
                sb.append(chars, start, len);
            }

            public void append(String s) {
                sb.append(s);
            }

            public String toString() {
                return sb.toString();
            }
        };
    }

    @GwtIncompatible
    private static TextBuffer wrap(final Writer writer) {
        return new TextBuffer() {
            public void append(char c) {
                try {
                    writer.write(c);
                } catch (IOException e) {
                    throw new WriteException(e);
                }
            }

            public void append(char[] chars, int off, int len) {
                try {
                    writer.write(chars, off, len);
                } catch (IOException e) {
                    throw new WriteException(e);
                }
            }

            public void append(String s) {
                try {
                    writer.write(s);
                } catch (IOException e) {
                    throw new WriteException(e);
                }
            }
        };
    }

    private static class WriteException extends RuntimeException {
        IOException reason;

        WriteException(IOException io) {
            reason = io;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Replacer replacer = (Replacer) o;

        return pattern != null ? pattern.equals(replacer.pattern) : replacer.pattern == null && (substitution != null ? substitution.equals(replacer.substitution) : replacer.substitution == null);

    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (substitution != null ? substitution.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Replacer{" +
                "pattern=" + pattern +
                ", substitution=" + substitution +
                '}';
    }
}