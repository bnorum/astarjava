public interface MinMaxHeap<T>
{
    int size();  // number of values of type T in queue
    MinMaxHeap<T> add(T x); // insert x into heap
    // returns and deletes the value with highest priority
    java.util.Optional<T> poll();  
    java.util.Optional<T> peek();  // returns value with highest priority

    // the following procedure repositions a value x in the queue after
    // a change to its priority.  returns false on failure.
    default boolean reposition(T x) { return false; }
}

