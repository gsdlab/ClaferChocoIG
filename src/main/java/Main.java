import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.clafer.compiler.ClaferSearchStrategy;
import org.clafer.javascript.Javascript;
import org.clafer.javascript.JavascriptFile;

public class Main
{
    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser() {
            {
                accepts( "file", "input file in .cfr or .js format" ).withRequiredArg().ofType( File.class )
                    .describedAs( "Clafer model file (.cfr) or Clafer Javascript file (.js)" );

                accepts( "v", "Validate the model by checking all assertions" );
                accepts( "repl", "run the tool in REPL (interactive) mode" );
                accepts( "moo", "run the tool in multi-objective optimization mode" );
                accepts( "prettify", "use simple and pretty output format (not formal)" );
                accepts( "version", "display the tool version" );
                accepts( "maxint", "specify maximum integer value" ).withRequiredArg().ofType( Integer.class );
                accepts( "minint", "specify minimum integer value" ).withRequiredArg().ofType( Integer.class );
                accepts( "n", "specify maximum number of instances" ).withRequiredArg().ofType( Integer.class );
                accepts( "scope", "override the default global scope value" ).withRequiredArg().ofType( Integer.class );
                accepts( "search", "PreferSmallerInstances/PreferLargerInstances/Random" ).withRequiredArg().ofType( ClaferSearchStrategy.class );
                accepts( "output", "output instances to file" ).withRequiredArg().ofType( File.class )
                .describedAs( "text file" );

                accepts( "help", "show help").forHelp();
            }
        };

        OptionSet options = parser.parse(args);

        if (options.has("version")) {
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
        if (options.has("help")) {
            parser.printHelpOn(System.out);
            return;
        }
        if (!options.has( "file" ))
            throw new Exception("Use --file to provide the input file");

        File inputFile = (File) options.valueOf("file");

        if (!inputFile.exists())
            throw new Exception("The provided input file does not exist: " + inputFile.getPath());

        String fileName = inputFile.toString();
        // If a .cfr file is given, compile it first and change the input file to the resulting .js file
        if (fileName.endsWith(".cfr")) {
            System.out.println("Compiling the Clafer model...");
            // compile the file
            try {
                Process compilerProcess = Runtime.getRuntime().exec("clafer -k -m choco " + fileName);
                compilerProcess.waitFor();
                if (compilerProcess.exitValue() != 0) {
                    System.out.println("Clafer compilation error: make sure your model is correct. Aborting...");
                    System.exit(1);
                }
            } catch (Exception e) {
                    System.out.println("Abnormal Clafer compiler termination. Aborting...");
                    System.exit(1);
            }

            // replace the extension to .js
            int extPos = fileName.lastIndexOf(".");
            if(extPos != -1) {
               fileName = fileName.substring(0, extPos) + ".js";
            }

            // change the inputFile to the resulting .js file
            inputFile = new File(fileName);
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

        // run the different modes
        JavascriptFile javascriptFile = null;
        try {
            if (options.has( "v"))
                System.out.println("=========== Parsing+Typechecking " + fileName + "  =============");
            javascriptFile = Javascript.readModel(inputFile);
        }
        catch(Exception e) {
            System.out.println("Unhandled compilation error occured. Please report this problem.");
            System.out.println(e.getMessage());
            return;
        }

        if (options.has("v"))
            Validate.runValidate(inputFile, javascriptFile, options, outStream);
        else if (options.has("repl"))
            REPL.runREPL(inputFile, javascriptFile, options);
        else
            Normal.runNormal(javascriptFile, options, outStream);
    }
}
