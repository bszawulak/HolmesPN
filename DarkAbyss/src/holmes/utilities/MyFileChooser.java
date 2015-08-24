package holmes.utilities;

import javax.swing.JFileChooser;
import javax.swing.plaf.metal.MetalFileChooserUI;

public class MyFileChooser extends MetalFileChooserUI  
{  
    public MyFileChooser(JFileChooser filechooser) throws NoSuchFieldException  
    {  
        super(filechooser);  
    }  
  
    @SuppressWarnings("unused")
	private void howToUse() {
    	/*
    	try {
    		fc = new JFileChooser()  
    		{  
    			private static final long serialVersionUID = 5248309092838175815L;
    			{  
    		        setUI(new MyFileChooser(this));  
    		    }
    		};
    	} catch (NoSuchFieldException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}  
        fc.setApproveButtonText("Hello");  
        //fc.showSaveDialog(null);  
    	
    	if(lastPath != null)
    		fc.setCurrentDirectory(new File(lastPath));
    	*/
    }
}  