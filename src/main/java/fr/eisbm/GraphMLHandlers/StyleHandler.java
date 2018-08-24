/**
 * 
 */
package fr.eisbm.GraphMLHandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author iroznovat
 *
 */
public class StyleHandler {

	/**
	 * 
	 */
	
	public Set<String> colorSet;
	public java.util.Map<String, SBGNMLStyle> styleMap;
	
	public StyleHandler() {
		colorSet = new HashSet<String>();
		styleMap = new HashMap<String, SBGNMLStyle>();
	}
}
