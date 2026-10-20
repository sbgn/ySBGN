package fr.eisbm.GRAPHML2SBGNML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Converts the example maps GraphML -> SBGN-ML -> GraphML and checks that the
 * SBGN-ML validates, that every node, edge, state variable and unit of
 * information is kept both ways, and that the same input gives the same file.
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
				{ "example_files/tags/tags_generated.graphml" }, { "acsn_diagrams/cellcycle_v1.graphml" },
				// metabolismregulation.github.io ACLY map: two state variables P on one node
				{ "src/test/resources/F006-acly.graphml" } });
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
		int[] sbgnCounts = sbgnCounts(sbgn);
		assertEquals("nodes -> glyphs", graphmlCounts[0], sbgnCounts[0]);
		assertEquals("edges -> arcs", graphmlCounts[1], sbgnCounts[1]);
		assertEquals("state variables and units of information from the palette", graphmlCounts[2], sbgnCounts[2]);

		assertTrue("conversion to GraphML", SBGNML2GraphML.convert(sbgn.getPath(), back.getPath()));
		int[] backCounts = graphmlCounts(back);
		assertEquals("glyphs -> nodes", sbgnCounts[0], backCounts[0]);
		assertEquals("arcs -> edges", sbgnCounts[1], backCounts[1]);
		assertEquals("state variables and units of information -> labels", sbgnCounts[2] + sbgnCounts[3],
				backCounts[2] + backCounts[3]);

		assertTrue(GraphML2SBGNML.convert(graphml.getPath(), sbgn2.getPath()));
		assertEquals("same input, same output", new String(Files.readAllBytes(sbgn.toPath())),
				new String(Files.readAllBytes(sbgn2.toPath())));
	}

	private static Document parse(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(file);
	}

	/**
	 * Nodes, edges, and the labels after a node's first one: with an icon (a
	 * state variable or unit of information from the yEd SBGN palette) and with
	 * text only. yEd also leaves empty labels, which are not counted.
	 */
	private static int[] graphmlCounts(File file) throws Exception {
		Document doc = parse(file);
		NodeList nodes = doc.getElementsByTagNameNS("*", "node");
		int nIconLabels = 0;
		int nTextLabels = 0;
		for (int i = 0; i < nodes.getLength(); i++) {
			for (Element data : children((Element) nodes.item(i), "data")) {
				for (Element realizer : children(data, "GenericNode")) {
					List<Element> labels = children(realizer, "NodeLabel");
					for (Element label : labels.subList(Math.min(1, labels.size()), labels.size())) {
						if (label.hasAttribute("iconData")) {
							nIconLabels++;
						} else if (!"false".equals(label.getAttribute("hasText"))
								&& !label.getTextContent().trim().isEmpty()) {
							nTextLabels++;
						}
					}
				}
			}
		}
		return new int[] { nodes.getLength(), doc.getElementsByTagNameNS("*", "edge").getLength(), nIconLabels,
				nTextLabels };
	}

	private static List<Element> children(Element parent, String szLocalName) {
		List<Element> list = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && szLocalName.equals(child.getLocalName())) {
				list.add((Element) child);
			}
		}
		return list;
	}

	/**
	 * Glyphs, arcs, state variables and units of information made from palette
	 * labels, and units of information made from text labels (ids ending _uof_n).
	 */
	private static int[] sbgnCounts(File file) throws Exception {
		Document doc = parse(file);
		NodeList glyphs = doc.getElementsByTagNameNS("*", "glyph");
		int nGlyphs = 0;
		int nResources = 0;
		int nTextUnits = 0;
		for (int i = 0; i < glyphs.getLength(); i++) {
			Element glyph = (Element) glyphs.item(i);
			String szClass = glyph.getAttribute("class");
			if (szClass.equals("state variable") || szClass.equals("unit of information")) {
				if (glyph.getAttribute("id").matches(".*_uof_\\d+")) {
					nTextUnits++;
				} else {
					nResources++;
				}
			} else if (!SUBGLYPHS.contains(szClass)) {
				nGlyphs++;
			}
		}
		return new int[] { nGlyphs, doc.getElementsByTagNameNS("*", "arc").getLength(), nResources, nTextUnits };
	}
}
