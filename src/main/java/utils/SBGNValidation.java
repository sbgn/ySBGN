package utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.sbgn.schematron.Issue;
import org.sbgn.schematron.SchematronValidator;
import org.xml.sax.SAXException;

public class SBGNValidation {
	
	/**
	 * Low level validation using XML Schema.
	 * This will check if the XML is properly structured.
	 * @throws SAXException 
	 * @throws JAXBException 
	 * @throws IOException 
	 */
	public static void lowLevelExample(File f) throws JAXBException, SAXException, IOException
	{			
		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn = SbgnUtil.readFromFile(f);
		SbgnUtil.writeToFile(sbgn, f);
		
		boolean isValid = SbgnUtil.isValid(f);
		
		if (isValid)
			System.out.println ("Low level validation SUCCEEDED.");
		else
			System.out.println ("Low level validation FAILED.");
	}
	
	/**
	 * High-level validation using schematron.
	 * This will check if the drawing rules of SBGN are fulfilled. 
	 * @throws SAXException 
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void highLevelExample(File f) throws IOException, ParserConfigurationException, TransformerException, SAXException
	{
		// Export validation reports to file for debugging
        SchematronValidator.setSvrlDump(true);

        // validation will result in a list of issues
		List<Issue> issues = SchematronValidator.validate(f);
		
		if(issues.size() > 0)
		{
		// print each issue individually.
		System.out.println ("High level validation FAILED. There are " + issues.size() + " validation problems"); 
		for (Issue issue : issues)
		{
			System.out.println (issue);
		}
		}
		else
		{
			System.out.println("High level validation SUCCEEDED.");
		}
	}
	
	public static void validateSBGN (String outputFile) throws JAXBException, SAXException, ParserConfigurationException, IOException, TransformerException
	{
		System.out.println("=== SBGN VALIDATION for "+ outputFile +"====");
        // Low level validation
		File f = new File (outputFile);
		lowLevelExample(f);

		//high level validation 
        File f2 = new File (outputFile);
		highLevelExample(f2);
	}
}
