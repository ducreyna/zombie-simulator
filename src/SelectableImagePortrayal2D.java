import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;

public class SelectableImagePortrayal2D extends ImagePortrayal2D
{
	private Environment env;

	public SelectableImagePortrayal2D(ImageIcon _icon, Environment _env)
	{
		super(_icon);
		env = _env;
	}
	
	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		Element e = (Element) object;
		paint = new Color(0, 0, 0);
		super.draw(object, graphics, info);

		if (e.getDrawCircle())
		{
			double scale = 2.0;
			double offset = 0.0;
			Ellipse2D.Double preciseEllipse = new Ellipse2D.Double();
	
			Rectangle2D.Double draw = info.draw;
			final double width = draw.width*scale + offset;
			final double height = draw.height*scale + offset;
	
			graphics.setPaint(paint);
	
			if (info.precise)
			{
				if (preciseEllipse == null) preciseEllipse = new Ellipse2D.Double();    // could get reset because it's transient
				preciseEllipse.setFrame(info.draw.x - width/2.0, info.draw.y - height/2.0, width, height);
				graphics.draw(preciseEllipse);
				return;
			}
	
			final int x = (int)(draw.x - width / 2.0);
			final int y = (int)(draw.y - height / 2.0);
			int w = (int)(width);
			int h = (int)(height);
	
			graphics.drawOval(x,y,w,h);
		}
	}
	
	@Override
	public boolean handleMouseEvent(GUIState guistate, Manipulating2D manipulating, LocationWrapper wrapper, MouseEvent event, DrawInfo2D fieldPortrayalDrawInfo, int type)
	{
		synchronized (guistate.state.schedule)
		{
			if (type == SimplePortrayal2D.TYPE_HIT_OBJECT && event.getID() == MouseEvent.MOUSE_CLICKED && javax.swing.SwingUtilities.isMiddleMouseButton(event))
			{
				Element e = (Element) wrapper.getObject();
				if(e.getDrawCircle())
				{
					e.setDrawCircle(false);
					//System.out.println("object : show selection circle");
				}
				else
				{
					e.setDrawCircle(true);
					//System.out.println("object : show selection circle");
				}
				env.environmentUI.display.repaint();
			}
			return super.handleMouseEvent(guistate, manipulating, wrapper, event, fieldPortrayalDrawInfo, type);
		}
	}
}
