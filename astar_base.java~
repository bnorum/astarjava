import java.util.Optional;

public class astar_base
{
    public static final int OPEN = 0;   // don't confuse with cost
    public static final int DESERT = 1;  
    public static final int FIRE = 2;
    public static final int WATER = 3;

    public static final int W = Hexagon.West;
    public static final int E = Hexagon.East;
    public static final int NW = Hexagon.NorthWest;
    public static final int SW = Hexagon.SouthWest;
    public static final int NE = Hexagon.NorthEast;
    public static final int SE = Hexagon.SouthEast;
    public static int[] DY = Hexagon.DY;
    public static int[][] DX = Hexagon.DX;
    protected static double RandFactor = 0.10; // must be between 0 and .166
                                            // affects amount of water/fire
    public static void setRandFactor(double x)
    {
	if (x>0 && x<0.166) RandFactor = x;
    }

    static boolean genmap = true;
    protected int[] costof= {1,0,8000,8};  // cost vector of each terrain
    
    public void setcosts(int l, int d, int f, int w)
    {
	//	if (l<0 || d<0 || f<0 || w<0) return;
	int[] vec = {l,d,f,w};
	costof = vec;
    }

    int M[][];  // the map itself, value=terrain type
    int ROWS, COLS; // size of map in array coords
    //protected pathfinder render;  // set externally - point to graphics program
    coord[][] PATH;
    //public void setrender(pathfinder p) { render=p; }
    //public void drawchar(int y, int x, char t) {render.trace(y,x,t);}

    public void customize() {} // called at start of constructor (override it)

    // constructor creates M, generates a random map
    public astar_base(int r0, int c0)  // typically 32x44
    {
	customize();
	if (!genmap) return;	 // set map externally
	M = new int[r0][c0];
        PATH = new coord[r0][c0]; // array of coord objects (all nulls)
	ROWS=r0;  COLS=c0;
	// generate random map  (initially all OPEN)
	int GENS = 10;  // number of generations
	double p, r;  // for random probability calculation
	int generation;  int i, j;
	for(generation=0;generation<GENS;generation++)
	    {
	     for(i=0;i<ROWS;i++) 
		 for(j=0;j<COLS;j++)
	     {
	       p = 0.004; // base probability factor
	       // calculate probability of water based on surrounding cells
	       for(int k=0;k<6;k++)
		   {
		       int ni = i + DY[k], nj = j + DX[i%2][k];
		       if (ni>=0 && ni<ROWS && nj>=0 && nj<COLS && M[ni][nj]==WATER) p+= RandFactor;
		   }
	       r = Math.random();
	       if (r<=p) M[i][j] = WATER;
	     } // for each cell i, j
	    } // for each generation

	for(generation=0;generation<GENS-1;generation++)
	    {
	     for(i=0;i<ROWS;i++) 
		 for(j=0;j<COLS;j++)
	     {
	       p = 0.004; // base probability factor
	       // calculate probability of fire based on surrounding cells
	       for(int k=0;k<6;k++)
		   {
		       int ni = i + DY[k], nj = j + DX[i%2][k];
		       if (ni>=0 && ni<ROWS && nj>=0 && nj<COLS && M[ni][nj]==FIRE) p+= RandFactor;
		   }
	       r = Math.random();
	       if (r<=p && M[i][j]==0) M[i][j] = FIRE;
	     } // for each cell i, j
	    } // for each generation

    } //constructor

    // Determines distance properly in hex coordinates:
    //   distance is max of |dx|, |dy|, and |dx-dy|.
    // Note: this does not give exact distance but does not overestimate
    // it, so it is admissible for algorithm A*
    public static int hexdist(int y1, int x1, int y2, int x2)
    {
        int dx = x1-x2, dy = y1-y2;
	int dd = Math.abs(dx - dy);
	dy = Math.abs(dy);
	int max = Math.abs(dx);
	if (dy>max) max = dy;
	if (dd>max) max = dd;
	return max;
    }

    // you need to override the search method in astar_base.java

    // USE THIS FUNCTION IF YOU LIKE:
    // makeneighbor attempts to create a coord object for coordinates y,x
    // adjacent to coord p (which will become its parent in the search tree)
    // if y or x is out of bounds, it returns null.  otherwise it creates
    // a coord object with parent p and where dist and cost are calculated
    // based on distance back to source, and estimated distance to ty,tx
    public coord makeneighbor(coord p, int y, int x, int ty, int tx)
    {
	if (y<0 || y>=ROWS || x<0 || x>=COLS) return null;
	int coordcost = costof[M[y][x]];
	int estimate = hexdist(y,x,ty,tx);
	coord node = new coord(y,x);
	node.knowncost = p.knowncost + coordcost;
	node.estcost = node.knowncost + estimate;
        node.set_parent(p);
	return node;
    }// makeneighbor


    // incorrect version: NEED TO OVERRIDE IN myastar.java:
    public java.util.Optional<coord> search(int sy, int sx, int ty, int tx)
    {
	return java.util.Optional.empty(); // means no path found
	/*
	// following code tries to find target randomly, not
	// likely to work (won't return until solution found).
        coord current = new coord(sy,sx);
        PATH[sy][sx] = current;
	while (current.y!=ty || current.x!=tx)
	    {
		//pick random direction
		coord next = null;
                while (next==null)
		{
		  int d = (int)(Math.random()*6);
  		  int cy = current.y, cx = current.x;
		  int ny = cy + DY[d];
		  int nx = cx + DX[cy%2][d];
		  if (nx>=0 && nx<COLS && ny>=0 && ny<ROWS)
		    {
			next = new coord(ny,nx);
                        PATH[ny][nx] = next;
                        next.set_parent(current);
			current = next; 
		    }
	       } // while next==null
	    }// main while
	return Optional.of(current);
	*/
    }// incorrect search

}//astar_base
