package gov.sbs.blame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlameTool {
	public static void main(String argv[]) {

		Map<String, List<PmdViolation>> vMap = new HashMap<String, List<PmdViolation>>();

		BlameTool blameSomeone = new BlameTool();

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

				// Modify the path to what GIT will understand
				fileName = fileName.replace(fileRoot + "/", "");
				
				Map<Integer, String> lineAndUser = blameSomeone.getWhoDidIt("git blame " + fileName);

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

					PmdViolation pmdViolation = new PmdViolation(	fileName,
																	new Integer(startLine), 
																	new Integer(endLine), 
																	rule,
																	msg);
					
					pmdViolation.setViolator(lineAndUser.get(pmdViolation.getStart()));

					vMap.get(fileName).add(pmdViolation);
				}
			}

			List<PmdViolation> allGuiltyParties = new ArrayList<PmdViolation>();
			for (String key : vMap.keySet()) {
				allGuiltyParties.addAll(vMap.get(key));
			}
			blameSomeone.generateReport(allGuiltyParties);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<Integer, String> getWhoDidIt(String command) {

		Map<Integer, String> lineAndPerson = new HashMap<Integer, String>();
		
		String nameRegex = Pattern.quote("(") + "(.*?)" + Pattern.quote(" ");
		Pattern namePattern = Pattern.compile(nameRegex);
		
		Pattern linePattern = Pattern.compile("\\s(\\w+)$");

		
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				//Need to clean the line up first to avoid picking up things in the code
				String[] cleanLine = line.split("\\)");
				line = cleanLine[0];
				
				String name = "";
				Integer lineNumber = null;
				
				Matcher nameMatcher = namePattern.matcher(line);
				while (nameMatcher.find()) {
				  name = nameMatcher.group(1); // Since (.*?) is capturing group 1
				  
				}

				Matcher lineMatcher = linePattern.matcher(line.trim());
				while(lineMatcher.find()){
					lineNumber = new Integer(lineMatcher.group().trim());
				}
				
				lineAndPerson.put(lineNumber, name);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lineAndPerson;
	}
	
	private void generateReport(List<PmdViolation> violations) {
		
		Collections.sort(violations, new Comparator<PmdViolation>() {
			public int compare(PmdViolation o1, PmdViolation o2) {
				return o1.getViolator().compareTo(o2.getViolator());
			}
		});
		
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("target/Blame.html"), "utf-8"));
		    writer.write(header());
		    
		    int counter = 0;
		    Map<String, Integer> violationCounts = new HashMap<String, Integer>();
		    Date now = new Date();
		    writer.write("<h1>Total team violations as of " + now + " : [" + violations.size() + "]</h1>");
		    
		    if (violations.size() == 0) {
		    	writer.write("<br/><img src=\"../WebContent/images/cabbagepatch.gif\"");
		    } else {
		    
			    String violatorName = "";
			    writer.write("<table>");
			    StringBuffer sb = new StringBuffer();
				for (PmdViolation v : violations) {
					
					if (!violatorName.equals(v.getViolator())) {
						counter = 1;
						violatorName = v.getViolator();
						sb.append("<tr>");
							sb.append("<td colspan=3>");
								sb.append("<b>" + v.getViolator() + " <font style=\"color: red;\">(Total Violations {"+v.getViolator()+"})</font></b>");
							sb.append("</td>");	
						sb.append("</tr>");
					}
					
					violationCounts.put("{"+v.getViolator()+"}", new Integer(counter++));
					
					sb.append("<tr>");
						sb.append("<td>");
							sb.append("<b>" + v.getFileName() + "</b>");
						sb.append("</td>");
						sb.append("<td style=\"padding-left: 10px; padding-right: 10px;\">");
							sb.append("Line " + v.getStart());
						sb.append("</td>");
						sb.append("<td>");
							sb.append(v.getRule());
						sb.append("</td>");
					sb.append("</tr>");
					sb.append("<tr>");
						sb.append("<td colspan=3 style=\"padding-left: 10px;\">");
							sb.append(v.getMsg());
						sb.append("</td>");
					sb.append("</tr>");
				}
				
				String rawOutput = sb.toString();
				String leadingName = "";
				int leadingCount = 0;
				for (String name : violationCounts.keySet()) {
					Integer violationCount = violationCounts.get(name);
					rawOutput = rawOutput.replace(name, violationCount.toString());
					
					if (leadingCount < violationCount) {
						leadingCount = violationCount;
						leadingName = name.replaceAll("\\{", "").replaceAll("\\}", "");
					}
				}
				writer.write(rawOutput);
				
				writer.write("</table>");
				
				writer.write("<br /><br />And the biggest violator is <u>" + leadingName + "</u> with " + leadingCount + " violations"); 
				
				writer.write("<br/><img src=\"../WebContent/images/notsofast.gif\"");
		    }
			
		    writer.write(footer());
		    
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {ex.printStackTrace();}
		}
	}
	
	private String header() {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head></head><body>");
		return sb.toString();
	}
	
	private String footer() {
		StringBuffer sb = new StringBuffer();
		sb.append("</body></html>");
		return sb.toString();
	}

}
