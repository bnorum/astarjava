// Values that can be inserted into a PriorityQ
public interface HeapValue<T> extends Comparable<T>
{
    int getIndex();
    void setIndex(int x);
}
