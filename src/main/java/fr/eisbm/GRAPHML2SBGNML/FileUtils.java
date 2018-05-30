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
	
	public static final String IN_SBGN_ED_RECOMMAP_FILE = "reconmap/reconmap.sbgn";
	public static final String OUT_YED_RECONMAP_FILE = "reconmap/ASTHMAP1.graphml";
	public static final String OUT_SBGN_ED_RECOMMAP_FILE = "example-files/out_reconMap.sbgn";
	public static final String OUT_SBGN_ED_RECOMMAP_VANTED_FILE = "example-files/out_reconMap.sbgn";
	
	public static final String IN_SBGN_ED_FILE = "files/cam-camk_dependent_signaling_to_the_nucleus.xml";
	public static final String OUT_YED_FILE = "files/Step3_ReconMap_yEd_from_SBGN.graphml";
	public static final String OUT_SBGN_ED_FILE = "files/F018-estrogen-SBGNv02.sbgn";
	public static final String OUT_SBGN_ED_VANTED_FILE = "metabolism-regulation/working/F015-tag-SBGNv02.sbgn";

	public static final String IN_YED_METABOLIC_FILE = "examples-proteins/F003v10/F003v10.graphml";
	public static final String IN_GLYCOSLYSIS = "files/examples/glycolysis2.sbgn";
	public static final String OUT_GLYCOSLYSIS = "files/examples/glycolysis.graphml";
	public static final String IN_MAPK_CASCADE = "files/examples/mapk_cascade.sbgn";
	public static final String OUT_MAPK_CASCADE = "files/examples/mapk_cascade.graphml";
	public static final String IN_CENTRAL_PLANT_METABOLISM = "files/examples/central_plant_metabolism.sbgn";
	public static final String OUT_CENTRAL_PLANT_METABOLISM = "files/examples/central_plant_metabolism.graphml";

	public static final String OUT_SBGN_GLYCOSLYSIS = "files/examples/out_glycolysis2.sbgn";
	public static final String IN_GRAPHML_GLYCOSLYSIS = "files/examples/glycolysis.graphml";
	public static final String OUT_SBGN_MAPK_CASCADE = "files/examples/out_mapk_cascade.sbgn";
	public static final String IN_GRAPHML_MAPK_CASCADE = "files/examples/mapk_cascade.graphml";
	public static final String OUT_SBGN_CENTRAL_PLANT_METABOLISM = "files/examples/out_central_plant_metabolism.sbgn";
	public static final String IN_GRAPHML_CENTRAL_PLANT_METABOLISM = "files/examples/central_plant_metabolism.graphml";

	public static final String IN_ACTIVATED_STATALPHA = "files/examples/activated_stat1alpha_induction_of_the_irf1_gene.sbgn";
	public static final String OUT_ACTIVATED_STATALPHA = "files/examples/activated_stat1alpha_induction_of_the_irf1_gene.graphml";
	public static final String IN_INSULIN = "files/examples/insulin-like_growth_factor_signaling.sbgn";
	public static final String OUT_INSULIN = "files/examples/insulin-like_growth_factor_signaling.graphml";
	public static final String IN_INSULIN_SUBMAP_MAPK_CASCADE = "files/examples/insulin-like_growth_factor_signaling_withSubmap_mapk_cascade.sbgn";
	public static final String OUT_INSULIN_SUBMAP_MAPK_CASCADE = "files/examples/insulin-like_growth_factor_signaling_withSubmap_mapk_cascade.graphml";
	public static final String IN_NEURONAL_MUSCLE_SIGNALLING = "files/examples/neuronal_muscle_signalling.sbgn";
	public static final String OUT_NEURONAL_MUSCLE_SIGNALLING = "files/examples/neuronal_muscle_signalling.graphml";
	public static final String IN_EPIDERMAL_GROWTH_FACTOR_RECEPTOR_PATHWAY = "files/examples/epidermal_growth_factor_receptor_pathway.sbgn";
	public static final String OUT_EPIDERMAL_GROWTH_FACTOR_RECEPTOR_PATHWAY = "files/examples/epidermal_growth_factor_receptor_pathway.graphml";
	public static final String IN_PRINCIPLE_OF_THE_PCR = "files/examples/principle_of_the_pcr.sbgn";
	public static final String OUT_PRINCIPLE_OF_THE_PCR = "files/examples/principle_of_the_pcr.graphml";
	
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
