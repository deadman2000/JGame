package com.deadman.jgame.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.resources.ResourceEntry;
import com.deadman.jgame.resources.ResourceManager;

public class Control
{
	protected Control parent;
	private ArrayList<Control> childs, childsReverse, controlsToAdd;

	public int x, y, width, height;

	private Layout _layout;
	public Layout.ChildSettings layoutChildSettings;

	public boolean isFocused = false;
	public boolean isPressed;
	public Drawable background;

	public int bgrColor = 0;

	public boolean clickOnBgr = false;

	public boolean visible = true;

	public Object tag;

	public static Drawable disabled_bgr;

	public boolean removed = false;
	private boolean childsChanged = false;

	public Control()
	{
	}

	public Control(int x, int y)
	{
		setPosition(x, y);
	}

	public Control(int x, int y, int w, int h)
	{
		setBounds(x, y, w, h);
	}

	public Control(int bgrId)
	{
		this(getDrawable(bgrId));
	}

	public Control(int bgrId, int x, int y)
	{
		this(getDrawable(bgrId), x, y);
	}

	public Control(Drawable pic)
	{
		background = pic;
		width = pic.width;
		height = pic.height;
	}

	public Control(Drawable pic, int x, int y)
	{
		this(pic);
		setPosition(x, y);
	}

	public Control(Drawable pic, int x, int y, int w, int h)
	{
		this(pic);
		setBounds(x, y, w, h);
	}

	public int right()
	{
		return x + width;
	}

	public String name;

	@Override
	public String toString()
	{
		if (name == null)
			return super.toString();
		return name;
	}

	public void showModal()
	{
		ModalEngine eng = new ModalEngine(this);
		eng.show();
	}

	public void close()
	{
		GameLoop.engine.close();
	}

	public final void destroy()
	{
		if (childs != null)
			for (Control c : childsReverse)
			c.destroy();

		onDestroy();
	}

	protected void onDestroy()
	{
	}

	public void setPosition(int x, int y)
	{
		if (x == this.x && y == this.y) return;

		this.x = x;
		this.y = y;
	}

	public void setSize(int width, int height)
	{
		if (width == this.width && height == this.height) return;

		this.width = width;
		this.height = height;
		onResize();
	}

	public void setWidth(int width)
	{
		if (width == this.width) return;

		this.width = width;
		onResize();
	}

	public void setHeight(int height)
	{
		if (height == this.height) return;

		this.height = height;
		onResize();
	}

	public void setBounds(int x, int y, int width, int height)
	{
		if (x == this.x && y == this.y && width == this.width && height == this.height) return;

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		onResize();
	}

	public void setLayout(Layout layout)
	{
		_layout = layout;
		_layout.target = this;
	}

	public Layout layout()
	{
		return _layout;
	}

	// Visibility

	public void show()
	{
		if (visible) return;

		visible = true;
		if (parent != null)
			parent.refreshLayout();
	}

	public void hide()
	{
		if (!visible) return;

		visible = false;
		if (parent != null)
			parent.refreshLayout();
	}

	// Layout

	public <T extends Layout.ChildSettings> T getLayoutSettings(Class<T> type, T def)
	{
		if (type.isInstance(layoutChildSettings))
			return type.cast(layoutChildSettings);

		return def;
	}

	public <T extends Layout.ChildSettings> T getLayoutSettings(Class<T> type)
	{
		if (type.isInstance(layoutChildSettings))
			return type.cast(layoutChildSettings);

		try
		{
			T s = type.newInstance();
			layoutChildSettings = s;
			return s;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	// Пересчитывает компоновку элементов
	public void refreshLayout()
	{
		if (_layout != null && childs != null)
			_layout.apply();
	}

	/*public static final int ANCHOR_NONE = 0;
	public static final int ANCHOR_LEFT = 1 << 0;
	public static final int ANCHOR_RIGHT = 1 << 1;
	public static final int ANCHOR_TOP = 1 << 2;
	public static final int ANCHOR_BOTTOM = 1 << 3;
	public static final int ANCHOR_LEFT_TOP = ANCHOR_LEFT | ANCHOR_TOP;
	public static final int ANCHOR_RIGHT_TOP = ANCHOR_RIGHT | ANCHOR_TOP;
	public static final int ANCHOR_ALL = ANCHOR_LEFT | ANCHOR_RIGHT | ANCHOR_TOP | ANCHOR_BOTTOM;*/

	protected void onResize()
	{
		if (parent != null)
			parent.onChildResize(this);
		refreshLayout();
	}

	protected void onChildResize(Control child)
	{
		if (_layout != null)
			_layout.onChildResize(child);
	}

	public Point parentToChild(Point p)
	{
		return new Point(p.x - x, p.y - y);
	}

	public Point clientToScreen(Point p)
	{
		p = new Point(p);
		p.translate(x, y);
		if (parent == null)
			return p;
		else
			return parent.clientToScreen(p);
	}

	public Point screenPos()
	{
		if (parent == null)
			return new Point(x, y);
			
		Point p = parent.screenPos();
		p.translate(x, y);
		return p;
	}

	// Координаты контрола на экране
	public int scrX, scrY; // TODO Убрать. Сделать glTranslate

	public byte bgrMode = BGR_FILL;
	public static final byte BGR_FILL = 0;
	public static final byte BGR_ONE = 1;
	public boolean clip = false;

	public void draw()
	{
		if (clip) enableClipping();

		if (bgrColor != 0)
			GameScreen.screen.drawRect(scrX, scrY, width, height, bgrColor);

		if (background != null)
		{
			switch (bgrMode)
			{
				case BGR_FILL:
					background.drawAt(scrX, scrY, width, height);
					break;
				case BGR_ONE:
					background.drawAt(scrX, scrY);
					break;
			}
		}

		if (childs != null)
		{
			for (Control c : childs)
			{
				if (c.visible)
				{
					c.scrX = scrX + c.x;
					c.scrY = scrY + c.y;
					c.draw();
				}
			}
		}

		onDraw();

		if (clip) disableClipping();
	}

	protected void onDraw()
	{
	}

	protected void enableClipping()
	{
		GameScreen.screen.enableClipping(scrX, scrY, width, height);
	}

	protected void disableClipping()
	{
		GameScreen.screen.disableClipping();
	}

	protected void onFocusLoss()
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onFocusLoss(this);

		if (childs != null)
			for (Control c : childsReverse)
			c.onFocusLoss();
	}

	// Останавливает обработку нажатий и перемещений мыши
	public boolean consumeMouse = false;
	private Point pressPos;
	int pressBtn;

	public void pressMouse(Point p, MouseEvent e)
	{
		//System.out.println(this + " press?");
		// Отправляем сигнал внутрь других элементов, чтобы сбросить фокус например
		if (childs != null)
		{
			for (Control c : childsReverse)
			{
				if (c.visible)
				{
					c.pressMouse(c.parentToChild(p), e);
					if (e.isConsumed())
						break;
				}
			}
		}
		
		
		if (intersectLocal(p))
		{
			//System.out.println("\tintersect");
			if (intersectBgr(p))
			{
				//System.out.println("\t\tfocus");
				isFocused = true;

				pressBtn = e.getButton();
				isPressed = true;

				//System.out.println(this + " onPressed");
				onPressed(p, e);
				//e.consume(); // With Snow not work MapContextMenu 
			}

			if (consumeMouse)
			{
				e.consume();
			}

			pressPos = p;
		}
		else
		{
			//System.out.println("\tnot intersect");
			if (isFocused)
			{
				//System.out.println("\t\tunfocus");
				isFocused = false;
				onFocusLoss();
			}
		}
	}

	protected void onPressed(Point p, MouseEvent e)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onPressed(this, p, e);
	}

	public void releaseMouse(Point p, MouseEvent e)
	{
		if (childs != null)
		{
			for (Control c : childsReverse)
				if (c.visible)
					c.releaseMouse(c.parentToChild(p), e);
		}

		if (isPressed && e.getButton() == pressBtn)
		{
			onReleased(p, e);
			isPressed = false;

			if (e.isConsumed()) return;

			if (intersectLocal(p) && intersectBgr(p))
				onControlPressed(e);
			if (pressPos != null && pressPos.distance(p) <= 1)
				onClick(p, e);
		}
	}

	protected void onControlPressed(MouseEvent e)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onControlPressed(this, e);
	}

	protected void onClick(Point p, MouseEvent e)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onClick(this, p, e);
	}

	protected void onReleased(Point p, MouseEvent e)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onReleased(this, p, e);
	}

	protected boolean mouseFocused = false; // True, если мышь в области контрола

	public void moveMouse(Point p, MouseEvent e)
	{
		if (childs != null)
		{
			for (Control c : childsReverse)
				if (c.visible)
				{
					c.moveMouse(c.parentToChild(p), e);
					if (e.isConsumed())
						break;
				}
		}

		boolean intersect = intersectLocal(p) && intersectBgr(p);
		if (!mouseFocused && intersect)
		{
			mouseFocused = true;
			onMouseEnter();
		}
		else if (mouseFocused && !intersect)
		{
			mouseFocused = false;
			onMouseLeave();
		}

		if (mouseFocused)
			onMouseMove(p, e);

		if (intersect && consumeMouse)
			e.consume();
	}

	protected void onMouseMove(Point p, MouseEvent e)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onMouseMove(this, p, e);
	}

	protected void onMouseEnter()
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onMouseEnter(this);
	}

	protected void onMouseLeave()
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onMouseLeave(this);
	}

	public void onMouseWheel(Point p, MouseWheelEvent e)
	{
		if (intersectLocal(p))
		{
			if (childs != null)
			{
				for (Control c : childsReverse)
					if (c.visible)
					{
						c.onMouseWheel(c.parentToChild(p), e);
						if (e.isConsumed())
							break;
					}
			}
		}
	}

	public void pressKey(KeyEvent e)
	{
		onKeyPressed(e);
		if (childs != null)
		{
			if (e.isConsumed())
				return;

			for (Control c : childsReverse)
				c.pressKey(e);
		}
	}

	protected void onKeyPressed(KeyEvent e)
	{
	}

	protected void onAction(int action)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onAction(this, action, null);
	}

	protected void onAction(int action, Object tag)
	{
		if (_listeners != null)
			for (ControlListener l : _listeners)
			l.onAction(this, action, tag);
	}

	// sub controls

	public ArrayList<Control> childs()
	{
		return childs;
	}

	public void addControl(Control c)
	{
		if (controlsToAdd == null) controlsToAdd = new ArrayList<>();
		controlsToAdd.add(c);
		c.setParent(this);
		childsChanged = true;
	}

	public void remove()
	{
		removed = true;
		parent.childsChanged = true;
		if (parent.controlsToAdd != null)
			parent.controlsToAdd.remove(this);
	}

	public boolean containsControl(Control control)
	{
		return (childs != null && childs.contains(control)) || (controlsToAdd != null && controlsToAdd.contains(control));
	}

	private void setParent(Control control)
	{
		parent = control;
		removed = false;
	}

	public void submitChilds()
	{
		if (childsChanged)
		{
			ArrayList<Control> list;

			if (childs != null)
			{
				list = new ArrayList<Control>();
				for (Control c : childs)
				{
					if (!c.removed && !list.contains(c))
						list.add(c);
				}
			}
			else
				list = new ArrayList<Control>();

			if (controlsToAdd != null)
			{
				for (Control c : controlsToAdd)
				{
					if (!c.removed && !list.contains(c))
						list.add(c);
				}
				controlsToAdd.clear();
			}

			ArrayList<Control> reverse = new ArrayList<>(list.size());
			for (int i = list.size() - 1; i >= 0; i--)
				reverse.add(list.get(i));

			childs = list;
			childsReverse = reverse;

			childsChanged = false;

			refreshLayout();
		}

		if (childs != null)
		{
			for (Control c : childs)
				c.submitChilds();
		}
	}

	// intersects

	protected boolean intersectLocal(Point p)
	{
		return p.x >= 0 && p.y >= 0 && p.x < width && p.y < height;
	}

	protected boolean intersectBgr(Point p)
	{
		if (clickOnBgr && background != null)
			return background.containsAt(p.x, p.y);
		else
			return true;
	}

	// listeners

	private ArrayList<ControlListener> _listeners;

	public void addControlListener(ControlListener listener)
	{
		if (_listeners == null)
			_listeners = new ArrayList<>();
		_listeners.add(listener);
	}

	public void removeControlListener(ControlListener listener)
	{
		if (_listeners != null)
			_listeners.remove(listener);
	}

	// Resources

	public static ResourceEntry getResource(int id)
	{
		return ResourceManager.getResource(id);
	}

	public static Drawable getDrawable(int id)
	{
		return Drawable.get(id);
	}

	public static Drawable[] getParts(int id)
	{
		return ResourceManager.getParts(id);
	}

	public static GameFont getFont(int id, int... pal)
	{
		return GameFont.get(id, pal);
	}

	// Actions

	public static final int ACTION_VALUE_CHANGED = 0;
	public static final int ACTION_POSITION_CHANGED = 1;
	public static final int ACTION_ITEM_SELECTED = 2;
	public static final int ACTION_ITEM_DBLCLICK = 3;
	public static final int ACTION_CHECKED = 4;
	public static final int ACTION_UNCHECKED = 5;
}
