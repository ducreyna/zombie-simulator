import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

public class Zombie implements Steppable
{
	private int speed = Constants.ZOMBIE_SPEED_MAX;
	private int perception = Constants.ZOMBIE_PERCEPTION_MAX;
	private Stoppable stoppable;
	private boolean isMaximumShot = false;
	private boolean isAlive = true;
	private boolean isBlockedForOneStep = false;
	
	public enum BODY_PART
	{
		ARM,
		LEG,
		TRUNK;
	}
	
	@Override
	public void step(SimState state) {
		Environment environment = (Environment)state;
		
		if(!this.isAlive)
		{
		
			this.stoppable.stop();
		}
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
}
