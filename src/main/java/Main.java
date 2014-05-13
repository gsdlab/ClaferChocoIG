import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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

import joptsimple.OptionParser;
import joptsimple.OptionSet;

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
		
        OptionParser parser = new OptionParser() {
            {
                accepts( "file", "input file in Javascript format" ).withRequiredArg().ofType( File.class )
                    .describedAs( "Javascript file" );
                
                accepts( "testaadl", "test the AADL to Clafer model" );
                accepts( "repl", "run the tool in REPL (interactive) mode" );
                accepts( "prettify", "use simple and pretty output format (not formal)" );
                accepts( "moo", "run the tool in multi-objective optimization mode" );
                accepts( "version", "display the tool version" );
                accepts( "maxint", "specify maximum integer value" ).withRequiredArg().ofType( Integer.class );
                accepts( "minint", "specify minimum integer value" ).withRequiredArg().ofType( Integer.class );
                accepts( "n", "specify maximum number of instances" ).withRequiredArg().ofType( Integer.class );
                accepts( "scope", "override global scope value" ).withRequiredArg().ofType( Integer.class );

                accepts( "output", "output instances to file" ).withRequiredArg().ofType( File.class )
                .describedAs( "Text file" );

                accepts( "help", "show help").forHelp();                
            }
        };

        OptionSet options = parser.parse(args);

		if (options.has("version"))
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
		
		if (options.has("help"))
		{
			parser.printHelpOn(System.out);
			return;
		}
		
		if (!options.has( "file" ) ){
			throw new Exception("Input file must be given using --file argument");		
		}
		
//			String fileName = 
		File inputFile = (File) options.valueOf("file");
		
		if (!inputFile.exists())
		{
			throw new Exception("File does not exist: " + inputFile.getPath());
		}

		PrintStream outStream = System.out;
		
		if (options.has("output"))
		{
			File outputFile = (File) options.valueOf("output");
			outStream = new PrintStream(outputFile);
		}
		
		if (!inputFile.exists())
		{
			throw new Exception("File does not exist: " + inputFile.getPath());
		}
				
		if (options.has( "moo" ) )
		{
			MOO.runOptimization(inputFile, options);
		}	
		else if (options.has( "repl" ) )
		{
			REPL.runREPL(inputFile, options);
		}
		else
		{
			Normal.runNormal(inputFile, options, outStream);
		}
		
		
	}
}
