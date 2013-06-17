import sim.engine.SimState;


public class Trap extends Element
{
	public Zombie zombieTrapped;
	public Dog dogSend;
	
	@Override
	public void step(SimState state)
	{
		this.environment = (Environment)state;
		
	}

	@Override
	public String toString()
	{
		return new String("Trap");
	}

	public void destroy()
	{
		if(dogSend != null)
		{
			this.environment.grid.remove(this.dogSend);
			this.dogSend.stoppable.stop();
		}
		
		this.environment.grid.remove(this);
		this.stoppable.stop();
	}
	
	public Zombie getZombieTrapped() {
		return zombieTrapped;
	}

	public void setZombieTrapped(Zombie zombieTrapped) {
		this.zombieTrapped = zombieTrapped;
	}
	
	public Dog getDogSend() {
		return dogSend;
	}
}
