import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;

import joptsimple.OptionSet;

import org.clafer.ast.AstClafer;
import org.clafer.ast.AstModel;
import org.clafer.collection.Triple;
import org.clafer.compiler.ClaferCompiler;
import org.clafer.compiler.ClaferOptimizer;
import org.clafer.compiler.ClaferSearch;
import org.clafer.compiler.ClaferSolver;
import org.clafer.instance.InstanceClafer;
import org.clafer.instance.InstanceModel;
import org.clafer.javascript.Javascript;
import org.clafer.objective.Objective;
import org.clafer.scope.Scope;


public class Normal {

	public static void runNormal(File inputFile, OptionSet options, PrintStream outStream) throws Exception
	{
		//----------------------------------------
		// Running the model itself(instantiating) 
		//----------------------------------------
		System.out.println("Running Model...");
		
		Triple<AstModel, Scope, Objective[]> modelTriple = Javascript.readModel(inputFile);

		AstModel model = modelTriple.getFst();        
        Scope scope = modelTriple.getSnd();
        Objective[] objectives = modelTriple.getThd();
        
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

    	ClaferSearch solver;
    	if (objectives.length == 0)
    	{    		
    		solver = ClaferCompiler.compile(model, scope);         
    	}
    	else
    	{
    		solver = ClaferCompiler.compile(model, scope, objectives);         
    	}
    	
        System.out.println("Generating instances...");        

        int n = 0;
    	if (options.has("n"))
    	{
			n = (int)options.valueOf("n");
    	}
    	else 
    		n = -1;
        
    	int index = 0; // instance id
        while (solver.find()) 
    	{
        	if (n >= 0 && index == n)
        		break;
        	
        	outStream.println("=== Instance " + (++index) + " Begin ===\n");                    
            InstanceModel instance = solver.instance();
            for (InstanceClafer c : instance.getTopClafers())
            {
            	Utils.printClafer(c, outStream);
            }
            outStream.println("--- Instance " + (index) + " End ---\n");                    
    	}

        System.out.println("Generated " + index +" instance(s)");        
        
	}
	
}
