
/**
 * Based on code made by Muhammad Imran
 */
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.impl.util.Uri;

public class XPathClient {

    Document doc;
    XPath xpath;
    DocumentBuilderFactory dbFactory;
    DocumentBuilder dBuilder;
    InputSource inputSource;

    public void loadXML(String xmlToParse) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        //creating xpath object
        getXPathObj();
        inputSource = new InputSource(new StringReader(xmlToParse));
        
    }

    public XPath getXPathObj() {

        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        return xpath;
    }
    
    public NodeList getPersonIDs(String xmlToParse)throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
    	loadXML(xmlToParse);
    	inputSource = new InputSource(new StringReader(xmlToParse));
    	NodeList nodes = (NodeList)xpath.evaluate("/people/person/idPerson",
        		inputSource, XPathConstants.NODESET);
    	 return nodes;
    }
    
    public Node getPersonById(int id, String xmlToParse )throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
    	//XPathExpression expr = xpath.compile("person[idPerson=' " + id + "']");
    	//loadXML(xmlToParse);
    	//inputSource = new InputSource(new StringReader(xmlToParse));
    	
    	Node node = (Node)xpath.evaluate("person[idPerson=' " + id + "']",
        		inputSource, XPathConstants.NODE);
    	//Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
    	 return node;
    }

    
    public NodeList getNodeListResult(String condition) throws XPathExpressionException {

    	XPathExpression expr = xpath.compile(condition);
        NodeList nodes = (NodeList) expr.evaluate(inputSource, XPathConstants.NODESET);
        
        return nodes;
        
    }
    
    public Node getNodeResult(String expression) throws XPathExpressionException{
      	XPathExpression expr = xpath.compile(expression);
        Node node = (Node) expr.evaluate(inputSource, XPathConstants.NODE);
        return node;
    }
    
    public Node getPersonFirstName(int id, String xmlToParse) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
    	loadXML(xmlToParse);
    	inputSource = new InputSource(new StringReader(xmlToParse));
    	System.err.println(id);
    	Node name = (Node) xpath.evaluate("person[idPerson=' " + id + "']/name",
        		inputSource, XPathConstants.NODE);
    	if(name == null) System.err.println("This is not gonna work");
    	return name;
    	
    }
    



}
