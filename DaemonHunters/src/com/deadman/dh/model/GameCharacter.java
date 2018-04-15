package com.deadman.dh.model;

import com.deadman.dh.Game;
import com.deadman.dh.ItemTypes;
import com.deadman.dh.R;
import com.deadman.dh.battle.BattleSide;
import com.deadman.dh.battle.EnemySearch;
import com.deadman.dh.fx.SoundEffect;
import com.deadman.dh.isometric.IsoObject;
import com.deadman.dh.isometric.IsoSprite;
import com.deadman.dh.isometric.IsoWay;
import com.deadman.dh.isometric.MapCell;
import com.deadman.dh.isometric.MoveArea;
import com.deadman.dh.model.items.AmmunitionSlot;
import com.deadman.dh.model.items.Armor;
import com.deadman.dh.model.items.IItemsPageListener;
import com.deadman.dh.model.items.Item;
import com.deadman.dh.model.items.ItemsPage;
import com.deadman.dh.model.items.UnitItemsPage;
import com.deadman.dh.model.items.Weapon;
import com.deadman.dh.model.itemtypes.ItemType;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.drawing.Animation;
import com.deadman.jgame.drawing.AnimationPlayer;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.sound.Sound;

/**
 * Класс персонажа (игрового или NPC)
 * @author dead_man
 *
 */
public class GameCharacter extends IsoObject implements IItemsPageListener
{
	public final int id;
	public String name;

	public int hpBase, hpMax, hpCount; // Здоровье
	public int mpBase, mpMax, mpCount; // Мана
	public int apBase, apCount, apMax; // Очки действия
	public int str, dex, intl; // Сила, ловкость, интеллект

	public static final int ROTATE_COST = 2; // Стоимость поворота

	public int[] armors = new int[Element.MAX_INDEX]; // Защита

	private int _dx, _dy; // Смещение при отрисовке

	public BattleSide side;

	// Внешний вид
	public IsoSprite body;
	public IsoSprite weaponSpr;
	public int bodyColor, hairColor;
	public Drawable portrait;

	public boolean isSelected; // Выделен для управления 

	public int visionRange = 15; // Радиус видимости

	public boolean completed; // Отметка о том что мы больше не будем им ходить

	public EnemySearch searching; // Для ИИ. Поиск врага

	public GameCharacter()
	{
		id = Game.rnd.nextInt();
		ammunition = new UnitItemsPage(this, "Ammunition", AmmunitionSlot.COUNT, 1);
		ammunition.addListener(this);
		backpack = new UnitItemsPage(this, "Backpack", 6, 5);
	}

	/**
	 * Генерирует случайного персонажа заданного класса и уровня
	 * @param className
	 * @param level
	 * @return
	 */
	public static GameCharacter generateClass(String className, int level)
	{
		GameCharacter ch = new GameCharacter();

		// TODO Хранить базу классов в ресурсах
		if (className.equals("witch"))
		{
			ch.male = false;
			ch.body = GameResources.main.getIso("human_leather")
										.colorize(ch.id);

			ch.equip(ItemTypes.armor);
			ch.hpMax = 50 + level * 10;
			ch.hpCount = ch.hpMax;

			ch.dex = 10;
			ch.str = 10;
			ch.intl = 10;
			ch.calcAttributes();

			ch.equip(ItemTypes.red_potion);
		}
		else
		{
			System.err.println("Unknown className " + className);
			return null;
		}

		return ch;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public byte getSlotType()
	{
		return IsoSprite.UNIT;
	}

	public void calcAttributes()
	{
		apMax = apBase + dex * 10;
		hpMax = hpBase + str * 10 + dex * 5;
		mpMax = mpBase + intl * 10;
		if (apCount > apMax) apCount = apMax;
		if (hpCount > hpMax) hpCount = hpMax;
		if (mpCount > mpMax) mpCount = mpMax;

		for (int i = 0; i < Element.MAX_INDEX; i++)
			armors[i] = 0;

		float oldLight = lightPassive;
		lightPassive = 0;

		for (Item it : ammunition.items)
			if (it != null)
				it.applyPassive(this);

		if (cell != null && lightPassive != oldLight)
			cell.map.calcLights();
	}

	// Sex

	public boolean male;

	// Inventory

	public UnitItemsPage backpack; // Рюкзак
	public UnitItemsPage ammunition; // Обмундирование

	@Override
	public void onItemMoved(ItemsPage page, Item item, int x, int y)
	{
		if (x == AmmunitionSlot.RIGHTHAND.id)
		{
			if (item != null)
				weaponSpr = body.weapons.get(item.equipSprite()); // TODO переделать. брать класс предмета, а не его имя
			else
				weaponSpr = null;
		}

		calcAttributes();
	}

	public void equip(ItemType type)
	{
		type.generate()
			.equip(this);
	}

	public void give(ItemType type) // Тоже, что и equip, только в рюкзак
	{
		moveToBackpack(type.generate());
	}

	public void moveToBackpack(Item item)
	{
		backpack.put(item);
	}

	public Item getAmmunition(AmmunitionSlot slot)
	{
		if (ammunition == null) return null;
		return ammunition.get(slot);
	}

	public Weapon getWeapon()
	{
		Item it = getAmmunition(AmmunitionSlot.RIGHTHAND);
		if (it != null && it instanceof Weapon)
			return (Weapon) it;
		return null;
	}

	public Armor getBodyArmor()
	{
		Armor it = (Armor) getAmmunition(AmmunitionSlot.BODY);
		if (it != null) return it;
		return null;
	}

	public int getAttackTime()
	{
		return getWeapon().wtype().attackTime;
	}

	// Хватает ли очков для атаки
	public boolean hasApAttack()
	{
		Weapon w = getWeapon();
		if (w == null) return false;

		return apCount >= w.wtype().attackTime;
	}

	// Можно ли атаковать юнита из текущей позиции
	public boolean canAttack(GameCharacter another)
	{
		Weapon w = getWeapon();
		if (w != null)
			return w.canAttack(this, another);
		return false; // TODO Рукопашная
	}

	public boolean canAttack(MapCell c)
	{
		Weapon w = getWeapon();
		if (w != null)
			return w.canAttack(this, c);
		return false; // TODO Рукопашная
	}

	private AnimationPlayer aniPlay;
	private AnimationPlayer aniWPlay;

	public AnimationPlayer setAttack()
	{
		Animation anim = (Animation) body.getState(getRotation(), STATE_ATTACK);
		aniPlay = new AnimationPlayer(anim, null);

		if (weaponSpr != null)
		{
			Animation animW = (Animation) weaponSpr.getState(getRotation(), STATE_ATTACK);
			if (animW != null)
				aniWPlay = new AnimationPlayer(animW, null);
		}

		playAttackSound();

		return aniPlay;
	}

	// Действия

	public void resetActionPoints()
	{
		apCount = apMax;
		completed = false;
	}

	public void giveExperience(int val)
	{
	}

	// Координаты внутри ячейки
	/*public void setCellCoords(byte cx, byte cy, byte cz)
	{
		x = cx;
		y = cy;
		z = cz;
	
		dx = cx - cy;
		dy = cx / 2 + cy / 2 + ((cx % 2) + (cy % 2)) / 2 - 8 - z;
	}*/

	public static final byte STATE_STAND = 0;
	public static final byte STATE_MOVE = 1;
	public static final byte STATE_ATTACK = 2;
	public static final byte STATE_DEAD = 15;

	public boolean useAP(int count)
	{
		if (apCount < count)
			return false;
		apCount -= count;
		_moving = null;
		return true;
	}

	@Override
	public int hitDamage(GameCharacter owner, Element el, int damage)
	{
		if (listener != null) listener.onDamaged(this, el, damage);

		//int alvl = armors[el.index];
		if (damage > hpCount)
		{
			damage = hpCount;
			kill();
			cell.ch = null;
			cell.putOnFloor(this);
			if (owner != null) // Может быть урон не от юнита (от огня или падения)
				owner.giveExperience(getKillExp());
			return damage;
		}
		else
		{
			hpCount -= damage;
			return damage;
		}
	}

	public UnitListener listener;

	private void kill()
	{
		hpCount = 0;
		setRotation((byte) 0);
		setState(STATE_DEAD);
	}

	/**
	 * Возвращает опыт, получаемый за убийство юнита
	 * @return
	 */
	private int getKillExp()
	{
		// TODO Доделать расчет числа опыта за убийство
		return 100;
	}

	public void heal(int value)
	{
		if (hpCount + value > hpMax)
		{
			value = hpMax - hpCount;
			hpCount = hpMax;
		}
		else
			hpCount += value;
		listener.onHeal(this, value);
	}

	public boolean isActive()
	{
		return hpCount > 0;
	}

	public boolean isVisible(MapCell cell)
	{
		return side.isVisible(cell);
	}

	@Override
	public void setState(byte st)
	{
		super.setState(st);
		aniPlay = null;
		aniWPlay = null;
		if (st == STATE_MOVE)
			updateMoveSound();
		else
			stopSound();
	}

	public void onMoving()
	{
		if (getState() == STATE_MOVE)
			updateMoveSound();
		if (side != null) side.buildVisibleArea();
	}

	@Override
	public void setRotation(byte rotation)
	{
		super.setRotation(rotation);
		if (side != null) side.buildVisibleArea();
	}

	// Отрисовка
	private static Drawable cur_sel_back = Drawable.get(R.iso.sel_back);
	private static Drawable cur_sel_front = Drawable.get(R.iso.sel_front);

	@Override
	public void drawAt(int sx, int sy)
	{
		sx += _dx;
		sy += _dy;

		if (isSelected) cur_sel_back.drawAt(sx, sy);

		if (aniPlay != null)
		{
			aniPlay.drawAt(sx, sy);
			if (aniWPlay != null)
				aniWPlay.drawAt(sx, sy);
		}
		else
		{
			body.drawAt(_state, sx, sy);
			if (weaponSpr != null)
				weaponSpr.drawAt(_state, sx, sy);
		}

		if (isSelected) cur_sel_front.drawAt(sx, sy);
	}

	public void drawAt(int maxz, MapCell c, int sx, int sy)
	{
		if (nearCell != null)
		{
			if (c == nearCell) // Рисуем соседнюю клетку
			{
				if (cell.z < nearCell.z)
					drawAt(sx, sy + MapCell.LEVEL_HEIGHT);
				else if (cell.z > nearCell.z)
					drawAt(sx, sy - MapCell.LEVEL_HEIGHT);
				else
					drawAt(sx, sy);
			}
			else
			{
				if (maxz < nearCell.z) // Рисуем, если соседнюю не видно
					drawAt(sx, sy);
			}
		}
		else
			drawAt(sx, sy);
	}

	@Override
	public void remove()
	{
		cell.ch = null;
		cell = null;
	}

	private MoveArea _moving;

	public MoveArea getMoveArea()
	{
		if (_moving == null || _moving.map != cell.map)
		{
			_moving = new MoveArea(cell.map, this);
			_moving.build();
		}
		return _moving;
	}

	public void resetMoveArea()
	{
		_moving = null;
	}

	public IsoWay trace(MapCell target)
	{
		return getMoveArea().trace(target);
	}

	public MapCell nearCell;

	public void setShift(int shift_x, int shift_y, int shift_z)
	{
		int newdx = shift_x;
		int newdy = shift_y - shift_z;

		if (shift_z > 0)
		{
			MapCell up = cell.map.getCell(cell.x, cell.y, cell.z + 1);
			if (up != null && up.floor == null)
				setNear(up);
			else
				setNear(null);
		}
		else if (shift_z < 0)
		{
			if (cell.floor != null)
			{
				MapCell down = cell.map.getCell(cell.x, cell.y, cell.z - 1);
				if (down != null)
					setNear(down);
				else
					setNear(null);
			}
			else
				setNear(null);
		}
		else if (nearCell != null)
			setNear(null);

		_dx = newdx;
		_dy = newdy;
	}

	private void setNear(MapCell c)
	{
		if (nearCell == c) return;

		if (nearCell != null)
			nearCell.ch = null;

		nearCell = c;
		if (c != null)
			c.ch = this;
	}

	// Звуки

	private Sound _snd;
	private byte _sndMaterial = -1;

	public boolean hasSound()
	{
		return _snd != null;
	}

	public void updateSounds(float fx, float fy)
	{
		_snd.setPosition(fx, fy, 0.2f);
	}

	private void updateMoveSound()
	{
		if (cell.obj != null) // Лестница или что-то, на что можно взобраться
			playMoveSound(cell.obj.sprite.material);
		else if (cell.floor != null)
			playMoveSound(cell.floor.sprite.material);
		else if (_snd != null) // Парит ??
			stopSound();
	}

	private SoundEffect getMoveSound(byte material)
	{
		switch (material)
		{
			case IsoSprite.MT_GRASS:
				return SoundEffect.STEPS_GRASS;
			case IsoSprite.MT_WOOD:
				return SoundEffect.STEPS_WOOD;
			case IsoSprite.MT_ROCK:
				return SoundEffect.STEPS_ROCK;
			case IsoSprite.MT_DIRT:
				return SoundEffect.STEPS_DIRT;
			default:
				return null;
		}
	}

	private void playMoveSound(byte material)
	{
		if (_sndMaterial == material) return;
		_sndMaterial = material;

		stopSound();

		SoundEffect eff = getMoveSound(material);
		if (eff == null) return;

		_snd = eff.create();
		_snd.setLooping(true);
		_snd.play();
	}

	private void playAttackSound()
	{
		_snd = SoundEffect.SWORD_WHIP.create();
		_snd.play();
	}

	private void stopSound()
	{
		if (_snd == null) return;
		_snd.stop();
		_snd.delete();
		_snd = null;
		_sndMaterial = -1;
	}

	// Пассивные эффекты

	public float lightPassive = 0f;

	@Override
	public float getLightSource()
	{
		return lightPassive;
	}
}
