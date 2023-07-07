public class coord implements HeapValue<coord>
{
    int y, x;         // coordinates this object corresponds to
    int estcost;      // total cost, including knowncost and estimate**
    int knowncost;    // known (definite) costfrom source, excluding estimate
    boolean interior = false;
    int parent_y, parent_x; // coordinates of parent node on path from root
    coord(int a, int b) {y=a; x=b; parent_y=parent_x=-1; }
    
    public void set_parent(coord parent) {
        parent_y = parent.y;  parent_x = parent.x;
    }
    public void set_terminus() {
        parent_y = -1;  parent_x = -1; // root node only
    }
    public boolean is_terminus() { return parent_y<0 && parent_x<0; }
    
    public void copy_from(coord B) //replace information with those from  B
    {                         // but retain heap index info (before reposition)
	y = B.y;  x = B.x;
	estcost = B.estcost;
	knowncost = B.knowncost;
	interior = B.interior;
	parent_x = B.parent_x;  parent_y = B.parent_y;
    }//copy

    @Override
    public boolean equals(Object oc) // conforms to old java specs
    {
	if (oc==null || !(oc instanceof coord)) return false;
	coord c = (coord)oc;
	return (x==c.x && y==c.y);
    }
    // should also override hashCode if overriding equals, but
    // we're not going to be use hashing

    public int compareTo(coord c) // compares cost (not same as .equals)
    {
	return estcost - c.estcost;
    }

    // for HeapVal interface, allows repositioning once estcost changes
    protected int Hi=-1;  // index in heap (for HeapValue interface)
    public int getIndex() { return Hi; }
    public void setIndex(int i) { Hi=i; }
} // coord
