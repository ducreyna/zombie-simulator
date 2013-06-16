import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.inspector.StableInt2D;
import sim.portrayal.simple.ImagePortrayal2D;


public class ImagePortrayal2DForHuman extends ImagePortrayal2D
{

	private Environment env;
	
	public ImagePortrayal2DForHuman(ImageIcon icon, Environment _env)
	{
		super(icon);
		env = _env;
	}
	
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
				env.drawPerception(location.getX(), location.getY(), Constants.HUMAN_PERCEPTION_MAX, _draw);
			}
			return super.handleMouseEvent(gui, manipulating, wrapper, event, fieldPortrayalDrawInfo, type);
		}
	}
}
