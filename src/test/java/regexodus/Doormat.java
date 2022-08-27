package regexodus;

/**
 * Dumb format. Meant for consumption by <a href="https://github.com/TheLogicMaster/switch-gdx">Switch-GDX</a>, which
 * doesn't have access to Formatter. May be useful in other places. Still, very limited.
 */
public class Doormat implements Substitution {

    private Doormat(Object... args){
        arguments = args;
        index = 0;
    }
    public static final Pattern pattern = Pattern.compile(
            "%(?:({=esc}%)|({=str}s)" +
                    "|({=int}({=flags}[ 0-]+)?({=width}[1-9][0-9]*)?d)" +
                    "|({=float}({=flags}[ 0-]+)?({=width}[1-9][0-9]*)?({=pre}\\.({=precise}[0-9]+))?f)" +
                    ")");

    private Object[] arguments;
    private int index = 0;
    @Override
    public void appendSubstitution(MatchResult match, TextBuffer dest) {
        if(match.isCaptured("str")) dest.append(arguments[index++].toString());
        else if(match.isCaptured("int")) {
            long n = ((Number)arguments[index++]).longValue();
            StringBuilder isb = new StringBuilder(Long.toString(n));
            int precision = -1;
            if(match.isCaptured("width")) precision = Integer.parseInt(match.group("width"));
            char pad = ' ';
            if(match.isCaptured("flags")){
                if(match.group("flags").contains("0")) pad = '0';
                if(n >= 0L && match.group("flags").contains(" ")) isb.insert(0, ' ');

                if(match.group("flags").contains("-"))
                    while (isb.length() < precision) isb.append(pad);
                else
                    while (isb.length() < precision) isb.insert(0, pad);
            }
            else{
                while (isb.length() < precision) isb.insert(0, pad);
            }
            dest.append(isb.toString());
        } else if(match.isCaptured("float")) {
            double f = ((Number)arguments[index++]).doubleValue();
            String is;
            if(match.isCaptured("pre")) {
                int p = Integer.parseInt(match.group("precise"));
                f += Math.copySign(0.5 * Math.pow(10.0, -p), f);
                is = Double.toString(f);
                int dot = is.lastIndexOf('.') + 1;
                String post = is.substring(dot, Math.min(is.length(), dot + p));
                is = is.substring(0, dot) + post;
            }
            else is = Double.toString(f);
            StringBuilder isb = new StringBuilder(is);
            int precision = -1;
            if(match.isCaptured("width")) precision = Integer.parseInt(match.group("width"));
            char pad = ' ';
            if(match.isCaptured("flags")){
                if(match.group("flags").contains("0")) pad = '0';
                // ugh, this copySign is needed because of -0.0
                if(Math.copySign(1.0, f) == 1.0 && match.group("flags").contains(" ")) isb.insert(0, ' ');

                if(match.group("flags").contains("-"))
                    while (isb.length() < precision) isb.append(pad);
                else
                    while (isb.length() < precision) isb.insert(0, pad);
            }
            else{
                while (isb.length() < precision) isb.insert(0, pad);
            }
            dest.append(isb.toString());
        } else if(match.isCaptured("esc")) dest.append('%');
    }

    public static String format(String format, Object... args) {
        Doormat doormat = new Doormat(args);
        Replacer replacer = new Replacer(pattern, doormat);
        return replacer.replace(format);
    }
}
