package holmes.utilities;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

public class HolmesFileView extends FileView {
	public Icon getIcon(File f)
	{
		FileSystemView view = FileSystemView.getFileSystemView();
		return view.getSystemIcon(f);
	}
}
