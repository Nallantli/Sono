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
					try {
						onMouseReleased.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (onMousePressed != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					try {
						onMousePressed.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (onMouseExited != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					try {
						onMouseExited.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				if (onMouseEntered != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					try {
						onMouseEntered.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (onMouseClicked != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					try {
						onMouseClicked.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
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
					try {
						onMouseMoved.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (onMouseDragged != null) {
					final Datum[] params = new Datum[] { new Datum(e.getButton()), new Datum(e.getClickCount()),
							new Datum(MouseEvent.getMouseModifiersText(e.getModifiersEx())), new Datum(e.getX()),
							new Datum(e.getY()), new Datum(e.getXOnScreen()), new Datum(e.getYOnScreen()) };
					try {
						onMouseDragged.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}
		});
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (onKeyTyped != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					try {
						onKeyTyped.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				if (onKeyPressed != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					try {
						onKeyPressed.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}

			@Override
			public void keyReleased(final KeyEvent e) {
				if (onKeyReleased != null) {
					final Datum[] params = new Datum[] { new Datum(e.getKeyChar()),
							new Datum(KeyEvent.getKeyText(e.getKeyCode())), new Datum(e.isActionKey() ? 1 : 0) };
					try {
						onKeyReleased.execute(params, null, null);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
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
	public LIB_Graphics(final Interpreter interpreter) {
		super(interpreter);
		if (SonoWrapper.getGlobalOption("WEB").equals("FALSE")) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}
	}

	public Datum INIT(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		if (SonoWrapper.getGlobalOption("WEB").equals("TRUE"))
			throw error("Graphics permissions are disabled for this interpreter.", line);
		final String title = data[0].getString(line, overrides);
		final int width = (int) data[1].getNumber(line, overrides);
		final int height = (int) data[2].getNumber(line, overrides);
		final WindowFunctions f = new WindowFunctions(title);
		if (data[3].getType() == Datum.Type.FUNCTION) {
			final Function close = data[3].getFunction(Datum.Type.ANY, line, overrides);
			f.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent e) {
					try {
						close.execute(null, line, overrides);
					} catch (final InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
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
	}

	public Datum SHOW(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		f.setVisible(true);
		return new Datum();
	}

	public Datum HIDE(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		f.setVisible(false);
		return new Datum();
	}

	public Datum FONT_INIT(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		final String fontName = data[0].getString(line, overrides);
		final String styleRaw = data[1].getString(line, overrides);
		final int size = (int) data[2].getNumber(line, overrides);
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
	}

	public Datum ADDMOUSELISTENER(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		final int id = (int) data[1].getNumber(line, overrides);
		final Function function = data[2].getFunction(Datum.Type.ANY, line, overrides);
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
	}

	public Datum ADDKEYLISTENER(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		final int id = (int) data[1].getNumber(line, overrides);
		final Function function = data[2].getFunction(Datum.Type.ANY, line, overrides);
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
	}

	public Datum SETSIZE(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		final int width = (int) data[1].getNumber(line, overrides);
		final int height = (int) data[2].getNumber(line, overrides);
		f.getContentPane().setPreferredSize(new Dimension(width, height));
		f.pack();
		return new Datum();
	}

	public Datum CLOSE(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
		return new Datum();
	}

	public Datum GRAPHICS_INIT(final Token line, final Object[] overrides) {
		final GraphicsPanel gp = new GraphicsPanel();
		return new Datum((Object) gp);
	}

	public Datum REPAINT(final Datum[] data, final Token line, final Object[] overrides) throws InterruptedException {
		final WindowFunctions f = (WindowFunctions) data[0].getPointer(line, overrides);
		f.revalidate();
		f.repaint();
		return new Datum();
	}

	public Datum GRAPHICS_ADD(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final GraphicsPanel gp = (GraphicsPanel) data[0].getPointer(line, overrides);
		final Paintable paintable = (Paintable) data[1].getPointer(line, overrides);
		final boolean success = gp.addBuffer(paintable);
		return new Datum(success ? 1 : 0);
	}

	public Datum GRAPHICS_REMOVE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final GraphicsPanel gp = (GraphicsPanel) data[0].getPointer(line, overrides);
		final Paintable paintable = (Paintable) data[1].getPointer(line, overrides);
		final boolean success = gp.removeBuffer(paintable);
		return new Datum(success ? 1 : 0);
	}

	public Datum COLOR_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final int r = (int) data[0].getNumber(line, overrides);
		final int g = (int) data[1].getNumber(line, overrides);
		final int b = (int) data[2].getNumber(line, overrides);
		final int a = (int) data[3].getNumber(line, overrides);
		final Color c = new Color(r, g, b, a);
		return new Datum((Object) c);
	}

	public Datum SHAPE_SETFILL(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable paintable = (Paintable) data[0].getPointer(line, overrides);
		final Color color = (Color) data[1].getPointer(line, overrides);
		paintable.setFill(color);
		return new Datum();
	}

	public Datum SHAPE_SETOUTLINE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable paintable = (Paintable) data[0].getPointer(line, overrides);
		final Color color = (Color) data[1].getPointer(line, overrides);
		paintable.setOutline(color);
		return new Datum();
	}

	public Datum SHAPE_RECTANGLE_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		Color fill = null;
		Color outline = null;
		if (data[0].getType() != Datum.Type.NULL) {
			fill = (Color) data[0].getPointer(line, overrides);
		}
		if (data[1].getType() != Datum.Type.NULL) {
			outline = (Color) data[1].getPointer(line, overrides);
		}
		final int x = (int) data[2].getNumber(line, overrides);
		final int y = (int) data[3].getNumber(line, overrides);
		final int width = (int) data[4].getNumber(line, overrides);
		final int height = (int) data[5].getNumber(line, overrides);
		final Paintable.Rectangle rectangle = new Paintable.Rectangle(fill, outline, x, y, width, height);
		return new Datum((Object) rectangle);
	}

	public Datum SHAPE_RECTANGLE_MOVE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Rectangle rectangle = (Paintable.Rectangle) data[0].getPointer(line, overrides);
		final int x = (int) data[1].getNumber(line, overrides);
		final int y = (int) data[2].getNumber(line, overrides);
		rectangle.setOrigin(x, y);
		return new Datum();
	}

	public Datum SHAPE_RECTANGLE_SIZE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Rectangle rectangle = (Paintable.Rectangle) data[0].getPointer(line, overrides);
		final int width = (int) data[1].getNumber(line, overrides);
		final int height = (int) data[2].getNumber(line, overrides);
		rectangle.setSize(width, height);
		return new Datum();
	}

	public Datum SHAPE_LINE_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Color fill = (Color) data[0].getPointer(line, overrides);
		final int x1 = (int) data[1].getNumber(line, overrides);
		final int y1 = (int) data[2].getNumber(line, overrides);
		final int x2 = (int) data[3].getNumber(line, overrides);
		final int y2 = (int) data[4].getNumber(line, overrides);
		final Paintable.Line lineshape = new Paintable.Line(fill, x1, y1, x2, y2);
		return new Datum((Object) lineshape);
	}

	public Datum SHAPE_LINE_MOVE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Line lineshape = (Paintable.Line) data[0].getPointer(line, overrides);
		final int x1 = (int) data[1].getNumber(line, overrides);
		final int y1 = (int) data[2].getNumber(line, overrides);
		final int x2 = (int) data[3].getNumber(line, overrides);
		final int y2 = (int) data[4].getNumber(line, overrides);
		lineshape.setPoints(x1, y1, x2, y2);
		return new Datum();
	}

	public Datum SHAPE_OVAL_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		Color fill = null;
		Color outline = null;
		if (data[0].getType() != Datum.Type.NULL) {
			fill = (Color) data[0].getPointer(line, overrides);
		}
		if (data[1].getType() != Datum.Type.NULL) {
			outline = (Color) data[1].getPointer(line, overrides);
		}
		final int x = (int) data[2].getNumber(line, overrides);
		final int y = (int) data[3].getNumber(line, overrides);
		final int width = (int) data[4].getNumber(line, overrides);
		final int height = (int) data[5].getNumber(line, overrides);
		final Paintable.Oval oval = new Paintable.Oval(fill, outline, x, y, width, height);
		return new Datum((Object) oval);
	}

	public Datum SHAPE_OVAL_MOVE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Oval oval = (Paintable.Oval) data[0].getPointer(line, overrides);
		final int x = (int) data[1].getNumber(line, overrides);
		final int y = (int) data[2].getNumber(line, overrides);
		oval.setOrigin(x, y);
		return new Datum();
	}

	public Datum SHAPE_OVAL_SIZE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Oval oval = (Paintable.Oval) data[0].getPointer(line, overrides);
		final int width = (int) data[1].getNumber(line, overrides);
		final int height = (int) data[2].getNumber(line, overrides);
		oval.setSize(width, height);
		return new Datum();
	}

	public Datum SHAPE_TEXT_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Color fill = (Color) data[0].getPointer(line, overrides);
		final int x = (int) data[1].getNumber(line, overrides);
		final int y = (int) data[2].getNumber(line, overrides);
		final String string = data[3].getString(line, overrides);
		final int align = (int) data[4].getNumber(line, overrides);
		final Paintable.Text text = new Paintable.Text(fill, x, y, string, align);
		return new Datum((Object) text);
	}

	public Datum SHAPE_TEXT_SETFONT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Text text = (Paintable.Text) data[0].getPointer(line, overrides);
		final Font font = (Font) data[1].getPointer(line, overrides);
		text.setFont(font);
		return new Datum();
	}

	public Datum SHAPE_TEXT_SETSTRING(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Text text = (Paintable.Text) data[0].getPointer(line, overrides);
		final String string = data[1].getString(line, overrides);
		text.setText(string);
		return new Datum();
	}

	public Datum SHAPE_TEXT_MOVE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final Paintable.Text text = (Paintable.Text) data[0].getPointer(line, overrides);
		final int x = (int) data[1].getNumber(line, overrides);
		final int y = (int) data[2].getNumber(line, overrides);
		text.setOrigin(x, y);
		return new Datum();
	}

	public Datum COMPONENT_ADD(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JComponent frame = ((JComponent) data[0].getPointer(line, overrides));
		final JComponent child = (JComponent) data[1].getPointer(line, overrides);
		if (data.length > 2)
			frame.add(child, data[2].getString(line, overrides));
		else
			frame.add(child);
		return new Datum();
	}

	public Datum COMPONENT_SETENABLE(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final AbstractButton component = (AbstractButton) data[0].getPointer(line, overrides);
		final boolean enabled = data[1].getBool(line, overrides);
		component.setEnabled(enabled);
		return new Datum();
	}

	public Datum COMPONENT_CLICK(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final AbstractButton component = (AbstractButton) data[0].getPointer(line, overrides);
		component.doClick();
		return new Datum();
	}

	public Datum COMPONENT_ACTION(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final AbstractButton component = ((AbstractButton) data[0].getPointer(line, overrides));
		final Function function = data[1].getFunction(Datum.Type.ANY, line, overrides);
		component.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					function.execute(new Datum[] { new Datum(component.isSelected() ? 1 : 0) }, line, overrides);
				} catch (final InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		});
		return new Datum();
	}

	public Datum COMPONENT_MENU_BAR_INIT(final Token line, final Object[] overrides) {
		final JMenuBar mb = new JMenuBar();
		return new Datum((Object) mb);
	}

	public Datum COMPONENT_MENU_ITEM_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JMenu menu = new JMenu(data[0].getString(line, overrides));
		return new Datum((Object) menu);
	}

	public Datum COMPONENT_MENU_LABEL_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JMenuItem item = new JMenuItem(data[0].getString(line, overrides));
		return new Datum((Object) item);
	}

	public Datum COMPONENT_TEXT_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JLabel component = new JLabel(data[0].getString(line, overrides));
		return new Datum((Object) component);
	}

	public Datum COMPONENT_TEXT_ALIGN(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JLabel component = (JLabel) data[0].getPointer(line, overrides);
		final int alignmentX = (int) data[1].getNumber(line, overrides);
		final int alignmentY = (int) data[2].getNumber(line, overrides);
		component.setHorizontalAlignment(alignmentX);
		component.setVerticalAlignment(alignmentY);
		return new Datum();
	}

	public Datum COMPONENT_TEXT_SETTEXT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JLabel component = (JLabel) data[0].getPointer(line, overrides);
		final String label = data[1].getString(line, overrides);
		component.setText(label);
		return new Datum();
	}

	public Datum COMPONENT_BUTTON_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JButton component = new JButton(data[0].getString(line, overrides));
		return new Datum((Object) component);
	}

	public Datum COMPONENT_BUTTON_SETTEXT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JButton component = (JButton) data[0].getPointer(line, overrides);
		final String label = data[1].getString(line, overrides);
		component.setText(label);
		return new Datum();
	}

	public Datum COMPONENT_CHECKBOX_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JCheckBox component = new JCheckBox(data[0].getString(line, overrides));
		return new Datum((Object) component);
	}

	public Datum COMPONENT_CHECKBOX_SETTEXT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JCheckBox component = (JCheckBox) data[0].getPointer(line, overrides);
		final String label = data[1].getString(line, overrides);
		component.setText(label);
		return new Datum();
	}

	public Datum COMPONENT_PANEL_BORDER_INIT(final Token line, final Object[] overrides) {
		final JPanel component = new JPanel();
		component.setLayout(new BorderLayout());
		return new Datum((Object) component);
	}

	public Datum COMPONENT_PANEL_BLOCK_INIT(final Token line, final Object[] overrides) {
		final JPanel component = new JPanel();
		component.setLayout(new BoxLayout(component, BoxLayout.PAGE_AXIS));
		return new Datum((Object) component);
	}

	public Datum COMPONENT_PANEL_INLINE_INIT(final Token line, final Object[] overrides) {
		final JPanel component = new JPanel();
		component.setLayout(new FlowLayout());
		return new Datum((Object) component);
	}

	public Datum COMPONENT_PANEL_TABLE_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JPanel component = new JPanel();
		component.setLayout(
				new GridLayout((int) data[0].getNumber(line, overrides), (int) data[1].getNumber(line, overrides)));
		return new Datum((Object) component);
	}

	public Datum COMPONENT_PANEL_SPLIT_INIT(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		int orientation;
		if (data[0].getString(line, overrides).equals("Horizontal"))
			orientation = JSplitPane.HORIZONTAL_SPLIT;
		else if (data[0].getString(line, overrides).equals("Vertical"))
			orientation = JSplitPane.VERTICAL_SPLIT;
		else
			throw new SonoRuntimeException(
					"Component.Panel.Split can only be initialized with \"Horizontal\" or \"Vertical\"", line);
		final JSplitPane component = new JSplitPane(orientation);
		return new Datum((Object) component);
	}

	public Datum COMPONENT_TABPANE_INIT(final Token line, final Object[] overrides) {
		final JTabbedPane component = new JTabbedPane();
		return new Datum((Object) component);
	}

	public Datum COMPONENT_TABPANE_ADD(final Datum[] data, final Token line, final Object[] overrides)
			throws InterruptedException {
		final JTabbedPane frame = ((JTabbedPane) data[0].getPointer(line, overrides));
		final JComponent child = (JComponent) data[1].getPointer(line, overrides);
		final String label = data[2].getString(line, overrides);
		frame.addTab(label, child);
		return new Datum();
	}
}