import java.awt.Color;


public class Constants
{
	// Constants for the environment
	public static int GRID_HEIGHT = 60;
	public static int GRID_WIDTH = 60;
	public static int INIT_HUMAN_COUNT = 20;
	public static int INIT_ZOMBIE_COUNT = 30;
	public static int INIT_BONUSPACK_COUNT = 10;
	public static Color BONUSPACK_COLOR = Color.black;
	
	// Constants for the Human Class
	public static int HUMAN_SPEED_MAX = 5;
	public static int HUMAN_PERCEPTION_MAX = 4;
	public static int HUMAN_LIFE_MAX = 25;
	public static int HUMAN_WEAPON_LEVEL_MAX = 5;
	public static int HUMAN_MUNITIONS_MAX_LEVEL_1 = 35;
	public static int HUMAN_MUNITIONS_MAX_LEVEL_2 = 30;
	public static int HUMAN_MUNITIONS_MAX_LEVEL_3 = 25;
	public static int HUMAN_MUNITIONS_MAX_LEVEL_4 = 20;
	public static int HUMAN_MUNITIONS_MAX_LEVEL_5 = 15;
	public static int HUMAN_TRAPS_MAX = 2;
	public static int HUMAN_XP_MAX = 10;

	public static int ZOMBIE_SPEED_MAX = 4;
	public static int ZOMBIE_PERCEPTION_MAX = 6;
	
	public static int BUNKER_LIFE_MAX_LEVEL_2 = 15;
	public static int BUNKER_LIFE_MAX_LEVEL_3 = 20;
	public static int BUNKER_LIFE_MAX_LEVEL_4 = 25;
	public static int BUNKER_LIFE_MAX_LEVEL_5 = 30;
	public static int BUNKER_LEVEL_MAX = 5;
	public static int BUNKER_PERCEPTION_MAX = 5;
	public static int BUNKER_STEP_VACCIN_MAX = 20;
	
	public static int DOG_SPEED_MAX = 1;
	
	public static enum Direction
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
}
