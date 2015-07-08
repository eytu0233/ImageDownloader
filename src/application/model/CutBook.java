package application.model;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class CutBook implements ClipboardOwner {

	private Clipboard clipboard;  
	  
    public CutBook() {  
        CutBookInit();  
    }  
  
    public void CutBookInit(){  
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();  
    }  
  
    /** 
     * 設定剪貼內容 
     * @param str 
     */      
    public void setBookContents(String str){  
        StringSelection contents = new StringSelection(str);  
        clipboard.setContents(contents, this);  
    }  
  
    /** 
     * 取出剪貼內容 
     * @return 
     */  
    public String getBookContents(){  
        Transferable content = clipboard.getContents(this);  
        try{  
            return (String) content.getTransferData(DataFlavor.stringFlavor);  
        }catch(Exception e){  
            e.printStackTrace();  
            //System.out.println(e);  
        }  
        return null;  
    }  
  
    public void lostOwnership(Clipboard clipboard, Transferable contents) {  
        //System.out.println("lostOwnership...");  
    }  

}
