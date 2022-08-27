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
            "%(?:({=esc}%)|({=str}s)" +
                    "|({=int}({=pad}[0 ])?({=width}[1-9][0-9]*)?d)" +
                    "|({=float}({=pad}[0 ])?({=width}[1-9][0-9]*)?({=pre}\\.({=precise}[0-9]+))?f)" +
                    ")");

    private Object[] arguments;
    private int index = 0;
    @Override
    public void appendSubstitution(MatchResult match, TextBuffer dest) {
        if(match.isCaptured("str")) dest.append(arguments[index++].toString());
        else if(match.isCaptured("int")) {
            StringBuilder isb = new StringBuilder(arguments[index++].toString());
            int precision = -1;
            if(match.isCaptured("width")) precision = Integer.parseInt(match.group("width"));
            char pad = ' ';
            if(match.isCaptured("pad")) pad = match.group("pad").charAt(0);
            while (isb.length() < precision) isb.insert(0, pad);
            dest.append(isb.toString());
        } else if(match.isCaptured("float")) {
            String is = arguments[index++].toString();
            if(match.isCaptured("pre")) {
                int dot = is.lastIndexOf('.') + 1;
                String post = is.substring(dot, Math.min(is.length(), dot + Integer.parseInt(match.group("precise"))));
                is = is.substring(0, dot) + post;
            }
            StringBuilder isb = new StringBuilder(is);
            int precision = -1;
            if(match.isCaptured("width")) precision = Integer.parseInt(match.group("width"));
            char pad = ' ';
            if(match.isCaptured("pad")) pad = match.group("pad").charAt(0);
            while (isb.length() < precision) isb.insert(0, pad);
            dest.append(isb.toString());
        } else if(match.isCaptured("esc")) dest.append('%');
    }

    public static String format(String format, Object... args) {
        Doormat doormat = new Doormat(args);
        Replacer replacer = new Replacer(pattern, doormat);
        return replacer.replace(format);
    }
}
