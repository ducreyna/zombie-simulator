import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;

public class Environment extends SimState
{
	private static final long serialVersionUID = 1;


	public int gridHeight = Constants.GRID_HEIGHT;
	public int gridWidth = Constants.GRID_WIDTH;

	public EnvironmentUI environmentUI;

    public IntGrid2D perceptionGrid = new IntGrid2D(gridWidth, gridHeight, 0);
	public SparseGrid2D grid = new SparseGrid2D(gridWidth, gridHeight);

	private int humanCount = Constants.INIT_HUMAN_COUNT;
	private int zombieCount = Constants.INIT_ZOMBIE_COUNT;
	private int bonusPackCount = Constants.INIT_BONUSPACK_COUNT;

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
		perceptionGrid = new IntGrid2D(gridWidth, gridHeight, 0);
		grid = new SparseGrid2D(gridWidth, gridHeight);

		setHumanCount(Constants.INIT_HUMAN_COUNT);
		setZombieCount(Constants.INIT_ZOMBIE_COUNT);
		setBonusPackCount(Constants.INIT_BONUSPACK_COUNT);

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

	public void setEnvironmentUI(EnvironmentUI environmentUI)
	{
		this.environmentUI = environmentUI;
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

	public boolean addBonusPack()
	{
		Int2D location = getEmpty2DLocation();
		return addElement(new BonusPack(), location.x, location.y);
	}

	public boolean addZombie(int _x, int _y)
	{
		return addElement(new Zombie(), _x, _y);
	}

	public boolean addElement(Element _e, int _x, int _y)
    {
		_e.x = _x;
		_e.y = _y;
		Stoppable stoppable  = schedule.scheduleRepeating(_e);
		_e.stoppable = stoppable;
		//System.out.println("add element at ("+_x+","+_y+")");
		return grid.setObjectLocation(_e, _x, _y);
    }

	/*
	 * @param _draw true to delete perception color on the map
	 */
    public void drawPerception(int _x, int _y, int _dist, boolean _draw)
    {
    	if (_x >= 0 && _y >= 0 && _dist > 0)
    	{
		    IntBag xPosBag = new IntBag();
		    IntBag yPosBag = new IntBag();
		    grid.getHexagonalLocations(_x, _y, _dist, SparseGrid2D.UNBOUNDED, true, xPosBag, yPosBag);

			int x, y;
		    for (int i = 0; i < xPosBag.size(); i++)
		    {
				x = xPosBag.get(i);
				y = yPosBag.get(i);
				if (_draw)
				{
					perceptionGrid.field[grid.stx(x)][grid.sty(y)] = 3;
				}
				else
				{
					perceptionGrid.field[grid.stx(x)][grid.sty(y)] = 0;
					environmentUI.display.repaint();
				}
		    }
    	}
    }
}
