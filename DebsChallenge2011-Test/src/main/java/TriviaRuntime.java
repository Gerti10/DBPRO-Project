import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.util.EventTypeBusModifier;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import java.io.InputStream;

public class TriviaRuntime {

    /**
     * This class sets the configurations and initializes esper runtime. It reads the epl queries from trivia.epl file,
     * compiles the queries into byte code and deploys it to the esper runtime
     * @return esper runtime
     * @throws RuntimeException if the trivia.epl file was not found in the relative path, or if errors occurs
     * compiling and deploying trivia.epl file
     */
    public EPRuntime setup() {
        Configuration config = new Configuration();
        config.getRuntime().getExecution().setPrioritized(true);
        config.getRuntime().getThreading().setInternalTimerEnabled(false);
        config.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
        config.getCompiler().getByteCode().setAccessModifiersPublic();

        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.initialize();

        // Resolve "trivia.epl" file.
        InputStream inputFile = this.getClass().getClassLoader().getResourceAsStream("trivia.epl");
        if (inputFile == null) {
            inputFile = this.getClass().getClassLoader().getResourceAsStream("etc/trivia.epl");
        }
        if (inputFile == null) {
            throw new RuntimeException("Failed to find file 'trivia.epl' in classpath or relative to classpath");
        }

        try {
            EPCompiler compiler = EPCompilerProvider.getCompiler(); //get a Compiler instance

            Module module = compiler.readModule(inputFile, "trivia.epl");

            CompilerArguments args = new CompilerArguments(config);
            EPCompiled compiled = compiler.compile(module, args);

            //set deployment id to 'trivia'
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId("trivia"));
        } catch (Exception e) {
            throw new RuntimeException("Error compiling and deploying EPL from 'trivia.epl': " + e.getMessage(), e);
        }

        return runtime;
    }



}
