/*
 * Copyright (c) 2003-2008, by Henrik Arro and Contributors
 *
 * This file is part of JSeq, a tool to automatically create
 * sequence diagrams by tracing program execution.
 *
 * See <http://jseq.sourceforge.net> for more information.
 *
 * JSeq is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * JSeq is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSeq. If not, see <http://www.gnu.org/licenses/>.
 */

package th.co.edge.jseq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.Connector.Argument;

public class Main {
    private static final String PROGRAM_NAME = "JSeq";
    private static final String PROGRAM_VERSION = "0.5.SNAPSHOT";
    private static final String[] STANDARD_EXCLUDES =
            { "java.*", "javax.*", "sun.*", "com.sun.*", "junit.*" };
    private ActivationList rootActivations = new ActivationList();
    private ConnectorType connectorType = null;
    private String attachAddress = null;
    private String classname = null;
    private String arguments = null;
    private String classpath = null;
    private String readFilename = null;
    private String saveFilename = null;
    private String outFilename = null;
    private Formatter formatter = FormatterRegistry.getInstance().get("svg");
    private boolean quiet = false;
    private boolean trace = true;
    private String startMethod = null;
    private List<String> includePatterns = new LinkedList<String>();
    private List<String> excludePatterns = new LinkedList<String>();
    private boolean stdExcludes = true;
    private boolean shouldRun = true;

    public static void main(String[] args) {
        try {
            final Main main = new Main(args);
            if (main.shouldRun) {
                // Make sure that generateSequenceDiagram is always called at
                // the end. This way, it is possible to attach to a long-running
                // process and use Ctrl-C to stop JSeq and still get a diagram.
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            main.generateSequenceDiagram();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                main.traceProgram();
                // Here is main.generateSequenceDiagram called by the shutdown
                // hook.
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(getUsage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Main(String[] args) {
        int inx;
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments given");
        }
        for (inx = 0; inx < args.length; ++inx) {
            String arg = args[inx];
            if (arg.charAt(0) != '-') {
                break;
            }
            if (arg.equals("-connector")) {
                connectorType = ConnectorType.valueOf(args[++inx]);
            } else if (arg.equals("-attach")) {
                attachAddress = args[++inx];
            } else if (arg.equals("-read")) {
                readFilename = args[++inx];
            } else if (arg.equals("-classpath") || arg.equals("-cp")) {
                classpath = args[++inx];
            } else if (arg.equals("-save")) {
                saveFilename = args[++inx];
            } else if (arg.equals("-out")) {
                outFilename = args[++inx];
            } else if (arg.equals("-format")) {
                formatter = FormatterRegistry.getInstance().get(args[++inx]);
            } else if (arg.equals("-quiet")) {
                quiet = true;
            } else if (arg.equals("-start")) {
                startMethod = args[++inx];
            } else if (arg.equals("-include")) {
                includePatterns.add(args[++inx]);
            } else if (arg.equals("-exclude")) {
                excludePatterns.add(args[++inx]);
            } else if (arg.equals("-nostdexcludes")) {
                stdExcludes = false;
            } else if (arg.equals("-notrace")) {
                trace = false;
            } else if (arg.equals("-version")) {
                System.out.println(getVersion());
                shouldRun = false;
            } else {
                throw new IllegalArgumentException("Illegal option: " +
                        args[inx]);
            }
        }
        if (inx < args.length) {
            classname = args[inx];
            StringBuffer sb = new StringBuffer();
            for (++inx; inx < args.length; ++inx) {
                sb.append(args[inx]);
                sb.append(' ');
            }
            arguments = sb.toString();
        }

        if (stdExcludes) {
            excludePatterns = addStandardExcludes(excludePatterns);
        }

        if (connectorType == null) {
            if (attachAddress == null) {
                connectorType = ConnectorType.LAUNCHING;
            } else {
                connectorType = ConnectorType.SOCKET;
            }
        }
    }

    private static List<String> addStandardExcludes(
            List<String> originalExcludes) {
        List<String> excludes = new LinkedList<String>(originalExcludes);
        for (String standardExclude : STANDARD_EXCLUDES) {
            excludes.add(standardExclude);
        }
        return excludes;
    }

    private void traceProgram() throws IOException, ClassNotFoundException {
        if (readFilename != null) {
            readActivationList(readFilename);
        } else if (attachAddress != null) {
            attachProgram();
        } else {
            runProgram();
        }
    }

    private void generateSequenceDiagram() throws IOException, FormatException {
        if (saveFilename != null) {
            saveActivationList(rootActivations, saveFilename);
        }
        if (!quiet) {
            ActivationList filteredActivations =
                    filterActivations(rootActivations);
            Diagram diagram = formatter.format(filteredActivations);
            if (outFilename == null) {
                System.out.println(diagram);
            } else {
                File file = new File(outFilename);
                diagram.save(file);
            }
        }
    }

    private ActivationList filterActivations(ActivationList activationList) {
        ActivationList filteredActivations = activationList;
        if (startMethod != null) {
            filteredActivations =
                    filteredActivations.find(new MethodFilter(startMethod));
        }
        for (String pattern : excludePatterns) {
            ClassExclusionFilter classExclusionFilter =
                    new ClassExclusionFilter(pattern);
            filteredActivations =
                    filteredActivations.filter(classExclusionFilter);
        }
        filteredActivations =
                filteredActivations.filter(new ConstructorFilter(
                        getClassLoader()));
        filteredActivations = filteredActivations.collapseRepetitions();

        return filteredActivations;
    }

    private ClassLoader getClassLoader() {
        URLClassLoader classLoader =
                new URLClassLoader(getClasspathURLs(), ClassLoader
                        .getSystemClassLoader());
        return classLoader;
    }

    private URL[] getClasspathURLs() {
        URL[] classpathURLs = new URL[0];
        if (classpath != null) {
            try {
                String[] classpathEntries = classpath.split("path.separator");
                classpathURLs = new URL[classpathEntries.length];
                for (int i = 0; i < classpathURLs.length; i++) {
                    classpathURLs[i] =
                            new File(classpathEntries[i]).toURI().toURL();
                }
            } catch (MalformedURLException e) {
                System.err.println(e);
            }
        }
        return classpathURLs;
    }

    private void readActivationList(String filename) throws IOException,
            ClassNotFoundException {
        ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(filename));
        rootActivations = (ActivationList) in.readObject();
        in.close();
    }

    private void saveActivationList(ActivationList activationList,
            String filename) throws IOException {
        ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(activationList);
        out.close();
    }

    private void attachProgram() {
        ProgramRunner runner =
                new ProgramRunner(rootActivations, attachAddress,
                        includePatterns, excludePatterns, startMethod, trace);
        runner.runProgram(connectorType);
    }

    private void runProgram() {
        ProgramRunner runner =
                new ProgramRunner(rootActivations, classname, arguments,
                        classpath, includePatterns, excludePatterns,
                        startMethod, trace);
        runner.runProgram(connectorType);
    }

    public static String getUsage() {
        return "Usage: jseq [-options] <class> [args...]\n"
                + "\t(to execute a class)\n"
                + "    or jseq [-options] -attach <address>\n"
                + "\t(to attach to a running VM at the specified address)\n"
                + "    or jseq [-options] -read <filename>\n"
                + "\t(to generate output from a previously saved run)\n"
                + "\n"
                + "Options for running a program:\n"
                + "\t[-classpath <path>]\tto set classpath\n"
                + "\t[-cp <path>]\tsame as -classpath\n"
                + "\t[-save <filename>]\tto save the program run in a file\n"
                + "\n"
                + "Options for attaching to a program:\n"
                + "\t[-connector {SOCKET,SHARED_MEMORY}]\tto choose JDI connector\n"
                + "\n"
                + "Options for generating sequence diagrams:\n"
                + "\t[-out <filename>]\tto save diagram in a file\n"
                + "\t[-format {text,png,sdedit,svg,argouml}]\tto specify format of output\n"
                + "\t[-quiet]\tto not generate any output\n"
                + "\t[-start <methodname>]\tto specify start method in diagram\n"
                + "\t[-include <class regexp>]\tto include only some classes in diagram\n"
                + "\t[-exclude <class regexp>]\tto exclude some classes from diagram\n"
                + "\t[-nostdexcludes]\tto not exclude java.*, javax.*, etc\n"
                + "\n" + "Other options:\n"
                + "\t[-notrace]\tto turn off tracing of method entries, etc.\n"
                + "\t[-version]\tto print version information and exit";
    }

    public static String getVersion() {
        return PROGRAM_NAME + " " + PROGRAM_VERSION;
    }

    private static class ProgramRunner {
        private ActivationList rootActivations;
        private String classname;
        private String arguments;
        private String classpath;
        private String attachAddress;
        private String startMethod;
        private boolean trace;
        private VirtualMachine vm;
        private Thread errThread;
        private Thread outThread;
        private List<String> includes;
        private List<String> excludes;
        private EventThread eventThread;

        public ProgramRunner(ActivationList rootActivations,
                String attachAddress, List<String> includes,
                List<String> excludes, String startMethod, boolean trace) {
            this.rootActivations = rootActivations;
            this.attachAddress = attachAddress;
            this.includes = includes;
            this.excludes = excludes;
            this.startMethod = startMethod;
            this.trace = trace;
            this.classpath = System.getProperty("java.class.path");
        }

        public ProgramRunner(ActivationList rootActivations, String classname,
                String arguments, String classpath, List<String> includes,
                List<String> excludes, String startMethod, boolean trace) {
            this.rootActivations = rootActivations;
            this.classname = classname;
            this.arguments = arguments;
            this.includes = includes;
            this.excludes = excludes;
            this.startMethod = startMethod;
            this.trace = trace;
            if (classpath == null) {
                this.classpath = System.getProperty("java.class.path");
            } else {
                this.classpath = classpath;
            }
        }

        public void runProgram(ConnectorType connectorType) {
            if (attachAddress == null) {
                vm = launchTarget(connectorType, classname + " " + arguments);
                redirectOutput();
            } else {
                vm = attachTarget(connectorType, attachAddress);
            }
            List<String> emptyStringList = new ArrayList<String>();
            eventThread =
                    new EventThread(vm, rootActivations, includes, excludes,
                            emptyStringList, false, trace);
            eventThread.setEventRequests(startMethod);
            eventThread.start();
            vm.resume();

            try {
                eventThread.join();
                if (attachAddress == null) {
                    errThread.join();
                    outThread.join();
                }
            } catch (InterruptedException exc) {
                // Ignore
            }
        }

        private VirtualMachine attachTarget(ConnectorType connectorType,
                String attachAddress) {
            AttachingConnector connector =
                    (AttachingConnector) findConnectorByType(connectorType);
            Map<String, String> arguments = new HashMap<String, String>();
            switch (connectorType) {
            case SOCKET:
                String[] hostAndPort = attachAddress.split(":");
                if (hostAndPort.length != 2) {
                    throw new IllegalArgumentException(
                            "Attach address should be formatted as 'hostname:port'");
                }
                arguments.put("hostname", hostAndPort[0]);
                arguments.put("port", hostAndPort[1]);
                break;
            case SHARED_MEMORY:
                arguments.put("name", attachAddress);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unexpected connector type: " + connectorType);
            }
            Map<String, Argument> connectorArguments =
                    getConnectorArgs(connector, arguments);
            try {
                return connector.attach(connectorArguments);
            } catch (IOException e) {
                throw new Error("Unable to launch target VM: " + e);
            } catch (IllegalConnectorArgumentsException e) {
                throw new Error("Internal error: " + e);
            }
        }

        private VirtualMachine launchTarget(ConnectorType connectorType,
                String mainArgs) {
            LaunchingConnector connector =
                    (LaunchingConnector) findConnectorByType(connectorType);
            Map<String, String> arguments = new HashMap<String, String>();
            switch (connectorType) {
            case LAUNCHING:
                arguments.put("main", mainArgs);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unexpected connector type: " + connectorType);
            }
            Map<String, Argument> connectorArguments =
                    getConnectorArgs(connector, arguments);
            Connector.StringArgument options =
                    (Connector.StringArgument) connectorArguments
                            .get("options");
            options.setValue(options.value() + "-classpath " + classpath);
            try {
                return connector.launch(connectorArguments);
            } catch (IOException exc) {
                throw new Error("Unable to launch target VM: " + exc);
            } catch (IllegalConnectorArgumentsException exc) {
                throw new Error("Internal error: " + exc);
            } catch (VMStartException exc) {
                throw new Error("Target VM failed to initialize: " +
                        exc.getMessage());
            }
        }

        private Connector findConnectorByType(ConnectorType connectorType) {
            Connector foundConnector = null;
            String name = connectorType.getName();
            List<Connector> connectors =
                    Bootstrap.virtualMachineManager().allConnectors();
            for (Connector connector : connectors) {
                if (connector.name().equals(name)) {
                    foundConnector = connector;
                    break;
                }
            }
            if (foundConnector == null) {
                throw new Error("Connector not found: " + name);
            }
            return foundConnector;
        }

        private Map<String, Argument> getConnectorArgs(Connector connector,
                Map<String, String> arguments) {
            Map<String, Argument> allArguments = connector.defaultArguments();
            for (String argumentName : arguments.keySet()) {
                String argumentValue = arguments.get(argumentName);
                Connector.Argument argument = allArguments.get(argumentName);
                if (argument == null) {
                    throw new IllegalArgumentException(
                            "Unknown argument name '" + argumentName +
                                    "' for " + connector);
                }
                argument.setValue(argumentValue);
            }
            return allArguments;
        }

        private void redirectOutput() {
            Process process = vm.process();
            errThread =
                    new StreamRedirectThread("error reader", process
                            .getErrorStream(), System.err);
            outThread =
                    new StreamRedirectThread("output reader", process
                            .getInputStream(), System.out);
            errThread.start();
            outThread.start();
        }
    }

    private static enum ConnectorType {
        SOCKET("com.sun.jdi.SocketAttach", true),
        SHARED_MEMORY("com.sun.jdi.SharedMemoryAttach", true),
        LAUNCHING("com.sun.jdi.CommandLineLaunch", false);

        private String name;
        private boolean attaching;

        private ConnectorType(String name, boolean attaching) {
            this.name = name;
            this.attaching = attaching;
        }

        public String getName() {
            return name;
        }

        public boolean isAttaching() {
            return attaching;
        }
    }
}
