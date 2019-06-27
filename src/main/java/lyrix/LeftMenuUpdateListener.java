package lyrix;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface LeftMenuUpdateListener {
    void update(String xmlPath) throws ParserConfigurationException, SAXException, IOException;
}
