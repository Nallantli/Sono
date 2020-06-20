package ext;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;

import main.Main;
import main.base.Library;
import main.sono.Datum;
import main.sono.Function;

abstract class Paintable {
	protected Color color;

	public Paintable(Color color) {
		setColor(color);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void paint(Graphics g) {
		g.setColor(color);
	}

	public static class Rectangle extends Paintable {
		protected int x;
		protected int y;
		protected int width;
		protected int height;

		public Rectangle(Color color, int x, int y, int width, int height) {
			super(color);
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void setOrigin(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void setSize(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.fillRect(x, y, width, height);
		}
	}

	public static class Oval extends Rectangle {
		public Oval(Color color, int x, int y, int width, int height) {
			super(color, x, y, width, height);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.fillOval(x, y, width, height);
		}
	}

	public static class Line extends Paintable {
		private int x1;
		private int y1;
		private int x2;
		private int y2;

		public Line(Color color, int x1, int y1, int x2, int y2) {
			super(color);
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		public void setPoints(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawLine(x1, y1, x2, y2);
		}
	}
}

class GraphicsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private transient List<Paintable> buffer;

	public GraphicsPanel() {
		this.buffer = new ArrayList<>();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		List<Paintable> temp = new ArrayList<>();
		temp.addAll(buffer);
		for (Paintable component : temp) {
			component.paint(g);
		}
	}

	public boolean addBuffer(Paintable component) {
		if (!buffer.contains(component))
			return buffer.add(component);
		return false;
	}

	public boolean removeBuffer(Paintable component) {
		if (buffer.contains(component))
			return buffer.remove(component);
		return false;
	}
}

public class LIB_Graphics extends Library {
	public LIB_Graphics() {
		super();
		commands.put("LIB_Graphics.INIT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			String title = list.get(0).getString(trace);
			int width = list.get(1).getNumber(trace).intValue();
			int height = list.get(2).getNumber(trace).intValue();
			JFrame f = new JFrame(title);
			if (list.get(3).getType() == Datum.Type.FUNCTION) {
				Function close = list.get(3).getFunction(Datum.Type.ANY, trace);
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						close.execute(new ArrayList<>(), trace);
					}
				});
			} else {
				f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			}
			f.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.getGlobalOption("PATH") + "/res/icon.png"));
			f.setResizable(false);
			f.getContentPane().setPreferredSize(new Dimension(width, height));
			f.pack();
			f.setVisible(true);
			return new Datum((Object) f);
		});
		commands.put("LIB_Graphics.SHOW", (final Datum datum, final List<String> trace) -> {
			JFrame f = (JFrame) datum.getPointer(trace);
			f.setVisible(true);
			return new Datum();
		});
		commands.put("LIB_Graphics.HIDE", (final Datum datum, final List<String> trace) -> {
			JFrame f = (JFrame) datum.getPointer(trace);
			f.setVisible(false);
			return new Datum();
		});
		commands.put("LIB_Graphics.SETSIZE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			JFrame f = (JFrame) list.get(0).getPointer(trace);
			int width = list.get(1).getNumber(trace).intValue();
			int height = list.get(2).getNumber(trace).intValue();
			f.getContentPane().setPreferredSize(new Dimension(width, height));
			f.pack();
			return new Datum();
		});
		commands.put("LIB_Graphics.CLOSE", (final Datum datum, final List<String> trace) -> {
			JFrame f = (JFrame) datum.getPointer(trace);
			f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
			return new Datum();
		});
		commands.put("LIB_Graphics.GETGRAPHICS", (final Datum datum, final List<String> trace) -> {
			JFrame f = (JFrame) datum.getPointer(trace);
			GraphicsPanel gp = new GraphicsPanel();
			Dimension d = f.getSize();
			f.getContentPane().add("Center", gp);
			f.pack();
			f.setSize(d);
			return new Datum((Object) gp);
		});
		commands.put("LIB_Graphics.REPAINT", (final Datum datum, final List<String> trace) -> {
			JFrame f = (JFrame) datum.getPointer(trace);
			f.revalidate();
			f.repaint();
			return new Datum();
		});
		commands.put("LIB_Graphics.GRAPHICS.ADD", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			GraphicsPanel gp = (GraphicsPanel) list.get(0).getPointer(trace);
			Paintable paintable = (Paintable) list.get(1).getPointer(trace);
			boolean success = gp.addBuffer(paintable);
			return new Datum(BigDecimal.valueOf(success ? 1 : 0));
		});
		commands.put("LIB_Graphics.GRAPHICS.REMOVE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			GraphicsPanel gp = (GraphicsPanel) list.get(0).getPointer(trace);
			Paintable paintable = (Paintable) list.get(1).getPointer(trace);
			boolean success = gp.removeBuffer(paintable);
			return new Datum(BigDecimal.valueOf(success ? 1 : 0));
		});
		commands.put("LIB_Graphics.SHAPE.SETCOLOR", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable paintable = (Paintable) list.get(0).getPointer(trace);
			int r = list.get(1).getNumber(trace).intValue();
			int g = list.get(2).getNumber(trace).intValue();
			int b = list.get(3).getNumber(trace).intValue();
			int a = list.get(4).getNumber(trace).intValue();
			paintable.setColor(new Color(r, g, b, a));
			return new Datum();
		});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.INIT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			int r = list.get(0).getNumber(trace).intValue();
			int g = list.get(1).getNumber(trace).intValue();
			int b = list.get(2).getNumber(trace).intValue();
			int a = list.get(3).getNumber(trace).intValue();
			int x = list.get(4).getNumber(trace).intValue();
			int y = list.get(5).getNumber(trace).intValue();
			int width = list.get(6).getNumber(trace).intValue();
			int height = list.get(7).getNumber(trace).intValue();
			Paintable.Rectangle rectangle = new Paintable.Rectangle(new Color(r, g, b, a), x, y, width, height);
			return new Datum((Object) rectangle);
		});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.MOVE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Rectangle rectangle = (Paintable.Rectangle) list.get(0).getPointer(trace);
			int x = list.get(1).getNumber(trace).intValue();
			int y = list.get(2).getNumber(trace).intValue();
			rectangle.setOrigin(x, y);
			return new Datum();
		});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.SIZE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Rectangle rectangle = (Paintable.Rectangle) list.get(0).getPointer(trace);
			int width = list.get(1).getNumber(trace).intValue();
			int height = list.get(2).getNumber(trace).intValue();
			rectangle.setSize(width, height);
			return new Datum();
		});
		commands.put("LIB_Graphics.SHAPE.LINE.INIT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			int r = list.get(0).getNumber(trace).intValue();
			int g = list.get(1).getNumber(trace).intValue();
			int b = list.get(2).getNumber(trace).intValue();
			int a = list.get(3).getNumber(trace).intValue();
			int x1 = list.get(4).getNumber(trace).intValue();
			int y1 = list.get(5).getNumber(trace).intValue();
			int x2 = list.get(6).getNumber(trace).intValue();
			int y2 = list.get(7).getNumber(trace).intValue();
			Paintable.Line line = new Paintable.Line(new Color(r, g, b, a), x1, y1, x2, y2);
			return new Datum((Object) line);
		});
		commands.put("LIB_Graphics.SHAPE.LINE.MOVE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Line line = (Paintable.Line) list.get(0).getPointer(trace);
			int x1 = list.get(1).getNumber(trace).intValue();
			int y1 = list.get(2).getNumber(trace).intValue();
			int x2 = list.get(1).getNumber(trace).intValue();
			int y2 = list.get(2).getNumber(trace).intValue();
			line.setPoints(x1, y1, x2, y2);
			return new Datum();
		});
		commands.put("LIB_Graphics.SHAPE.OVAL.INIT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			int r = list.get(0).getNumber(trace).intValue();
			int g = list.get(1).getNumber(trace).intValue();
			int b = list.get(2).getNumber(trace).intValue();
			int a = list.get(3).getNumber(trace).intValue();
			int x = list.get(4).getNumber(trace).intValue();
			int y = list.get(5).getNumber(trace).intValue();
			int width = list.get(6).getNumber(trace).intValue();
			int height = list.get(7).getNumber(trace).intValue();
			Paintable.Oval oval = new Paintable.Oval(new Color(r, g, b, a), x, y, width, height);
			return new Datum((Object) oval);
		});
		commands.put("LIB_Graphics.SHAPE.OVAL.MOVE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Oval oval = (Paintable.Oval) list.get(0).getPointer(trace);
			int x = list.get(1).getNumber(trace).intValue();
			int y = list.get(2).getNumber(trace).intValue();
			oval.setOrigin(x, y);
			return new Datum();
		});
		commands.put("LIB_Graphics.SHAPE.OVAL.SIZE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Oval oval = (Paintable.Oval) list.get(0).getPointer(trace);
			int width = list.get(1).getNumber(trace).intValue();
			int height = list.get(2).getNumber(trace).intValue();
			oval.setSize(width, height);
			return new Datum();
		});
	}
}