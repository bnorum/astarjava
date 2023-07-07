import java.util.Optional;
public class myastar extends astar_base
{
    public myastar(int r, int c)
    { super(r,c); }

    @Override
    public void customize() {
       ////// Things you can do here...
	//setRandFactor(0.13); // increase amount of water/fire
	//setcosts(2,0,1,10); // cost of land, desert, fire, water
	pathfinder.gap = 15; // change size of graphical hexgagons
        //pathfinder.yoff = 20; // graphical top margin adjustment
        pathfinder.delaytime = 300; //change animation speed
    }
    public static void main(String[] av) {
	pathfinder.main(av);
    }//main

    // must @Override function:
    @Override
    public Optional<coord> search(int sy, int sx, int ty, int tx) {
	
	System.out.println("goal: y: "+ ty + " x: " + tx);
	//interior is PATH[][].interior
	
	//   W NW NE E SE SW
	// { 0,-1,-1,0, 1, 1};  //Y
	//  //X EVEN
	// //X ODD
        int[] DY = { 0,-1,-1,0, 1, 1};
	int[][] DX = {{1,0, -1,-1, -1,0},
		      {1, 1, 0,-1, 0, 1}};
	
	// add with frontier.add((coord)current)	

	coord current;
	PriorityHeap<coord> frontier = new PriorityHeap<coord>(128,false);
	coord start = new coord(sy, sx);
	PATH[sy][sx] = start;

	frontier.add(start);
	
	while (frontier.size() > 0) { //while frontier has items
	    
	    coord[] CC = new coord[1];
	    frontier.poll().ifPresent(s -> CC[0] = s);
	    current = CC[0]; // java is a bad language 

	    
	    current.interior = true; //move current to interior
	    if (current.x == tx && current.y == ty) {
		System.out.println("You are here: y: " + current.y + " x: " + current.x);
		return Optional.of(current);
	    }//stop loop if current coords = goal coords
	    
	    //System.out.println(frontier.size());
	    
	    for (int i = 0; i <= 5; i++) {
		int ny = current.y+DY[i];
		int nx = current.x+DX[current.y%2][i];
		coord neighbor = makeneighbor(current, ny, nx, ty, tx);

		if (neighbor == null) {continue;} //do nothing if oob
		//continue goes to next iteration of for loop

		if(costof[M[ny][nx]] < 0) {continue;}//impassable
		
		if (PATH[ny][nx]==null) { //not on frontier
		    frontier.add(neighbor);
		    PATH[ny][nx] = neighbor;
		}//if null    

		if (PATH[ny][nx].interior==true) {}//do nothing
		
		else { //on frontier
		    
		    
		    if ((PATH[ny][nx].compareTo(neighbor)) > 0) { 
			PATH[ny][nx].copy_from(neighbor);
			PATH[ny][nx].set_parent(current);
			frontier.reposition(PATH[ny][nx]);
		    }//if neighbor
		}//else
	    }//for i
	}//while
	System.out.println("The loop has ended, and so it should return optional.empty() because we didn't find a solution.");
	return Optional.empty();
    }//search
}//myastar class
