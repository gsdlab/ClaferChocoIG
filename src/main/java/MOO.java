import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import joptsimple.OptionSet;

import org.clafer.ast.AstClafer;
import org.clafer.ast.AstModel;
import org.clafer.collection.Triple;
import org.clafer.compiler.ClaferCompiler;
import org.clafer.compiler.ClaferOptimizer;
import org.clafer.instance.InstanceClafer;
import org.clafer.instance.InstanceModel;
import org.clafer.javascript.Javascript;
import org.clafer.objective.Objective;
import org.clafer.scope.Scope;


public class MOO {

	public static void runOptimization(File inputFile, OptionSet options) throws Exception
	{
		//----------------------------------------
		// Running the model itself(instantiating) 
		//----------------------------------------
		System.out.println("Running Model...");
		
		Triple<AstModel, Scope, Objective[]> modelTriple = Javascript.readModel(inputFile);

		AstModel model = modelTriple.getFst();
        
        Objective[] goals = modelTriple.getThd();
        if (goals.length == 0) {
            throw new Exception("No goals.");
        }
//        Objective goal = goals[0];
        
//        System.out.println(goal.isMaximize() ? "Maximize" : "Minimize");       	
        
        Scope scope = modelTriple.getSnd(); 
        
        if (options.has("testaadl")) // testing scopes for AADL model
		{
			Properties configFile = new Properties();
			try {
				configFile.load(Main.class.getClassLoader().getResourceAsStream("aadl.scopes"));
				
				for (Enumeration<Object> e = configFile.keys(); e.hasMoreElements();)
				{
					String key = (e.nextElement()).toString();					
					int value = Integer.parseInt(configFile.getProperty(key));
					System.out.println(key + " = " + value);
					
					if (key.equals("defaultScope"))
					{
						scope = scope.toBuilder().defaultScope(value).toScope();
						System.out.println("Set default scope: " + value);
					}
					else if (key.equals("maxInt"))
					{
						int scopeHigh = value;
						int scopeLow = -(scopeHigh + 1);
						scope = scope.toBuilder().intLow(scopeLow).intHigh(scopeHigh).toScope();
						System.out.println("Set maxInt: " + value);
					}
					else
					{
						AstClafer clafer = Utils.getModelChildByName(model, key);
						if (clafer == null)
						{
							System.out.println("The clafer is not found: '" + key + "'");
							continue;
						}
							
						scope = scope.toBuilder().setScope(clafer, value).toScope();					
						System.out.println("Set clafer scope: '" + key + "' = " + value);
					}
				}
				
			} catch (IOException e) {
	 
				e.printStackTrace();
			}						
		}
        else
        {
        	if (options.has("scope"))
        	{
            	scope = scope.toBuilder().defaultScope((int) options.valueOf("scope")).toScope();
        	}

        	if (options.has("maxint"))
        	{
				int scopeHigh = (int)options.valueOf("maxint");
				int scopeLow;
				
	        	if (options.has("minint"))
	        	{
	        		scopeLow = (int)options.valueOf("minint");
	        	}
	        	else
	        	{
	        		scopeLow = -(scopeHigh + 1);
	        	}
	        	
				scope = scope.toBuilder().intLow(scopeLow).intHigh(scopeHigh).toScope();
        	}
        }
        
        ClaferOptimizer solver = ClaferCompiler.compile(model, 
        		scope, 
        	    goals);         
        
        System.out.println("Generating optimal instances...");        
        
    	int index = 0; // optimal instance id
        while (solver.find()) 
    	{
            System.out.println("=== Instance " + (++index) + " Begin ===\n");                    
            InstanceModel instance = solver.instance();
            for (InstanceClafer c : instance.getTopClafers())
            {
            	Utils.printClafer(c, System.out);
            }
            System.out.println("--- Instance " + (index) + " End ---\n");                    
    	}

        System.out.println("All optimal instances within the scope generated\n");                    
		
	}
	
}
