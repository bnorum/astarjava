import java.util.Optional;
import java.util.Comparator;

// Heap keeps largest value on top according to the Comparator.

/*  This program uses the java.util.Comparator interface in addition to
    the Comparable interface.  This makes it easier to change how objects
    are compared.  
 
    interface Comparator<T> {
       int compare(T a, T b)
    }

    sample usage: 
    Comparator<Double> dcmp = (x,y) -> (int)(x*1000000+.5)-(int)(y*1000000+.5);
    Given objects a and b, instead of calling a.compareTo(b) call instead
    dcmp.compare(a,b).

    However, we still need the Comparable interface in order to create a
    default Comparator.

    The MinMaxHeap interface is as follows (in separate file):

public interface MinMaxHeap<T>
{
    int size();  // number of values of type T in queue
    MinMaxHeap<T> add(T x); // insert x into heap
    java.util.Optional<T> poll(); //deletes and returns highest priority value
    java.util.Optional<T> peek(); //highest priority value without delete

    // the following procedure repositions a value x in the queue after
    // a change to its priority.  returns false on failure.
    default boolean reposition(T x) { return false; }
}
*/

public class PriorityHeap<T extends HeapValue<? super T>>
    implements MinMaxHeap<T>  {
    
    // default comparator uses the same Comparable implementation.
    // usage: cmp.compare(a,b) instead of a.compareTo(b)
    protected Comparator<T> cmp = (x,y) -> x.compareTo(y);
    public void setComparator(Comparator<T> newcmp) {
	if (newcmp!=null && size<2) cmp = newcmp;
    }

    protected T[] H; // Heap array underneath
    protected int size = 0;
    protected boolean maxheap = true;   // minheap if false
    public static final int default_cap = 16;
    public int size() { return size; }
    public int capacity() { return H.length; }

    @SuppressWarnings("unchecked")
    protected T[] makearray(int n) {return (T[]) new HeapValue[n];}

    public PriorityHeap(int n, boolean mh) {//mh true means maxheap
	size = 0;
	maxheap = mh;
	if (maxheap) cmp = (x,y) -> x.compareTo(y); //maxheap
	else cmp = (x,y) -> y.compareTo(x);  // minheap
	if (n<1) n= default_cap;
	H = makearray(n);
    }//constructor
    public PriorityHeap() { this(default_cap,true); } //calls other constructor

    // constructor takes array, size, and calls heapify
    public PriorityHeap(T[] A, int n, boolean mh) { 
	this(default_cap,true);
	if (A!=null && A.length>=n && n>0) {
	    H = A;
	    size = n;
	    maxheap = mh;
	    cmp = (x,y) -> x.compareTo(y);
	    if (!maxheap) cmp = (x,y) -> y.compareTo(x);
	    heapify();
	}
    }// another constructor

    // index calculations of left, right child, parent
    protected int left(int i) { return 2*i+1; }
    protected int right(int i) {return 2*i+2; }
    protected int parent(int c) {return (c-1)/2;}

    // swap H[i] with H[k]:
    protected void swap(int i, int k) {
	T tmp = H[i];  H[i] = H[k];  H[k] = tmp;
        H[i].setIndex(i);  H[k].setIndex(k);
    }

    // Basic operation to swap upwards starting at a given index i,
    // returns final index of object.
    protected int swapup(int i) {
	int p = parent(i);
	while (i>0 && i<size && cmp.compare(H[i],H[p])>0) {
	    swap(i,p);
	    i = p;
	    p = parent(i);
	}//while
	return i;
    }//swapup

    protected int swapdown(int i) {
	if (i<0) return i;
	int sc = 1;  // swap candidate
	while (sc != -1) { // while possible to swap
	    sc = -1; // no swap
	    int li = left(i); // index of left child
	    int ri = right(i);// index of right child
	    if (li<size && cmp.compare(H[i],H[li])<0) sc = li;
	    // swap with larger of two children
	    if (ri<size
		&& cmp.compare(H[i],H[ri])<0
		&& cmp.compare(H[li],H[ri])<0)    sc = ri;
	    if (sc != -1) {   // swap if candidate is valid, else stop
		swap(i,sc);
		i = sc;
	    }
	}//while
	return i;
    }//swapdown

    protected void resize() {
	T[] H2 = makearray(H.length*2);
	System.arraycopy(H,0,H2,0,size);
	H = H2;
    } // force capacity to be 2*size

    
    // MinMaxHeap interface functions
    public PriorityHeap<T> add(T x) {   // amortized O(log n) worst case
	if (x==null) {                // amortized O(1) average case
	    System.err.println("null value ignored in PriorityHeap.add");
	    return this;
	}
	if (size == H.length) resize(); // double capacity
	H[size] = x; // place at end of tree
        H[size].setIndex(size);
        size++;
	swapup(size-1);
	return this;
    }//add

    public Optional<T> peek() {       // O(1)
	if (size==0) return Optional.empty();
	else return Optional.of(H[0]);
    }//peek

    // delete and return highest value    
    public Optional<T> poll() {       // O(log n)
	if (size==0) return Optional.empty();
	Optional<T> answer = Optional.of(H[0]);
	H[0] = H[size-1]; // copy last value to root
        H[0].setIndex(0);
        if (size>1) H[size-1] = null; // inform GC
        size--;
	swapdown(0);
	return answer;
    }//poll

    // transforms the first n values of any array into a heap
    public void heapify() { 
	if (size<2) return;
	// there are always (size+1)/2 number of leaf nodes
	int nonleafs = size - (size+1)/2;
	for(int i = nonleafs-1;i>=0;i--) swapdown(i);
    }//heapify, O(n) because of convergent infinite series

    public boolean reposition(T x)
    {
        if (x==null) return false;
        int xi = x.getIndex();
        if (xi<0 || xi>=size || H[xi]!=x) {
            System.err.printf("invalid HeapValue index %d, no repositioning took place\n",xi);
            return false;
        }
        int newi = swapup(xi);
        if (newi==xi) swapdown(xi); // only call if swapup didn't change xi
        return true;
    }//reposition

    T[] getarray()  { return H; } // returns underlying array (for diagnostics)

    /*
    // a little main for testing
    public static void main(String[] av) throws Exception {
	int n = 100;
	if (av.length>0) n = Integer.parseInt(av[0]);
	var Heap = new PriorityHeap<Integer>();
	for(int i=0;i<n;i++) {
	    Heap.add((Integer)(int)(Math.random()*n*2));
	}

	//HeapDisplay HD = new HeapDisplay(1000,700); // graphical display
	//HD.drawtree(Heap.getarray(),Heap.size());
	Thread.sleep(5000); // 5 sec. delay
	for(int i=0;i<n/2;i++) System.out.println(Heap.poll());
	//HD.drawtree(Heap.getarray(),Heap.size());

	Heap.peek()
	    .ifPresent(x -> System.out.println("top value = "+x));
       	Thread.sleep(5000); // 5 sec. delay
        // testing heapify:
        Character[] C = {'a','b','c','d','e','f','g','h','i','j','k'};
        var CH = new PriorityHeap<Character>(C,C.length,true); // make max heap
        //HD.drawtree(C,C.length);
    }//main
    */
}//Binary Heap class
