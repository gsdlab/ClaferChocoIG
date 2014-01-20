import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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

	private static int instanceID = 0; // id of an instance previously been generated 
	
	public static void main(String[] args) throws Exception {
		
		String commandExit = "q";
		String commandNext = "n";
		String commandReload = "r";
		String commandScopeGlobal = "globalScope";
		String commandScopeInt = "maxInt";
		String commandScopeIndividual = "scope";
		String commandMinUnsat = "minUnsat";
		String commandUnsatCore = "unsatCore";
		String commandListScopes = "saveScopes";
		String commandSooMode = "sooMode";
		
		String commandScopeIncGlobal = "incGlobalScope";
		String commandScopeIncIndividual = "incScope";
		
		if (args.length < 1)
		{
			throw new Exception("Not Enough Arguments. Must be at least one");
		}

		if (args[0].equals("--version"))
		{
			Properties configFile = new Properties();
			try {
				configFile.load(Main.class.getClassLoader().getResourceAsStream("version.properties"));
				String name = configFile.getProperty("name");
				String releaseDate = configFile.getProperty("releasedate");
				String version = configFile.getProperty("version");
				System.out.println(name + " v" + version + "." + releaseDate);
			} catch (IOException e) {
	 
				e.printStackTrace();
			}
						
			return;
		}
				
		String fileName = args[0];
		
		File inputFile = new File(fileName);
		
		if (!inputFile.exists())
		{
			throw new Exception("File Does Not Exist");
		}

		String scopesFile = fileName.substring(0, fileName.length() - 3) + ".cfr-scope";		

		//----------------------------------------
		// Running the model itself(instantiating) 
		//----------------------------------------

		Triple<AstModel, Scope, Objective[]> modelTripple = null;		
		
		try{
			modelTripple = Javascript.readModel(inputFile);
		}
		catch(Exception e)
		{
			if (e.getMessage().indexOf("ReferenceError: \"string\" is not defined.") >= 0)
			{
				System.out.println("Error: strings are not supported in this ClaferChocoIG implementation. Exiting...");
			}
			else	
			{
				System.out.println(e.getMessage());
			}
			
			return;
		}
		
		AstModel model = modelTripple.getFst();        
		Scope scope = modelTripple.getSnd();
		
		Mode currentMode = Mode.IG; // start with IG mode
		
		ClaferSolver solver = null;
		ClaferOptimizer optimizer = null;
		
		solver = compileModel(model, scope);
		
		if (solver != null)
		{
			nextInstance(solver);
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
			
			if (currentMode == Mode.IG) // normal mode
			{
				
				if (command.equals(commandNext)) // next instance
				{
					if (solver == null)
					{
						solver = compileModel(model, scope);				
					}
					
					nextInstance(solver);
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
					solver = compileModel(model, scope);
					
					if (solver == null)
					{
						System.out.println("Could not reload");					
					}
					else
					{
						System.out.println("Reset");					
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
	
					scope = scope.toBuilder().defaultScope(scopeValue).toScope();
					solver = compileModel(model, scope);
					
					if (solver != null)						
						System.out.println("Model is ready after the scope change");
				}
				else if (command.equals(commandScopeIncGlobal))
				{				
					System.out.println("Increase global scope: " + s);				
	
					if (commandParts.length != 2)
					{
						System.out.println("The format of the command is: '" + commandScopeIncGlobal + " <integer>'");
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
					solver = compileModel(model, scope);
					
					if (solver != null)						
						System.out.println("Model is ready after the scope change");
				}
				else if (command.equals(commandScopeInt))
				{				
					System.out.println("Max Integer: " + s);				
	
					if (commandParts.length != 2)
					{
						System.out.println("The format of the command is: '" + commandScopeInt + " <integer>'");
						System.out.println("Given: '" + s + "'");
						continue;
					}
	
					int scopeHigh;
					
					try{
						scopeHigh = Integer.parseInt(commandParts[1]);
					}
					catch(Exception e)
					{
						System.out.println("Expected integer numbers. Given '" + commandParts[1] + "' '" + commandParts[2] + "'");
						continue;					
					}
	
					int scopeLow = -(scopeHigh + 1);
					scope = scope.toBuilder().intLow(scopeLow).intHigh(scopeHigh).toScope();
					solver = compileModel(model, scope);
					
					if (solver != null)						
						System.out.println("Model is ready after the scope change");

				}
				else if (command.equals(commandListScopes)) // getting list of scopes
				{
					List<ClaferNameScopePair> claferScopePairs = new ArrayList<ClaferNameScopePair>();
					
					List<AstClafer> allClafers = Utils.getAllModelClafers(model);
					
					for (AstClafer curClafer: allClafers)
					{
						int curScope;
						
						try{
							curScope = scope.getScope(curClafer);
						}
						catch(Exception e)
						{
							curScope = 0;
						}
						
						claferScopePairs.add(new ClaferNameScopePair(curClafer.getName(), curScope));
					}		
					
					Collections.sort(claferScopePairs);
					
					Utils.produceScopeFile(claferScopePairs, scopesFile);
				}
				else if (command.equals(commandScopeIndividual))
				{
					System.out.println("Individual scope: " + s);
	
					if (commandParts.length != 3)
					{
						System.out.println("The format of the command is: '" + commandScopeIndividual + " <clafer> <integer>'");
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
						
					scope = scope.toBuilder().setScope(clafer, claferScopeValue).toScope();					
					solver = compileModel(model, scope);
					
					if (solver != null)						
						System.out.println("Model is ready after the scope change");

				}
				else if (command.equals(commandScopeIncIndividual))
				{
					System.out.println("Increase individual scope: " + s);
	
					if (commandParts.length != 3)
					{
						System.out.println("The format of the command is: '" + commandScopeIncIndividual + " <clafer> <integer>'");
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
					solver = compileModel(model, scope);
					
					if (solver != null)						
						System.out.println("Model is ready after the scope change");
				}
				else if (command.equals(commandSooMode))
				{
					
			        Objective[] goals = modelTripple.getThd();
			        if (goals.length == 0) {
						System.out.println("Cannot switch to the single-objective optimization mode, because there are no goals defined.");
			        } else if (goals.length > 1) {
						System.out.println("Cannot switch to the single-objective optimization mode, because there is more than one goal defined.");
			        }
			        else
			        {
						try
						{		
				            optimizer = ClaferCompiler.compile(model, 
				            		scope, 
				            		goals[0]);        				        	
						}
						catch (Exception e)
						{
							solver = null;
							System.out.println(e.getMessage());
							continue;
						}		
						
						System.out.println("Switched to the single-objective optimization mode. Use the same command to switch back. Use 'Next' command to get the next optimal instance.");
						System.out.println("-----------------------------------------------------");
						currentMode = Mode.Soo;
			        	
			        }
					
				}
				else
				{
					System.out.println("Unhandled command: " + s);				
				}
			}
			else // mode == SOO 
			{
				if (command.equals(commandSooMode))
				{
					optimizer = null;
					System.out.println("Switched back to the normal mode.");
					currentMode = Mode.IG;				
				}
				else if (command.equals(commandNext)) // next instance
				{
					if (optimizer.find())
					{
						
			        	Pair<Integer, InstanceModel> solution = optimizer.instance();

			        	int optimalValue = solution.getFst();
		                InstanceModel instance = solution.getSnd();
			            for (InstanceClafer c : instance.getTopClafers())
			            {
			            	Utils.printClafer(c, System.out);
			            }		            
			            System.out.println("Quality value: " + optimalValue);
						System.out.println("-----------------------------------------------------");
					}
					else
					{
						System.out.println("No more optimal instances found.");						
					}
				}
				else
				{
					System.out.println("Invalid command in the SOO mode: " + s);				
					System.out.println("If you believe the command is correct, please switch to the normal mode and try again.");
				}
				
			}
		}
		
		System.out.println("Exit command");		
	}

	private static void nextInstance(ClaferSolver solver) 
	{
		if (solver == null)
		{
			System.out.println("Could not start the instantiation");
			return;
		}

		if (solver.find())
		{
			instanceID++;
			System.out.println("=== Instance " + instanceID + " ===");
	        System.out.println(solver.instance());
		}
		else
		{
			System.out.println("No more instances found. Please consider increasing scopes");						
		}
	}

	private static ClaferSolver compileModel(AstModel model, Scope scope) 
	{
		ClaferSolver solver;
		instanceID = 0; // reset instance ID
		try
		{		
			solver = ClaferCompiler.compile(model, scope); 
		}
		catch (Exception e)
		{
			solver = null;			
			System.out.println(e.getMessage());
		}		
		
		return solver;
	}	
	
}
