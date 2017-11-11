package com.deadman.dh.guild;

public interface IGuildPatternSource
{
	void reset();

	GuildBuildPattern getPattern();

	void disable(GuildBuildPattern m);

}
