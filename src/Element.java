import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class Element implements Steppable
{
	public int x, y;
	public Environment environment;
	public Stoppable stoppable;
}
