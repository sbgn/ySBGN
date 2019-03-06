package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.xml.sax.SAXException;

import af.AF2GraphML;
import fr.eisbm.SBGNHandlers.transformToSBGN02;
import pd.PD2GraphML;

public class SBGNML2GraphML {

	public static void main(String[] args) {

		File inputFile = new File(Utils.IN_SBGN_FILE);
		try {
			System.out.println(
					"SBGN file validation: " + (SbgnUtil.isValid(inputFile) ? "validates" : "does not validate"));
			convert(Utils.IN_SBGN_FILE);
			System.out.println("simulation finished");
		} catch (JAXBException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void convert(String szInputFileName) {

		SBGNML2GraphML sg = new SBGNML2GraphML();
		String szOutFileName = szInputFileName.substring(0, szInputFileName.indexOf(".")).concat("_generated.graphml");
		sg.parseSBGNFile(szInputFileName, szOutFileName);

		String szSBGNv02FileName = szOutFileName.replace(".graphml", "-SBGNv02.sbgn");
		transformToSBGN02.transformToSBGNv02(szInputFileName, szSBGNv02FileName);
	}

	public void parseSBGNFile(String szInSBGNFileName, String szOutGraphMLFileName) {
		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn;
		try {
			sbgn = Utils.readFromFile(szInSBGNFileName);
			
			if(((org.sbgn.bindings.Map) sbgn.getMap()).getLanguage().equals("process description"))
			{
				PD2GraphML pdConverter = new PD2GraphML();
				pdConverter.parseSBGNFile( szInSBGNFileName,  szOutGraphMLFileName);
			}
			else if(((org.sbgn.bindings.Map) sbgn.getMap()).getLanguage().equals("activity flow"))
			{
				AF2GraphML afConverter = new AF2GraphML();
				afConverter.parseSBGNFile(szInSBGNFileName,  szOutGraphMLFileName);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
