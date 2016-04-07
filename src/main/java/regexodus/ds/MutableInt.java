package regexodus.ds;

/**
 * Created by Tommy Ettinger on 4/7/2016.
 */
public class MutableInt {
    public int i;
    public MutableInt() {}
    public MutableInt(int num)
    {
        i = num;
    }
    public int set(int num)
    {
        return i = num;
    }
    public int get()
    {
        return i;
    }
    public int decrement()
    {
        return --i;
    }
    public int increment()
    {
        return ++i;
    }
    public int add(int num)
    {
        return (i += num);
    }
}
