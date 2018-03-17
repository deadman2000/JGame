package com.deadman.dh.battle;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.dialogs.Book;
import com.deadman.dh.dialogs.InGameMenu;
import com.deadman.dh.dialogs.MessageBox;
import com.deadman.dh.isometric.IsoControl;
import com.deadman.dh.isometric.IsoCursor;
import com.deadman.dh.isometric.IsoMap;
import com.deadman.dh.isometric.IsoViewer;
import com.deadman.dh.isometric.IsoWay;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.MoveArea;
import com.deadman.dh.isometric.Particle;
import com.deadman.dh.isometric.RedBall;
import com.deadman.dh.isometric.RouteNode;
import com.deadman.dh.model.Cart;
import com.deadman.dh.model.Element;
import com.deadman.dh.model.GameCharacter;
import com.deadman.dh.model.Mission;
import com.deadman.dh.model.MissionScenario;
import com.deadman.dh.model.Poi;
import com.deadman.dh.model.Squad;
import com.deadman.dh.model.Unit;
import com.deadman.dh.model.UnitListener;
import com.deadman.dh.model.items.AmmunitionGrid;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemMovingValidator;
import com.deadman.dh.model.items.ItemSlot;
import com.deadman.dh.model.items.ItemsGrid;
import com.deadman.dh.model.items.ItemsGridList;
import com.deadman.dh.model.items.Weapon;
import com.deadman.jgame.GameEngine;
import com.deadman.jgame.GameLoop;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.drawing.GameFont;
import com.deadman.jgame.drawing.GameScreen;
import com.deadman.jgame.ui.Button;
import com.deadman.jgame.ui.Control;
import com.deadman.jgame.ui.ControlListener;
import com.deadman.jgame.ui.Label;
import com.deadman.jgame.ui.ProgressBar;
import com.deadman.jgame.ui.RelativeLayout;

public class MissionEngine extends GameEngine
{
	public IsoMap map;
	public Mission mission;

	public ArrayList<BattleSide> sides = new ArrayList<BattleSide>();
	public BattleSide player;

	private BattleSide currentSide;
	private int currentSideIndex;

	private Action currentAction;
	private boolean interactive;
	private Drawable attackCursor = Drawable.get(R.cursors.attack);
	private boolean _isControlDown = false; // TODO Перенести в GameScreen как шифт

	private static final int DROP_COST = 4;
	private static final int ITEM_USE_COST = 4;

	private static final GameFont fnt3x5_ol_red = getFont(R.fonts.font3x5, 0xFFde5252).outline(0xFF2f2f2f);
	private static final GameFont fnt3x5_ol_green = getFont(R.fonts.font3x5, 0xFF6ee066).outline(0xFF2f2f2f);

	public MissionEngine(Mission mission, Squad squad)
	{
		createControls();

		this.mission = mission;

		init(squad);
	}

	public MissionEngine() // Конструктор для теста
	{
		createControls();

		MissionScenario scenario = MissionScenario.getScenario(10);
		mission = new Mission(new Poi(0, 0), 10, scenario, 0);

		Squad squad = new Squad();
		squad.setCart(Cart.carts[1]);
		for (int i = 0; i < Game.PLAYER_UNITS; i++)
		{
			Unit u = Unit.generate();
			squad.setUnit(u, i);

			Game.ItemTypes.red_potion
										.generate()
										.moveTo(u.backpack, 0, 0);
			u.equip(Game.ItemTypes.armor);
			u.equip(Game.ItemTypes.book);
			u.give(Game.ItemTypes.torch);

			if (i < 2)
				u.equip(Game.ItemTypes.sword);
			else
			{
				u.equip(Game.ItemTypes.bow);
				Game.ItemTypes.arrow
									.generate()
									.setCount(100)
									.moveTo(u.backpack, 0, 0);
			}
		}

		init(squad);
		//Music.playOGG(R.music.GAME8_ogg);
	}

	void init(Squad squad)
	{
		mission.scenario.generate(this, squad);
		calcTeamCharacters();

		for (BattleSide side : sides)
		{
			for (GameCharacter ch : side.units)
			{
				ch.listener = unitListener;
			}
		}
	}

	public void setMap(IsoMap m)
	{
		map = m;
		mapViewer.setMap(map);
		mapViewer.centerView();
	}

	private IsoViewer mapViewer;

	private Control cPortrait;
	private ProgressBar pbAP, pbHP, pbMP;
	private Control ctlAPCost;

	private Button btEndTurn, btExit;

	private Control plUnitInfo;
	private AmmunitionGrid agAmmunition;
	private ItemsGrid igBackpack;
	private ItemsGridList igGround;

	private MessageAnimation messAnim;

	private void createControls()
	{
		cursor = Game.ItemCursor;

		addControl(mapViewer = new IsoViewer());
		RelativeLayout	.settings(mapViewer)
						.fill();
		mapViewer.cursor = IsoCursor.CURSOR_RECT;
		mapViewer.addControlListener(mapViewer_listener);

		Control p = new Control(R.ui.miss_portrait_bgr);
		addControl(p);

		addControl(cPortrait = new Control(4, 4, Unit.PORTRAIT_WIDTH, Unit.PORTRAIT_HEIGHT));
		cPortrait.addControlListener(action_listener);

		pbHP = createPB(41, 4, R.ui.pb_miss_red);
		pbAP = createPB(41, 13, R.ui.pb_miss_green);
		pbMP = createPB(41, 22, R.ui.pb_miss_blue);

		addControl(ctlAPCost = new Control(43, 13, 0, 6));
		ctlAPCost.bgrColor = 0x7f000000;

		btEndTurn = createIB(3, 85, R.ui.ic_miss_end);
		btExit = createIB(3, 106, R.ui.ic_miss_exit);

		// Панель с инвентарем
		addControl(plUnitInfo = new Control());
		plUnitInfo.width = 120;
		RelativeLayout	.settings(plUnitInfo)
						.alignRight()
						.fillHeight();
		plUnitInfo.bgrColor = 0xffc3bca7;
		plUnitInfo.visible = false;
		plUnitInfo.consumeMouse = true;

		plUnitInfo.addControl(agAmmunition = new AmmunitionGrid(4, 58));
		agAmmunition.setValidator(new MissionItemValidator(agAmmunition));

		plUnitInfo.addControl(igBackpack = new ItemsGrid(4, 158));
		igBackpack.setBgrs(R.slots.item_slot);
		igBackpack.setValidator(new MissionItemValidator(igBackpack));

		plUnitInfo.addControl(igGround = new ItemsGridList(4, 260, 111, ItemSlot.ITEM_HEIGHT + 6));
		igGround.setValidator(new MissionItemValidator(igGround));

		messAnim = new MessageAnimation();

		submitChilds();
	}

	class MissionItemValidator extends ItemMovingValidator
	{
		private Control page;

		public MissionItemValidator(Control page)
		{
			this.page = page;
		}

		@Override
		public boolean canDrop(Item item, Object target)
		{
			if (ItemSlot.somePages(target, ItemSlot.pickedSource()))
				return true; // Не меняем источник

			if (selectedUnit.useAP(DROP_COST))
			{
				updateUnit();
				return true;
			}
			else
				noAPMessage();
			return false;
		}

		@Override
		public boolean canUse(Item item, Object source)
		{
			if (page == igGround) return false; // Не можем использовать с земли!
			return item.canActivate();
		}

		@Override
		public void useItem(Item item)
		{
			activateItem(item);
		}
	}

	void activateItem(Item item)
	{
		if (item == null || !item.canActivate()) return;

		if (selectedUnit.apCount >= ITEM_USE_COST) // Хватает ОД
		{
			if (item.activate(selectedUnit, selectedUnit)) // Активировалось успешно
			{
				selectedUnit.useAP(ITEM_USE_COST); // Тратим ОД
				updateUnit(); // Обновляем интерфейс
			}
		}
		else
			noAPMessage();
	}

	UnitListener unitListener = new UnitListener()
	{
		public void onDamaged(GameCharacter ch, Element el, int value)
		{
			System.out.println(ch + " damaged " + el + "  " + value);
			showCellMessage(Integer.toString(value), fnt3x5_ol_red, ch.cell);
		};

		public void onHeal(GameCharacter ch, int value)
		{
			showCellMessage(Integer.toString(value), fnt3x5_ol_green, ch.cell);
		};
	};

	private void showCellMessage(String msg, GameFont fnt, MapCell cell)
	{
		IsoControl c = new IsoControl(cell);

		Label l = new Label(fnt);
		l.char_interval = -1;
		l.setText(msg);
		l.x = -l.width / 2;
		l.y = -MapCell.LEVEL_HEIGHT / 2;
		c.addControl(l);

		mapViewer.addIsoControl(c);
		messAnim.add(c);
	}

	// прогресс-бары для хп, мыны и действий
	private ProgressBar createPB(int x, int y, int pic)
	{
		addControl(new Control(R.ui.pb_miss_bgr, x, y));
		ProgressBar pb = new ProgressBar(pic, x + 2, y + 1);
		addControl(pb);
		return pb;
	}

	private Button createIB(int x, int y, int im)
	{
		Button ib = new Button(R.ui.bt_miss_action, R.ui.bt_miss_action_pr);
		ib.setPosition(x, y);
		ib.image_pad_x = 2;
		ib.image_pad_y = 2;
		if (im >= 0)
			ib.image = getDrawable(im);
		ib.addControlListener(action_listener);
		addControl(ib);
		return ib;
	}

	private ControlListener action_listener = new ControlListener()
	{
		@Override
		public void onControlPressed(Control sender, MouseEvent e)
		{
			e.consume(); // Чтобы не нажималось на карту

			if (ItemSlot.pickedItem() != null) // Отменяем нажатие, если в руках предмет
				return;

			if (sender == btEndTurn)
			{
				endTurn();
			}
			else if (sender == btExit)
			{
				endMission();
			}
			else if (sender == cPortrait)
			{
				toggleUnitInfo();
			}
		};
	};

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		if (_isControlDown != e.isControlDown())
		{
			_isControlDown = e.isControlDown();
			cellSelected();
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				InGameMenu.showMenu();
				break;
			case KeyEvent.VK_F2:
				Book.inst()
					.show();
				break;
			case KeyEvent.VK_I:
				toggleUnitInfo();
				break;
			default:
				super.onKeyPressed(e);
				return;
		}

		e.consume();
	}

	@Override
	public void onKeyReleased(KeyEvent e)
	{
		if (_isControlDown != e.isControlDown())
		{
			_isControlDown = e.isControlDown();
			cellSelected();
		}
		super.onKeyReleased(e);
	}

	@Override
	public void onMousePressed(Point p, MouseEvent e)
	{
		if (!e.isConsumed())
		{
			if (e.getButton() == 3) // ПКМ
			{
				if (ItemSlot.pickedItem() != null)
				{
					activateItem(ItemSlot.pickedItem());
					if (ItemSlot.pickedItem().count == 0)
						ItemSlot.drop();
					return;
				}
			}
		}
		super.onMousePressed(p, e);
	}

	void setActionCost(int val)
	{
		if (selectedUnit != null)
		{
			ctlAPCost.width = val * 100 / selectedUnit.apMax;
			ctlAPCost.x = 43 + selectedUnit.apCount * 100 / selectedUnit.apMax - ctlAPCost.width;
		}
		else
		{
			ctlAPCost.width = 0;
		}
	}

	private void toggleUnitInfo()
	{
		if (selectedUnit == null)
		{
			plUnitInfo.visible = false;
			return;
		}

		if (plUnitInfo.visible)
			plUnitInfo.visible = false;
		else
		{
			bindInfo();
			plUnitInfo.visible = true;
		}
	}

	private void bindInfo()
	{
		igBackpack.setPage(selectedUnit.backpack);
		agAmmunition.setPage(selectedUnit.ammunition);

		if (selectedUnit.cell.items == null)
			selectedUnit.cell.items = new ArrayList<>();
		igGround.setCell(selectedUnit.cell);
	}

	// Пересчитывает количество персонажей и определяет победившую сторону
	public void calcTeamCharacters()
	{
		// Игра заканчивается, если не осталось игроков-людей
		// Появляется возможность закончить игру, если не осталось врагов
		int enemyCount = 0; // Число команд-врагов
		int playersCount = 0; // Число команд-игроков
		for (BattleSide side : sides)
		{
			if (side.isActive())
			{
				if (side.isEnemy)
					enemyCount++;
				else if (side instanceof PlayerSide) playersCount++;
			}
		}

		if (playersCount == 0)
		{
			System.out.println("Player defeat");
			MessageBox.show("Вы проиграли");
			endMission();
		}
		else if (enemyCount == 0)
		{
			System.out.println("Player win");
			MessageBox.show("Вы победили");
			enableExit();
		}
		else
			disableExit();
	}

	private void enableExit()
	{
		btExit.visible = true;
	}

	private void disableExit()
	{
		btExit.visible = false;
	}

	public static void beginMission(Mission mission, Squad squad)
	{
		MissionEngine eng = new MissionEngine(mission, squad);
		eng.show();
		eng.beginBattle();
	}

	public void beginBattle()
	{
		for (BattleSide s : sides)
			s.resetVisibleArea();
		beginTurn(0);
	}

	public void beginTurn(int sideIndex)
	{
		currentSideIndex = sideIndex;
		currentSide = sides.get(sideIndex);
		currentSide.beginTurn();
		currentSide.resetVisibleArea();

		if (!currentSide.isPlayer())
		{
			disableMap();
			generateAction();
		}
		else
		{
			mapViewer.currentSide = currentSide;
			MessageBox.show("Новый ход");
			selectLastUnist();
			enableMap();
		}
	}

	private void selectLastUnist()
	{
		if (currentSide.lastSelect != null && currentSide.lastSelect.isActive())
		{
			selectUnit(currentSide.lastSelect);
			return;
		}

		for (GameCharacter ch : currentSide.units)
		{
			if (ch.isActive())
			{
				selectUnit(ch);
				return;
			}
		}
		System.err.println("no units to select");
	}

	private void endTurn()
	{
		if (!currentSide.isEnemy)
			hideMoveArea();
		currentSide.resetVisibleArea();

		int i = currentSideIndex + 1;
		if (i >= sides.size())
			i = 0;
		beginTurn(i);
	}

	private void generateAction()
	{
		ComputerSide ai = (ComputerSide) currentSide;
		Action act = ai.generateAction();
		if (act != null)
			doAction(act);
		else
			endTurn();
	}

	private void endMission()
	{
		System.out.println("End mission");

		int enemyCount = 0; // Число команд-врагов
		for (BattleSide side : sides)
		{
			side.onMissionEnd();

			if (side.isActive())
			{
				if (side.isEnemy)
					enemyCount++;
				if (side instanceof PlayerSide)
				{
					Squad sq = ((PlayerSide) side).squad;
					sq.moveToHome();
				}
			}
			else
			{
				// Убитая сторона 
				if (side instanceof PlayerSide)
				{
					Game.squads.remove(((PlayerSide) side).squad);
				}
			}
		}

		if (enemyCount == 0)
			Game.removeMission(mission);

		close();
	}

	private void enableMap()
	{
		interactive = true;
		mapViewer.showCursor = true;
		selectUnit(selectedUnit);
		cellSelected();
	}

	private void disableMap()
	{
		interactive = false;
		mapViewer.showCursor = false;
	}

	private ArrayList<Particle> particles = new ArrayList<>();

	public void putParticle(Particle p)
	{
		particles.add(p);
	}

	@Override
	public void tick()
	{
		if (GameLoop.engine == this)
		{
			if (currentAction != null)
			{
				currentAction = currentAction.tick();
				if (currentAction == null)
				{
					onActionEnd();
				}
			}
		}
		messAnim.tick();
	}

	private ControlListener mapViewer_listener = new ControlListener()
	{
		@Override
		public void onClick(Object sender, Point p, MouseEvent e)
		{
			if (!interactive) return;

			switch (e.getButton())
			{
				case 1:
					hitCellLeft();
					break;
				case 2:
					if (mapViewer.focusedCell != null) particles.add(new RedBall(mapViewer.focusedCell)); // TEST
					break;
				case 3:
					hitCellRight();
					break;
			}
		};

		@Override
		public void onAction(Object sender, int action, Object tag)
		{
			if (action == Control.ACTION_ITEM_SELECTED)
			{
				cellSelected();
			}
		}
	};

	protected void cellSelected()
	{
		if (!interactive) return;

		if (selectedUnit == null) return;

		hideWay();
		unblend();

		MapCell c = mapViewer.focusedCell;
		if (c == null)
		{
			cursor = Game.ItemCursor;
			return;
		}

		String t = c.toString();
		if (c.ch != null) t += " " + c.ch + " AP:" + c.ch.apCount;
		RouteNode rn = moveArea.getCalc(c);
		if (rn != null) t += " RN: " + rn;
		t += "  LS: " + c.getLightSource();
		GameScreen.screen.setTitle(t);

		if (ItemSlot.pickedItem() != null) // В руках предмет
		{
			mapViewer.cursor = null;
			setActionCost(DROP_COST);
			return;
		}

		setActionCost(0);
		if (_isControlDown)
		{
			showAttack(c);
			return;
		}

		cursor = Game.ItemCursor;
		mapViewer.cursor = IsoCursor.CURSOR_RECT;

		if (c.ch != null)
		{
			if (c.ch.side == currentSide) // Свой
				return;

			// Враг
			if (currentSide.isVisible(c)) // Меняем курсор и прочее, если только видим врага
			{
				showAttack(c);
				return;
			}
		}

		IsoWay way = moveArea.trace(c);
		if (way != null)
		{
			setActionCost(way.cost);
			showWay(way);
		}
	}

	private void showAttack(MapCell c)
	{
		mapViewer.cursor = IsoCursor.CURSOR_ATTACK;
		cursor = attackCursor;
		if (selectedUnit.canAttack(c))
		{
			setActionCost(selectedUnit.getAttackTime());

			blendAround(selectedUnit.cell);
			blendAround(c);
			return;
		}

		Weapon w = selectedUnit.getWeapon();
		if (w == null) return; // TODO Рукопашная
		if (w.isMeele()) // оружие ближнего боя => надо подойти
		{
			IsoWay way = moveArea.traceNear(c);
			if (way != null)
			{
				if (way.completed)
					setActionCost(way.cost + selectedUnit.getAttackTime());
				else
					setActionCost(way.cost);
				showWay(way);
			}
		}
	}

	private void hitCellLeft()
	{
		MapCell c = mapViewer.focusedCell;
		if (c == null) return;

		if (ItemSlot.pickedItem() != null) // Бросаем предмет на землю
		{
			if (ItemSlot.pickedSource() == igGround) // Вернули на землю
			{
				selectedUnit.cell.putOnFloor(ItemSlot.pickedItem());
				ItemSlot.drop();
			}
			else
			{
				if (selectedUnit.useAP(DROP_COST))
				{
					updateUnit();
					selectedUnit.cell.putOnFloor(ItemSlot.pickedItem());
					ItemSlot.drop();
				}
				else
					noAPMessage();
			}
			return;
		}

		if (_isControlDown)
		{
			doAttack(c);
			return;
		}

		if (c.ch != null)
		{
			if (c.ch.side == currentSide)
			{
				selectUnit(c.ch);
				return;
			}

			// Враг
			if (currentSide.isVisible(c))
			{
				doAttack(c);
				return;
			}
		}

		if (currWay != null)
			doAction(new MoveAction(this, selectedUnit, currWay));
	}

	private void doAttack(MapCell cell)
	{
		AttackAction att = new AttackAction(this, selectedUnit, cell);
		if (currWay != null)
			doActions(new MoveAction(this, selectedUnit, currWay), att);
		else
			doAction(att);
	}

	private void hitCellRight()
	{
		MapCell c = mapViewer.focusedCell;
		if (c == null) return;

		if (selectedUnit.cell != c)
		{
			if (selectedUnit.isRotated(c)) return;

			if (selectedUnit.apCount >= GameCharacter.ROTATE_COST)
				doAction(new RotateAction(selectedUnit, c, true));
			else
				noAPMessage();
		}
	}

	private void noAPMessage()
	{
		showMessage("Недостаточно очков действия", fnt3x5_ol_red, screen.cursorPos.x, screen.cursorPos.y);
	}

	private void showMessage(String text, GameFont font, int x, int y)
	{
		Label la = new Label(font, x, y, text);
		la.char_interval = -1;
		addControl(la);
		messAnim.add(la);
	}

	private void doAction(Action act)
	{
		if (!act.isValid())
			return;

		currentAction = act;
		disableMap();
		selectedUnit.isSelected = false;
		hideMoveArea();
		setActionCost(0);
	}

	private void doActions(Action... actions)
	{
		doAction(new ListAction(actions));
	}

	private void onActionEnd()
	{
		calcTeamCharacters();
		for (GameCharacter c : currentSide.units)
			c.resetMoveArea();

		if (!currentSide.isPlayer())
			generateAction();
		else
			enableMap();
	}

	private GameCharacter selectedUnit;
	private MoveArea moveArea;

	protected void selectUnit(GameCharacter unit)
	{
		if (selectedUnit != null) selectedUnit.isSelected = false;
		hideMoveArea();

		currentSide.lastSelect = unit;
		selectedUnit = unit;

		if (unit == null)
			plUnitInfo.visible = false;
		else if (plUnitInfo.visible) bindInfo();

		if (unit != null)
		{
			unit.isSelected = true;
			cPortrait.background = unit.portrait;
			updateUnit();
		}
	}

	private void updateUnit()
	{
		if (selectedUnit == null) return;

		pbHP.setValue((float) selectedUnit.hpCount / selectedUnit.hpMax);
		pbAP.setValue((float) selectedUnit.apCount / selectedUnit.apMax);
		pbMP.setValue((float) selectedUnit.mpCount / selectedUnit.mpMax);
		setActionCost(0);

		hideMoveArea();
		moveArea = selectedUnit.getMoveArea();
		showMoveArea();
	}

	boolean showMove = false;

	private void showMoveArea()
	{
		if (moveArea == null || showMove) return;

		showMove = true;
		moveArea.show();
	}

	private void hideMoveArea()
	{
		if (moveArea == null || !showMove) return;

		showMove = false;
		moveArea.hide();
		hideWay();
	}

	private IsoWay currWay;

	private void showWay(IsoWay way)
	{
		hideWay();
		if (way != null)
		{
			for (int i = 1; i < way.cells.length; i++)
			{
				MapCell c = way.cells[i];
				c.state |= MapCell.TRACE_FLAG;
				blendAround(c);
			}
		}
		currWay = way;
	}

	private void hideWay()
	{
		if (currWay == null) return;
		for (int i = 1; i < currWay.cells.length; i++)
		{
			MapCell c = currWay.cells[i];
			c.state &= MapCell.NOT_TRACE_FLAG;
		}
		currWay = null;
		unblend();
	}

	private ArrayList<MapCell> blended = new ArrayList<>();

	private void blendAround(MapCell c)
	{
		blended.add(c);
		blend(c.x + 1, c.y + 1, c.z);
		blend(c.x + 2, c.y + 2, c.z);

		blendWallL(c.x + 1, c.y, c.z);
		blendWallL(c.x + 2, c.y + 1, c.z);

		blendWallR(c.x, c.y + 1, c.z);
		blendWallR(c.x + 1, c.y + 2, c.z);
	}

	private void unblend()
	{
		for (MapCell c : blended)
		{
			unblend(c.x + 1, c.y + 1, c.z);
			unblend(c.x + 2, c.y + 2, c.z);

			unblend(c.x + 1, c.y, c.z);
			unblend(c.x + 2, c.y + 1, c.z);

			unblend(c.x, c.y + 1, c.z);
			unblend(c.x + 1, c.y + 2, c.z);
		}
		blended.clear();
	}

	public void blend(int x, int y, int z)
	{
		MapCell cb = map.getCell(x, y, z);
		if (cb == null) return;
		if (cb.ch != null) return; // Не прозрачим юнитов
		if (cb != null) cb.state |= MapCell.BLEND_WALL_L_FLAG | MapCell.BLEND_WALL_R_FLAG | MapCell.BLEND_OBJ_FLAG;
	}

	public void blendWallL(int x, int y, int z)
	{
		MapCell cb = map.getCell(x, y, z);
		if (cb != null) cb.state |= MapCell.BLEND_WALL_L_FLAG;
	}

	public void blendWallR(int x, int y, int z)
	{
		MapCell cb = map.getCell(x, y, z);
		if (cb != null) cb.state |= MapCell.BLEND_WALL_R_FLAG;
	}

	public void unblend(int x, int y, int z)
	{
		MapCell cb = map.getCell(x, y, z);
		if (cb != null) cb.state &= MapCell.NOT_BLEND_FLAG;
	}

}
