import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public class BonusPack extends Element implements Steppable
{
	// Minimum 5 munitions
	private int munitions = (int)(Math.random() * Constants.HUMAN_MUNITIONS_MAX_LEVEL_5) + 5;
	// Minimum 5 PF
	private int life = (int)(Math.random() * Constants.HUMAN_LIFE_MAX) + 5;
	// Weapon could be upgraded to the max or minimum 1
	private int weaponUpgrade = (int)(Math.random() * Constants.HUMAN_WEAPON_LEVEL_MAX) + 1;
	
	public Stoppable stoppable;
	
	@Override
	public void step(SimState arg0)
	{
		// DO NOTHING
		// BONUS PACK DOES NOT MOVE IN THE ENVIRONMENT
	}


	public int getMunitions()
	{
		return munitions;
	}


	public int getLife()
	{
		return life;
	}


	public int getWeaponUpgrade()
	{
		return weaponUpgrade;
	}
}
