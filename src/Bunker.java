import java.util.ArrayList;

import javax.swing.ImageIcon;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.util.Bag;
import sim.util.IntBag;


public class Bunker extends Element
{
	private int life = Constants.BUNKER_LIFE_MAX_LEVEL_2;
	private int level = 1;
	private ArrayList<Human> humans = new ArrayList<Human>();
	private Bag neighbours = new Bag();
    private IntBag neighX = new IntBag();
    private IntBag neighY = new IntBag();
	private IntBag neighboursX;
	private IntBag neighboursY;
	private ArrayList<Bag> neighboursArray;
	private int perception = Constants.BUNKER_PERCEPTION_MAX;
	
	private boolean vaccinAvailable = false;
	private int cptStepVaccin = 0;
	private boolean recoltZombie = false;
	private Dog myDog = null;
		
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
			
			if(myDog != null)
			{
				this.environment.grid.remove(myDog);
				myDog.stoppable.stop();
			}
			
			this.environment.grid.remove(this);
			this.stoppable.stop();
		}
		else
		{
			// Check level for vaccin
			if(this.level >= 3 && !vaccinAvailable)
			{
				cptStepVaccin ++;
			}
			if(this.cptStepVaccin == Constants.BUNKER_STEP_VACCIN_MAX)
			{
				vaccinAvailable = true;
				cptStepVaccin = 0;
			}
			
			// Check if a dog is recolting a zombie
			if(myDog != null && myDog.isZombieTraped())
			{
				vaccinAvailable = true;
				this.environment.grid.remove(myDog);
				myDog.stoppable.stop();
				myDog = null;
				recoltZombie = false;
			}
			
			// Check Humans inside
			for(int i = 0; i < humans.size(); i++)
			{
				if(humans.get(i).x != this.x || humans.get(i).y != this.y)
				{
					humans.get(i).setMyBunker(null);
					humans.remove(i);
					if(humans.size() > 1)
					{
						downgrade(environment);
					}
					else if(humans.size() == 1)
					{
						humans.get(0).setMyBunker(null);
						humans.get(0).show();
						if(myDog != null)
						{
							this.environment.grid.remove(myDog);
							myDog.stoppable.stop();
						}
						this.environment.grid.remove(this);
						this.stoppable.stop();
					}
					else
					{
						if(myDog != null)
						{
							this.environment.grid.remove(myDog);
							myDog.stoppable.stop();
						}
						this.environment.grid.remove(this);
						this.stoppable.stop();
					}
				}
			}
			
			// Perception
			environment.grid.getHexagonalNeighbors(x, y, this.perception, SparseGrid2D.TOROIDAL, neighbours, neighboursX, neighboursY);
			neighboursArray = this.perception();
			
			// Check if there is a trap
			boolean trapFound = false;
			boolean zombieFound = false;
			Trap trap = null;
			Zombie zombie = null;
			
			for(int i = 0; i < neighboursArray.size(); i++)
			{
				for(int j = 0; j < neighboursArray.get(i).size(); j++)
				{
					if(neighboursArray.get(i).get(j) instanceof Trap)
					{
						trapFound = true;
						trap = (Trap) neighboursArray.get(i).get(j);
					}
					else if(neighboursArray.get(i).get(j) instanceof Zombie)
					{
						zombieFound = true;
						zombie = (Zombie) neighboursArray.get(i).get(j);
					}
				}
				
				if(trapFound && zombieFound && !recoltZombie)
				{
					if(trap.x == zombie.x && trap.y == zombie.y)
					{
						recoltZombie = true;
						myDog = new Dog();
						myDog.xTarget = zombie.x;
						myDog.yTarget = zombie.y;
						environment.addElement(myDog, x, y);
						
						trap.dogSend = myDog;
//						System.out.println("Zombie sur un piege");
						break;
					}
					else
					{
						trapFound = false;
						zombieFound = false;
						trap = null;
						zombie = null;
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
		ImagePortrayal2D imagePortRayal = new ImagePortrayal2D(bunkerIcon);
		imagePortRayal.scale = 2.0;
		env.environmentUI.environmentPortrayal.setPortrayalForObject(this, imagePortRayal);
		env.environmentUI.display.repaint();
	}
	
	/**
	 * Public method to upgrade the bunker
	 * @param candidates Humans candidates to enter in the bunker
	 */
	public boolean upgrade(Human candidate, Environment env)
	{
		if(!isFull())
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
			
			ImagePortrayal2D imagePortRayal = new ImagePortrayal2D(bunkerIcon);
			imagePortRayal.scale = 2.0;
			env.environmentUI.environmentPortrayal.setPortrayalForObject(this, imagePortRayal);
			env.environmentUI.display.repaint();
			return true;
		}
		return false;
	}
	
	private ArrayList<Bag> perception()
	{
		return this.perception(this.neighbours, this.neighboursX, this.neighboursY);
	}
	
	private ArrayList<Bag> perception(Bag neighbours, IntBag posX, IntBag posY) 
	{
		ArrayList<Bag> result = new ArrayList<Bag>();
//		this.perception = (int)(Math.random() * Constants.HUMAN_PERCEPTION_MAX) + 1;
		
		for(int i = 0; i <= (this.perception + 1); i++)
		{
			result.add(i, new Bag());
		}
		
		for(int i = 1; i < neighbours.size(); i++)
		{
			Object object = neighbours.get(i);
			
			if(object instanceof Element)
			{
				int xB = ((Element)neighbours.get(i)).x;
				int yB = ((Element)neighbours.get(i)).y;
				double distance = Math.sqrt(Math.pow(xB - this.x, 2) + Math.pow(yB - this.y, 2));
				if((int)distance > (this.perception + 1))
				{
					distance = Math.abs(this.environment.gridWidth - (int)distance);
				}
				if((int)distance < result.size())
					result.get((int)distance).add(object);
			}
		}
		
		return result;
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
		return (level >= Constants.BUNKER_LEVEL_MAX) ? true : false;
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

	public boolean isVaccinAvailable()
	{
		return vaccinAvailable;
	}
	
	public String toString()
	{
		return new String("Bunker");
	}
}
