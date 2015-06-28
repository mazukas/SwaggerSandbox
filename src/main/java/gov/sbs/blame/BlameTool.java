package gov.sbs.blame;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlameTool {
	public static void main(String argv[]) {
		
		Map<String, List<PmdViolation>> vMap = new HashMap<String, List<PmdViolation>>();

		try {

			String fileRoot = new File("").getAbsolutePath();
			
			File fXmlFile = new File("target/pmd.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList filesList = doc.getElementsByTagName("file");

			for (int fileCounter = 0; fileCounter < filesList.getLength(); fileCounter++) {

				Element fileNode = (Element) filesList.item(fileCounter);
				String fileName = fileNode.getAttributes().getNamedItem("name").getNodeValue();
				
				//Modify the path to what GIT will understand
				fileName = fileName.replace(fileRoot+"/", "");
				
				if (!vMap.containsKey(fileName)) {
					vMap.put(fileName, new ArrayList<PmdViolation>());
				}

				NodeList violationsList = fileNode.getElementsByTagName("violation");

				for (int violationCounter = 0; violationCounter < violationsList.getLength(); violationCounter++) {
					Node violationNode = violationsList.item(violationCounter);
					
					String startLine = violationNode.getAttributes().getNamedItem("beginline").getNodeValue();
					String endLine = violationNode.getAttributes().getNamedItem("endline").getNodeValue();
					String rule = violationNode.getAttributes().getNamedItem("rule").getNodeValue();
					String msg = violationNode.getTextContent();
					
					PmdViolation pmdViolation = new PmdViolation(fileName, new Integer(startLine), new Integer(endLine), rule, msg);
					
					vMap.get(fileName).add(pmdViolation);
					
					//System.out.println(pmdViolation);
					
				}
			}
			
			for (String key : vMap.keySet()) {
				System.out.println(vMap.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
