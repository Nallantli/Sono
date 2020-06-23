package ext;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
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

	public static class Text extends Paintable {
		private int x;
		private int y;
		private String text;
		private Font font = null;

		public Text(Color color, int x, int y, String text) {
			super(color);
			this.x = x;
			this.y = y;
			this.text = text;
		}

		public void setFont(Font font) {
			this.font = font;
		}

		public void setOrigin(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (font != null)
				g.setFont(font);
			g.drawString(text, x, y);
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
	private Function onMouseMoved = null;
	private Function onMouseDragged = null;
	private Function onMouseReleased = null;
	private Function onMousePressed = null;
	private Function onMouseExited = null;
	private Function onMouseEntered = null;
	private Function onMouseClicked = null;

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
			f.addMouseListener(new MouseInputListener() {
				@Override
				public void mouseMoved(MouseEvent e) {
					if (onMouseMoved != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMouseMoved.execute(params, trace);
					}
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (onMouseDragged != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMouseDragged.execute(params, trace);
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (onMouseReleased != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMouseReleased.execute(params, trace);
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (onMousePressed != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMousePressed.execute(params, trace);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if (onMouseExited != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMouseExited.execute(params, trace);
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					if (onMouseEntered != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMouseEntered.execute(params, trace);
					}
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if (onMouseClicked != null) {
						List<Datum> params = new ArrayList<>();
						params.add(new Datum(BigDecimal.valueOf(e.getButton())));
						params.add(new Datum(BigDecimal.valueOf(e.getClickCount())));
						params.add(new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())));
						params.add(new Datum(BigDecimal.valueOf(e.getX())));
						params.add(new Datum(BigDecimal.valueOf(e.getY())));
						params.add(new Datum(BigDecimal.valueOf(e.getXOnScreen())));
						params.add(new Datum(BigDecimal.valueOf(e.getYOnScreen())));
						onMouseClicked.execute(params, trace);
					}
				}
			});
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
		commands.put("LIB_Graphics.FONT.INIT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			String fontName = list.get(0).getString(trace);
			String styleRaw = list.get(1).getString(trace);
			int size = list.get(2).getNumber(trace).intValue();
			String[] split = styleRaw.split("\\s");
			int style = Font.PLAIN;
			for (String s : split) {
				switch (s) {
					case "Bold":
						style |= Font.BOLD;
						break;
					case "Italic":
						style |= Font.ITALIC;
						break;
					case "Plain":
						style |= Font.PLAIN;
						break;
				}
			}
			Font font = new Font(fontName, style, size);
			return new Datum((Object)font);
		});
		commands.put("LIB_Graphics.ADDMOUSELISTENER", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			int id = list.get(0).getNumber(trace).intValue();
			Function function = list.get(1).getFunction(Datum.Type.ANY, trace);
			switch (id) {
				case 0:
					onMouseMoved = function;
					break;
				case 1:
					onMouseDragged = function;
					break;
				case 2:
					onMouseReleased = function;
					break;
				case 3:
					onMousePressed = function;
					break;
				case 4:
					onMouseExited = function;
					break;
				case 5:
					onMouseEntered = function;
					break;
				case 6:
					onMouseClicked = function;
					break;
				default:
					throw error("Unknown mouse event ID <" + id + ">", trace);
			}
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
		commands.put("LIB_Graphics.SHAPE.TEXT.INIT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			int r = list.get(0).getNumber(trace).intValue();
			int g = list.get(1).getNumber(trace).intValue();
			int b = list.get(2).getNumber(trace).intValue();
			int a = list.get(3).getNumber(trace).intValue();
			int x = list.get(4).getNumber(trace).intValue();
			int y = list.get(5).getNumber(trace).intValue();
			String string = list.get(6).getString(trace);
			Paintable.Text text = new Paintable.Text(new Color(r, g, b, a), x, y, string);
			return new Datum((Object) text);
		});
		commands.put("LIB_Graphics.SHAPE.TEXT.SETFONT", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Text text = (Paintable.Text) list.get(0).getPointer(trace);
			Font font = (Font) list.get(1).getPointer(trace);
			text.setFont(font);
			return new Datum();
		});
		commands.put("LIB_Graphics.SHAPE.TEXT.MOVE", (final Datum datum, final List<String> trace) -> {
			List<Datum> list = datum.getVector(trace);
			Paintable.Text text = (Paintable.Text) list.get(0).getPointer(trace);
			int x = list.get(1).getNumber(trace).intValue();
			int y = list.get(2).getNumber(trace).intValue();
			text.setOrigin(x, y);
			return new Datum();
		});
	}
}