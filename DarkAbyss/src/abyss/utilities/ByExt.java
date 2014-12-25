package abyss.utilities;

import java.io.File;
import java.io.FilenameFilter;

public class ByExt implements FilenameFilter{
	String ext; 
	public ByExt(String ext){ //podajemy interesujące nas rozszerzenie
	  this.ext = ext;
	}
	public boolean accept(File dir, String str){
	  return str.endsWith(ext); //tylko pliki z rozszerzeniem ext
	}
}
