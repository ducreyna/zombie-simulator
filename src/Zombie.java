import java.util.ArrayList;

import javax.swing.ImageIcon;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.IntBag;

public class Zombie extends Element
{	
	private int speed = Constants.ZOMBIE_SPEED_MAX;
	private int perception = Constants.ZOMBIE_PERCEPTION_MAX;
	private boolean isMaximumShot = false;
	private boolean isAlive = true;
	private boolean isBlockedForOneStep = false;
	private boolean isVaccinated = false;
	private boolean isTrapped = false;
	private int trapTime = 10;
	private int vaccinTime = 3;
	private Constants.Direction direction = Constants.Direction.RIGHT;
	private int numberOfRandom = 0;
	private Bag neighbours = new Bag();
	private IntBag neighboursX = new IntBag();
	private IntBag neighboursY = new IntBag();
	private ArrayList<Bag> neighboursArray = new ArrayList<Bag>();
	private Trap currentTrap;
	
	public enum BODY_PART
	{
		ARM,
		LEG,
		TRUNK,
		HEAD;
	}
	
	@Override
	public void step(SimState state) {
		this.environment = (Environment)state;

		
		if(!this.isAlive)
		{
			environment.grid.remove(this);
			environment.zombieCount--;
			this.stoppable.stop();
		}
		else if(!this.isBlockedForOneStep && this.vaccinTime > 0 && !this.isTrapped)
		{
			if(this.isVaccinated)
			{
				this.vaccinTime--;
				
				if(vaccinTime == 0) this.transformToHuman();
			}
			environment.grid.getHexagonalNeighbors(x, y, this.perception, SparseGrid2D.TOROIDAL, neighbours, neighboursX, neighboursY);
			
			
			this.neighboursArray = this.perception();
			
			if(this.neighboursArray.get(0).size() != 0)
			{
				Bag bag = (Bag)this.neighboursArray.get(0);
				for(Object elt : bag)
				{
					if(elt instanceof Trap) {
						this.isTrapped = true;
						this.currentTrap = (Trap)elt;
						this.currentTrap.zombieTrapped = this;
						break;
					}
				}
				

				if(!this.isTrapped)
				{
					// Bouffer humain ou d�truit bunker
					
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
			} 
			else if(this.neighboursArray.get(1).size() != 0)
			{
				// Bouffer human ou d�truit bunker
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
				Boolean loopFinished = false;
				for(Bag bag : this.neighboursArray)
				{
					if(bag.size() != 0)
					{
						Element elt = (Element)bag.get(0);
						this.move(this.environment, elt.x, elt.y);
						loopFinished = true;
						break;
					}
				}
				
				if(!loopFinished)
				{
					this.randomMove(this.environment);
				}
			}	
		}
		else if(this.isBlockedForOneStep && this.vaccinTime > 0)
		{
			this.isBlockedForOneStep = false;
		}
		else if(this.isTrapped)
		{
				this.trapTime--;
				
				if(this.trapTime == 0)
				{
					this.isTrapped = false;
					this.trapTime = 10;	
					
					this.currentTrap.destroy();
					this.currentTrap = null;
				}
		}

	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPerception() {
		return perception;
	}

	public void setPerception(int perception) {
		this.perception = perception;
	}

	public boolean isMaximumShot() {
		return isMaximumShot;
	}

	public void setMaximumShot(boolean isMaximumShot) {
		this.isMaximumShot = isMaximumShot;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public boolean isVaccinated() {
		return isVaccinated;
	}

	public void setVaccinated(boolean isVaccinated) {
		this.isVaccinated = isVaccinated;
	}

	public boolean isTrapped() {
		return isTrapped;
	}

	public void setTrapped(boolean isTrapped) {
		this.isTrapped = isTrapped;
	}

	public int getTrapTime() {
		return trapTime;
	}

	public void setTrapTime(int trapTime) {
		this.trapTime = trapTime;
	}

	public int getVaccinTime() {
		return vaccinTime;
	}

	public void setVaccinTime(int vaccinTime) {
		this.vaccinTime = vaccinTime;
	}

	public Constants.Direction getDirection() {
		return direction;
	}

	public void setDirection(Constants.Direction direction) {
		this.direction = direction;
	}

	public Trap getCurrentTrap() {
		return currentTrap;
	}

	public void setCurrentTrap(Trap currentTrap) {
		this.currentTrap = currentTrap;
	}

	public void setBlockedForOneStep(boolean isBlockedForOneStep) {
		this.isBlockedForOneStep = isBlockedForOneStep;
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
			if((object instanceof Human) || (object instanceof Bunker) || (object instanceof Trap))
			{
				int xB = ((Element)neighbours.get(i)).x;
				int yB = ((Element)neighbours.get(i)).y;
				double distance = Math.sqrt(Math.pow(xB - this.x, 2) + Math.pow(yB - this.y, 2));
				if(distance > (this.perception + 1))
				{ 
					distance = Math.abs(this.environment.gridWidth - distance);
				}
				if((int)distance < result.size())
				{
					if(!((object instanceof Trap) && (int)distance != 0))
					result.get((int)distance).add(object);
				}
			}
		}
		
		return result;
	}
	
	public void headShot()
	{
		this.isAlive = false;
		setImage(direction, true, BODY_PART.HEAD);
	}
	
	public void bodyShot(BODY_PART part)
	{
		switch(part)
		{
			case ARM:
				if(!this.isMaximumShot) this.speed--;
				setImage(direction, true, BODY_PART.ARM);
				break;
			case LEG:
				if(!this.isMaximumShot) this.speed--;
				else this.isBlockedForOneStep = true;
				setImage(direction, true, BODY_PART.LEG);
				break;
			case TRUNK:
				if(!this.isMaximumShot) this.speed--;
				setImage(direction, true, BODY_PART.TRUNK);
				// ELSE : MOVE BACK 1 CASE
				break;
				default:
					break;
		}
		
		if(this.speed == 1) this.isMaximumShot = true;
	}
	
	public void transformToHuman()
	{
		environment.addHuman(this.x, this.y);
		environment.grid.remove(this);
		environment.zombieCount--;
		//environment.transformationGrid.field[environment.grid.stx(this.x)][environment.grid.sty(this.y)] = 8;
		this.stoppable.stop();
	}
	
	private void move(Environment model, int l, int c)
	{
		this.numberOfRandom = 0;
		int distance = Math.max(Math.abs(this.x - l), Math.abs(this.y - c));
		
		this.speed = (int)(Math.random() * Constants.HUMAN_SPEED_MAX) + 1;
		System.out.println((int)distance);
		
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
					setImage(Constants.Direction.TOP, false, null);
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x), model.grid.sty(y + 1));
						y = model.grid.sty(y + 1);
					}
				}
				else if(this.y > c) // Move down
				{
					setImage(Constants.Direction.BOTTOM, false, null);
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
					setImage(Constants.Direction.RIGHT, false, null);
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y));
						x = model.grid.stx(x + 1);
					}
				}
				else if(this.x > l) // Move left
				{
					setImage(Constants.Direction.LEFT, false, null);
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
					setImage(Constants.Direction.BOTTOM, false, null);
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y + 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y + 1);
					}
				}
				else if(this.x < l && this.y > c) // Move up left
				{
					setImage(Constants.Direction.TOP, false, null);
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x + 1), model.grid.sty(y - 1));
						x = model.grid.stx(x + 1);
						y = model.grid.sty(y - 1);
					}
				}
				else if(this.x > l && this.y < c) // Move down right
				{
					setImage(Constants.Direction.BOTTOM, false, null);
					for(int i=0; i<this.speed; i++)
					{
						model.grid.setObjectLocation(this, model.grid.stx(x - 1), model.grid.sty(y + 1));
						x = model.grid.stx(x - 1);
						y = model.grid.sty(y + 1);
					}
				}
				else if(this.x > l && this.y > c) // Move up right
				{
					setImage(Constants.Direction.TOP, false, null);
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
				setImage(direction, false, null);
				break;
			case BOTTOM:
				this.direction = Constants.Direction.BOTTOM;
				model.grid.setObjectLocation(this, x, model.grid.sty(y + randomB));
				y = model.grid.sty(y + randomB);
				setImage(direction, false, null);
				break;
			case LEFT:
				// LEFT
				this.direction = Constants.Direction.LEFT;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), y);
				x = model.grid.stx(x - randomB);
				setImage(direction, false, null);
				break;
			case TOPLEFT:
				// TOPLEFT
				this.direction = Constants.Direction.TOP;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y - randomB);
				setImage(direction, false, null);
				break;
			case TOPRIGHT:
				// TOPRIGHT
				this.direction = Constants.Direction.TOP;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y - randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y - randomB);
				setImage(direction, false, null);
				break;
			case BOTTOMLEFT:
				// BOTTOMLEFT
				this.direction = Constants.Direction.BOTTOM;
				model.grid.setObjectLocation(this, model.grid.stx(x - randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x - randomB);
				y = model.grid.sty(y + randomB);
				setImage(direction, false, null);
				break;
			case BOTTOMRIGHT:
				// BOTTOMRIGHT
				this.direction = Constants.Direction.BOTTOM;
				model.grid.setObjectLocation(this, model.grid.stx(x + randomB), model.grid.sty(y + randomB));
				x = model.grid.stx(x + randomB);
				y = model.grid.sty(y + randomB);
				setImage(direction, false, null);
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
	
	public void setImage(Constants.Direction direction, boolean hurt, BODY_PART bodyPart)
	{
		ImageIcon imageIcon = null;
		
		if(!hurt)
		{
			switch(direction)
			{
			case BOTTOM:
				imageIcon = new ImageIcon("ressources/zombie_bottom.png");
				break;
			case TOP:
				imageIcon = new ImageIcon("ressources/zombie_up.png");
				break;
			case LEFT:
				imageIcon = new ImageIcon("ressources/zombie_left.png");
				break;
			case RIGHT:
				imageIcon = new ImageIcon("ressources/zombie_right.png");
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
				switch(bodyPart)
				{
				case ARM:
					imageIcon = new ImageIcon("ressources/zombie_bottom_arm.png");
					break;
				case LEG:
					imageIcon = new ImageIcon("ressources/zombie_bottom_leg.png");
					break;
				case TRUNK:
					imageIcon = new ImageIcon("ressources/zombie_bottom_trunk.png");
					break;
				default:
					imageIcon = new ImageIcon("ressources/zombie_bottom_head.png");
					break;
				}
				break;
			case TOP:
				switch(bodyPart)
				{
				case ARM:
					imageIcon = new ImageIcon("ressources/zombie_top_arm.png");
					break;
				case LEG:
					imageIcon = new ImageIcon("ressources/zombie_top_leg.png");
					break;
				case TRUNK:
					imageIcon = new ImageIcon("ressources/zombie_top_trunk.png");
					break;
				default:
					imageIcon = new ImageIcon("ressources/zombie_top_head.png");
					break;
				}
				break;
			case LEFT:
				switch(bodyPart)
				{
				case ARM:
					imageIcon = new ImageIcon("ressources/zombie_left_arm.png");
					break;
				case LEG:
					imageIcon = new ImageIcon("ressources/zombie_left_leg.png");
					break;
				case TRUNK:
					imageIcon = new ImageIcon("ressources/zombie_left_trunk.png");
					break;
				default:
					imageIcon = new ImageIcon("ressources/zombie_left_head.png");
					break;
				}
				break;
			case RIGHT:
				switch(bodyPart)
				{
				case ARM:
					imageIcon = new ImageIcon("ressources/zombie_right_arm.png");
					break;
				case LEG:
					imageIcon = new ImageIcon("ressources/zombie_right_leg.png");
					break;
				case TRUNK:
					imageIcon = new ImageIcon("ressources/zombie_right_trunk.png");
					break;
				default:
					imageIcon = new ImageIcon("ressources/zombie_right_head.png");
					break;
				}
				break;
			default:
				break;
			}
		}
		
		if(environment != null)
		{
			environment.environmentUI.environmentPortrayal.setPortrayalForObject(this, new SelectableImagePortrayal2D(imageIcon, environment));
			environment.environmentUI.display.repaint();
		}
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
	
	public String toString()
	{
		return new String("Zombie");
	}
}
