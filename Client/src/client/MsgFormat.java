
package client;

import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.awt.Color;

/**
 * Defines the message format when appending message to sender and receiver
 */
public class MsgFormat {
    
    public static AttributeSet styleMsg(Color color, String fontFamily, int size){
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet style_attribute = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
        
        style_attribute = sc.addAttribute(style_attribute, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        style_attribute = sc.addAttribute(style_attribute, StyleConstants.FontFamily, fontFamily); 
        style_attribute = sc.addAttribute(style_attribute, StyleConstants.FontSize, size);
        
        return style_attribute;
    }
}
