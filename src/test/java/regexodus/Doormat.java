package regexodus;

/**
 * Dumb format.
 */
public class Doormat implements Substitution {

    private Doormat(Object... args){
        arguments = args;
        index = 0;
    }
    public static final Pattern pattern = Pattern.compile(
            "%(?:({=str}s)|({=int}({=pad}[0 ])?({=precise}[1-9][0-9]*)?d))");

    private Object[] arguments;
    private int index = 0;
    @Override
    public void appendSubstitution(MatchResult match, TextBuffer dest) {
        if(match.isCaptured("str")) dest.append(arguments[index++].toString());
        else if(match.isCaptured("int")) {
            StringBuilder is = new StringBuilder(arguments[index++].toString());
            int precision = -1;
            if(match.isCaptured("precise")) precision = Integer.parseInt(match.group("precise"));
            char pad = ' ';
            if(match.isCaptured("pad")) pad = match.group("pad").charAt(0);
            while (is.length() < precision) is.insert(0, pad);
            dest.append(is.toString());
        }
    }

    public static String format(String format, Object... args) {
        Doormat doormat = new Doormat(args);
        Replacer replacer = new Replacer(pattern, doormat);
        return replacer.replace(format);
    }
}
