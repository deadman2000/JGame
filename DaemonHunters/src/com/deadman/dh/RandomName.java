package com.deadman.dh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import com.deadman.jgame.resources.ResourceManager;

public class RandomName
{
	private ArrayList<String> names;
	private Random random = new Random();

	public RandomName(int id)
	{
		try
		{
			names = new ArrayList<String>(100);
			BufferedReader br = new BufferedReader(new InputStreamReader(ResourceManager.getInputStream(id)));
			String line;
			while ((line = br.readLine()) != null)
			{
				names.add(line);
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public RandomName(int id, long seed)
	{
		this(id);
		random = new Random(seed);
	}

	public String getRandomName()
	{
		return getRandomName(random);
	}
	
	public String getRandomName(Random rnd)
	{
		int ind = rnd.nextInt(names.size());
		String name = names.get(ind);
		names.remove(ind);
		return name;
	}
}
