package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;
import javax.xml.bind.JAXBException;
import org.sbgn.bindings.Sbgn;

import af.AF2GraphML;
import pd.PD2GraphML;

public class SBGNML2GraphML {

	public static void convert(String szInputFileName) {
		// the extension is cut at the last dot of the file name, not at the first dot of the path
		String szName = new File(szInputFileName).getName();
		int iExtension = szName.lastIndexOf(".");
		String szBase = (iExtension > 0) ? szInputFileName.substring(0, szInputFileName.length() - szName.length() + iExtension)
				: szInputFileName;
		convert(szInputFileName, szBase.concat("_generated.graphml"));
	}

	public static boolean convert(String szInputFileName, String szOutFileName) {
		long start = System.currentTimeMillis();
		SBGNML2GraphML sg = new SBGNML2GraphML();

		//if the output file already exists, it will be overwritten during the current conversion step
		if((new File(szOutFileName)).exists())
		{
			System.out.println("The selected output file exists and it will be overwritten during the current conversion step." );
		}
		
		boolean bConversion = sg.parseSBGNFile(szInputFileName, szOutFileName);
		long end = System.currentTimeMillis();
		// finding the time difference and converting it into seconds
		float sec = (end - start) / 1000F;
		System.out.println(sec + " seconds");
		return bConversion;
	}

	public boolean parseSBGNFile(String szInSBGNFileName, String szOutGraphMLFileName) {
		// Now read from "f" and put the result in "sbgn"
		Sbgn sbgn;
		try {
			sbgn = Utils.readFromFile(szInSBGNFileName);

			if (((org.sbgn.bindings.Map) sbgn.getMap()).getLanguage().equals("process description")) {
				PD2GraphML pdConverter = new PD2GraphML();
				return pdConverter.parseSBGNFile(szInSBGNFileName, szOutGraphMLFileName);
			} else if (((org.sbgn.bindings.Map) sbgn.getMap()).getLanguage().equals("activity flow")) {
				AF2GraphML afConverter = new AF2GraphML();
				return afConverter.parseSBGNFile(szInSBGNFileName, szOutGraphMLFileName);
			} else {
				System.out.println("Unsupported SBGN language: " + ((org.sbgn.bindings.Map) sbgn.getMap()).getLanguage());
			}
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return false;
	}
}
