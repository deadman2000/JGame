package com.deadman.gameeditor.resources;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;

public class FileChanges
{
	private static HashMap<IFile, FileChanges> map = new HashMap<>();

	// md = MessageDigest.getInstance("MD5");

	/*private String getMD5(IFile file) throws Exception
	{
		DigestInputStream dis = new DigestInputStream(file.getContents(), md);
		byte[] buffer = new byte[8192];
		try
		{
			while (dis.read(buffer) != -1)
				;
		}
		finally
		{
			dis.close();
		}
		return DatatypeConverter.printHexBinary(md.digest());
	}*/
	
	private long modStamp;

	public FileChanges(IFile file)
	{
		modStamp = file.getModificationStamp();
	}
	
	public static boolean isChanged(IFile file)
	{
		FileChanges current = new FileChanges(file);
		FileChanges changes = map.get(file);
		map.put(file, current);
		return changes == null || current.modStamp != changes.modStamp;
	}
}
