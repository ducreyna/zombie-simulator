import java.util.ArrayList;
import java.util.Iterator;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.IntBag;


public class Human extends Element implements Steppable
{
//	public int x, y;
	public Stoppable stoppable;
	private Environment environment;
	
	private int speed = Constants.HUMAN_SPEED_MAX;
	private int perception = Constants.HUMAN_PERCEPTION_MAX;
	private int life = Constants.HUMAN_LIFE_MAX;
	private int weaponLevel = 1;
	private int munitions = 10;
	private int maxMunitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_1;
	private int XP = 0;
	
	private enum Direction
	{
		RIGHT,
		LEFT,
		TOP,
		BOTTOM,
		TOPLEFT,
		TOPRIGHT,
		BOTTOMLEFT,
		BOTTOMRIGHT;
	}
	
	private Direction direction = Direction.RIGHT;
	private int numberOfRandom = 0;
	private Bag neighbours = new Bag();
    private IntBag neighX = new IntBag();
    private IntBag neighY = new IntBag();
	private IntBag neighboursX;
	private IntBag neighboursY;
	private ArrayList<Bag> neighboursArray;
	
	@Override
	public void step(SimState state) 
	{
		this.environment = (Environment)state;
		
		if(this.life != 0)
		{
			// Weapon Upgrade
			if(XP >= Constants.HUMAN_XP_MAX)
			{
				if(weaponLevel != Constants.HUMAN_WEAPON_LEVEL_MAX)
				{
					weaponLevel ++;
					upgradeWeapon();
				}
				XP = 0;
			}
			
			// Perception
			environment.grid.getHexagonalNeighbors(x, y, this.perception, SparseGrid2D.TOROIDAL, neighbours, neighboursX, neighboursY);
			neighboursArray = this.perception();
			
			if(this.munitions <= 5 || this.life <= 5)
			{
				// We need to find munitions or points of life
				BonusPack bonusPack = null;
				boolean bonusPackFound = false;
				boolean zombieFound = false;
				
				for(int i = 0; i < neighboursArray.size(); i++)
				{
					for(int j = 0; j < neighboursArray.get(i).size(); j++)
					{
						if(neighboursArray.get(i).get(j) instanceof Zombie)
						{
							if((i == 0 || i == 1) && this.munitions > 0)
							{
								// Zombie very close
								zombieFound = true;
								shoot((Zombie) neighboursArray.get(i).get(j));
								break;
							}
						}
						else if(neighboursArray.get(i).get(j) instanceof BonusPack)
						{
							bonusPackFound = true;
							bonusPack = (BonusPack) neighboursArray.get(i).get(j);
							if(i == 0 || i == 1)
							{
								// We are enough close to pick up the bonus pack
								// Weapon
								if((bonusPack.getWeaponUpgrade() + this.weaponLevel) > Constants.HUMAN_WEAPON_LEVEL_MAX)
								{
									this.weaponLevel = Constants.HUMAN_WEAPON_LEVEL_MAX;
								}
								else
								{
									this.weaponLevel += bonusPack.getWeaponUpgrade();
								}
								upgradeWeapon();
								// Munitions
								if((bonusPack.getMunitions() > this.maxMunitions)
										|| ((bonusPack.getMunitions() + this.munitions) > this.maxMunitions))
								{
									this.munitions = maxMunitions;
								}
								else
								{
									this.munitions += bonusPack.getMunitions();
								}
								// Life
								if((bonusPack.getLife() + this.life) > Constants.HUMAN_LIFE_MAX)
								{
									this.life = Constants.HUMAN_LIFE_MAX;
								}
								else
								{
									this.life += bonusPack.getLife();
								}
								
								// Removing bonus pack
								environment.grid.remove(bonusPack);
								bonusPack.stoppable.stop();
							}
							break;
						}
					}
					if(bonusPackFound || zombieFound)
						break;
				}
				
				if(bonusPack != null)
				{
					// We move to the bonus pack
					move(environment, bonusPack.x, bonusPack.y);
				}
				else
				{
					// We continue the research
					randomMove(environment);
				}
			}
			else
			{
				boolean doRandomMove = true;
				boolean bunkerFound = false;
				boolean zombieFound = false;
				ArrayList<Human> humansGroup = new ArrayList<Human>();
				Bunker bunker = null;
				
				// We have enough munitions and life
				for(int i = 0; i < neighboursArray.size(); i++)
				{
//					System.out.println(neighboursArray.get(i).size());
					for(int j = 0; j < neighboursArray.get(i).size(); j++)
					{
//						System.out.println(neighboursArray.get(i).get(j));
						doRandomMove = false;
						
						if(i == 0)
						{
							// Elements on the same case
							if(neighboursArray.get(i).get(j) instanceof Bunker)
							{
								bunkerFound = true;
								bunker = (Bunker) neighboursArray.get(i).get(j);
							}
							else if(neighboursArray.get(i).get(j) instanceof Human)
							{
								humansGroup.add((Human)neighboursArray.get(i).get(j));
							}
							else if(neighboursArray.get(i).get(j) instanceof Zombie)
							{
								zombieFound = true;
								shoot((Zombie)neighboursArray.get(i).get(j));
								break;
							}
						}
						else
						{
							// Default behaviour
							if(neighboursArray.get(i).get(j) instanceof Zombie)
							{
								shoot((Zombie)neighboursArray.get(i).get(j));
								break;
							}
							else if(neighboursArray.get(i).get(j) instanceof Bunker)
							{
								bunkerFound = true;
								bunker = (Bunker) neighboursArray.get(i).get(j);
							}
							else if(neighboursArray.get(i).get(j) instanceof Human)
							{
								humansGroup.add((Human) neighboursArray.get(i).get(j));
//								move(environment, ((Human)neighboursArray.get(i).get(j)).x, ((Human)neighboursArray.get(i).get(j)).y);
//								break;
							}
						}
					}
					
					if(i == 0)
					{
						// Elements on the same case
						if(!bunkerFound && !zombieFound)
						{
							 if(humansGroup.size() >= 1)
							 {
								// TODO Construire un bunker
							 }
						}
						else if(!bunker.isInBunker(this) && !bunker.isFull())
						{
							// We integrate the bunker
							bunker.upgrade(this);
						}
						bunkerFound = false;
						bunker = null;
						humansGroup.clear();
					}
					else
					{
						if(bunkerFound && bunker.isFull())
						{
							// Check humans in the bunker
							for(int k = 0; k < humansGroup.size(); k++)
							{
								if(!bunker.isInBunker(humansGroup.get(k)))
								{
									move(environment, humansGroup.get(k).x, humansGroup.get(k).y);
									break;
								}
							}
						}
						else if(!humansGroup.isEmpty())
						{
							move(environment, humansGroup.get(0).x, humansGroup.get(0).y);
						}
						bunkerFound = false;
						bunker = null;
						humansGroup.clear();
					}
					
					if(!doRandomMove)
						break;
				}
				if(doRandomMove)
					randomMove(environment);
			}
		}
		else
		{
			// TODO Supprimer humain + ajouter zombie
			environment.grid.remove(this);
			this.stoppable.stop();
		}
	}

	private ArrayList<Bag> perception()
	{
		return this.perception(this.neighbours, this.neighboursX, this.neighboursY);
	}
	
	private ArrayList<Bag> perception(Bag neighbours, IntBag posX, IntBag posY) 
	{
		ArrayList<Bag> result = new ArrayList<Bag>();
//		this.perception = (int)(Math.random() * Constants.HUMAN_PERCEPTION_MAX) + 1;
		
		for(int i = 0; i <= this.perception; i++)
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
				if(distance > (this.perception + 1))
				{
					distance = this.environment.gridWidth - distance;
				}
				result.get((int)distance).add(object);
			}
		}
		
		return result;
	}
	
	/**
	 * Private function to shoot on a zombie
	 */
	private void shoot(Zombie zombie)
	{
		if(this.munitions > 0)
		{
			int randomBody; 
			
			if(weaponLevel == 1)
			{
				randomBody = (int)(Math.random() * (weaponLevel + 8));
			}
			else if(weaponLevel == 2)
			{
				randomBody = (int)(Math.random() * (weaponLevel + 6));
			}
			else if(weaponLevel == 3)
			{
				randomBody = (int)(Math.random() * (weaponLevel + 4));
			}
			else if(weaponLevel == 4)
			{
				randomBody = (int)(Math.random() * (weaponLevel + 2));
			}
			else
			{
				// Weapon Max level
				randomBody = (int)(Math.random() * weaponLevel);
			}
			
			switch(randomBody)
			{
			case 1:
				// Touch a leg
				zombie.bodyShot(Zombie.BODY_PART.LEG);
				XP ++;
				break;
			case 2:
				// Touch an arm
				zombie.bodyShot(Zombie.BODY_PART.ARM);
				XP ++;
				break;
			case 3:
				// Touch a trunk
				zombie.bodyShot(Zombie.BODY_PART.TRUNK);
				XP += 2;
				break;
			case 4:
				// Headshot
				zombie.headShot();
				XP += 5;
				break;
			default:
				// Shot missed
				break;
			}
			munitions --;
		}
	}
	
	private void upgradeWeapon()
	{
		if(weaponLevel == 2)
		{
			maxMunitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_2;
		}
		else if(weaponLevel == 3)
		{
			maxMunitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_3;
		}
		else if(weaponLevel == 4)
		{
			maxMunitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_4;
		}
		else
		{
			maxMunitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_5;
		}
	}
	
	private void move(Environment model, int l, int c)
	{
		this.numberOfRandom = 0;
		double distance = Math.sqrt(Math.pow(l - this.x, 2) + Math.pow(c - this.y, 2));
		
		this.speed = (int)(Math.random() * Constants.HUMAN_SPEED_MAX) + 1;
		
		if(distance > 0 && distance <= this.speed)
		{
			// We move directly to the case (l,c)
			model.grid.setObjectLocation(this, model.grid.stx(l), model.grid.sty(c));
			x = model.grid.stx(l);
			y = model.grid.sty(c);
		}
		else if(distance > 0)
		{
			if(l == this.x) // If we are on the same column
			{
				if(this.y < c) // Move up
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x), model.grid.sty(y + 1));
						y = model.grid.sty(y + 1);
					}
				}
				else if(this.y > c) // Move down
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x), model.grid.sty(y - 1));
						y = model.grid.sty(y - 1);
					}
				}
			}
			else if(c == this.y) // If we are on the same row
			{
				if(this.x < l) // Move right
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y));
						x = model.grid.stx(x + 1);
					}
				}
				else if(this.x > l) // Move left
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x - 1), model.grid.sty(y));
						x = model.grid.stx(x - 1);
					}
				}
			}
			else
			{
				if(this.x < l && this.y < c) // Move down left
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y + 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y + 1);
					}
				}
				else if(this.x < l && this.y > c) // Move up left
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y - 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y - 1);
					}
				}
				else if(this.x > l && this.y < c) // Move down right
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x - 1), model.grid.sty(y + 1));
						x = model.grid.stx(x - 1);
						y = model.grid.sty(y + 1);
					}
				}
				else if(this.x > l && this.y > c) // Move up right
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y + 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y + 1);
					}
				}
			}
		}
			
	}
	
	/**
	 * Private method to make a random move
	 * @param model The simulation model
	 */
	private void randomMove(Environment model) 
	{
		if(this.numberOfRandom  >= 9)
		{
			int lowerA = 0;
			int higherA = 3;

			ArrayList<Direction> listOfDirection = new ArrayList<Direction>();
			listOfDirection.add(Direction.BOTTOM);
			listOfDirection.add(Direction.TOP);
			listOfDirection.add(Direction.LEFT);
			listOfDirection.add(Direction.RIGHT);

			listOfDirection.remove(this.direction);
			
			int randomA = (int)(Math.random() * (higherA-lowerA)) + lowerA;

			this.direction = listOfDirection.get(randomA);
			this.numberOfRandom = 0;
		}
		
		int lowerB = 1;
		int higherB = this.speed + 1;
		
		int randomB = (int)(Math.random() * (higherB-lowerB)) + lowerB;
			switch(this.direction)
			{
			case TOP:
				this.direction = Direction.TOP;
				model.grid.setObjectLocation(this, x, model.grid.sty(y - randomB));
				y = model.grid.sty(y - randomB);
				break;
			case BOTTOM:
				this.direction = Direction.BOTTOM;
				model.grid.setObjectLocation(this, x, model.grid.sty(y + randomB));
				y = model.grid.sty(y + randomB);
				break;
			case LEFT:
				// LEFT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), y);
				x = model.grid.stx(x - randomB);
				break;
			case TOPLEFT:
				// TOPLEFT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y - randomB);
				break;
			case TOPRIGHT:
				// TOPRIGHT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y - randomB);
				break;
			case BOTTOMLEFT:
				// BOTTOMLEFT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y + randomB);
				break;
			case BOTTOMRIGHT:
				// BOTTOMRIGHT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y + randomB);
				break;
			default:
				// RIGHT
				this.direction = Direction.RIGHT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), y);
				x = model.grid.stx(x + randomB);
				break;
			}
		this.numberOfRandom++;
	}
}
