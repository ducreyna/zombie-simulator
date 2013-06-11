import java.util.ArrayList;

import javax.swing.ImageIcon;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.util.Bag;
import sim.util.IntBag;

public class Zombie extends Element
{	
	private int speed = Constants.ZOMBIE_SPEED_MAX;
	private int perception = Constants.ZOMBIE_PERCEPTION_MAX;
	private boolean isMaximumShot = false;
	private boolean isAlive = true;
	private boolean isBlockedForOneStep = false;
	private Constants.Direction direction = Constants.Direction.RIGHT;
	private int numberOfRandom = 0;
	private Bag neighbours = new Bag();
	private IntBag neighboursX = new IntBag();
	private IntBag neighboursY = new IntBag();
	private ArrayList<Bag> neighboursArray = new ArrayList<Bag>();
	
	public enum BODY_PART
	{
		ARM,
		LEG,
		TRUNK;
	}
	
	@Override
	public void step(SimState state) {
		this.environment = (Environment)state;

		
		if(!this.isAlive)
		{
			environment.grid.remove(this);
			this.stoppable.stop();
		}
		else if(!this.isBlockedForOneStep)
		{
			environment.grid.getHexagonalNeighbors(x, y, this.perception, SparseGrid2D.TOROIDAL, neighbours, neighboursX, neighboursY);
			
			
			this.neighboursArray = this.perception();
			
			if(this.neighboursArray.get(0).size() != 0)
			{
				// Bouffer humain ou détruit bunker
				Bag bag = (Bag)this.neighboursArray.get(0);
				Bunker possibleBunker = this.getBunkerFromBag(bag);
				if(possibleBunker != null)
				{
					int damage = 2;
					possibleBunker.attack(damage);
				}
				else
				{
					int damage = 2;
					((Human)bag.get(0)).attack(damage);
				}
			} 
			else if(this.neighboursArray.get(1).size() != 0)
			{
				// Bouffer human ou détruit bunker
				Bag bag = (Bag)this.neighboursArray.get(1);
				Bunker possibleBunker = this.getBunkerFromBag(bag);
				if(possibleBunker != null)
				{
					int damage = 2;
					possibleBunker.attack(damage);
				}
				else
				{
					int damage = 2;
					((Human)bag.get(0)).attack(damage);
				}
			}
			else
			{
				Boolean loopFinished = true;
				for(Bag bag : this.neighboursArray)
				{
					if(bag.size() != 0)
					{
						Element elt = (Element)bag.get(0);
						this.move(this.environment, elt.x, elt.y);
						loopFinished = false;
						break;
					}
				}
				
				if(!loopFinished)
				{
					this.randomMove(this.environment);
				}
			}	
		}
		else
		{
			this.isBlockedForOneStep = false;
		}
	}

	private ArrayList<Bag> perception()
	{
		return this.perception(this.neighbours, this.neighboursX, this.neighboursY);
	}
	
	private ArrayList<Bag> perception(Bag neighbours, IntBag posX, IntBag posY) 
	{
		ArrayList<Bag> result = new ArrayList<Bag>();
		
		for(int k=0; k <= this.perception; k++)
		{
			result.add(k, new Bag());
		}
		
		for(int i=1; i < neighbours.size(); i++)
		{
			Object object = neighbours.get(i);
			if((object instanceof Human) || (object instanceof Bunker))
			{
				int xB = ((Element)neighbours.get(i)).x;
				int yB = ((Element)neighbours.get(i)).y;
				double distance = Math.sqrt(Math.pow(xB - this.x, 2) + Math.pow(yB - this.y, 2));
				if(distance > (this.perception + 1))
				{ 
					distance = Math.abs(this.environment.gridWidth - distance);
				}
				result.get((int)distance).add(object);
			}
		}
		
		return result;
	}
	
	public void headShot()
	{
		this.isAlive = false;
	}
	
	public void bodyShot(BODY_PART part)
	{
		switch(part)
		{
			case ARM:
				if(!this.isMaximumShot) this.speed--;
				break;
			case LEG:
				if(!this.isMaximumShot) this.speed--;
				else this.isBlockedForOneStep = true;
				break;
			case TRUNK:
				if(!this.isMaximumShot) this.speed--;
				// ELSE : MOVE BACK 1 CASE
				break;
				default:
					break;
		}
	}
	
	public void destroyBunker(Bunker bunker)
	{
		// TO DO : remove 2 point to bunker
	}
	
	private void move(Environment model, int l, int c)
	{
		this.numberOfRandom = 0;
		int distance = Math.max(Math.abs(this.x - l), Math.abs(this.y - c));
		
//		this.speed = (int)(Math.random() * Constants.HUMAN_SPEED_MAX) + 1;
//		System.out.println((int)distance);
		
		if(distance <= this.speed)
		{
			// We move directly to the case (l,c)
			model.grid.setObjectLocation(this, model.grid.stx(l), model.grid.sty(c));
			x = model.grid.stx(l);
			y = model.grid.sty(c);
		}
		else 
		{
			if(l == this.x) // If we are on the same column
			{
				if(this.y < c) // Move up
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x), model.grid.sty(y + 1));
						y = model.grid.sty(y + 1);
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("zombie_up.png")));
						this.environment.environmentUI.display.repaint();
					}
				}
				else if(this.y > c) // Move down
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x), model.grid.sty(y - 1));
						y = model.grid.sty(y - 1);
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_bottom.png")));
						this.environment.environmentUI.display.repaint();
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
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_right.png")));
						this.environment.environmentUI.display.repaint();
					}
				}
				else if(this.x > l) // Move left
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x - 1), model.grid.sty(y));
						x = model.grid.stx(x - 1);
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_left.png")));
						this.environment.environmentUI.display.repaint();
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
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_bottom.png")));
						this.environment.environmentUI.display.repaint();
					}
				}
				else if(this.x < l && this.y > c) // Move up left
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y - 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y - 1);
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_up.png")));
						this.environment.environmentUI.display.repaint();
					}
				}
				else if(this.x > l && this.y < c) // Move down right
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x - 1), model.grid.sty(y + 1));
						x = model.grid.stx(x - 1);
						y = model.grid.sty(y + 1);
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_bottom.png")));
						this.environment.environmentUI.display.repaint();
					}
				}
				else if(this.x > l && this.y > c) // Move up right
				{
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y + 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y + 1);
						this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_up.png")));
						this.environment.environmentUI.display.repaint();
					}
				}
			}
		}
			
	}
	
	private void randomMove(Environment model) 
	{
		if(this.numberOfRandom  >= 9)
		{
			int lowerA = 0;
			int higherA = 3;

			ArrayList<Constants.Direction> listOfDirection = new ArrayList<Constants.Direction>();
			listOfDirection.add(Constants.Direction.BOTTOM);
			listOfDirection.add(Constants.Direction.TOP);
			listOfDirection.add(Constants.Direction.LEFT);
			listOfDirection.add(Constants.Direction.RIGHT);

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
				this.direction = Constants.Direction.TOP;
				model.grid.setObjectLocation(this, x, model.grid.sty(y - randomB));
				y = model.grid.sty(y - randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_up.png")));
				this.environment.environmentUI.display.repaint();
				break;
			case BOTTOM:
				this.direction = Constants.Direction.BOTTOM;
				model.grid.setObjectLocation(this, x, model.grid.sty(y + randomB));
				y = model.grid.sty(y + randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_bottom.png")));
				this.environment.environmentUI.display.repaint();
				break;
			case LEFT:
				// LEFT
				this.direction = Constants.Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), y);
				x = model.grid.stx(x - randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_left.png")));
				this.environment.environmentUI.display.repaint();
				break;
			case TOPLEFT:
				// TOPLEFT
				this.direction = Constants.Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y - randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_up.png")));
				this.environment.environmentUI.display.repaint();
				break;
			case TOPRIGHT:
				// TOPRIGHT
				this.direction = Constants.Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y - randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_up.png")));
				this.environment.environmentUI.display.repaint();
				break;
			case BOTTOMLEFT:
				// BOTTOMLEFT
				this.direction = Constants.Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y + randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_bottom.png")));
				this.environment.environmentUI.display.repaint();
				break;
			case BOTTOMRIGHT:
				// BOTTOMRIGHT
				this.direction = Constants.Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y + randomB);
				this.environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new ImagePortrayal2D(new ImageIcon("ressources/zombie_bottom.png")));
				this.environment.environmentUI.display.repaint();
				break;
			default:
				// RIGHT
				this.direction = Constants.Direction.RIGHT;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), y);
				x = model.grid.stx(x + randomB);
				break;
			}
		this.numberOfRandom++;
	}
	
	private Bunker getBunkerFromBag(Bag bag)
	{
		for(Object elt : bag)
		{
			if(elt instanceof Bunker) return (Bunker)elt;
		}
		
		return null;
	}

	public int getNumberOfRandom()
	{
		return numberOfRandom;
	}

	public void setNumberOfRandom(int numberOfRandom)
	{
		this.numberOfRandom = numberOfRandom;
	}

	public boolean isBlockedForOneStep()
	{
		return isBlockedForOneStep;
	}
}
