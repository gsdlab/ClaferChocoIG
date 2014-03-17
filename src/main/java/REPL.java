import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import joptsimple.OptionSet;

import org.clafer.ast.AstClafer;
import org.clafer.ast.AstModel;
import org.clafer.collection.Triple;
import org.clafer.compiler.ClaferCompiler;
import org.clafer.compiler.ClaferOptimizer;
import org.clafer.compiler.ClaferSolver;
import org.clafer.compiler.ClaferUnsat;
import org.clafer.instance.InstanceClafer;
import org.clafer.instance.InstanceModel;
import org.clafer.javascript.Javascript;
import org.clafer.objective.Objective;
import org.clafer.scope.Scope;

public class REPL {
	private static int instanceID = 0; // id of an instance previously been generated 
	private static int optimalInstanceID = 0; // id of an instance previously been generated 
	
	public static void runREPL(File inputFile, OptionSet options) throws Exception {
		
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
		
		String scopesFile = inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 3) + ".cfr-scope";		

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
				System.out.println("The model contains string clafers, which are currently not supported in Choco-based IG.\nPlease press \"Quit\" or \"Stop\" to exit this instance generator and try another one.");
			}
			else	
			{
				System.out.println("Unhandled compilation error occured. Please report this problem.");
				System.out.println(e.getMessage());
			}
			
			String s = "";
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while(!(s = br.readLine()).equals(commandExit))
			{
				
			}

			return;
		}
		
		AstModel model = modelTripple.getFst();        
		Scope scope = modelTripple.getSnd();

    	if (options.has("scope"))
    	{
        	scope = scope.toBuilder().defaultScope((int) options.valueOf("scope")).toScope();
    	}

    	if (options.has("maxint"))
    	{
			int scopeHigh = (int)options.valueOf("maxint");
			int scopeLow = -(scopeHigh + 1);
			scope = scope.toBuilder().intLow(scopeLow).intHigh(scopeHigh).toScope();
    	}	
    	else
    	{
    		/* setting the default int range */
    		int scopeHighDef = 127;
    		int scopeLowDef = -(scopeHighDef + 1);
    		scope = scope.toBuilder().intLow(scopeLowDef).intHigh(scopeHighDef).toScope();
    	}
		
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
				else if (command.equals(commandMinUnsat)) // unsat
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
						
						System.out.println("Switched to the single-objective optimization mode.");
						System.out.println("Use the same command to switch back to normal mode.");
						System.out.println("Use 'Next' command to get the next optimal instance.");
						System.out.println();
						currentMode = Mode.Soo;
						optimalInstanceID = 0;
			        	
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
			        	InstanceModel instance = optimizer.instance();
			        	optimalInstanceID++;
						System.out.println("=== Optimal Instance " + optimalInstanceID + " ===\n");

			            for (InstanceClafer c : instance.getTopClafers())
			            {
			            	Utils.printClafer(c, System.out);
			            }
					}
					else
					{
						System.out.println("No more optimal instances found.");						
					}
				}
				else
				{
					System.out.println("Invalid command in the SOO mode: " + s);				
					System.out.println("If you believe the command is valid, please switch to the normal mode and try again.");
				}
				
			}
		}
		
		System.out.println("Exit command");		
	}

	private static void nextInstance(ClaferSolver solver) throws IOException 
	{
		if (solver == null)
		{
			System.out.println("Could not start the instantiation");
			return;
		}

		if (solver.find())
		{
			instanceID++;
			System.out.println("=== Instance " + instanceID + " ===\n");
			
            InstanceModel instance = solver.instance();
            for (InstanceClafer c : instance.getTopClafers())
            {
            	Utils.printClafer(c, System.out);
            }
            System.out.println("--- instance " + (instanceID) + " ends ---\n");    			
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

enum Mode{
	IG,
	Soo
}
