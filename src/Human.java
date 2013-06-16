import java.util.ArrayList;

import javax.swing.ImageIcon;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.util.Bag;
import sim.util.IntBag;


public class Human extends Element
{
	private int speed = Constants.HUMAN_SPEED_MAX;
	private int perception = Constants.HUMAN_PERCEPTION_MAX;
	private int life = Constants.HUMAN_LIFE_MAX;
	private int weaponLevel = 1;//(int)(Math.random() * (Constants.HUMAN_WEAPON_LEVEL_MAX - 1)) + 1;
	private int munitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_1;
	private int maxMunitions = Constants.HUMAN_MUNITIONS_MAX_LEVEL_1;
	private int nbTraps = Constants.HUMAN_TRAPS_MAX;
	private int XP = 0;
	
	enum Direction
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
	
	private boolean firstTime = true;
	private Direction direction = Direction.RIGHT;
	private int numberOfRandom = 0;
	private Bag neighbours = new Bag();
    private IntBag neighX = new IntBag();
    private IntBag neighY = new IntBag();
	private IntBag neighboursX;
	private IntBag neighboursY;
	private ArrayList<Bag> neighboursArray;
	private boolean isBitten = false;
	
	public boolean goAway = false;
	private int cptStepGoAway = 0;
	private Bunker myBunker = null;
	
	@Override
	public void step(SimState state) 
	{
		this.environment = (Environment)state;
		
		if(firstTime)
		{
			weaponLevel = (int)(Math.random() * (Constants.HUMAN_WEAPON_LEVEL_MAX - 2)) + 1;
			upgradeWeapon();
			firstTime = false;
		}
		
		if(isBitten)
		{
			life -= 1;
		}
		
		if(cptStepGoAway >= 3)
		{
			goAway = false;
			cptStepGoAway = 0;
		}
		
		if(this.life >= 0)
		{
			if(nbTraps > 0)
			{
				int randomSetUpTrap = (int)(Math.random() * 2);
				if(randomSetUpTrap == 1)
				{
					// Success to set up a trap
					boolean objectFound = false;
					Bag objects = environment.grid.getObjectsAtLocation(x, y);
					for(int i = 0; i < objects.size(); i++)
					{
						if(objects.get(i) instanceof Bunker || objects.get(i) instanceof BonusPack)
						{
							objectFound = true;
						}
					}
					if(!objectFound)
						environment.addElement(new Trap(), x, y);
				}
				nbTraps --;
			}
			
			if(myBunker != null)
			{
				// Human earns experience with his partners
//				XP ++;
			}
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
			
			if(this.munitions <= 10 || this.life <= 10 || this.isBitten)
			{
				// We need to find munitions or points of life
				BonusPack bonusPack = null;
				boolean bonusPackFound = false;
				boolean doRandomMove = true;
				int i, j;
				
				for(i = 0; i < neighboursArray.size(); i++)
				{
					for(j = 0; j < neighboursArray.get(i).size(); j++)
					{
						if(neighboursArray.get(i).get(j) instanceof Zombie)
						{
							if(this.munitions > 0 && life >= 5)
							{
								if(i == 0 || i == 1)
								{
									// Zombie very close
									doRandomMove = false;
									shoot((Zombie) neighboursArray.get(i).get(j));
									setImage(direction, true);
//									break;
								}
							}
						}
						else if(neighboursArray.get(i).get(j) instanceof BonusPack)
						{
							doRandomMove = false;
							bonusPackFound = true;
							bonusPack = (BonusPack) neighboursArray.get(i).get(j);
							break;
						}
					}
					
					if(bonusPackFound)
					{
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
							isBitten = false;
							
							// Removing bonus pack
							environment.grid.remove(bonusPack);
							bonusPack.stoppable.stop();
							// Add a new Bonus pack on the map
							environment.addBonusPack();
							bonusPackFound = false;
							bonusPack = null;
							break;
						}
						else
						{
							// We move to the bonus pack
							move(environment, bonusPack.x, bonusPack.y);
							bonusPackFound = false;
							bonusPack = null;
							break;
						}
					}
					else
					{
						// We continue the research
						doRandomMove = true;
//						randomMove(environment);
//						break;
					}
				}
				if(doRandomMove)
					randomMove(environment);
			}
			else
			{
				boolean doRandomMove = true;
				boolean bunkerFound = false;
				boolean zombieFound = false;
				boolean bonusPackFound = false;
				ArrayList<Human> humansGroup = new ArrayList<Human>();
				Bunker bunker = null;
				
				int i,j;
				// We have enough munitions and life
				for(i = 0; i < neighboursArray.size(); i++)
				{
					for(j = 0; j < neighboursArray.get(i).size(); j++)
					{
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
								setImage(direction, true);
								break;
							}
						}
						else
						{
							// Default behaviour
							if(neighboursArray.get(i).get(j) instanceof Zombie)
							{
								zombieFound = true;
								shoot((Zombie)neighboursArray.get(i).get(j));
								setImage(direction, true);
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
							}
							else if(neighboursArray.get(i).get(j) instanceof BonusPack)
							{
								bonusPackFound = true;
							}
						}
					}
					
					if(i == 0  && !zombieFound)
					{
						// Elements on the same case
						if(!bunkerFound && !bonusPackFound)
						{
							humansGroup.remove(this);
							 if(humansGroup.size() >= 1 && !goAway)
							 {
								 boolean constructBunker = false;
								 Bag objects = environment.grid.getObjectsAtLocation(x, y);
								 for(int k = 0; k < objects.size(); k++)
								 {
									 if(objects.get(k) instanceof Bunker || objects.get(k) instanceof Trap
											 || objects.get(k) instanceof BonusPack)
										 constructBunker = true;
								 }
								 if(!constructBunker)
								 {
//									move(environment, humansGroup.get(0).x, humansGroup.get(0).y);							 
									humansGroup.add(this);
									Bunker buildBunker = new Bunker();
									buildBunker.setHumans(humansGroup);
									environment.addElement(buildBunker, this.x, this.y);
							        humansGroup.clear();
							        this.hide();
							        break;
								 }
							 }
							 else
							 {
								 cptStepGoAway ++;
								 doRandomMove = true;
							 }
						}
						else if(!bunker.isInBunker(this) && !bunker.isFull())
						{
							// We integrate the bunker
							if(bunker.upgrade(this, environment))
							{
								myBunker = bunker;
								this.hide();
								move(environment, bunker.x, bunker.y);
								bunkerFound = false;
								bunker = null;
								break;
							}
						}
					}
					else if(!zombieFound)
					{
						if(bunkerFound)
						{
							if(bunker.isFull())
							{
								bunkerFound = false;
								bunker = null;
								doRandomMove = true;
							}
							else
							{
								move(environment, bunker.x, bunker.y);
								bunkerFound = false;
								bunker = null;
								break;
							}
						}
						else if(!humansGroup.isEmpty())
						{
							move(environment, humansGroup.get(0).x, humansGroup.get(0).y);
							humansGroup.clear();
							break;
						}
						else
						{
							doRandomMove = true;
						}
					}
					
					if(!doRandomMove || zombieFound)
						break;
				}
				if(doRandomMove)
					randomMove(environment);
			}
		}
		else
		{
			environment.addZombie(x, y);
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
				if(myBunker != null && myBunker.getLevel() >= 4)
				{
					// TODO soigner zombie
				}
				else
				{
					zombie.bodyShot(Zombie.BODY_PART.LEG);
				}
				XP +=2;
				break;
			case 2:
				// Touch an arm
				if(myBunker != null && myBunker.getLevel() >= 4)
				{
					// TODO soigner zombie
				}
				else
				{
					zombie.bodyShot(Zombie.BODY_PART.ARM);
				}
				XP +=2;
				break;
			case 3:
				// Touch a trunk
				if(myBunker != null && myBunker.getLevel() >= 4)
				{
					// TODO soigner zombie
				}
				else
				{
					zombie.bodyShot(Zombie.BODY_PART.TRUNK);
				}
				XP += 3;
				break;
			case 4:
				// Headshot
				if(myBunker != null && myBunker.getLevel() >= 4)
				{
					// TODO soigner zombie
				}
				else
				{
					zombie.headShot();
				}
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
		munitions = maxMunitions;
	}
	
	private void move(Environment env, int l, int c)
	{
		this.numberOfRandom = 0;
		double distance = Math.sqrt(Math.pow(l - this.x, 2) + Math.pow(c - this.y, 2));
		
		this.speed = (int)(Math.random() * (Constants.HUMAN_SPEED_MAX -1)) + 1;

		if((int)distance > 0 && (int)distance <= this.speed)
		{
			// We move directly to the case (l,c)
			env.grid.setObjectLocation(this, env.grid.stx(l), env.grid.sty(c));
			x = env.grid.stx(l);
			y = env.grid.sty(c);
		}
		else if((int)distance > 0)
		{
			if(l == this.x) // If we are on the same column
			{
				if(this.y < c) // Move up
				{
					this.setImage(Direction.TOP, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x), env.grid.sty(y + 1));
						y = env.grid.sty(y + 1);
					}
					this.direction = Direction.TOP;
				}
				else if(this.y > c) // Move down
				{
					this.setImage(Direction.BOTTOM, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x), env.grid.sty(y - 1));
						y = env.grid.sty(y - 1);
					}
					this.direction = Direction.BOTTOM;
				}
			}
			else if(c == this.y) // If we are on the same row
			{
				if(this.x < l) // Move right
				{
					this.setImage(Direction.RIGHT, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y));
						x = env.grid.stx(x + 1);
					}
					this.direction = Direction.RIGHT;
				}
				else if(this.x > l) // Move left
				{
					this.setImage(Direction.LEFT, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x - 1), env.grid.sty(y));
						x = env.grid.stx(x - 1);
					}
					this.direction = Direction.LEFT;
				}
			}
			else
			{
				if(this.x < l && this.y < c) // Move down left
				{
					this.setImage(Direction.BOTTOM, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y + 1));
						x = env.grid.stx(x + 1);
						y = env.grid.sty(y + 1);
					}
					this.direction = Direction.BOTTOM;
				}
				else if(this.x < l && this.y > c) // Move up left
				{
					this.setImage(Direction.TOP, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y - 1));
						x = env.grid.stx(x + 1);
						y = env.grid.sty(y - 1);
					}
					this.direction = Direction.TOP;
				}
				else if(this.x > l && this.y < c) // Move down right
				{
					this.setImage(Direction.BOTTOM, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x - 1), env.grid.sty(y + 1));
						x = env.grid.stx(x - 1);
						y = env.grid.sty(y + 1);
					}
					this.direction = Direction.BOTTOM;
				}
				else if(this.x > l && this.y > c) // Move up right
				{
					this.setImage(Direction.TOP, false);
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y + 1));
						x = env.grid.stx(x + 1);
						y = env.grid.sty(y + 1);
					}
					this.direction = Direction.TOP;
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
				this.setImage(Direction.TOP, false);
				break;
			case BOTTOM:
				this.direction = Direction.BOTTOM;
				model.grid.setObjectLocation(this, x, model.grid.sty(y + randomB));
				y = model.grid.sty(y + randomB);
				this.setImage(Direction.BOTTOM, false);
				break;
			case LEFT:
				// LEFT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), y);
				x = model.grid.stx(x - randomB);
				this.setImage(Direction.LEFT, false);
				break;
			case TOPLEFT:
				// TOPLEFT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y - randomB);
				this.setImage(Direction.TOP, false);
				break;
			case TOPRIGHT:
				// TOPRIGHT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y - randomB);
				this.setImage(Direction.TOP, false);
				break;
			case BOTTOMLEFT:
				// BOTTOMLEFT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y + randomB);
				this.setImage(Direction.BOTTOM, false);
				break;
			case BOTTOMRIGHT:
				// BOTTOMRIGHT
				this.direction = Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y + randomB);
				this.setImage(Direction.BOTTOM, false);
				break;
			default:
				// RIGHT
				this.direction = Direction.RIGHT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), y);
				x = model.grid.stx(x + randomB);
				this.setImage(Direction.RIGHT, false);
				break;
			}
		this.numberOfRandom++;
	}
	
	/**
	 * Public method to attack a human
	 * @param damage
	 */
	public void attack(int damages)
	{
		this.life -= damages;
		this.isBitten = true;
	}
	
	public void setImage(Direction direction, boolean shoot)
	{
		ImageIcon imageIcon = null;
		
		if(!shoot)
		{
			switch(direction)
			{
			case BOTTOM:
				imageIcon = new ImageIcon("ressources/human_bottom.png");
				break;
			case TOP:
				imageIcon = new ImageIcon("ressources/human_up.png");
				break;
			case LEFT:
				imageIcon = new ImageIcon("ressources/human_left.png");
				break;
			case RIGHT:
				imageIcon = new ImageIcon("ressources/human_right.png");
				break;
			default:
				break;
			}
		}
		else
		{
			switch(direction)
			{
			case BOTTOM:
				imageIcon = new ImageIcon("ressources/human_bottom_shoot.png");
				break;
			case TOP:
				imageIcon = new ImageIcon("ressources/human_up_shoot.png");
				break;
			case LEFT:
				imageIcon = new ImageIcon("ressources/human_left_shoot.png");
				break;
			case RIGHT:
				imageIcon = new ImageIcon("ressources/human_right_shoot.png");
				break;
			default:
				break;
			}
		}
		
		environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2DForHuman(imageIcon, environment));
		environment.environmentUI.display.repaint();
	}
	
	/**
	 * Hide human image
	 */
	public void hide()
	{
		environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("")));
		environment.environmentUI.display.repaint();
	}
	
	/**
	 * Show human image
	 */
	public void show()
	{
		environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2DForHuman(new ImageIcon("ressources/human_bottom.png"), environment));
		environment.environmentUI.display.repaint();
	}

	public int getSpeed()
	{
		return speed;
	}

	public int getPerception()
	{
		return perception;
	}

	public int getLife()
	{
		return life;
	}

	public int getWeaponLevel()
	{
		return weaponLevel;
	}

	public int getMunitions()
	{
		return munitions;
	}

	public int getMaxMunitions()
	{
		return maxMunitions;
	}

	public int getXP()
	{
		return XP;
	}

	public boolean isBitten()
	{
		return isBitten;
	}

	public void setMyBunker(Bunker myBunker)
	{
		this.myBunker = myBunker;
	}
}
