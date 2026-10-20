package fr.eisbm.GraphMLHandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SBGN-ML ids are of type xsd:ID, i.e. NCName: they start with a letter or an
 * underscore and contain no colon. yEd ids do not always fit (nested nodes
 * "n1::n0", resources "1"), so the file would not validate. The ids that do not
 * fit, and ids used twice (e.g. "cardinality_2" on several arcs), are renamed.
 * Arcs refer to glyphs and ports as objects, so they follow; the references
 * written as text in the extensions (render idList, annotation rdf:about) are
 * updated here.
 */
public class IdHandler {

	private final java.util.Map<String, String> idMap = new HashMap<String, String>();
	private final Set<String> usedIds = new HashSet<String>();
	private final Set<String> assignedIds = new HashSet<String>();

	public void makeValidIds(Map map) {
		collectIds(map.getGlyph());
		for (Arc arc : map.getArc()) {
			usedIds.add(arc.getId());
			collectIds(arc.getGlyph());
		}

		renameGlyphs(map.getGlyph(), null);
		for (Arc arc : map.getArc()) {
			arc.setId(validId(arc.getId(), null));
			renameGlyphs(arc.getGlyph(), arc.getId());
		}

		if (!idMap.isEmpty()) {
			updateReferences(map);
			for (Glyph glyph : map.getGlyph()) {
				updateReferences(glyph);
			}
			for (Arc arc : map.getArc()) {
				updateReferences(arc);
			}
		}
	}

	private void collectIds(List<Glyph> glyphList) {
		for (Glyph glyph : glyphList) {
			usedIds.add(glyph.getId());
			for (Port port : glyph.getPort()) {
				usedIds.add(port.getId());
			}
			collectIds(glyph.getGlyph());
		}
	}

	private void renameGlyphs(List<Glyph> glyphList, String szParentId) {
		for (Glyph glyph : glyphList) {
			glyph.setId(validId(glyph.getId(), szParentId));
			for (Port port : glyph.getPort()) {
				port.setId(validId(port.getId(), glyph.getId()));
			}
			renameGlyphs(glyph.getGlyph(), glyph.getId());
		}
	}

	private String validId(String szId, String szParentId) {
		if (szId == null) {
			return szId;
		}
		if (isNCName(szId) && !assignedIds.contains(szId)) {
			assignedIds.add(szId);
			return szId;
		}
		String szNewId = szId.replaceAll("::", "_").replaceAll("[^A-Za-z0-9_.-]", "_");
		if (!isNCName(szNewId)) {
			// e.g. a resource id "1": named after its parent glyph or arc
			szNewId = ((szParentId != null) ? szParentId + "_" : "id_") + szNewId;
		}
		String szUniqueId = szNewId;
		for (int i = 2; usedIds.contains(szUniqueId) || assignedIds.contains(szUniqueId); i++) {
			szUniqueId = szNewId + "_" + i;
		}
		assignedIds.add(szUniqueId);
		if (!isNCName(szId) && !idMap.containsKey(szId)) {
			idMap.put(szId, szUniqueId);
		}
		return szUniqueId;
	}

	private static boolean isNCName(String szId) {
		return szId.matches("[A-Za-z_][A-Za-z0-9_.-]*");
	}

	private void updateReferences(SBGNBase element) {
		if (element.getExtension() != null) {
			for (Element e : element.getExtension().getAny()) {
				updateReferences(e);
			}
		}
		if (element instanceof Glyph) {
			for (Glyph child : ((Glyph) element).getGlyph()) {
				updateReferences(child);
			}
		}
	}

	private void updateReferences(Element e) {
		NamedNodeMap attributes = e.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attr = (Attr) attributes.item(i);
			String szValue = attr.getValue();
			if (attr.getName().equals("idList")) {
				StringBuilder sb = new StringBuilder();
				for (String szId : szValue.trim().split("\\s+")) {
					sb.append(sb.length() > 0 ? " " : "").append(idMap.getOrDefault(szId, szId));
				}
				attr.setValue(sb.toString());
			} else if (szValue.startsWith("#") && idMap.containsKey(szValue.substring(1))) {
				attr.setValue("#" + idMap.get(szValue.substring(1)));
			}
		}
		NodeList children = e.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				updateReferences((Element) children.item(i));
			}
		}
	}
}
