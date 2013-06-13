import java.util.ArrayList;

import javax.swing.ImageIcon;

import sim.engine.SimState;
import sim.portrayal.simple.ImagePortrayal2D;


public class Bunker extends Element
{
	private int life = Constants.BUNKER_LIFE_MAX_LEVEL_2;
	private int level = 1;
	private ArrayList<Human> humans = new ArrayList<Human>();
		
	@Override
	public void step(SimState state)
	{
		this.environment = (Environment)state;
		
		if(life <= 0)
		{
			for(int i = 0; i < humans.size(); i++)
			{
				humans.get(i).show();
				humans.get(i).goAway = true;
			}
			
			this.environment.grid.remove(this);
			this.stoppable.stop();
		}
		else
		{
			for(int i = 0; i < humans.size(); i++)
			{
				if(humans.get(i).x != this.x || humans.get(i).y != this.y)
				{
					humans.get(i).setMyBunker(null);
					humans.remove(i);
					if(humans.size() > 0)
					{
						downgrade(environment);
					}
					else
					{
						this.environment.grid.remove(this);
						this.stoppable.stop();
					}
				}
			}
		}
	}
	
	/**
	 * Private methode to downgrade a bunker level when a human leaves it
	 * @param env
	 */
	private void downgrade(Environment env)
	{
		ImageIcon bunkerIcon = null;
		this.level = humans.size();
		
		switch(level)
		{
		case 1:
			bunkerIcon = new ImageIcon("ressources/bunker_2.png");
			break;
		case 2:
			bunkerIcon = new ImageIcon("ressources/bunker_2.png");
			break;
		case 3:
			bunkerIcon = new ImageIcon("ressources/bunker_3.png");
			break;
		case 4:
			bunkerIcon = new ImageIcon("ressources/bunker_4.png");
			break;
		case 5:
			bunkerIcon = new ImageIcon("ressources/bunker_5.png");
			break;
		default:
			break;
		}
		env.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(bunkerIcon));
		env.environmentUI.display.repaint();
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
		env.environmentUI.display.repaint();
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

	public int getLife()
	{
		return life;
	}
	
	public int getNbHumans()
	{
		return this.humans.size();
	}
}
