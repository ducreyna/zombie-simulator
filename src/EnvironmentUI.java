import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.HexaSparseGridPortrayal2D;
import sim.portrayal.grid.HexaValueGridPortrayal2D;
import sim.portrayal.inspector.StableInt2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;

public class EnvironmentUI extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;
	public Environment environment;

	HexaSparseGridPortrayal2D environmentPortrayal = new HexaSparseGridPortrayal2D();
    HexaValueGridPortrayal2D perceptionPortrayal = new HexaValueGridPortrayal2D("Perception");

	
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
		environment = (Environment) state;
		environment.setEnvironmentUI(this);
		
		// TODO
        ColorMap map = new SimpleColorMap(0, 10, Color.green, Color.red);
        perceptionPortrayal.setField(environment.perceptionGrid);
        perceptionPortrayal.setMap(map);
        
        environmentPortrayal.setField(environment.grid);
        ImageIcon humanIcon = new ImageIcon("ressources/human_bottom.png");
        ImageIcon zombieIcon = new ImageIcon("ressources/zombie_bottom.png");
        ImageIcon bunkerIcon = new ImageIcon("ressources/bunker_2.png");
        ImageIcon bonusIcon = new ImageIcon("ressources/bonus.png");
        ImageIcon trapIcon = new ImageIcon("ressources/trap.png");
        ImageIcon dogIcon = new ImageIcon("ressources/dog.png");
   
        environmentPortrayal.setPortrayalForClass(Human.class, new ImagePortrayal2D(humanIcon) {
        	public boolean handleMouseEvent(GUIState gui, Manipulating2D manipulating, LocationWrapper wrapper, MouseEvent event, DrawInfo2D fieldPortrayalDrawInfo, int type)
        	{
        		synchronized(gui.state.schedule)
        		{
        			if (type == SimplePortrayal2D.TYPE_HIT_OBJECT && event.getID() == MouseEvent.MOUSE_CLICKED)
        			{
        				StableInt2D location = (StableInt2D) wrapper.getLocation();
        				System.out.println("clic on a human at (x,y) : ("+location.getX()+","+location.getY()+")");
        				boolean _draw;
        				if (event.getButton() == MouseEvent.BUTTON1) { _draw = true; } else { _draw = false; }
        				environment.drawPerception(location.getX(), location.getY(), Constants.HUMAN_PERCEPTION_MAX, _draw);
        			}
        			return super.handleMouseEvent(gui, manipulating, wrapper, event, fieldPortrayalDrawInfo, type);
        		}
        	}
        });
        environmentPortrayal.setPortrayalForClass(Zombie.class, new ImagePortrayal2D(zombieIcon));  
        ImagePortrayal2D image = new ImagePortrayal2D(bunkerIcon);
        image.scale = 2.0;
        environmentPortrayal.setPortrayalForClass(Bunker.class, image);
        environmentPortrayal.setPortrayalForClass(BonusPack.class, new ImagePortrayal2D(bonusIcon));
        environmentPortrayal.setPortrayalForClass(Trap.class, new ImagePortrayal2D(trapIcon));
        environmentPortrayal.setPortrayalForClass(Dog.class, new ImagePortrayal2D(dogIcon));
        
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
		
		display.attach(perceptionPortrayal, "Human Perception");
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
