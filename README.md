# RegExodus
Regular expression lib; portable across Java variants, including GWT

## The Problem

Java applications using libraries like [LibGDX](https://libgdx.badlogicgames.com/)
can target multiple platforms with minimal changes to the codebase... most of the
time. Targeting HTML via [Google Web Toolkit](http://www.gwtproject.org/), or GWT,
involves using a subset of Java's standard library, one that does not include the
java.util.regex package and has only a few methods that take Strings to be interpreted
as semi-compatible regular expressions. These methods, like `String.matches(String)`,
use Java syntax for regular expressions on most targets, but use JavaScript syntax on
HTML via GWT. This incompatibility is particularly painful in regard to Unicode,
where JS is rather crippled compared to Java's fully-fledged understanding of Unicode.

Matching any letter seems easy enough with `[A-Za-z]` until the need to match letters
in French, German, Hebrew, and more comes up as the application finds I18N necessary.
Then, if you need to perform case-insensitive matching, things get even more troubling
with naive solutions... There needs to be a better way.

## A Solution

While working on the [SquidLib](https://github.com/SquidPony/SquidLib) game development
library, several useful classes needed to be marked as incompatible with GWT due to
the lack of a useful regular expression implementation that works cross-platform.
I set out to find a pure-Java regular expression engine that could run without a serious
speed loss on desktop and mobile platforms but could still work on GWT.

I found [JRegex](http://sourceforge.net/projects/jregex), a project by Sergey A.
Samokhodkin that was last substantially updated in 2002, and decided to modernize
it (using generics in collections that warn without it, using the newer HashMap and
ArrayList instead of HashTable and Vector for better single-threaded performance, and
so on). JRegex, at first glance, appeared to meet all the criteria I initially had, and
now that it has been modernized, its speed is reasonable for less-intensive usages of
regular expressions (when matching or replacing on desktop, expect 0.3x to 0.5x the rate
of java.util.regex , probably never faster than the normal regular expressions on desktop
but always better on GWT when compared with not having an implementation at all), and it
is essentially compatible with a superset of the java.util.regex API. The downside was,
it originally used the Unicode character database that ships with Java... except on GWT.
With some tricky code to minimize file sizes that encodes a bitset with a small int array
and a String by [gagern](https://gist.github.com/gagern/89db1179766a702c564d) using the
[Node.js Unicode database](https://github.com/mathiasbynens/node-unicode-data), I managed
to get the full Unicode 13.0.0 category information for the Basic Multilingual Plane (and
later, case folding information) in a single small-ish file of Java code. The compression
code is not in the distributed jar of source, but is in etc/generator.js , and the end
result is distributed in src/main/java/regexodus/Category.java (which also has case
folding information, and uses code adapted from [libGDX](https://github.com/libgdx/libgdx)
by way of [jdkgdxds](https://github.com/tommyettinger/jdkgdxds)). Now RegExodus acts like
an updated version of JRegex that carries much of Unicode with it, in a jar no more than
1/7 of a megabyte in size (currently). Though testing so far has been light, it seems to be
fully compatible with GWT, in development or production mode.

The name RegExodus comes from both the idea of taking Java regular expressions and
letting them free to roam various platforms, and because The Ten Commandments was on TV
when I was thinking of names for the project.

## Usage

Code-wise, usage should be transparent or require minimal changes if porting from
java.util.regex code like Pattern and Matcher; just change the package from
java.util.regex.Pattern to regexodus.Pattern, or use the new-in-0.1.6 regexodus.regex
package that copies java.util.regex's API more closely. It is possible that GWT's option
for "super-sourced" packages to replace unimplemented parts of the JRE may work here
to imitate an implementation of java.util.regex with a close approximation, but it
hasn't been attempted. Super-sourcing won't be completely compatible at the moment,
but is likely to work at least reasonably well with regexodus.regex .

Some usage will be easier if you can fully embrace RegExodus' style of regular
expressions, and the classes that use them. The `Replacer` class has a different API
than java.util.regex offers, and you can implement the `Substitution` interface for
more-involved replacements. The `Category` class has useful Unicode 13.0.0 info that
isn't especially easy to get from the JDK, and is great when you want to evaluate if
a particular character is, say, a lower-case letter. The documentation for the regex
flavor available here is mostly in the class JavaDocs for `Pattern`.

Installation should be simple if you use a build tool like Maven, Gradle, or the like.
For version or snapshot releases you can use JitPack (this repository is recommended
if you want snapshots) and Maven Central is an easy alternative for
version releases if you aren't able to add a third-party repository.
[JitPack instructions for common build tools are here](https://jitpack.io/#tommyettinger/RegExodus),
and [Maven Central instructions for more build tools are
here](http://search.maven.org/#artifactdetails%7Ccom.github.tommyettinger%7Cregexodus%7C0.1.14%7Cjar);
the 0.1.14 release is preferred for now, based on the 1.2 line of JRegex. You can
also download pre-built jars from the GitHub Releases page, or build from
source; this has no dependencies other than JUnit for tests.

## Changelog

0.1.2 adds support for a missing Java regex feature, `\Q...\E` literal sections.
It also fixes some not-insignificant issues with features not present in Java's
regex implementation, like an array index bug involving `\m...`, where those
character escapes with base-10 numbers could check outside the input string and
crash if the escape was at the end of a pattern.

0.1.3 fixes a bug in case-insensitive matching where it would previously only
match lower-case text if case-insensitive mode was on. Now it correctly matches
both `"A"` and `"a"` if given either `Pattern.compile("A", "i")` or
`Pattern.compile("a", "i")`. This was thought to have been tested, but the test
wasn't very good and this behavior may have persisted through several releases.

0.1.4 fixes a nasty bug that broke many long ranges in a character class
(spanning between Unicode blocks) where character ranges weren't always what
they claimed to be. If you use earlier than 0.1.4, updating is strongly
recommended to this or any more recent version.

0.1.5 adds additional features to backreferences and replacement, making certain
replacement-based operations much more convenient, like iterative replacement
that only replaces one match at a time. It also enhances backreferences so you
could require that an already-captured group be followed by that same group in
reverse character order ("cat" could be required to be followed by "tac"), among
other features like locally-case-insensitive backreferences, or even mirrored
brackets (if one of "(" or "{" was captured, you could require the backreference
to be the correctly matching ")" or "}") for most of the Unicode brackets.

0.1.6 adds an additional compatibility mode for Java regex compatibility, with
the new regexodus.regex package that can be swapped in as a mostly-complete
replacement for java.util.regex on platforms that don't have it. It also adds
some additional pseudo-Unicode categories for matching the tricky rules that
govern valid Java identifiers:  Js for the *s*tart of a Java identifier, and Jp
for any subsequent *p*art of a Java identifier. These can be used to match a
complete Java identifier with `Pattern.compile("\\p{Js}\\p{Jp}*")`. A convenience
class, ChanceSubstitution, allows an easier way to randomize the times when
a replacement is actually performed, leaving the match unchanged otherwise.
Matcher.foundStrings is a simple wrapper around the new MatchIterator.asList,
which both allow you to get all matching portions of a String as a List of
Strings, even if there are no groups in the Matcher's Pattern.

0.1.7 fixes a bug when getting a String from a Pattern that could (and often did,
when debugging or serializing to text) overflow the stack. It also adds two new
methods to make serializing Patterns easier, and allows you to retrieve the flags
from a Pattern. The bug fixed was relatively severe under some circumstances, so
updating is recommended.

~~0.1.8~~ had serious issues on GWT and has been replaced by 0.1.12.

0.1.9 improves GWT compatibility and adds the Unicode-like categories for
horizontal, vertical, and all whitespace as `Gh`, `Gv`, and `G`, respectively
(think G for Gap). These whitespace Category values include characters that are
conspicuously absent from the Unicode Z categories, such as tabs and all newline
characters in current use (\t, \r and \n are all in control categories instead of
whitespace under Z). The GWT compatibility changes entailed a package change,
taking `regexodus.regex` and moving it to `emu.java.util.regex`, but this allows
third-party libraries to use the normal Java regex API via GWT's super-source
mechanism and have it call RegExodus' shim layer instead, transparently. There
are possible issues if other libraries also super-source to implement
`java.util.regex`; libGDX does this and there are probably others out there.
I'm not sure what takes precedence in that case, but it seems to work so far in
basic GWT testing (SuperDev mode).

0.1.10 fixes compatibility with GWT 2.8.2 and lets the `\p{InBasicLatin}` and
`\P{Greek}` types of Unicode block matchers work (for the first time, possibly?).
It also updates Unicode Standard compatibility to 11.0.0, though only for the
Basic Multilingual Plane.

0.1.11 fixes a 20-year-old bug in `Matcher.setTarget(CharSequence, int, int)`
that affected any targets with a non-zero start. It replaces the utility data
structure `CharCharMap` with an implementation from jdkgdxds, which is largely
the same as libGDX's style, and fixes a few long-standing bugs in the old
version. It removes the utility data structure `CharArrayList` because it was
completely unnecessary here. There's an option in replacements to upper-case a
group captured from the search string, which rounds out the previous lower-casing
option. Finally, the Unicode data has been updated to 13.0.0.

0.1.12 is mostly a minor update, but fixes a bug in case mapping where certain
chars would have strangely-incorrect results for upper-case or lower-case
conversions, like `Category.caseUp('s')` returned `'ſ'` (the 1700s-era long-S).
Some actually-useful parts of PerlSubstitution are now public and documented,
where before they were only usable if reading the RegExodus sources. There's
also some cleanup on internals, which may help with debugging.

0.1.13 fixes some long-standing usually-minor issues with the `equals()` method
on Pattern and Term (it was extremely rarely used, but could enter an infinite
loop). It makes sure Pattern compares the flags, such as case-insensitive
mode or ignore-whitespace mode, as part of the Pattern's equality. This release
also matches Java's behavior with the `\\G` escape at the start of text.

0.1.14 includes some GWT fixes and changes the `inherits` line you need to:
```
<inherits name='regexodus.regexodus' />
```
It also fixes the behavior of Matcher in some cases, and Category for blocks
and non-BMP letters (which aren't supported, just now officially).

## Credit

This is a modified fork of JRegex, by Sergey A. Samokhodkin, meant to improve
compatibility with Android and GWT. This builds off Ed Ropple's work to make
JRegex Maven-friendly. This fork started with Ed Ropple's copy of jregex 1.2_01
([available on GitHub](https://github.com/eropple/jregex)). In addition, portions
of this code use modified versions of the collections from
[jdkgdxds](https://github.com/tommyettinger/jdkgdxds) (in the regexodus.ds package,
CharCharMap is derived from jdkgdxds, which is derived from
[libGDX](https://github.com/libgdx/libgdx)). Significant work by the team
responsible for [the Node.js Unicode database](https://github.com/mathiasbynens/node-unicode-data)
is invaluable here, especially [gagern](https://github.com/gagern) for creating
the compression technique that RegExodus uses on Unicode category data.

You can get the original jregex at: http://sourceforge.net/projects/jregex

## License

3-Clause BSD. See the file LICENSE in this directory for details.
