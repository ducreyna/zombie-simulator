import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

public class Environment extends SimState
{
	private static final long serialVersionUID = 1;

	public int gridHeight = 100;
	public int gridWidth = 100;

    public DoubleGrid2D perceptionGrid = new DoubleGrid2D(gridWidth, gridHeight, 0);
	public SparseGrid2D grid = new SparseGrid2D(gridWidth, gridHeight);
	
	public int humanCount = 100;
	public int zombieCount = 25;
	public int bonusPackCount = 5;

	Human[] humans;
	Zombie[] zombies;
	BonusPack[] bonusPacks;

	public Environment(long seed)
	{
		super(seed);
		humans = new Human[humanCount];
		zombies = new Zombie[zombieCount];
		bonusPacks = new BonusPack[bonusPackCount];
	}

	// Resets and starts a simulation
	public void start()
	{
		super.start();

		// it's faster to make a new sparse field than to clear it
		perceptionGrid = new DoubleGrid2D(gridWidth, gridHeight,0);
		grid = new SparseGrid2D(gridWidth, gridHeight);
		
		setHumanCount(5);
		setZombieCount(25);
		setBonusPackCount(5);

		humans = new Human[humanCount];
		zombies = new Zombie[zombieCount];
		bonusPacks = new BonusPack[bonusPackCount];
		
		// TODO : add randomly humans, zombies and bonusPacks
        
		for(int i = 0; i < humanCount; i++)
        {
	        humans[i] = new Human();
			Int2D location = new Int2D(random.nextInt(gridWidth), random.nextInt(gridHeight));
			Bag bag = null;
			while ((bag = grid.getObjectsAtLocation(location.x, location.y)) != null)
			{
				location = new Int2D(random.nextInt(gridWidth), random.nextInt(gridWidth));
			}
			
			humans[i].x = location.x;
			humans[i].y = location.y;
			
	        grid.setObjectLocation(humans[i], location.x, location.y);
	        Stoppable stoppable  = schedule.scheduleRepeating(humans[i]);
	        humans[i].stoppable = stoppable;
        }
	}
	public int getGridHeight()
	{
		return gridHeight;
	}

	public int getGridWidth()
	{
		return gridWidth;
	}

	public int getHumanCount()
	{
		return humanCount;
	}

	public int getZombiCount()
	{
		return zombieCount;
	}

	public int getBonusPackCount()
	{
		return bonusPackCount;
	}

	public void setHumanCount(int _val)
	{
		if (_val >= 0)
			humanCount = _val;
	}

	public void setZombieCount(int _val)
	{
		if (_val >= 0)
			zombieCount = _val;
	}

	public void setBonusPackCount(int _val)
	{
		if (_val >= 0)
			bonusPackCount = _val;
	}
	
	public static void main(String[] args)
	{
		doLoop(Environment.class, args);
		System.exit(0);
	}
}
