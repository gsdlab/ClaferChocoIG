import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.clafer.ast.AstAbstractClafer;
import org.clafer.ast.AstClafer;
import org.clafer.ast.AstConcreteClafer;
import org.clafer.ast.AstModel;
import org.clafer.collection.Pair;
import org.clafer.instance.InstanceClafer;
import org.clafer.instance.InstanceModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Utils {
	
	private static String claferFilter (String s)
	{
		return s.replaceAll("c[^_]*_", "");
	}

	private static String join(Collection<?> col, String delim) {
	    StringBuilder sb = new StringBuilder();
	    Iterator<?> iter = col.iterator();
	    if (iter.hasNext())
	        sb.append(iter.next().toString());
	    while (iter.hasNext()) {
	        sb.append(delim);
	        sb.append(iter.next().toString());
	    }
	    return sb.toString();
	}
	
	public static AstConcreteClafer getConcreteClaferChildByName(AstClafer root, String name) {
		// TODO Auto-generated method stub

		List<AstConcreteClafer> children = root.getChildren();
		
		for (AstConcreteClafer clafer : children)
		{
			if (clafer.getName().equals(name))
			{
				return clafer;
			}
		}

		for (AstConcreteClafer clafer : children)
		{
			if (clafer.hasChildren())
			{
				AstConcreteClafer result = getConcreteClaferChildByName(clafer, name);
				if (result != null)
					return result;
			}
		}
		
		return null;
	}	
    
	public static AstClafer getModelChildByName(AstModel model, String name) {
		// TODO Auto-generated method stub
		
		List<AstAbstractClafer> abstractChildren = model.getAbstracts();
		
		for (AstAbstractClafer clafer : abstractChildren)
		{
			if (clafer.getName().equals(name))
			{
				return clafer;
			}
		}

		for (AstAbstractClafer clafer : abstractChildren)
		{
			AstClafer foundchild = getConcreteClaferChildByName(clafer, name);
			if (foundchild != null)
			{
				return foundchild;
			}
		}		

		
		List<AstConcreteClafer> concreteChildren = model.getChildren();
		
		for (AstConcreteClafer clafer : concreteChildren)
		{
			if (clafer.getName().equals(name))
			{
				return clafer;
			}
		}

		for (AstConcreteClafer clafer : concreteChildren)
		{
			AstClafer foundchild = getConcreteClaferChildByName(clafer, name);
			if (foundchild != null)
			{
				return foundchild;
			}
		}		
		
		return null;
	}	
    
    
    
}
