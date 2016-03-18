package convertersaidaxydiff;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author Carlos
 */
public class ReadXML {
    
    private Document doc;
    
    public ReadXML(Document doc){
        this.doc = doc;
    }
    
    public Document leXML(String caminho){
        try{
            File fXmlFile = new File(caminho);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            this.doc = dBuilder.parse(fXmlFile);
            
            doc.getDocumentElement().normalize();
        }catch(Exception e){
            e.printStackTrace();
        }
        return this.doc;
    }
}
