import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

public class Environment extends SimState
{
	private static final long serialVersionUID = 1;

	public int gridHeight = 60;
	public int gridWidth = 60;

    public DoubleGrid2D perceptionGrid = new DoubleGrid2D(gridWidth, gridHeight, 0);
	public SparseGrid2D grid = new SparseGrid2D(gridWidth, gridHeight);
	
	private int humanCount;
	private int zombieCount;
	private int bonusPackCount;

	Human[] humans;
	Zombie[] zombies;
	BonusPack[] bonusPacks;

	public static void main(String[] args)
	{
		doLoop(Environment.class, args);
		System.exit(0);
	}
	
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
		
		setHumanCount(20);
		setZombieCount(25);
		setBonusPackCount(5);

		humans = new Human[humanCount];
		zombies = new Zombie[zombieCount];
		bonusPacks = new BonusPack[bonusPackCount];
		        
		// add randomly humans 
		for(int i = 0; i < humanCount; i++)
        {
	        humans[i] = new Human();

	        Int2D location = getEmpty2DLocation();
			
	        addElement(humans[i], location.x, location.y);
        }
		
		// add randomly zombies 
		for(int i = 0; i < zombieCount; i++)
        {
	        zombies[i] = new Zombie();
	        
	        Int2D location = getEmpty2DLocation();
			
	        addElement(zombies[i], location.x, location.y);
        }
		
		// add randomly bonus packs
		for(int i = 0; i < bonusPackCount; i++)
        {
	        bonusPacks[i] = new BonusPack();
	        
	        Int2D location = getEmpty2DLocation();
	        
			addElement(bonusPacks[i], location.x, location.y);
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
	
	/**
	 * There is no other object at the returned location
	 */
	private Int2D getEmpty2DLocation()
	{
		Int2D location = new Int2D(random.nextInt(gridWidth), random.nextInt(gridHeight));
		Bag bag = null;
		while ((bag = grid.getObjectsAtLocation(location.x, location.y)) != null)
		{
			location = new Int2D(random.nextInt(gridWidth), random.nextInt(gridWidth));
		}
		return location;
	}
	
	public boolean addElement(Element _e, int _x, int _y)
    {							
		_e.x = _x;
		_e.y = _y;									
		Stoppable stoppable  = schedule.scheduleRepeating(_e);
		_e.stoppable = stoppable;
		return grid.setObjectLocation(_e, _x, _y);
    }
}
