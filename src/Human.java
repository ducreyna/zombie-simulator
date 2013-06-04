import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public class Human implements Steppable
{
	public int x, y;
	public Stoppable stoppable;
	
	private int speed = Constants.HUMAN_SPEED_MAX;
	private int perception = Constants.HUMAN_PERCEPTION_MAX;
	private int life = Constants.HUMAN_LIFE_MAX;
	private int weaponLevel = Constants.HUMAN_WEAPON_LEVEL;
	private int munitions = Constants.HUMAN_MUNITIONS_MAX;
	
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
	
	@Override
	public void step(SimState state) 
	{
		Environment model = (Environment)state;
		
		if(this.life != 0)
		{
			// TODO perception
		}
		else
		{
			// TODO Supprimer humain + ajouter zombie
		}
	}
	
	/**
	 * Private function to shoot on a zombie
	 */
	private void shoot()
	{
		
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
