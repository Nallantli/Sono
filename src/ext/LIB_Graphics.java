package ext;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.awt.event.WindowEvent;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;

import main.SonoWrapper;
import main.base.Library;
import main.sono.Datum;
import main.sono.Function;
import main.sono.Interpreter;

abstract class Paintable {
	protected Color fill;
	protected Color outline;

	public Paintable(final Color fill, final Color outline) {
		setFill(fill);
		setOutline(outline);
	}

	public void setFill(final Color fill) {
		this.fill = fill;
	}

	public void setOutline(final Color outline) {
		this.outline = outline;
	}

	public abstract void paint(final Graphics g);

	public static class Rectangle extends Paintable {
		protected int x;
		protected int y;
		protected int width;
		protected int height;

		public Rectangle(final Color fill, final Color outline, final int x, final int y, final int width,
				final int height) {
			super(fill, outline);
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void setOrigin(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public void setSize(final int width, final int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public void paint(final Graphics g) {
			if (fill != null) {
				g.setColor(fill);
				g.fillRect(x, y, width, height);
			}
			if (outline != null) {
				g.setColor(outline);
				g.drawRect(x, y, width, height);
			}
		}
	}

	public static class Oval extends Paintable {
		protected int x;
		protected int y;
		protected int width;
		protected int height;

		public Oval(final Color fill, final Color outline, final int x, final int y, final int width,
				final int height) {
			super(fill, outline);
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void setOrigin(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public void setSize(final int width, final int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public void paint(final Graphics g) {
			if (fill != null) {
				g.setColor(fill);
				g.fillOval(x, y, width, height);
			}
			if (outline != null) {
				g.setColor(outline);
				g.drawOval(x, y, width, height);
			}
		}
	}

	public static class Line extends Paintable {
		private int x1;
		private int y1;
		private int x2;
		private int y2;

		public Line(final Color fill, final int x1, final int y1, final int x2, final int y2) {
			super(fill, null);
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		public void setPoints(final int x1, final int y1, final int x2, final int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void paint(final Graphics g) {
			g.setColor(fill);
			g.drawLine(x1, y1, x2, y2);
		}
	}

	public static class Text extends Paintable {
		private int x;
		private int y;
		private String text;
		private Font font = null;
		private final int align;

		public Text(final Color fill, final int x, final int y, final String text, final int align) {
			super(fill, null);
			this.x = x;
			this.y = y;
			this.text = text;
			this.align = align;
		}

		public void setFont(final Font font) {
			this.font = font;
		}

		public void setText(final String text) {
			this.text = text;
		}

		public void setOrigin(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void paint(final Graphics g) {
			g.setColor(fill);
			if (font != null)
				g.setFont(font);
			final FontMetrics metrics = g.getFontMetrics(g.getFont());
			int temp_x = x;
			switch (align) {
				case 1:
					temp_x = x - metrics.stringWidth(text) / 2;
					break;
				case 2:
					temp_x = x - metrics.stringWidth(text);
					break;
				default:
					break;
			}
			g.drawString(text, temp_x, y);
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
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final List<Paintable> temp = new ArrayList<>();
		temp.addAll(buffer);
		for (final Paintable component : temp) {
			component.paint(g);
		}
	}

	public boolean addBuffer(final Paintable component) {
		if (!buffer.contains(component))
			return buffer.add(component);
		return false;
	}

	public boolean removeBuffer(final Paintable component) {
		if (buffer.contains(component))
			return buffer.remove(component);
		return false;
	}
}

class WindowFunctions extends JFrame {
	private static final long serialVersionUID = 1L;

	private Function onMouseMoved = null;
	private Function onMouseDragged = null;
	private Function onMouseReleased = null;
	private Function onMousePressed = null;
	private Function onMouseExited = null;
	private Function onMouseEntered = null;
	private Function onMouseClicked = null;
	private Function onKeyPressed = null;
	private Function onKeyReleased = null;
	private Function onKeyTyped = null;

	public WindowFunctions(final String title) {
		super(title);
		getContentPane().addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (onMouseReleased != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseReleased.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (onMousePressed != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMousePressed.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (onMouseExited != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseExited.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				if (onMouseEntered != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseEntered.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (onMouseClicked != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseClicked.execute(params, new ArrayList<>());
				}
			}
		});
		getContentPane().addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(final MouseEvent e) {
				if (onMouseMoved != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseMoved.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (onMouseDragged != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseDragged.execute(params, new ArrayList<>());
				}
			}
		});
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (onKeyTyped != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					onKeyTyped.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				if (onKeyPressed != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					onKeyPressed.execute(params, new ArrayList<>());
				}
			}

			@Override
			public void keyReleased(final KeyEvent e) {
				if (onKeyReleased != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					onKeyReleased.execute(params, new ArrayList<>());
				}
			}
		});
	}

	public void setOnMouseMoved(final Function onMouseMoved) {
		this.onMouseMoved = onMouseMoved;
	}

	public void setOnMouseDragged(final Function onMouseDragged) {
		this.onMouseDragged = onMouseDragged;
	}

	public void setOnMouseReleased(final Function onMouseReleased) {
		this.onMouseReleased = onMouseReleased;
	}

	public void setOnMousePressed(final Function onMousePressed) {
		this.onMousePressed = onMousePressed;
	}

	public void setOnMouseExited(final Function onMouseExited) {
		this.onMouseExited = onMouseExited;
	}

	public void setOnMouseEntered(final Function onMouseEntered) {
		this.onMouseEntered = onMouseEntered;
	}

	public void setOnMouseClicked(final Function onMouseClicked) {
		this.onMouseClicked = onMouseClicked;
	}

	public void setOnKeyPressed(final Function onKeyPressed) {
		this.onKeyPressed = onKeyPressed;
	}

	public void setOnKeyReleased(final Function onKeyReleased) {
		this.onKeyReleased = onKeyReleased;
	}

	public void setOnKeyTyped(final Function onKeyTyped) {
		this.onKeyTyped = onKeyTyped;
	}
}

public class LIB_Graphics extends Library {
	public LIB_Graphics() {
		super();
		commands.put("LIB_Graphics.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					if (SonoWrapper.getGlobalOption("GRAPHICS").equals("FALSE"))
						throw error("Graphics permissions are disabled for this interpreter.", trace);
					final Datum[] list = datum.getVector(trace);
					final String title = list[0].getString(trace);
					final int width = (int) list[1].getNumber(trace);
					final int height = (int) list[2].getNumber(trace);
					final WindowFunctions f = new WindowFunctions(title);
					if (list[3].getType() == Datum.Type.FUNCTION) {
						final Function close = list[3].getFunction(Datum.Type.ANY, trace);
						f.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(final WindowEvent e) {
								close.execute(null, trace);
							}
						});
					} else {
						f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					}
					f.setIconImage(Toolkit.getDefaultToolkit()
							.getImage(SonoWrapper.getGlobalOption("PATH") + "/res/icon.png"));
					f.setResizable(false);
					f.getContentPane().setPreferredSize(new Dimension(width, height));
					f.pack();
					f.setVisible(true);
					return new Datum((Object) f);
				});
		commands.put("LIB_Graphics.SHOW",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final WindowFunctions f = (WindowFunctions) datum.getPointer(trace);
					f.setVisible(true);
					return new Datum();
				});
		commands.put("LIB_Graphics.HIDE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final WindowFunctions f = (WindowFunctions) datum.getPointer(trace);
					f.setVisible(false);
					return new Datum();
				});
		commands.put("LIB_Graphics.FONT.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final String fontName = list[0].getString(trace);
					final String styleRaw = list[1].getString(trace);
					final int size = (int) list[2].getNumber(trace);
					final String[] split = styleRaw.split("\\s");
					int style = Font.PLAIN;
					for (final String s : split) {
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
					final Font font = new Font(fontName, style, size);
					return new Datum((Object) font);
				});
		commands.put("LIB_Graphics.ADDMOUSELISTENER",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final WindowFunctions f = (WindowFunctions) list[0].getPointer(trace);
					final int id = (int) list[1].getNumber(trace);
					final Function function = list[2].getFunction(Datum.Type.ANY, trace);
					switch (id) {
						case 0:
							f.setOnMouseMoved(function);
							break;
						case 1:
							f.setOnMouseDragged(function);
							break;
						case 2:
							f.setOnMouseReleased(function);
							break;
						case 3:
							f.setOnMousePressed(function);
							break;
						case 4:
							f.setOnMouseExited(function);
							break;
						case 5:
							f.setOnMouseEntered(function);
							break;
						case 6:
							f.setOnMouseClicked(function);
							break;
						default:
							throw error("Unknown mouse event ID <" + id + ">", trace);
					}
					return new Datum();
				});
		commands.put("LIB_Graphics.ADDKEYLISTENER",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final WindowFunctions f = (WindowFunctions) list[0].getPointer(trace);
					final int id = (int) list[1].getNumber(trace);
					final Function function = list[2].getFunction(Datum.Type.ANY, trace);
					switch (id) {
						case 0:
							f.setOnKeyPressed(function);
							break;
						case 1:
							f.setOnKeyReleased(function);
							break;
						case 2:
							f.setOnKeyTyped(function);
							break;
						default:
							throw error("Unknown key event ID <" + id + ">", trace);
					}
					return new Datum();
				});
		commands.put("LIB_Graphics.SETSIZE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final WindowFunctions f = (WindowFunctions) list[0].getPointer(trace);
					final int width = (int) list[1].getNumber(trace);
					final int height = (int) list[2].getNumber(trace);
					f.getContentPane().setPreferredSize(new Dimension(width, height));
					f.pack();
					return new Datum();
				});
		commands.put("LIB_Graphics.CLOSE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final WindowFunctions f = (WindowFunctions) datum.getPointer(trace);
					f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
					return new Datum();
				});
		commands.put("LIB_Graphics.GETGRAPHICS",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final WindowFunctions f = (WindowFunctions) datum.getPointer(trace);
					final GraphicsPanel gp = new GraphicsPanel();
					final Dimension d = f.getSize();
					f.getContentPane().add("Center", gp);
					f.pack();
					f.setSize(d);
					return new Datum((Object) gp);
				});
		commands.put("LIB_Graphics.REPAINT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final WindowFunctions f = (WindowFunctions) datum.getPointer(trace);
					f.revalidate();
					f.repaint();
					return new Datum();
				});
		commands.put("LIB_Graphics.GRAPHICS.ADD",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final GraphicsPanel gp = (GraphicsPanel) list[0].getPointer(trace);
					final Paintable paintable = (Paintable) list[1].getPointer(trace);
					final boolean success = gp.addBuffer(paintable);
					return new Datum(success ? 1 : 0);
				});
		commands.put("LIB_Graphics.GRAPHICS.REMOVE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final GraphicsPanel gp = (GraphicsPanel) list[0].getPointer(trace);
					final Paintable paintable = (Paintable) list[1].getPointer(trace);
					final boolean success = gp.removeBuffer(paintable);
					return new Datum(success ? 1 : 0);
				});
		commands.put("LIB_Graphics.COLOR.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final int r = (int) list[0].getNumber(trace);
					final int g = (int) list[1].getNumber(trace);
					final int b = (int) list[2].getNumber(trace);
					final int a = (int) list[3].getNumber(trace);
					final Color c = new Color(r, g, b, a);
					return new Datum((Object) c);
				});
		commands.put("LIB_Graphics.SHAPE.SETFILL",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable paintable = (Paintable) list[0].getPointer(trace);
					final Color color = (Color) list[1].getPointer(trace);
					paintable.setFill(color);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.SETOUTLINE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable paintable = (Paintable) list[0].getPointer(trace);
					final Color color = (Color) list[1].getPointer(trace);
					paintable.setOutline(color);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					Color fill = null;
					Color outline = null;
					if (list[0].getType() != Datum.Type.NULL) {
						fill = (Color) list[0].getPointer(trace);
					}
					if (list[1].getType() != Datum.Type.NULL) {
						outline = (Color) list[1].getPointer(trace);
					}
					final int x = (int) list[2].getNumber(trace);
					final int y = (int) list[3].getNumber(trace);
					final int width = (int) list[4].getNumber(trace);
					final int height = (int) list[5].getNumber(trace);
					final Paintable.Rectangle rectangle = new Paintable.Rectangle(fill, outline, x, y, width, height);
					return new Datum((Object) rectangle);
				});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.MOVE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Rectangle rectangle = (Paintable.Rectangle) list[0].getPointer(trace);
					final int x = (int) list[1].getNumber(trace);
					final int y = (int) list[2].getNumber(trace);
					rectangle.setOrigin(x, y);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.SIZE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Rectangle rectangle = (Paintable.Rectangle) list[0].getPointer(trace);
					final int width = (int) list[1].getNumber(trace);
					final int height = (int) list[2].getNumber(trace);
					rectangle.setSize(width, height);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.LINE.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Color fill = (Color) list[0].getPointer(trace);
					final int x1 = (int) list[1].getNumber(trace);
					final int y1 = (int) list[2].getNumber(trace);
					final int x2 = (int) list[3].getNumber(trace);
					final int y2 = (int) list[4].getNumber(trace);
					final Paintable.Line line = new Paintable.Line(fill, x1, y1, x2, y2);
					return new Datum((Object) line);
				});
		commands.put("LIB_Graphics.SHAPE.LINE.MOVE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Line line = (Paintable.Line) list[0].getPointer(trace);
					final int x1 = (int) list[1].getNumber(trace);
					final int y1 = (int) list[2].getNumber(trace);
					final int x2 = (int) list[3].getNumber(trace);
					final int y2 = (int) list[4].getNumber(trace);
					line.setPoints(x1, y1, x2, y2);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.OVAL.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					Color fill = null;
					Color outline = null;
					if (list[0].getType() != Datum.Type.NULL) {
						fill = (Color) list[0].getPointer(trace);
					}
					if (list[1].getType() != Datum.Type.NULL) {
						outline = (Color) list[1].getPointer(trace);
					}
					final int x = (int) list[2].getNumber(trace);
					final int y = (int) list[3].getNumber(trace);
					final int width = (int) list[4].getNumber(trace);
					final int height = (int) list[5].getNumber(trace);
					final Paintable.Oval oval = new Paintable.Oval(fill, outline, x, y, width, height);
					return new Datum((Object) oval);
				});
		commands.put("LIB_Graphics.SHAPE.OVAL.MOVE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Oval oval = (Paintable.Oval) list[0].getPointer(trace);
					final int x = (int) list[1].getNumber(trace);
					final int y = (int) list[2].getNumber(trace);
					oval.setOrigin(x, y);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.OVAL.SIZE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Oval oval = (Paintable.Oval) list[0].getPointer(trace);
					final int width = (int) list[1].getNumber(trace);
					final int height = (int) list[2].getNumber(trace);
					oval.setSize(width, height);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.INIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Color fill = (Color) list[0].getPointer(trace);
					final int x = (int) list[1].getNumber(trace);
					final int y = (int) list[2].getNumber(trace);
					final String string = list[3].getString(trace);
					final int align = (int) list[4].getNumber(trace);
					final Paintable.Text text = new Paintable.Text(fill, x, y, string, align);
					return new Datum((Object) text);
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.SETFONT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Text text = (Paintable.Text) list[0].getPointer(trace);
					final Font font = (Font) list[1].getPointer(trace);
					text.setFont(font);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.SETSTRING",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Text text = (Paintable.Text) list[0].getPointer(trace);
					final String string = list[1].getString(trace);
					text.setText(string);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.MOVE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(trace);
					final Paintable.Text text = (Paintable.Text) list[0].getPointer(trace);
					final int x = (int) list[1].getNumber(trace);
					final int y = (int) list[2].getNumber(trace);
					text.setOrigin(x, y);
					return new Datum();
				});
	}
}