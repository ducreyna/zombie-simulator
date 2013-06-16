import sim.engine.SimState;
import sim.util.Bag;


public class Dog extends Element
{
	private int speed = Constants.DOG_SPEED_MAX;
	private boolean zombieTraped = false;
	
	public int xTarget;
	public int yTarget;
	
	@Override
	public void step(SimState environment)
	{
		Environment env = (Environment) environment;
		
		if(this.x == xTarget && this.y == yTarget)
		{
			zombieTraped = true;
			
			Bag objects = env.grid.getObjectsAtLocation(x, y);
			for(int i = 0; i < objects.size(); i++)
			{
				if(objects.get(i) instanceof Zombie || objects.get(i) instanceof Trap)
				{
					Element elt = (Element) objects.get(i);
					env.grid.remove(elt);
					elt.stoppable.stop();
				}
			}
		}
		else
		{
			move(env, xTarget, yTarget);
		}
	}
	
	
	/**
	 * Private method to do a movement on the map
	 * @param env
	 * @param l
	 * @param c
	 */
	private void move(Environment env, int l, int c)
	{
		double distance = Math.sqrt(Math.pow(l - this.x, 2) + Math.pow(c - this.y, 2));
		
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
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x), env.grid.sty(y + 1));
						y = env.grid.sty(y + 1);
					}
				}
				else if(this.y > c) // Move down
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x), env.grid.sty(y - 1));
						y = env.grid.sty(y - 1);
					}
				}
			}
			else if(c == this.y) // If we are on the same row
			{
				if(this.x < l) // Move right
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y));
						x = env.grid.stx(x + 1);
					}
				}
				else if(this.x > l) // Move left
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x - 1), env.grid.sty(y));
						x = env.grid.stx(x - 1);
					}
				}
			}
			else
			{
				if(this.x < l && this.y < c) // Move down left
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y + 1));
						x = env.grid.stx(x + 1);
						y = env.grid.sty(y + 1);
					}
				}
				else if(this.x < l && this.y > c) // Move up left
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y - 1));
						x = env.grid.stx(x + 1);
						y = env.grid.sty(y - 1);
					}
				}
				else if(this.x > l && this.y < c) // Move down right
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x - 1), env.grid.sty(y + 1));
						x = env.grid.stx(x - 1);
						y = env.grid.sty(y + 1);
					}
				}
				else if(this.x > l && this.y > c) // Move up right
				{
					for(int i=0; i<this.speed; i++)
					{
						env.grid.setObjectLocation(this, env.grid.stx(x + 1), env.grid.sty(y + 1));
						x = env.grid.stx(x + 1);
						y = env.grid.sty(y + 1);
					}
				}
			}
		}	
	}


	public boolean isZombieTraped()
	{
		return zombieTraped;
	}

}
