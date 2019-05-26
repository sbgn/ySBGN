package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.xml.sax.SAXException;

import af.AF2GraphML;
import pd.PD2GraphML;
import utils.Utils;

public class SBGNML2GraphML {

	public static void convert(String szInputFileName) {
		long start = System.currentTimeMillis();
		SBGNML2GraphML sg = new SBGNML2GraphML();
		String szOutFileName = szInputFileName.substring(0, szInputFileName.indexOf(".")).concat("_generated.graphml");
		
		//if the output file already exists, it will be overwritten during the current conversion step
		if((new File(szOutFileName)).exists())
		{
			System.out.println("The selected output file exists and it will be overwritten during the current conversion step." );
		}
		
		sg.parseSBGNFile(szInputFileName, szOutFileName);
		long end = System.currentTimeMillis();
		// finding the time difference and converting it into seconds
		float sec = (end - start) / 1000F;
		System.out.println(sec + " seconds");
	}

	public void parseSBGNFile(String szInSBGNFileName, String szOutGraphMLFileName) {
		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn;
		try {
			File f = new File(szInSBGNFileName);

			// Now read from "f" and put the result in "sbgn"
			sbgn = SbgnUtil.readFromFile(f);

			if ((sbgn.getMap().get(0)).getLanguage().equals("process description")) {
				PD2GraphML pdConverter = new PD2GraphML();
				pdConverter.parseSBGNFile(szInSBGNFileName, szOutGraphMLFileName);
			} else if ((sbgn.getMap().get(0)).getLanguage().equals("activity flow")) {
				AF2GraphML afConverter = new AF2GraphML();
				afConverter.parseSBGNFile(szInSBGNFileName, szOutGraphMLFileName);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
