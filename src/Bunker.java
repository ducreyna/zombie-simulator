import java.util.ArrayList;

import javax.swing.ImageIcon;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.simple.ImagePortrayal2D;


public class Bunker extends Element implements Steppable
{
	private int life = Constants.BUNKER_LIFE_MAX_LEVEL_2;
	private int level = 1;
	private ArrayList<Human> humans = new ArrayList<Human>();
	
	public Environment environment;
	public Stoppable stoppable;
	
	@Override
	public void step(SimState state)
	{
		this.environment = (Environment) state;
		
		if(life <= 0)
		{
			this.environment.grid.remove(this);
			stoppable.stop();
		}
	}
	
	/**
	 * Public method to upgrade the bunker
	 * @param candidates Humans candidates to enter in the bunker
	 */
	public void upgrade(Human candidate, Environment env)
	{
		ImageIcon bunkerIcon = null;
		if(this.humans.size() < Constants.BUNKER_LEVEL_MAX)
		{
			this.humans.add(candidate);
		}
		
		this.level = humans.size();
		switch(level)
		{
		case 1:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_2;
			bunkerIcon = new ImageIcon("ressources/bunker_2.png");
			break;
		case 2:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_2;
			bunkerIcon = new ImageIcon("ressources/bunker_2.png");
			break;
		case 3:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_3;
			bunkerIcon = new ImageIcon("ressources/bunker_3.png");
			break;
		case 4:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_4;
			bunkerIcon = new ImageIcon("ressources/bunker_4.png");
			break;
		case 5:
			life = Constants.BUNKER_LIFE_MAX_LEVEL_5;
			bunkerIcon = new ImageIcon("ressources/bunker_5.png");
			break;
		default:
			break;
		}
		
		env.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(bunkerIcon));
//		env.environmentUI.display.reset();
		env.environmentUI.display.repaint();
		//		env.environmentUI.display.attach(env.environmentUI.environmentPortrayal, "Environment");
	}
	
	public void setHumans(ArrayList<Human> humans)
	{
		this.humans = humans;
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
