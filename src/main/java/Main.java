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
		String commandReload = "r";
		String commandScopeGlobal = "globalScope";
		String commandScopeIndividual = "individualScope";
		String commandMinUnsat = "minUnsat";
		String commandUnsatCore = "unsatCore";
				
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
		
		ClaferSolver solver = null;
		
		try
		{		
			solver = ClaferCompiler.compile(model, scope); 
		}
		catch (Exception e)
		{
			solver = null;
			System.out.println(e.getMessage());
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = "";
		
		while(!(s = br.readLine()).equals(commandExit))
		{
			s = s.trim();
			String commandParts[] = s.split(" ");
			if (commandParts.length == 0)
			{
				System.out.println("Empty Command");
				continue;
			}
			
			String command = commandParts[0];
			
			if (command.equals(commandNext)) // next instance
			{
				if (solver == null)
				{
					try
					{		
						solver = ClaferCompiler.compile(model, scope); 
					}
					catch (Exception e)
					{
						solver = null;
						System.out.println(e.getMessage());
					}					
				}
			
				if (solver == null)
				{
					System.out.println("Could not start the instantiation");
				}
				else
				{
					if (solver.find())
					{
				        System.out.println(solver.instance());
					}
					else
					{
						System.out.println("No more instances found. Please consider increasing scopes");						
					}
				}
			}
			else if (command.equals(commandMinUnsat)) // reloading
			{
				System.out.println("Min UNSAT command:");					
				ClaferUnsat unsat = ClaferCompiler.compileUnsat(model, scope);
				// Print the Min-Unsat and near-miss example.
				System.out.println(unsat.minUnsat());		
			}
			else if (command.equals(commandUnsatCore)) // reloading
			{
				System.out.println("UNSAT Core command:");					
				ClaferUnsat unsat = ClaferCompiler.compileUnsat(model, scope);
				// Print the Min-Unsat and near-miss example.
				System.out.println(unsat.unsatCore());
			}			
			else if (command.equals(commandReload)) // reloading
			{
				try
				{		
					solver = ClaferCompiler.compile(model, scope); 
				}
				catch (Exception e)
				{
					solver = null;
					System.out.println(e.getMessage());
				}
				
				if (solver == null)
				{
					System.out.println("Could not reload");					
				}
				else
				{
					System.out.println("Reloaded");					
				}
				
			}
			else if (command.equals(commandScopeGlobal))
			{				
				System.out.println("Global scope: " + s);				

				if (commandParts.length != 2)
				{
					System.out.println("The format of the command is: '" + commandScopeGlobal + " <integer>'");
					System.out.println("Given: '" + s + "'");
					continue;
				}

				int scopeValue;
				
				try{
					scopeValue = Integer.parseInt(commandParts[1]);
				}
				catch(Exception e)
				{
					System.out.println("The scope has to be an integer number. Given '" + commandParts[1] + "'");
					continue;					
				}

				scope = scope.toBuilder().adjustDefaultScope(scopeValue).toScope();
				try
				{		
					solver = ClaferCompiler.compile(model, scope); 
				}
				catch (Exception e)
				{
					solver = null;
					System.out.println(e.getMessage());
					continue;
				}					
				
				System.out.println("Model is ready after the scope change");
			}
			else if (command.equals(commandScopeIndividual))
			{
				System.out.println("Individual scope: " + s);

				if (commandParts.length != 3)
				{
					System.out.println("The format of the command is: '" + commandScopeGlobal + " <clafer> <integer>'");
					System.out.println("Given: '" + s + "'");
					continue;
				}								
								
				String claferName = commandParts[1];
				int claferScopeValue;
				
				try{
					claferScopeValue = Integer.parseInt(commandParts[2]);
				}
				catch(Exception e)
				{
					System.out.println("The scope has to be an integer number. Given '" + commandParts[2] + "'");
					continue;					
				}
				
				AstClafer clafer = Utils.getModelChildByName(model, claferName);
				if (clafer == null)
				{
					System.out.println("The clafer is not found: '" + claferName + "'");
					continue;
				}
					
				scope = scope.toBuilder().adjustScope(clafer, claferScopeValue).toScope();
				
				try
				{		
					solver = ClaferCompiler.compile(model, scope); 
				}
				catch (Exception e)
				{
					solver = null;
					System.out.println(e.getMessage());
					continue;
				}					
				
				System.out.println("Model is ready after the scope change");
			}
			else
			{
				System.out.println("Unhandled command: " + s);				
			}
		}
		
		System.out.println("Exit command");		
	}	
	
}
