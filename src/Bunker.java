import java.util.ArrayList;

import sim.engine.SimState;


public class Bunker extends Element
{
	private int life = Constants.BUNKER_LIFE_MAX_LEVEL_1;
	private int level = 1;
	private ArrayList<Human> humans = new ArrayList<Human>();
		
	@Override
	public void step(SimState state)
	{
		this.environment = (Environment)state;
		
		if(life <= 0)
		{
			this.environment.grid.remove(this);
			this.stoppable.stop();
		}
	}
	
	/**
	 * Public method to upgrade the bunker
	 * @param candidates Humans candidates to enter in the bunker
	 */
	public void upgrade(Human candidate)
	{
		if(this.humans.size() < Constants.BUNKER_LEVEL_MAX)
		{
			this.humans.add(candidate);
		}
		
		this.level = humans.size();
		switch(level)
		{
		case 2:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_2;
			break;
		case 3:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_3;
			break;
		case 4:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_4;
			break;
		default:
			break;
		}
		
		// TODO Modifier l'image dans l'environnement
	}
	
	public boolean isInBunker(Human human)
	{
		return this.humans.contains(human);
	}
	
	public boolean isFull()
	{
		return (level == Constants.BUNKER_LEVEL_MAX) ? true : false;
	}
	
	/**
	 * Public method call to inflict damage to the bunker
	 * @param damage
	 */
	public void attack(int damage)
	{
		life -= damage;
	}

	public int getLevel()
	{
		return level;
	}
}
