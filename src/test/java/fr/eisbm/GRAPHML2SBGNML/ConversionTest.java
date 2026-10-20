package fr.eisbm.GRAPHML2SBGNML;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sbgn.SbgnUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Converts the example maps GraphML -> SBGN-ML -> GraphML and checks that the
 * SBGN-ML validates, that every node and edge is kept both ways, and that the
 * same input gives the same file.
 */
@RunWith(Parameterized.class)
public class ConversionTest {

	private static final Set<String> SUBGLYPHS = new HashSet<String>(Arrays.asList("unit of information",
			"state variable", "cardinality", "terminal", "outcome", "existence", "location", "entity"));

	@Parameters(name = "{0}")
	public static Collection<Object[]> maps() {
		return Arrays.asList(new Object[][] { { "example_files/pathways/glycolysis_generated.graphml" },
				{ "example_files/pathways/CAMKII_colored_generated.graphml" },
				{ "example_files/pathways/central_plant_metabolism_generated.graphml" },
				{ "example_files/process_port_diagrams/ProcessPorts.graphml" },
				{ "example_files/tags/tags_generated.graphml" }, { "acsn_diagrams/cellcycle_v1.graphml" } });
	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private final String szMap;

	public ConversionTest(String szMap) {
		this.szMap = szMap;
	}

	@Test
	public void convertsBothWays() throws Exception {
		File graphml = new File(szMap);
		File sbgn = folder.newFile("map.sbgn");
		File sbgn2 = folder.newFile("map2.sbgn");
		File back = folder.newFile("back.graphml");

		assertTrue("conversion to SBGN-ML", GraphML2SBGNML.convert(graphml.getPath(), sbgn.getPath()));
		assertTrue("SBGN-ML validates", SbgnUtil.isValid(sbgn));

		int[] graphmlCounts = graphmlCounts(graphml);
		assertArrayEquals("nodes and edges -> glyphs and arcs", graphmlCounts, sbgnCounts(sbgn));

		assertTrue("conversion to GraphML", SBGNML2GraphML.convert(sbgn.getPath(), back.getPath()));
		assertArrayEquals("glyphs and arcs -> nodes and edges", graphmlCounts, graphmlCounts(back));

		assertTrue(GraphML2SBGNML.convert(graphml.getPath(), sbgn2.getPath()));
		assertEquals("same input, same output", new String(Files.readAllBytes(sbgn.toPath())),
				new String(Files.readAllBytes(sbgn2.toPath())));
	}

	private static Document parse(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(file);
	}

	private static int[] graphmlCounts(File file) throws Exception {
		Document doc = parse(file);
		return new int[] { doc.getElementsByTagNameNS("*", "node").getLength(),
				doc.getElementsByTagNameNS("*", "edge").getLength() };
	}

	private static int[] sbgnCounts(File file) throws Exception {
		Document doc = parse(file);
		NodeList glyphs = doc.getElementsByTagNameNS("*", "glyph");
		int nGlyphs = 0;
		for (int i = 0; i < glyphs.getLength(); i++) {
			if (!SUBGLYPHS.contains(((Element) glyphs.item(i)).getAttribute("class"))) {
				nGlyphs++;
			}
		}
		return new int[] { nGlyphs, doc.getElementsByTagNameNS("*", "arc").getLength() };
	}
}
