package com.deadman.dh.model;

import java.util.Random;

import com.deadman.dh.Game;
import com.deadman.dh.R;
import com.deadman.dh.guild.Guild;
import com.deadman.dh.guild.GuildBuilding;
import com.deadman.dh.resources.GameResources;
import com.deadman.jgame.drawing.Drawable;
import com.deadman.jgame.io.DataInputStreamBE;
import com.deadman.jgame.resources.ResourceManager;

public class Unit extends GameCharacter
{
	static final int MAX_LVL = 100;

	static final int[] expTable;

	public static final int PORTRAIT_WIDTH = 31;
	public static final int PORTRAIT_HEIGHT = 32;

	static
	{
		expTable = new int[MAX_LVL];
		for (int i = 1; i <= MAX_LVL; i++)
		{
			expTable[i - 1] = (int) (Math.pow(i, 2) * 150);
		}
	}

	// Генерация

	public static Unit generate()
	{
		return new Unit();
	}

	public Unit()
	{
		setRandomName(Game.rnd);

		if (Game.rnd.nextBoolean())
			portrait = Drawable.get(R.portraits.paulson);
		else
			portrait = Drawable.get(R.portraits.conrad);

		lvl = 1;
		experience = Game.rnd.nextInt(expTable[lvl]);

		str = 10 + Game.rnd.nextInt(5);
		dex = 10 + Game.rnd.nextInt(5);
		intl = 10 + Game.rnd.nextInt(5);

		skSword = new Skill("sword", this, 0.033, 0.016, 0);
		skShield = new Skill("shield", this, 0.033, 0, 0);
		skThrow = new Skill("throw", this, 0.016, 0.033, 0);
		skCraft = new Skill("craft", this, 0, 0.05, 0);
		skScience = new Skill("science", this, 0, 0, 0.05);
		skMagic = new Skill("magic", this, 0, 0, 0.08);

		calcAttributes();
		mpCount = mpMax;
		hpCount = hpMax;

		body = GameResources.main.getIso("human_leather")
				.colorize(id);
	}

	// Опыт и скиллы

	public int lvl;
	public int experience;
	public final Skill skSword, skShield, skThrow, skCraft, skScience, skMagic;
	public int statPoints; // Очки прокачки

	public int getExpNext()
	{
		return expTable[lvl];
	}
	
	@Override
	public void giveExperience(int val)
	{
		experience += val;
		while (expTable[lvl] < experience)
		{
			// Level UP!
			lvl++;
			statPoints++;
		}
		// if (statPoints > 0)
		// Тут можно замутить автопрокачку в зависимости от специализации	
	}

	// Гильдия

	public Guild guild;
	public GuildBuilding building;

	public void setInBuilding(GuildBuilding gb)
	{
		if (building != null)
			building.units.remove(this);
		building = gb;
		gb.units.add(this);
		guild = gb.guild;
	}

	// Увольнение из гильдии
	public void removeFromGuild()
	{
		guild.removeUnit(this);
	}

	// Names generator

	private static int fnamesCount = -1, snamesCount;

	private static void readNamesCount()
	{
		try
		{
			DataInputStreamBE in = ResourceManager.getInputStream(R.unit_fnames_tbl);
			fnamesCount = in.readInt();
			in.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		try
		{
			DataInputStreamBE in = ResourceManager.getInputStream(R.unit_snames_tbl);
			snamesCount = in.readInt();
			in.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void setRandomName(Random r)
	{
		if (fnamesCount == -1) readNamesCount();

		try
		{
			int ind = r.nextInt(fnamesCount);
			DataInputStreamBE in = ResourceManager.getInputStream(R.unit_fnames_tbl);
			in.skip(4 + 4 * ind);
			int pos = in.readInt();
			in.skip(pos);

			byte m = in.readByte();
			male = m != 0;
			int l = in.read();
			name = in.readString(l);

			in.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		try
		{
			int ind = r.nextInt(snamesCount);
			DataInputStreamBE in = ResourceManager.getInputStream(R.unit_snames_tbl);
			in.skip(4 + 4 * ind);
			int pos = in.readInt();
			in.skip(pos);

			int l = in.read();
			name += " " + in.readString(l);

			in.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}
