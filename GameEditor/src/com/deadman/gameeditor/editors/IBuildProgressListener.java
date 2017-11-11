package com.deadman.gameeditor.editors;

public interface IBuildProgressListener
{
	void onBuildProgressChanged(int progress, int total);
	
	void onBuildCompleted();
}
