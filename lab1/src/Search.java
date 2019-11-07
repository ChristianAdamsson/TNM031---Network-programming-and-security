package lab1;


import java.util.*;

public class Search {
	Table initTable;
	Table completedTable;
	Table currTable;
	boolean usemanhattan = true;
	private HashSet<Table> visited = new HashSet<Table>();
	
	public PriorityQueue<Table> queue = new PriorityQueue<Table>(100, new Comparator<Table>()
	{
		@Override
		public int compare(Table b1, Table b2)
		{	
			return b1.getF() - b2.getF();
		}

	});
	



public Search() {
	List<Integer> startTable = new ArrayList<Integer>();
	
	/*startTable.add((1));
	startTable.add((0)); // value 0 is the empty space
	startTable.add((2));
	startTable.add((4));
	startTable.add((5));
	startTable.add((3));
	startTable.add((7));
	startTable.add((8));
	startTable.add((6));
*/
	startTable.add((8));
	startTable.add((6)); 
	startTable.add((7));
	startTable.add((2));
	startTable.add((5));
	startTable.add((4));
	startTable.add((3));
	startTable.add((0));
	startTable.add((1));
	

	/*startTable.add(8);
	startTable.add(1); 
	startTable.add(2);
	startTable.add(0);
	startTable.add(4);
	startTable.add(3);
	startTable.add(7);
	startTable.add(6);
	startTable.add(5);
	*/


	List<Integer> EndTable = new ArrayList<Integer>();
	EndTable.add((1));
	EndTable.add((2));
	EndTable.add((3));
	EndTable.add((4));
	EndTable.add((5));
	EndTable.add((6));
	EndTable.add((7));
	EndTable.add((8));
	EndTable.add((0)); // value 0 is the empty spac
	
	initTable = new Table(startTable);
	completedTable = new Table(EndTable);
	
	initTable.g = 0;
	if (usemanhattan)
	{
		int distance = 0;
		for (int j = 0; j < initTable.objects.size(); j++)
		{
			distance+= manhattanDistance(initTable.getIndex(initTable.getvalue(j)), 
				completedTable.getIndex(initTable.getvalue(j)));
		}
		initTable.h = distance;
		
	}
	else
	{
		initTable.h = Numofwrongpos(initTable, completedTable); 
	}
	
	
	
	run();
	
}

private int Numofwrongpos(Table a, Table b) {
	int badpos = 0;
		for( int i = 0; i < a.objects.size(); ++i) {
			if(a.objects.get(i) != b.objects.get(i)) {
				badpos++;
			}
		}

		return badpos;
	
}
public Table createTable(int neighbour) {
	List<Integer> tempobj = new ArrayList<Integer>();
	for(int i = 0; i < currTable.objects.size(); i++) {
		tempobj.add(currTable.objects.get(i));
	}
	Collections.swap(tempobj, currTable.getIndex(0), currTable.getIndex(neighbour));
	
	Table b = new Table(tempobj);
	return b;
}
private void addToQueue(Table nextState) 
{
	if (nextState != null && !visited.contains(nextState))
	{
		this.queue.add(nextState);
	}
			
}

private void run() {
	queue.add(initTable);
	long startTime = System.currentTimeMillis();
	//currTable = initTable;
	while(!queue.isEmpty())
	{
		currTable = queue.poll();
		//currTable = queue.poll();
		
		visited.add(currTable);
		if (currTable.h == 0) // reached goal
		{
			System.out.println("Goal reached!");
			System.out.println("Number of moves: " + currTable.g);
			System.out.println("Duration (s): " + (System.currentTimeMillis() - startTime)/1000.0);
			currTable.print();
			return;
		}
		List<Integer> neighbours = new ArrayList<Integer>();
		neighbours = currTable.getneighbours(currTable.getvalue(0)); // get neighbours of 0
					
		for (int i = 0; i < neighbours.size(); i++)
		{
			Table b = createTable(neighbours.get(i));
		if (usemanhattan)
			{
				int distance = 0;
				for (int j = 0; j < b.objects.size(); j++)
				{
					distance+=manhattanDistance(b.getIndex(b.getvalue(j)), completedTable.getIndex(b.getvalue(j)));
				}
				b.h = distance;
			}
			else
			{
			
			b.h = Numofwrongpos(b, completedTable);
			}
			b.g = currTable.g + 1;	
			addToQueue(b);
			
			
		}
	//}
	System.out.println(visited.size());
	//currTable.print();
	}

}
static int manhattanDistance(int a, int b) 
{
    return Math.abs(a / 3 - b / 3) + Math.abs(a % 3 - b % 3);
}	
	

public static void main(String[] args)
{
	Search search = new Search();
}
}
