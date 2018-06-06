package fr.eisbm.GRAPHML2SBGNML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sbgn.bindings.Sbgn;

public class FileUtils {

	public static final String COM_YWORKS_SBGN_SIMPLE_CHEMICAL = "com.yworks.sbgn.SimpleChemical";
	public static final String COM_YWORKS_SBGN_PROCESS = "com.yworks.sbgn.Process";
	public static final String COM_YWORKS_SBGN_UNSPECIFIED_ENTITY = "com.yworks.sbgn.UnspecifiedEntity";
	public static final String COM_YWORKS_SBGN_MACROMOLECULE = "com.yworks.sbgn.Macromolecule";
	public static final String COM_YWORKS_SBGN_TAG = "com.yworks.sbgn.Tag";
	public static final String COM_YWORKS_SBGN_STATE_VARIABLE = "com.yworks.sbgn.StateVariable";
	public static final String COM_YWORKS_SBGN_OPERATOR = "com.yworks.sbgn.Operator";
	public static final String COM_YWORKS_SBGN_COMPLEX = "com.yworks.sbgn.Complex";
	public static final String COM_YWORKS_SBGN_SOURCE_AND_SINK = "com.yworks.sbgn.EmptySet";
	public static final String COM_YWORKS_SBGN_PERTURBING_AGENT = "com.yworks.sbgn.PerturbingAgent";
	public static final String COM_YWORKS_SBGN_PHENOTYPE = "com.yworks.sbgn.Phenotype";
	public static final String COM_YWORKS_SBGN_NUCLEIC_ACID_FEATURE = "com.yworks.sbgn.NucleicAcidFeature";
	public static final String COM_YWORKS_SBGN_SUBMAP = "com.yworks.sbgn.Submap";
	public static final String COM_YWORKS_SBGN_STYLE_MCOUNT = "com.yworks.sbgn.style.mcount";
	public static final String COM_YWORKS_SBGN_UNIT_OF_INFORMATION = "com.yworks.sbgn.UnitOfInformation";
	
	public static final String SBGN_OMITTED_PROCESS = "omitted process";
	public static final String SBGN_UNCERTAIN_PROCESS = "uncertain process";
	public static final String SBGN_DISSOCIATION = "dissociation";
	public static final String SBGN_ASSOCIATION = "association";
	public static final String SBGN_PROCESS = "process";
	public static final String SBGN_SOURCE_AND_SINK = "source and sink";
	public static final String SBGN_TAG = "tag";
	public static final String SBGN_NOT = "not";
	public static final String SBGN_AND = "and";
	public static final String SBGN_OR = "or";
	public static final String SBGN_COMPARTMENT = "compartment";
	public static final String SBGN_COMPLEX_MULTIMER = "complex multimer";
	public static final String SBGN_COMPLEX = "complex";
	public static final String SBGN_NUCLEIC_ACID_FEATURE = "nucleic acid feature";
	public static final String SBGN_PHENOTYPE = "phenotype";
	public static final String SBGN_NUCLEIC_ACID_FEATURE_MULTIMER = "nucleic acid feature multimer";
	public static final String SBGN_MACROMOLECULE = "macromolecule";
	public static final String SBGN_MACROMOLECULE_MULTIMER = "macromolecule multimer";
	public static final String SBGN_SUBMAP = "submap";
	public static final String SBGN_PERTURBING_AGENT = "perturbing agent";
	public static final String SBGN_UNSPECIFIED_ENTITY = "unspecified entity";
	public static final String SBGN_SIMPLE_CHEMICAL_MULTIMER = "simple chemical multimer";
	public static final String SBGN_SIMPLE_CHEMICAL = "simple chemical";
	public static final String SBGN_CONSUMPTION = "consumption";
	public static final String SBGN_PRODUCTION = "production";
	public static final String SBGN_CATALYSIS = "catalysis";
	public static final String SBGN_LOGIC_ARC = "logic arc";
	public static final String SBGN_EQUIVALENCE_ARC = "equivalence arc";
	public static final String SBGN_STIMULATION = "stimulation";
	public static final String SBGN_INHIBITION = "inhibition";
	public static final String SBGN_MODULATION = "modulation";
	public static final String SBGN_NECESSARY_STIMULATION = "necessary stimulation";
	public static final String SBGN_UNIT_OF_INFORMATION = "unit of information";
	public static final String SBGN_STATE_VARIABLE = "state variable";
	public static final String SBGN_CARDINALITY = "cardinality";
	
	public static final String BQMODEL_IS = "bqmodel:is";
	public static final String BQMODEL_IS_DESCRIBED_BY = "bqmodel:isDescribedBy";
	public static final String BQBIOL_IS = "bqbiol:is";
	public static final String BQBIOL_IS_DESCRIBED_BY = "bqbiol:isDescribedBy";
	
	public static final String Y_SMART_NODE_LABEL_MODEL_PARAMETER = "y:SmartNodeLabelModelParameter";
	public static final String Y_MODEL_PARAMETER = "y:ModelParameter";
	public static final String Y_SMART_NODE_LABEL_MODEL = "y:SmartNodeLabelModel";
	public static final String Y_LABEL_MODEL = "y:LabelModel";
	public static final String Y_SHAPE = "y:Shape";
	public static final String Y_GROUP_NODE = "y:GroupNode";
	public static final String Y_BORDER_INSETS = "y:BorderInsets";
	public static final String Y_INSETS = "y:Insets";
	public static final String Y_STATE = "y:State";
	public static final String Y_PROXY_AUTO_BOUNDS_NODE = "y:ProxyAutoBoundsNode";
	public static final String Y_GENERIC_GROUP_NODE = "y:GenericGroupNode";
	public static final String Y_REALIZERS = "y:Realizers";
	public static final String YFILES_FOLDERTYPE = "yfiles.foldertype";
	public static final String Y_BEND_STYLE = "y:BendStyle";
	public static final String Y_EDGE_LABEL = "y:EdgeLabel";
	public static final String Y_ARROWS = "y:Arrows";
	public static final String Y_LINE_STYLE = "y:LineStyle";
	public static final String Y_POINT = "y:Point";
	public static final String Y_PATH = "y:Path";
	public static final String Y_POLY_LINE_EDGE = "y:PolyLineEdge";
	public static final String Y_NODE_LABEL = "y:NodeLabel";
	public static final String Y_BORDER_STYLE = "y:BorderStyle";
	public static final String Y_FILL = "y:Fill";
	public static final String YED_NODE_REALIZER_ICON = "yed:NodeRealizerIcon";
	public static final String Y_RESOURCE = "y:Resource";
	public static final String Y_GEOMETRY = "y:Geometry";
	public static final String Y_GENERIC_NODE = "y:GenericNode";
	public static final String Y_PROPERTY = "y:Property";
	public static final String Y_STYLE_PROPERTIES = "y:StyleProperties";
	public static final String Y_SHAPE_NODE = "y:ShapeNode";
	
	
	public static final String IN_SBGN_FILE = "files/examples/test.sbgn";
	public static final String IN_YED_FILE = "files/examples/test.graphml";
	
	public static final int DEFAULT_FONT_SIZE = 10;
	
	
	public static Sbgn readFromFile(String szFileName) throws JAXBException {
		Sbgn result = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(szFileName));
			BOMskip(reader);
			String content = reader.lines().collect(Collectors.joining());

			// set given sbgn to always be 0.2 to avoid compatibility problems
			content = content.replaceFirst("http://sbgn\\.org/libsbgn/0\\.3", "http://sbgn.org/libsbgn/0.2");

			JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
			Unmarshaller unmarshaller = context.createUnmarshaller();
			result = (Sbgn) unmarshaller.unmarshal(new StringReader(content));
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Skip BOM char. BOM is present in output of Newt. See
	 * https://stackoverflow.com/a/18275066 courtesy to Ludovic Roy
	 * 
	 * @param reader
	 * @throws IOException
	 */
	public static void BOMskip(Reader reader) throws IOException {
		reader.mark(1);
		char[] possibleBOM = new char[1];
		reader.read(possibleBOM);

		if (possibleBOM[0] != '\ufeff') {
			reader.reset();
		}
	}

}
