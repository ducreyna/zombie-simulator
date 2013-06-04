import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.HexaSparseGridPortrayal2D;

public class EnvironmentUI extends GUIState
{
  public Display2D display;
	public JFrame displayFrame;

	HexaSparseGridPortrayal2D environmentPortrayal = new HexaSparseGridPortrayal2D();

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
		
		// TODO : define the portrayals

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

		// specify the backdrop color -- what gets painted behind the displays
		display.setBackdrop(Color.black);
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
