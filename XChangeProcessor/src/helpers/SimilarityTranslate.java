package helpers;

import java.io.StringWriter;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import br.uff.ic.gems.phoenix.exception.XmlParserException;
import org.w3c.dom.*;
import br.uff.ic.gems.phoenix.XmlParser;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SimilarityTranslate {

    private int ID = 0;

    /**
     * Cria um documento para representar um documento XLM.
     *
     * @return doc Um documento.
     */
    public static Document createDomDocument() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
        }
        return null;
    }

    public HashMap<String, String> documentWithIDsTEST(String similarityDiff){
        this.ID = 0;

        HashMap<String, String> output = new HashMap<String, String>();

        Document XMLwithID1 = createDomDocument();
        Document XMLwithID2 = createDomDocument();

        Document xml3 = null;
        try {
            xml3 = XmlParser.createDOMDocument(similarityDiff);
        } catch (XmlParserException e) {
            e.printStackTrace();
        }
        Node rootXML = xml3.getDocumentElement().getFirstChild();

        Element element1 = XMLwithID1.createElement(rootXML.getNodeName());
        XMLwithID1.appendChild(element1);

        Element element2 = XMLwithID2.createElement(rootXML.getNodeName());
        XMLwithID2.appendChild(element2);

        createXMLwithIDsNEWWWW(rootXML, XMLwithID1, XMLwithID2, element1, element2, true);

        output.put("left", toString(XMLwithID1));
        output.put("right", toString(XMLwithID2));

        return output;
    }

    private Element creatElementFromSide(Node no, Document doc, String side){
        Element newNode = doc.createElement(no.getNodeName());
        NamedNodeMap nodeAttrs = no.getAttributes();

        for(int i = 0; i < nodeAttrs.getLength(); i++){
            String attrName = nodeAttrs.item(i).getNodeName();
            if(side.equalsIgnoreCase("left")){
                if(attrName.startsWith("left:"))
                    newNode.setAttribute(attrName.replace("left:", ""), nodeAttrs.item(i).getNodeValue());
                else if(!attrName.startsWith("diff:") && !attrName.startsWith("right:"))
                    newNode.setAttribute(attrName, nodeAttrs.item(i).getNodeValue());
            } else {
                if(attrName.startsWith("right:") && side.equalsIgnoreCase("right"))
                    newNode.setAttribute(attrName.replace("right:", ""), nodeAttrs.item(i).getNodeValue());
                else if(!attrName.startsWith("diff:") && !attrName.startsWith("left:"))
                    newNode.setAttribute(attrName, nodeAttrs.item(i).getNodeValue());
            }
        }

        return newNode;
    }

    private void createXMLwithIDsNEWWWW(Node no, Document doc1, Document doc2, Element e1, Element e2, boolean first) {
        if (no.getFirstChild().getNodeName().equals("diff:value")) {
            NamedNodeMap attributes = (NamedNodeMap) no.getFirstChild().getAttributes();
            Attr version1 = (Attr) attributes.getNamedItem("diff:left");
            Attr version2 = (Attr) attributes.getNamedItem("diff:right");

            Element newNode1 = creatElementFromSide(no, doc1, "left");
            e1.appendChild(newNode1);
            if (version1 != null) {
                newNode1.appendChild(doc1.createTextNode(version1.getValue()));
            }

            Element newNode2 = creatElementFromSide(no, doc2, "right");
            e2.appendChild(newNode2);
            if (version2 != null) {
                newNode2.appendChild(doc2.createTextNode(version2.getValue()));
            }
        } else {
            NodeList listChildren = no.getChildNodes();
            for (int i = 0; i < no.getChildNodes().getLength(); i++) {
                Node node = listChildren.item(i);
                NamedNodeMap nodeAttr = node.getAttributes();

                double sim = Double.parseDouble(nodeAttr.getNamedItem("diff:similarity").getNodeValue());
                if(sim == 0.0){
                    if(first) this.ID++;

                    if(nodeAttr.getNamedItem("diff:side").getNodeValue().equalsIgnoreCase("left")) {
                        copyToSide(node, doc1, e1, "left", first);
                    } else {
                        copyToSide(node, doc2, e2, "right", first);
                    }
                } else if(sim == 1.0){
                    if(first) this.ID++;
                    copyToSide(node, doc1, e1, "left", first);
                    copyToSide(node, doc2, e2, "right", first);
                } else {
                    if(first) {
                        this.ID++;
                        Element newE1 = creatElementFromSide(node, doc1, "left");
                        e1.appendChild(newE1);

                        Element newId1 = doc1.createElement("xchangeid");
                        e1.getLastChild().appendChild(newId1);
                        newId1.appendChild(doc1.createTextNode(Integer.toString(this.ID)));

                        Element newE2 = creatElementFromSide(node, doc2, "right");
                        e2.appendChild(newE2);

                        Element newId2 = doc2.createElement("xchangeid");
                        e2.getLastChild().appendChild(newId2);
                        newId2.appendChild(doc2.createTextNode(Integer.toString(this.ID)));

                        createXMLwithIDsNEWWWW(node, doc1, doc2, newE1, newE2, false);
                    } else
                        createXMLwithIDsNEWWWW(node, doc1, doc2, e1, e2, false);
                }
            }
        }
    }
    private void copyToSide(Node no, Document doc, Element e, String side, boolean first) {
        if (first){
            Element newE = creatElementFromSide(no, doc, side);
            e.appendChild(newE);

            Element newId = doc.createElement("xchangeid");
            e.getLastChild().appendChild(newId);
            newId.appendChild(doc.createTextNode(Integer.toString(this.ID)));

            copyRecursive(no, doc, newE, side);
        } else
            copyRecursive(no, doc, e, side);
    }

    private void copyRecursive(Node no, Document doc, Element e, String side) {
        if (!no.hasChildNodes() && no.getNodeType() == Node.TEXT_NODE) {
            Element novoNo = creatElementFromSide(no.getParentNode(), doc, side);
            e.appendChild(novoNo);
            novoNo.appendChild(doc.createTextNode(no.getNodeValue()));
        } else {

            NodeList listChildren = no.getChildNodes();
            for (int i = 0; i < no.getChildNodes().getLength(); i++) {
                if (listChildren.item(i).hasChildNodes()
                        && listChildren.item(i).getFirstChild().getNodeType() != Node.TEXT_NODE) {

                    Element novoNo = creatElementFromSide(listChildren.item(i), doc, side);
                    e.appendChild(novoNo);

                    copyRecursive(listChildren.item(i), doc, novoNo, side);
                } else
                    copyRecursive(listChildren.item(i), doc, e, side);
            }
        }
    }

    /**
     * Tranforma um documento em uma String.
     *
     * @param doc
     * @return writer.toString()
     */
    public String toString(Document doc) {
        Element root = doc.getDocumentElement();
        StringWriter writer = new StringWriter();

        try {
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            serializer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException ex) {
            //Mensagem de Exceção
        }
        return writer.toString();
    }
}
