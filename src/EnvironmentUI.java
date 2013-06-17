import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.Inspector;
import sim.portrayal.grid.HexaSparseGridPortrayal2D;
import sim.portrayal.grid.HexaValueGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;

public class EnvironmentUI extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;
	public Environment environment;

	public HexaSparseGridPortrayal2D environmentPortrayal = new HexaSparseGridPortrayal2D();
	public HexaValueGridPortrayal2D perceptionPortrayal = new HexaValueGridPortrayal2D("Perception");

	public JFrame chartFrame;
	public org.jfree.data.xy.XYSeries series1, series2, series3;
	public sim.util.media.chart.TimeSeriesChartGenerator chart;

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

	public Object getSimulationInspectedObject()
	{
		return state;
	}

	public Inspector getInspector()
	{
		Inspector i = super.getInspector();
		i.setVolatile(true);
		return i;
	}

	public void start()
	{
		super.start();
		setupPortrayals();

		chart.removeAllSeries();
		series1 = new org.jfree.data.xy.XYSeries("nombre d'humains", false);
		series2 = new org.jfree.data.xy.XYSeries("nombre de zombies", false);
		series3 = new org.jfree.data.xy.XYSeries("nombre de bonus", false);
		chart.addSeries(series1, null);
		chart.addSeries(series2, null);
		chart.addSeries(series3, null);
		chart.addLegend();
		scheduleRepeatingImmediatelyAfter(new Steppable()
		{
			public void step(SimState state)
			{
				double x = state.schedule.time(); 
				double y1 = environment.humanCount;
				double y2 = environment.zombieCount;
				double y3 = environment.bonusPackCount;

				if (x >= state.schedule.EPOCH && x < state.schedule.AFTER_SIMULATION)
				{
					series1.add(x, y1, true);
					series2.add(x, y2, true);
					series3.add(x, y3, true);
					chart.updateChartLater(state.schedule.getSteps());
				}
			}
		});
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

		environmentPortrayal.setPortrayalForClass(Human.class, new ImagePortrayal2DForHuman(humanIcon, environment));		
		environmentPortrayal.setPortrayalForClass(Zombie.class, new SelectableImagePortrayal2D(zombieIcon, environment));
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

		display = new Display2D(width, height, this) {
			@Override
			public void step(SimState state) {
				// remove all perceptions when simulation runs
				environment.perceptionGrid.setTo(environment.transformationGrid);
				super.step(state);
			}
		};
		display.setScale(2.3);
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);

		display.attach(perceptionPortrayal, "Human Perception");
		display.attach(environmentPortrayal, "Environment");

		// specify the backdrop color -- what gets painted behind the displays
		display.setBackdrop(Color.green);

		chart = new sim.util.media.chart.TimeSeriesChartGenerator();
		chart.setTitle("Statistiques globales");
		chart.setRangeAxisLabel("");
		chart.setDomainAxisLabel("Temps");
		chartFrame = chart.createFrame();
		chartFrame.setVisible(true);
		chartFrame.pack();
		c.registerFrame(chartFrame);
	}

	public void finish()
	{
		super.finish();

		chart.update(state.schedule.getSteps(), true);
		chart.repaint();
		chart.stopMovie();
	}

	public void quit()
	{
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;

		chart.update(state.schedule.getSteps(), true);
		chart.repaint();
		chart.stopMovie();
		if (chartFrame != null)	chartFrame.dispose();
		chartFrame = null;
	}
}
