package ext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import main.SonoWrapper;
import main.base.Library;
import main.sono.Datum;
import main.sono.Function;
import main.sono.Interpreter;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

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
		private String textValue;
		private Font font = null;
		private final int align;

		public Text(final Color fill, final int x, final int y, final String textValue, final int align) {
			super(fill, null);
			this.x = x;
			this.y = y;
			this.textValue = textValue;
			this.align = align;
		}

		public void setFont(final Font font) {
			this.font = font;
		}

		public void setText(final String textValue) {
			this.textValue = textValue;
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
			int tempX = x;
			switch (align) {
				case 1:
					tempX = x - metrics.stringWidth(textValue) / 2;
					break;
				case 2:
					tempX = x - metrics.stringWidth(textValue);
					break;
				default:
					break;
			}
			g.drawString(textValue, tempX, y);
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

	private transient Function onMouseMoved = null;
	private transient Function onMouseDragged = null;
	private transient Function onMouseReleased = null;
	private transient Function onMousePressed = null;
	private transient Function onMouseExited = null;
	private transient Function onMouseEntered = null;
	private transient Function onMouseClicked = null;
	private transient Function onKeyPressed = null;
	private transient Function onKeyReleased = null;
	private transient Function onKeyTyped = null;

	public WindowFunctions(final String title) {
		super(title);
		getContentPane().addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (onMouseReleased != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseReleased.execute(params, null);
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (onMousePressed != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMousePressed.execute(params, null);
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (onMouseExited != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseExited.execute(params, null);
				}
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				if (onMouseEntered != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseEntered.execute(params, null);
				}
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (onMouseClicked != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseClicked.execute(params, null);
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
					onMouseMoved.execute(params, null);
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (onMouseDragged != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					onMouseDragged.execute(params, null);
				}
			}
		});
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (onKeyTyped != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					onKeyTyped.execute(params, null);
				}
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				if (onKeyPressed != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					onKeyPressed.execute(params, null);
				}
			}

			@Override
			public void keyReleased(final KeyEvent e) {
				if (onKeyReleased != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					onKeyReleased.execute(params, null);
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

		if (!SonoWrapper.getGlobalOption("GRAPHICS").equals("FALSE")) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}

		commands.put("LIB_Graphics.INIT", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			if (SonoWrapper.getGlobalOption("GRAPHICS").equals("FALSE"))
				throw error("Graphics permissions are disabled for this interpreter.", line);
			final Datum[] list = datum.getVector(line);
			final String title = list[0].getString(line);
			final int width = (int) list[1].getNumber(line);
			final int height = (int) list[2].getNumber(line);
			final WindowFunctions f = new WindowFunctions(title);
			if (list[3].getType() == Datum.Type.FUNCTION) {
				final Function close = list[3].getFunction(Datum.Type.ANY, line);
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(final WindowEvent e) {
						close.execute(null, line);
					}
				});
			} else {
				f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			}
			f.setIconImage(Toolkit.getDefaultToolkit().getImage(SonoWrapper.getGlobalOption("PATH") + "/res/sono.png"));
			f.setResizable(false);
			f.getContentPane().setPreferredSize(new Dimension(width, height));
			f.pack();
			f.setVisible(true);
			return new Datum(new Datum[] { new Datum((Object) f), new Datum((Object) f.getContentPane()) });
		});
		commands.put("LIB_Graphics.SHOW", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final WindowFunctions f = (WindowFunctions) datum.getPointer(line);
			f.setVisible(true);
			return new Datum();
		});
		commands.put("LIB_Graphics.HIDE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final WindowFunctions f = (WindowFunctions) datum.getPointer(line);
			f.setVisible(false);
			return new Datum();
		});
		commands.put("LIB_Graphics.FONT.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final String fontName = list[0].getString(line);
					final String styleRaw = list[1].getString(line);
					final int size = (int) list[2].getNumber(line);
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
							default:
								break;
						}
					}
					final Font font = new Font(fontName, style, size);
					return new Datum((Object) font);
				});
		commands.put("LIB_Graphics.ADDMOUSELISTENER",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final WindowFunctions f = (WindowFunctions) list[0].getPointer(line);
					final int id = (int) list[1].getNumber(line);
					final Function function = list[2].getFunction(Datum.Type.ANY, line);
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
							throw error("Unknown mouse event ID <" + id + ">", line);
					}
					return new Datum();
				});
		commands.put("LIB_Graphics.ADDKEYLISTENER",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final WindowFunctions f = (WindowFunctions) list[0].getPointer(line);
					final int id = (int) list[1].getNumber(line);
					final Function function = list[2].getFunction(Datum.Type.ANY, line);
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
							throw error("Unknown key event ID <" + id + ">", line);
					}
					return new Datum();
				});
		commands.put("LIB_Graphics.SETSIZE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final Datum[] list = datum.getVector(line);
			final WindowFunctions f = (WindowFunctions) list[0].getPointer(line);
			final int width = (int) list[1].getNumber(line);
			final int height = (int) list[2].getNumber(line);
			f.getContentPane().setPreferredSize(new Dimension(width, height));
			f.pack();
			return new Datum();
		});
		commands.put("LIB_Graphics.CLOSE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final WindowFunctions f = (WindowFunctions) datum.getPointer(line);
			f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
			return new Datum();
		});
		commands.put("LIB_Graphics.GRAPHICS.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final GraphicsPanel gp = new GraphicsPanel();
					return new Datum((Object) gp);
				});
		commands.put("LIB_Graphics.REPAINT", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final WindowFunctions f = (WindowFunctions) datum.getPointer(line);
			f.revalidate();
			f.repaint();
			return new Datum();
		});
		commands.put("LIB_Graphics.GRAPHICS.ADD",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final GraphicsPanel gp = (GraphicsPanel) list[0].getPointer(line);
					final Paintable paintable = (Paintable) list[1].getPointer(line);
					final boolean success = gp.addBuffer(paintable);
					return new Datum(success ? 1 : 0);
				});
		commands.put("LIB_Graphics.GRAPHICS.REMOVE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final GraphicsPanel gp = (GraphicsPanel) list[0].getPointer(line);
					final Paintable paintable = (Paintable) list[1].getPointer(line);
					final boolean success = gp.removeBuffer(paintable);
					return new Datum(success ? 1 : 0);
				});
		commands.put("LIB_Graphics.COLOR.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final int r = (int) list[0].getNumber(line);
					final int g = (int) list[1].getNumber(line);
					final int b = (int) list[2].getNumber(line);
					final int a = (int) list[3].getNumber(line);
					final Color c = new Color(r, g, b, a);
					return new Datum((Object) c);
				});
		commands.put("LIB_Graphics.SHAPE.SETFILL",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable paintable = (Paintable) list[0].getPointer(line);
					final Color color = (Color) list[1].getPointer(line);
					paintable.setFill(color);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.SETOUTLINE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable paintable = (Paintable) list[0].getPointer(line);
					final Color color = (Color) list[1].getPointer(line);
					paintable.setOutline(color);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					Color fill = null;
					Color outline = null;
					if (list[0].getType() != Datum.Type.NULL) {
						fill = (Color) list[0].getPointer(line);
					}
					if (list[1].getType() != Datum.Type.NULL) {
						outline = (Color) list[1].getPointer(line);
					}
					final int x = (int) list[2].getNumber(line);
					final int y = (int) list[3].getNumber(line);
					final int width = (int) list[4].getNumber(line);
					final int height = (int) list[5].getNumber(line);
					final Paintable.Rectangle rectangle = new Paintable.Rectangle(fill, outline, x, y, width, height);
					return new Datum((Object) rectangle);
				});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.MOVE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Rectangle rectangle = (Paintable.Rectangle) list[0].getPointer(line);
					final int x = (int) list[1].getNumber(line);
					final int y = (int) list[2].getNumber(line);
					rectangle.setOrigin(x, y);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.RECTANGLE.SIZE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Rectangle rectangle = (Paintable.Rectangle) list[0].getPointer(line);
					final int width = (int) list[1].getNumber(line);
					final int height = (int) list[2].getNumber(line);
					rectangle.setSize(width, height);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.LINE.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Color fill = (Color) list[0].getPointer(line);
					final int x1 = (int) list[1].getNumber(line);
					final int y1 = (int) list[2].getNumber(line);
					final int x2 = (int) list[3].getNumber(line);
					final int y2 = (int) list[4].getNumber(line);
					final Paintable.Line lineshape = new Paintable.Line(fill, x1, y1, x2, y2);
					return new Datum((Object) lineshape);
				});
		commands.put("LIB_Graphics.SHAPE.LINE.MOVE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Line lineshape = (Paintable.Line) list[0].getPointer(line);
					final int x1 = (int) list[1].getNumber(line);
					final int y1 = (int) list[2].getNumber(line);
					final int x2 = (int) list[3].getNumber(line);
					final int y2 = (int) list[4].getNumber(line);
					lineshape.setPoints(x1, y1, x2, y2);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.OVAL.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					Color fill = null;
					Color outline = null;
					if (list[0].getType() != Datum.Type.NULL) {
						fill = (Color) list[0].getPointer(line);
					}
					if (list[1].getType() != Datum.Type.NULL) {
						outline = (Color) list[1].getPointer(line);
					}
					final int x = (int) list[2].getNumber(line);
					final int y = (int) list[3].getNumber(line);
					final int width = (int) list[4].getNumber(line);
					final int height = (int) list[5].getNumber(line);
					final Paintable.Oval oval = new Paintable.Oval(fill, outline, x, y, width, height);
					return new Datum((Object) oval);
				});
		commands.put("LIB_Graphics.SHAPE.OVAL.MOVE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Oval oval = (Paintable.Oval) list[0].getPointer(line);
					final int x = (int) list[1].getNumber(line);
					final int y = (int) list[2].getNumber(line);
					oval.setOrigin(x, y);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.OVAL.SIZE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Oval oval = (Paintable.Oval) list[0].getPointer(line);
					final int width = (int) list[1].getNumber(line);
					final int height = (int) list[2].getNumber(line);
					oval.setSize(width, height);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Color fill = (Color) list[0].getPointer(line);
					final int x = (int) list[1].getNumber(line);
					final int y = (int) list[2].getNumber(line);
					final String string = list[3].getString(line);
					final int align = (int) list[4].getNumber(line);
					final Paintable.Text text = new Paintable.Text(fill, x, y, string, align);
					return new Datum((Object) text);
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.SETFONT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Text text = (Paintable.Text) list[0].getPointer(line);
					final Font font = (Font) list[1].getPointer(line);
					text.setFont(font);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.SETSTRING",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Text text = (Paintable.Text) list[0].getPointer(line);
					final String string = list[1].getString(line);
					text.setText(string);
					return new Datum();
				});
		commands.put("LIB_Graphics.SHAPE.TEXT.MOVE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final Paintable.Text text = (Paintable.Text) list[0].getPointer(line);
					final int x = (int) list[1].getNumber(line);
					final int y = (int) list[2].getNumber(line);
					text.setOrigin(x, y);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.ADD",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JComponent frame = ((JComponent) list[0].getPointer(line));
					final JComponent child = (JComponent) list[1].getPointer(line);
					if (list.length > 2)
						frame.add(child, list[2].getString(line));
					else
						frame.add(child);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.SETENABLE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final AbstractButton component = (AbstractButton) list[0].getPointer(line);
					final boolean enabled = list[1].getBool(line);
					component.setEnabled(enabled);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.CLICK",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final AbstractButton component = (AbstractButton) datum.getPointer(line);
					component.doClick();
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.ACTION",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final AbstractButton component = ((AbstractButton) list[0].getPointer(line));
					final Function function = list[1].getFunction(Datum.Type.ANY, line);
					component.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							function.execute(new Datum[] { new Datum(component.isSelected() ? 1 : 0) }, line);
						}
					});
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.MENU.BAR.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JMenuBar mb = new JMenuBar();
					return new Datum((Object) mb);
				});
		commands.put("LIB_Graphics.COMPONENT.MENU.ITEM.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JMenu menu = new JMenu(datum.getString(line));
					return new Datum((Object) menu);
				});
		commands.put("LIB_Graphics.COMPONENT.MENU.LABEL.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JMenuItem item = new JMenuItem(datum.getString(line));
					return new Datum((Object) item);
				});
		commands.put("LIB_Graphics.COMPONENT.TEXT.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JLabel component = new JLabel(datum.getString(line));
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.TEXT.ALIGN",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JLabel component = (JLabel) list[0].getPointer(line);
					final int alignmentX = (int) list[1].getNumber(line);
					final int alignmentY = (int) list[2].getNumber(line);
					component.setHorizontalAlignment(alignmentX);
					component.setVerticalAlignment(alignmentY);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.TEXT.SETTEXT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JLabel component = (JLabel) list[0].getPointer(line);
					final String label = list[1].getString(line);
					component.setText(label);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.BUTTON.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JButton component = new JButton(datum.getString(line));
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.BUTTON.SETTEXT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JButton component = (JButton) list[0].getPointer(line);
					final String label = list[1].getString(line);
					component.setText(label);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.CHECKBOX.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JCheckBox component = new JCheckBox(datum.getString(line));
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.CHECKBOX.SETTEXT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JCheckBox component = (JCheckBox) list[0].getPointer(line);
					final String label = list[1].getString(line);
					component.setText(label);
					return new Datum();
				});
		commands.put("LIB_Graphics.COMPONENT.PANEL.BORDER.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JPanel component = new JPanel();
					component.setLayout(new BorderLayout());
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.PANEL.BLOCK.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JPanel component = new JPanel();
					component.setLayout(new BoxLayout(component, BoxLayout.PAGE_AXIS));
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.PANEL.INLINE.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JPanel component = new JPanel();
					component.setLayout(new FlowLayout());
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.PANEL.TABLE.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JPanel component = new JPanel();
					component.setLayout(new GridLayout((int) list[0].getNumber(line), (int) list[1].getNumber(line)));
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.PANEL.SPLIT.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					int orientation = JSplitPane.VERTICAL_SPLIT;
					if (datum.getString(line).equals("Horizontal"))
						orientation = JSplitPane.HORIZONTAL_SPLIT;
					else if (datum.getString(line).equals("Vertical"))
						orientation = JSplitPane.VERTICAL_SPLIT;
					else
						throw new SonoRuntimeException(
								"Component.Panel.Split can only be initialized with \"Horizontal\" or \"Vertical\"",
								line);
					final JSplitPane component = new JSplitPane(orientation);
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.TABPANE.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final JTabbedPane component = new JTabbedPane();
					return new Datum((Object) component);
				});
		commands.put("LIB_Graphics.COMPONENT.TABPANE.ADD",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final Datum[] list = datum.getVector(line);
					final JTabbedPane frame = ((JTabbedPane) list[0].getPointer(line));
					final JComponent child = (JComponent) list[1].getPointer(line);
					final String label = list[2].getString(line);
					frame.addTab(label, child);
					return new Datum();
				});
	}
}