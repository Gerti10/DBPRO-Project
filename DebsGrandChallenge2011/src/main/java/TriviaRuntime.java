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
    public EPRuntime setup() {
        //A Configuration object is responsible for specifying which LoginModules should be used for a particular
        //application, and in what order the LoginModules should be invoked.
        Configuration config = new Configuration();
        config.getRuntime().getExecution().setPrioritized(true);
        config.getRuntime().getThreading().setInternalTimerEnabled(false); //no use of Threads from the esper runtime
        config.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
        config.getCompiler().getByteCode().setAccessModifiersPublic();

        EPRuntime runtime = EPRuntimeProvider.getDefaultRuntime(config);
        runtime.initialize();

        // Resolve "trivia.epl" file.
        //InputStream represents inout stream of bytes
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
            EPCompiled compiled = compiler.compile(module, args); //compile trivia.epl

            // set deployment id to 'trivia'
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId("trivia"));
        } catch (Exception e) {
            throw new RuntimeException("Error compiling and deploying EPL from 'trivia.epl': " + e.getMessage(), e);
        }

        return runtime;
    }



}
