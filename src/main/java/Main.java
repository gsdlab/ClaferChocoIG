import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.clafer.ast.*;

import static org.clafer.ast.Asts.*;

import org.clafer.collection.Pair;
import org.clafer.collection.Triple;
import org.clafer.compiler.*;
import org.clafer.scope.*;
import org.clafer.instance.InstanceClafer;
import org.clafer.instance.InstanceModel;
import org.clafer.javascript.Javascript;
import org.clafer.objective.Objective;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Main
{

	public static void main(String[] args) throws Exception {
		
		String commandExit = "q";
		String commandNext = "n";
		String commandScopeGlobal = "globalScope";
		String commandScopeIndividual = "globalScope";
		
		
		
//		String currentDir = System.getProperty("user.dir");
		
//		System.out.println("Current Directory: " + currentDir);
		
		if (args.length < 1)
		{
			throw new Exception("Not Enough Arguments. Need Choco JS File Path");
		}
		
		String fileName = args[0];
				
		File inputFile = new File(fileName);
		
		if (!inputFile.exists())
		{
			throw new Exception("File Does Not Exist");
		}

		//----------------------------------------
		// Running the model itself(instantiating) 
		//----------------------------------------
		
		Triple<AstModel, Scope, Objective[]> modelTripple = Javascript.readModel(inputFile);

		AstModel model = modelTripple.getFst();        
		Scope scope = modelTripple.getSnd();
		
//		scope.toBuilder().
		
//		ClaferSolver solver = ClaferCompiler.compile(model, scope); 
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = "";
		
		while((s = br.readLine()) != commandExit)
		{
			System.out.println(s);
		}
		
//        solver.find();
        
//        System.out.println(solver.instance());
	}

}
