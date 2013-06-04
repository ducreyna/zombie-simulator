import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;

public class Environment extends SimState
{
	private static final long serialVersionUID = 1;

	public int gridHeight = 100;
	public int gridWidth = 100;

	public int humanCount = 100;
	public int zombieCount = 25;
	public int bonusPackCount = 5;

	Human[] humans;
	Zombie[] zombies;
	BonusPack[] bonusPacks;

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

	public SparseGrid2D grid = new SparseGrid2D(gridWidth, gridHeight);

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
		grid = new SparseGrid2D(gridWidth, gridHeight);
		
		setHumanCount(100);
		setZombieCount(25);
		setBonusPackCount(5);

		humans = new Human[humanCount];
		zombies = new Zombie[zombieCount];
		bonusPacks = new BonusPack[bonusPackCount];
		
		// TODO : add randomly humans, zombies and bonusPacks
	}
	
	public static void main(String[] args)
	{
		doLoop(Environment.class, args);
		System.exit(0);
	}
}
