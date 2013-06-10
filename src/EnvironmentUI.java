import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.HexaSparseGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class EnvironmentUI extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;

	HexaSparseGridPortrayal2D environmentPortrayal = new HexaSparseGridPortrayal2D();
    //HexaValueGridPortrayal2D perceptionPortrayal = new HexaValueGridPortrayal2D("Perception");
	
	public static void main(String[] args)
	{
		new EnvironmentUI().createController();
	}

	public EnvironmentUI()
	{
		super(new Environment(System.currentTimeMillis()));
	}

	public EnvironmentUI(SimState state)
	{
		super(state);
	}

	public static String getName()
	{
		return "Zombies invasion simulator";
	}

	public void start()
	{
		super.start();
		setupPortrayals();
	}

	public void load(SimState state)
	{
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals()
	{
		Environment env = (Environment) state;
		
        //ColorMap map = new SimpleColorMap(32000, 32000, Color.red, Color.red);
        //perceptionPortrayal.setField(env.perceptionGrid);
        //perceptionPortrayal.setMap(map);
        
        environmentPortrayal.setField(env.grid);
        ImageIcon humanIcon = new ImageIcon("ressources/human_bottom.png");
        ImageIcon zombieIcon = new ImageIcon("ressources/zombie_bottom.png");
        ImageIcon bunkerIcon = new ImageIcon("ressources/bunker_2.png");
   
        environmentPortrayal.setPortrayalForClass(Human.class, new ImagePortrayal2D(humanIcon));
        environmentPortrayal.setPortrayalForClass(Zombie.class, new ImagePortrayal2D(zombieIcon));
        environmentPortrayal.setPortrayalForClass(BonusPack.class, new OvalPortrayal2D(Color.black));
        environmentPortrayal.setPortrayalForClass(Bunker.class, new ImagePortrayal2D(bunkerIcon));
        
		// reschedule the displayer
		display.reset();

		// redraw the display
		display.repaint();
	}
	
	/*
	 * The ratio of the width of a hexagon to its height: 1 / Sin(60 degrees),
	 * otherwise known as 2 / Sqrt(3)
	 */
	public static final double HEXAGONAL_RATIO = 2 / Math.sqrt(3);

	public void init(Controller c)
	{
		super.init(c);

		/*
		Make the Display2D. We'll have it display stuff later.

		Horizontal hexagons are staggered. This complicates computations.
		Thus if you have a M x N grid scaled to SCALE,
		then your height is (N + 0.5) * SCALE
		and your width is ((M - 1) * (3/4) + 1) * HEXAGONAL_RATIO * SCALE

		You might need to adjust by 1 or 2 pixels in each direction to get circles
		which usually come out as circles and not as ovals.
		 */
		
		final double scale = 4;
		final double m = 100;
		final double n = 100;
		final int height = (int) ((n + 0.5) * scale);
		final int width = (int) (((m - 1) * 3.0 / 4.0 + 1) * HEXAGONAL_RATIO * scale);

		display = new Display2D(width, height, this);
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);

		// TODO : attach the portrayals
		//display.attach(perceptionPortrayal, "Human Perception");
		display.attach(environmentPortrayal, "Environment");

		// specify the backdrop color -- what gets painted behind the displays
		display.setBackdrop(Color.green);
	}

	public void quit()
	{
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;
	}
}
