		CSC17  Final Programming Assignment.

You are to implement the A* search algorithm on a hexagonal grid as
described in class.  I've provided you with enough code so you can focus
on the basic algorithm.  These instructions assume that you were present
during the classroom presentation and understand Dijkstra's Algorithm.

                     DATA STRUCTURES OVERVIEW:

We must represent the "interior" and the "frontier" of Dijkstra's
Algorithm efficiently so that the algorithm will run in the expected
O(n*log n) time.  The recommendation, and the base code, assumes that:

   1. The hexagonal map is represented by a 2D array int[][] M.  The
      value of each M[y][x] represents the type of terrain
      (1=land, 3=water, etc).  There is also a "costof" vector that
      indicates the cost of each type of terrain.

        ** Coordinates are always given with y first, then x **

   1. Each "node" in the search graph is represented by a "coord" object,
      containing x,y coordinates, its cost to source and estimated cost to
      destination. Each coord object will also indicate its "parent"
      coordinates on the path back to source, along with other information.

   2. The frontier is represented by a PriorityHeap (minheap) of coord
      objects that has the ability to "reposition" objects when their
      priorities change.  I have provided you with my PriorityHeap class.

   3. However, one disadvantage of priority heaps is that search is
      still O(n).  So in addition to the heap, we will also have a 2D
      array coord[][] PATH, so coord objects can be looked up by y,x
      coordinates.  Keep in mind that the array contains *pointers* to
      objects, so the priority heap and the 2D array can point to the
      same set of objects.

   4. For the interior, instead of another data structure we just keep a
      boolean flag 'interior' inside each coord object.

   Thus given y,x coordinates, we can lookup PATH[y][x].  If this value
   is null, that means we've never encountered this coordinate (so we
   need to add one). If PATH[y][x].interior==true, then this coordinate is
   on the interior (and we do nothing).  Otherwise, PATH[y][x] is on the
   frontier which means that there may yet be a better way to get to y,x
   from the source.

----------------------------------------------------------------------

First, download all relevant files (in .zip), including gifs and jpeg
images from the homepage into a separate folder.  You need to
understand carefully certain parts of the *coord.java* file and the
*astar_base.java* file.

***1. coord.java.  The coord class represents "nodes" in our search graph.

coord objects are designed to be referenced both from a 2D array
(coord[][] PATH in astar_base class), and a PriorityHeap class with a
"reposition" method, and thus implement HeapValue<coord>.

public class coord implements HeapValue<coord>
{
    int y, x;         // map coordinates this object corresponds to
    int estcost;      // total cost, including knowncost and estimate***
    int knowncost;    // known cost from source, excluding estimate
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

    // for HeapValue interface, allows repositioning once estcost changes
    protected int Hi=-1;  // index in heap (for HeapValue interface)
    public int getIndex() { return Hi; }
    public void setIndex(int i) { Hi=i; }
} // coord (contents of coord.java)

It's very important that you understand:

* knowncost represents the cost accumulated so far on the path from
  the source coord to the current coord (y,x).

* estcost should represent knowncost + estimated cost to the target.

  The suggested way to estimate is to use the astar_base.hexdist function.

* All coord objects must be placed in the coord[][] PATH array.  The
  parent_y,parent_x coordinates allows you to lookup the "parent" coord
  object in the array.  There is no parent if .is_terminus() is true.

* compareTo(coord c): this method implements the interface
  Comparable<coord>.  It compares the .estcost between 'this' and c.

  NOTE ALSO that this function is not compatible with equals:
  A.compareTo(B)==0 if coords A and B have the same estcost measure, but
  A.equals(B)==true if A, B have the same x,y values.

* interior boolean flag indicates if node belongs to the interior or frontier

* the method 'copy_from' copies info from a coord object into 'this'
object, but does not copy the HeapValue index, so we can change its
position in a priority heap that has the ability to 'reposition.'


***2. astar_base.java:
   In the astar_base class, code has already been written (in the
   constructor) to generate a random map containing land (grassy
   texture), water and fire.  You shouldn't have to touch this unless
   you add another type of terrain (see optional challenges).
   
   The following CRITICAL structures are declared in the astar_base class
   that you will inherit in your myastar subclass:

 int[][] M;  // the map with value=terrain type
 coord[][] PATH; // array of coord objects
 int ROWS, COLS; // size of both M and PATH arrays

   Initially, each PATH[y][x] is null, as that's the default when arrays
   are created. For the base program, the values in M are 0 for open land,
   3 for water and 2 for fire.  Values 1 and others are not currently used.
   
 public static int[] DY = Hexagon.DY;
 public static int[][] DX = Hexagon.DX;

   Although the map is a 2D array, each array cell is treated as a
   hexagon which means that it has six immediate neighbors.  The
   Hexagon class (in Hexagon.java) extends java.awt.Polygon.  The
   neighbors are designated in order west, northwest, northeast, east,
   southeast, and southwest.  The vector DY defines the row coordinate
   displacement: so given current row coordinate y, y+DY[5] will give
   the row coordinate of its southwest neighbor.  The column (DX)
   displacement is more complicated because it's different for even
   and odd rows.  Therefore DX is defined as a 2D int[][] array with
   two vectors: the first (0th) vector is to be used if the current
   row coordinate is 0 or even, and the second (1st) vector is to be used
   if the current row coordinate is odd.  Thus, if the current row
   is y and the current column is x, then x+DX[y%2][5] will give the
   column coordinate of the southwest neighbor.  **As usual, you must
   always check if y,x stay within bounds**.

 int[] costof = {1,0,8000,8};  // cost vector
 public void setcosts(int l, int d, int f, int w) // call to change costs

   The costof vector indicates the cost of each type of terrain.  This
   means, for example, that the cost of land is 1 and the cost of
   water is 8. The cost of going to a map coordinate y,x is costof[M[y][x]].
   *A cost of -1 means that the terrain type is impassable.*

 public static int hexdist(int y1, int x1, int y2, int x2) {...}

   This function conservatively estimates the shortest distance between
   two coordinates in the hexagonal grid. This function can be used to
   calculate the heuristic estimate that's part of algorithm A*.

 public coord makeneighbor(coord p, int y, int x, int ty, int tx)

   This is a convenient function that you might find useful, but you need
   to study carefully how it works in order to use it in the right way. 


-------------------- WHAT YOU NEED TO WRITE ----------------------------

***You must write a 'class myastar extends astar_base'.  A template has
been created in myastar.java.  You must call the subclass "myastar"
because that's referred to in patherfinder.java. ALL your code must
be in this class.  You need to

   @Override
   public Optional<coord> search(int sy, int sx, int ty, int tx)

  This method should find an optimal path from the source coordinates
sy,sx to the destination coordinates ty,tx.  You need to construct a
coord object with y,x=ty,tx, and with parenty,parentx set that leads
back to a coord with y,x=sy,sx.  The parenty,parentx coordinates
should "point" to another coord objects on the coord[][]PATH array.

***Your search function will either return Optional.of(the coord
object with ty,tx), or Optional.empty() if there is no solution.  If
some terrain has cost -1, then there may be no solution.  My
pathfinder program will then use this information to produce an
animation.  **The animation doesn't run until you have found the
entire path.**

You must implement the A* algorithm that runs in O(6*n*log n) time.

Pseudocode Outline:

public Optional<coord> search(int sy, int sx, int ty, int tx) { ...

    insert a new coord(sy,sx) representing the start node, into the frontier.
    Declare a coord pointer "current".
    
    while (frontier is not empty)
    {
       set "current" to frontier coord with lowest estcost (poll)

       move current from frontier to interior.
       stop loop if current.y==ty && current.x==tx. (return current)

       for each of the (up to) six neighbors of the current node
       {
         ny=current.y + DY[direction index]
         nx=current.x + DX[current.y%2][direction index]

         check if ny,nx are within bounds 0..ROWS, 0..COLS...

         coord neighbor = new coord(ny,nx);

         set the neighbor's "parent" to coordinates of current.

         set neighbor.knowncost to cost of terrain (costof[M[ny][nx]]) 
          + current.knowncost

          calculate neighbor.estcost using neighbor.knowncost and hexdist.

         if PATH[ny][nx]==null : add neighbor to frontier (heap and PATH)
         
         else if PATH[ny][nx].interior==true: do nothing
         
         else (PATH[ny][nx].interior==false) : compare  the new neighbor
           coord's estcost with the existing one (PATH[ny][nx]). If the
           neighbor has lower cost, **copy** the information from neighbor
           into the existing coord (PATH[ny][nx]), and call reposition on
           the priority heap.
       }//for
    } // while
}//search

Additionally, You can customize the cost vector in the 'void customize()`
function: follow the samples in the skeleton.

==================================================================

   **********--------Running the program...---------***********

javac *.java (in project directory)

Run with  java myastar (or java pathfinder):  
   The program will generate a new map, run your search function and 
   save map and starting positions in myastar.run

Run with  java myastar myastar.run
   will now load map, starting positions and cost vector from specified file.
   You can rename myastar.run to something else if you wish to save it.

Run with  java myastar saved1.run nocost
   will now load map and positions, but NOT the cost vector from a file. 
   This way you can see how different terrain costs will affect the same map.

Run with  java myastar download "your name, 702111111" will download
   a map configuration from the professor's server, and upload the
   coord path you create back to the server. Place in one string
   argument both your name and Hofstra ID. The server will reply with
   a message indicating if your path is correct or has some problem
   that needs to be fixed.  If download fails (because the server is
   not currently running or is too busy), it will revert to generating
   a new map.  map config will be saved in myastar.run regardless of
   it was downloaded or generated. Rename the file so you can test it locally.
   There are 7 different map/cost configurations on the server.

   Please note that the server is only reachable within Hofstra because of
   Hofstra's firewalls.  The server may be down during some class times.
   If you do not give accurate information (name, ID number) to the server,
   you may be blocked from further connections.

--------------------------------------------

Graphical Adjustments.

Much of graphics code in pathfinder.java is rather fragile and will break
easily if you change it.  You should concentrate on Algorithm A*.  There
are a few adjustments you can make, such as the size of the graphical
hexagons (the default is 24), which will also affect the size of the
window.  There are some static variables that can be change by overriding
the `void customize()` function - follow the instructions (in comments) in
the myastar.java skeleton.

Furthermore, the main of myastar.java can be given 2 integer arguments
indicating the number of rows and columns of the matrix to generate:

  java myastar 20 30  -- will create a 20x30 hexagonal grid.

If you wish to make further changes, such as the graphical gifs, you
will have to change pathfinder.java.  You may not change any other
posted file besides pathfinder.java, and you take full responsibility
for changing it

*** Do not ask the professor for help if something goes wrong because
    you changed pathfinder.java.  Revert to the original.

--------------------------------------------

CHALLENGE:  

  Build a road.  You can use the unused terrain type (doesn't have to be
desert).  The cost of traveling on the road needs to be significantly less
than all other types of terrain, so your figure should find the road and
use it frequently.  It may be hard to generate the road randomly so just
hand code it onto the existing map (just need a straight line).  Find 
appropriate images to represent the road and the human on the road.  You 
can change pathfinder.java only if you do this part, and only change the
images that are loaded.  You shouldn't have to change astar_base: just build
the road on top of the existing map in your subclass.

MEGACHALLENGE:

  Build docks next to water.  Travel on water should only be possible 
between docks.

GIGACHALLEGE:

  Currently there is only one "player" that's trying to find the
  target.  You can have two (or more) players.  Each player will try
  to reach the same target from a different starting point and with a
  different cost vector.  This option will require major and careful
  changes to pathfinder.java.  Try to make the modifications to the program
  inside subclasses as much as reasonable.

The optional challenges may not work with the pathfinder download feature,
so don't attempt them before you have finished checking your basic program.

