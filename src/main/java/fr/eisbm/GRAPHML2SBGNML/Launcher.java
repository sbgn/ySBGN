package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;

import javafx.application.Application;

/**
 * This class is needed to avoid having the main class extending Application.
 * This was causing some problem with maven packaging.
 * See: https://stackoverflow.com/a/38133937
 *
 * Without arguments the GUI is started. With arguments the file is converted
 * on the command line, without the GUI:
 * java -jar ySBGN.jar map.graphml [map.sbgn] or java -jar ySBGN.jar map.sbgn [map.graphml]
 */
public class Launcher {
	public static void main(String[] args) {
		if (args.length == 0) {
			Application.launch(App.class, args);
			return;
		}
		System.exit(runCommandLine(args));
	}

	static int runCommandLine(String[] args) {
		if (args[0].equals("-h") || args[0].equals("--help") || args.length > 2) {
			System.out.println("Usage: java -jar ySBGN.jar <input.graphml> [output.sbgn]");
			System.out.println("       java -jar ySBGN.jar <input.sbgn|input.xml> [output.graphml]");
			System.out.println("Without arguments the graphical interface is started.");
			return args.length > 2 ? 2 : 0;
		}

		String szInput = args[0];
		if (!new File(szInput).isFile()) {
			System.err.println("Input file not found: " + szInput);
			return 2;
		}

		String szLower = szInput.toLowerCase();
		boolean bConversion;
		if (szLower.endsWith(".graphml")) {
			String szOutput = (args.length > 1) ? args[1] : szInput.substring(0, szInput.length() - 8).concat(".sbgn");
			bConversion = GraphML2SBGNML.convert(szInput, szOutput);
		} else if (szLower.endsWith(".sbgn") || szLower.endsWith(".xml")) {
			String szOutput = (args.length > 1) ? args[1]
					: szInput.substring(0, szInput.lastIndexOf(".")).concat("_generated.graphml");
			bConversion = SBGNML2GraphML.convert(szInput, szOutput);
		} else {
			System.err.println("Unknown input type (expected .graphml, .sbgn or .xml): " + szInput);
			return 2;
		}
		return bConversion ? 0 : 1;
	}
}
