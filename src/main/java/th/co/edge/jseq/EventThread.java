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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;

/**
 * An <code>EventThred</code> traces the execution of a Java process, keeping
 * track of all method calls as <code>Activation</code>s in an
 * <code>ActivationList</code> containing all root activations, that is, all
 * method calls that occur "magically" from outside the traced methods, for
 * example from the main method. Each Java thread is traced as a separate root
 * activation.
 */
public class EventThread extends Thread {

    private final VirtualMachine vm;
    private final ActivationList rootActivations;
    private final List<String> includes;
    private final List<String> excludes;
    private final List<String> boundaryMethods;
    private boolean publicOnly;
    private final boolean trace;

    private String startClassName;
    private String startMethodName;

    private boolean connected = true;
    private boolean vmDied = true;

    private Map<ThreadReference, ThreadTrace> traceMap =
            new HashMap<ThreadReference, ThreadTrace>();

    /**
     * Creates a new <code>EventThread</code> that traces a given JDI
     * <code>VirtualMachine</code>, filling in a list of root activations.
     *
     * <p>
     * A list of included and excluded method names determine which methods to
     * trace. If the list of included method names is empty, all methods except
     * the excluded ones are traced, and correspondingly if the list of excluded
     * method name is empty. If both lists are non-empty, only the methods in
     * the included list are traced, except the ones in the excluded list.
     * Method names in the included and excluded lists can either be a fully
     * qualified method name, or a simple wild-card expression starting or
     * ending with "*".
     *
     * <p>
     * For example, if the included list contains "foo.Bar.*", and the excluded
     * list is empty, all methods in the class <code>foo.Bar</code> will be
     * traced. If the included list is empty and the excluded list contains
     * "java.*", everything but methods in the <code>java</code> package will
     * be traced. Finally, if the included list contains "foo.Bar.*" and the
     * excluded list contains "foo.Bar.baz", all methods in the class
     * <code>foo.Bar</code> will be traced, except for the method
     * <code>baz</code>.
     *
     * <p>
     * It is also possible to specify a list of boundary methods. A <it>bounday
     * method</it> is a method where tracing stops at entry to the method and
     * is resumed when the method exits. This list should contain fully
     * qualified method names, no wild-cards are accepted.
     *
     * <p>
     * For example, if the list of boundary methods contains "foo.Bar.baz",
     * tracing will include the call to <code>baz</code>, but no methods
     * called by <code>baz</code>. Normal tracing is resumed after
     * <code>baz</code> returns.
     *
     * @param vm
     *            the JDI <code>VirtualMachine</code>, a representation of
     *            the Java process to trace
     * @param rootActivations
     *            the list of root activations that will be filled in during the
     *            program trace
     * @param includes
     *            a list of method name pattern that will be included in the
     *            program trace, where name patterns may start or end with the
     *            wild-card "*"
     * @param excludes
     *            a list of method name patterns that will be excluded in the
     *            program trace, where name patterns may start or end with the
     *            wild-card "*"
     * @param boundaryMethods
     *            a list of boundary methods, methods where tracing will stop
     *            during the execution of the method
     * @param publicOnly
     *            if <code>true</code> only include public methods
     *            in the program trace
     * @param trace
     *            if <code>true</code> the program trace (method entry and
     *            exit, for example) will be echoed to <code>System.out</code>
     */
    public EventThread(VirtualMachine vm, ActivationList rootActivations,
            List<String> includes, List<String> excludes,
            List<String> boundaryMethods, boolean publicOnly,
            boolean trace) {
        super("event-handler");
        this.vm = vm;
        this.rootActivations = rootActivations;
        this.includes = includes;
        this.excludes = excludes;
        this.trace = trace;
        this.boundaryMethods = boundaryMethods;
        this.publicOnly = publicOnly;
    }

    /**
     * Handles all events generated by JDI, running for as long as the Java
     * process is active, or until an attached process disconnects the event
     * thread.
     */
    @Override
    public void run() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    Event event = it.nextEvent();
                    handleEvent(event);
                }
                eventSet.resume();
            } catch (InterruptedException exc) {
                System.err.println(exc);
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates all JDI event requests, that is, defines the set of events that
     * will be handled by this <code>EventThread</code>.
     *
     * @param startMethod
     *            the fully qualified name of the method where tracing should
     *            start, or <code>null</code> if tracing should start
     *            immediately
     */
    public void setEventRequests(String startMethod) {
        if (startMethod != null) {
            setStartMethodBreakpoints(startMethod);
        } else {
        	activateTracingOfAllMethods();
        }
    }

    private void activateTracingOfAllMethods() {
    	EventRequestManager mgr = vm.eventRequestManager();
    	
    	MethodEntryRequest methodEntryRequest = mgr.createMethodEntryRequest();
        for (String includePattern : includes) {
        	methodEntryRequest.addClassFilter(includePattern);
        }
        for (String excludePattern : excludes) {
        	methodEntryRequest.addClassExclusionFilter(excludePattern);
        }
        enableRequest(methodEntryRequest);
        
        MethodExitRequest methodExitRequest = mgr.createMethodExitRequest();
        for (String includePattern : includes) {
        	methodExitRequest.addClassFilter(includePattern);
        }
        for (String excludePattern : excludes) {
        	methodExitRequest.addClassExclusionFilter(excludePattern);
        }
        enableRequest(methodExitRequest);
    	
    	ExceptionRequest exceptionRequest = mgr.createExceptionRequest(null, true, true);
        for (String includePattern : includes) {
        	exceptionRequest.addClassFilter(includePattern);
        }
        for (String excludePattern : excludes) {
        	exceptionRequest.addClassExclusionFilter(excludePattern);
        }
        enableRequest(exceptionRequest);
    	
        enableRequest(mgr.createThreadDeathRequest());
    }

	private void enableRequest(EventRequest request) {
		request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        request.enable();
	}

    private void setStartMethodBreakpoints(String qualifiedMethodName) {
        int lastDot = qualifiedMethodName.lastIndexOf('.');
        if (lastDot < 0) {
            String message =
                    "Not a qualified method name: \"" + qualifiedMethodName +
                            "\"";
            throw new IllegalArgumentException(message);
        }
        startClassName = qualifiedMethodName.substring(0, lastDot);
        startMethodName = qualifiedMethodName.substring(lastDot + 1);

        List<ReferenceType> startClasses = vm.classesByName(startClassName);
        boolean methodFound = false;
        for (ReferenceType refType : startClasses) {
            List<Method> methods = refType.methodsByName(startMethodName);
            if (!methods.isEmpty()) {
                setMethodBreakpoints(methods);
                methodFound = true;
            }
        }
        if (!methodFound) {
            EventRequestManager mgr = vm.eventRequestManager();
            ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
            cpr.addClassFilter(startClassName);
            enableRequest(cpr);
        }
    }

    private void setMethodBreakpoints(List<Method> methods) {
        EventRequestManager mgr = vm.eventRequestManager();
        for (Method method : methods) {
            try {
                Location location = method.allLineLocations().get(0);
                BreakpointRequest bpr = mgr.createBreakpointRequest(location);
                enableRequest(bpr);
            } catch (AbsentInformationException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteAllEventRequests() {
        EventRequestManager mgr = vm.eventRequestManager();
        mgr.deleteEventRequests(mgr.classPrepareRequests());
        mgr.deleteEventRequests(mgr.methodEntryRequests());
        mgr.deleteEventRequests(mgr.methodExitRequests());
        mgr.deleteEventRequests(mgr.exceptionRequests());
        mgr.deleteAllBreakpoints();
    }

    private class ThreadTrace {
        private final ThreadReference thread;

        private Activation currentActivation = null;

        private String currentBoundaryMethod;

        private boolean stopWhenActivationDone = false;

        public ThreadTrace(ThreadReference thread) {
            this.thread = thread;
            trace("====== " + thread.name() + " ======");
        }

        private void classPrepareEvent(ClassPrepareEvent event)
                throws NoSuchMethodException {
            if (event.referenceType().name().equals(startClassName)) {
                List<Method> methods =
                        event.referenceType().methodsByName(startMethodName);
                if (methods.isEmpty()) {
                    throw new NoSuchMethodException(startClassName + "." +
                            startMethodName);
                }
                setMethodBreakpoints(methods);
            }
        }

        private void methodEntryEvent(MethodEntryEvent event) {
        	String className = getClassName(event);
            Method method = event.method();
            String qualifiedMethodName = className + "." + method.name();
            if (isBoundaryMethod(qualifiedMethodName)) {
                deleteAllEventRequests();
                currentBoundaryMethod = qualifiedMethodName;
                EventRequestManager mgr = vm.eventRequestManager();
                MethodExitRequest methodExitRequest = mgr.createMethodExitRequest();
                methodExitRequest.addClassFilter(className);
                enableRequest(methodExitRequest);
            }

            if (publicOnly && !method.isPublic()) {
            	return;
            }

            methodEntry(className, method);
        }

        private boolean isBoundaryMethod(String qualifiedMethodName) {
            for (String boundaryMethod : boundaryMethods) {
                if (qualifiedMethodName.equals(boundaryMethod)) {
                    return true;
                }
            }
            return false;
        }

        private void methodEntry(String className, Method method) {
            trace(className + "." + method.name() + " " + method.argumentTypeNames());
            int frameCount = -1;
            try {
                frameCount = thread.frameCount();
            } catch (IncompatibleThreadStateException e) {
                e.printStackTrace();
                // Ignore -- I THINK this exception cannot occur here...
            }
            Activation activation =
                    new Activation(currentActivation, className, method,
                            frameCount);
            if (currentActivation == null) {
                rootActivations.add(activation);
            }
            currentActivation = activation;
        }

        private void methodExitEvent(MethodExitEvent event) {
            if (currentActivation == null) return;

            String className = getClassName(event);
            String methodName = event.method().name();
            String qualifiedMethodName = className + "." + methodName;

            if (className.equals(currentActivation.getClassName()) &&
                    methodName.equals(currentActivation.getMethod().name())) {
                currentActivation = currentActivation.getParent();
            }
            if (currentActivation == null && stopWhenActivationDone) {
                deleteAllEventRequests();
            }
            if (currentBoundaryMethod != null &&
                    currentBoundaryMethod.equals(qualifiedMethodName)) {
            	deleteAllEventRequests();
            	activateTracingOfAllMethods();
            }
        }

        private void exceptionEvent(ExceptionEvent event) {
            EventRequestManager mgr = vm.eventRequestManager();
            StepRequest request =
                    mgr.createStepRequest(thread, StepRequest.STEP_MIN,
                            StepRequest.STEP_INTO);
            request.addCountFilter(1);
            enableRequest(request);
        }

        private void stepEvent(StepEvent event) {
            EventRequestManager mgr = vm.eventRequestManager();
            mgr.deleteEventRequest(event.request());
            // Find the activation that catches the exception.
            int frameCount = -1;
            try {
                frameCount = thread.frameCount();
            } catch (IncompatibleThreadStateException e) {
                System.err.println(e);
            }
            while (currentActivation.getParent() != null) {
                if (currentActivation.getFrameCount() == frameCount) {
                    break;
                }
                currentActivation = currentActivation.getParent();
            }
        }

        private void breakpointEvent(BreakpointEvent event) {
            String methodName = event.location().method().name();
            if (!methodName.equals(startMethodName)) {
                String message =
                        "Unexpected breakpoint. Should be in method " +
                                startMethodName + ", but was in method " +
                                methodName + ". Event=" + event;
                throw new IllegalArgumentException(message);
            }
            methodEntry(startClassName, event.location().method());
            EventRequestManager mgr = vm.eventRequestManager();
            mgr.deleteEventRequest(event.request());
            activateTracingOfAllMethods();
            stopWhenActivationDone = true;
        }

        private void threadDeathEvent(ThreadDeathEvent event) {
            trace("====== " + thread.name() + " end ======");
        }

        private String getClassName(LocatableEvent event) {
        	ReferenceType instanceType = getInstanceType(event);
        	if (instanceType != null) {
        		return instanceType.name();
        	} else {
        		return getDeclaringType(event).name();
        	}
        }

        private ReferenceType getDeclaringType(LocatableEvent event) {
            return event.location().method().declaringType();
        }

        private ReferenceType getInstanceType(LocatableEvent event) {
            ThreadReference thread = event.thread();
            if (thread == null) return null;
			StackFrame frame = null;
			try {
				frame = thread.frame(0);
			} catch (IncompatibleThreadStateException e) {
			}
			if (frame == null) return null;
			ObjectReference thisObject = frame.thisObject();
			if (thisObject == null) return null;
			ReferenceType referenceType = thisObject.referenceType();
			return referenceType;
        }
    }

    private ThreadTrace threadTrace(ThreadReference thread) {
        ThreadTrace threadTrace = traceMap.get(thread);
        if (threadTrace == null) {
            threadTrace = new ThreadTrace(thread);
            traceMap.put(thread, threadTrace);
        }
        return threadTrace;
    }

    private void handleEvent(Event event) throws NoSuchMethodException {
        if (event instanceof ClassPrepareEvent) {
            classPrepareEvent((ClassPrepareEvent) event);
        } else if (event instanceof MethodEntryEvent) {
            methodEntryEvent((MethodEntryEvent) event);
        } else if (event instanceof MethodExitEvent) {
            methodExitEvent((MethodExitEvent) event);
        } else if (event instanceof ExceptionEvent) {
            exceptionEvent((ExceptionEvent) event);
        } else if (event instanceof StepEvent) {
            stepEvent((StepEvent) event);
        } else if (event instanceof BreakpointEvent) {
            breakpointEvent((BreakpointEvent) event);
        } else if (event instanceof ThreadDeathEvent) {
            threadDeathEvent((ThreadDeathEvent) event);
        } else if (event instanceof VMStartEvent) {
            vmStartEvent((VMStartEvent) event);
        } else if (event instanceof VMDeathEvent) {
            vmDeathEvent((VMDeathEvent) event);
        } else if (event instanceof VMDisconnectEvent) {
            vmDisconnectEvent((VMDisconnectEvent) event);
        } else {
            throw new Error("Internal error: Unexpected event type");
        }
    }

    private synchronized void handleDisconnectedException() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent((VMDeathEvent) event);
                    } else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent((VMDisconnectEvent) event);
                    }
                }
                eventSet.resume();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    private void vmStartEvent(VMStartEvent event) {
        trace("-- VM Started --");
    }

    private void classPrepareEvent(ClassPrepareEvent event)
            throws NoSuchMethodException {
        threadTrace(event.thread()).classPrepareEvent(event);
    }

    private void methodEntryEvent(MethodEntryEvent event) {
        threadTrace(event.thread()).methodEntryEvent(event);
    }

    private void methodExitEvent(MethodExitEvent event) {
        threadTrace(event.thread()).methodExitEvent(event);
    }

    private void stepEvent(StepEvent event) {
        threadTrace(event.thread()).stepEvent(event);
    }

    private void breakpointEvent(BreakpointEvent event) {
        threadTrace(event.thread()).breakpointEvent(event);
    }

    private void threadDeathEvent(ThreadDeathEvent event) {
        ThreadTrace threadTrace = traceMap.get(event.thread());
        if (threadTrace != null) {
            threadTrace.threadDeathEvent(event);
        }
    }

    private void exceptionEvent(ExceptionEvent event) {
        ThreadTrace threadTrace = traceMap.get(event.thread());
        if (threadTrace != null) {
            threadTrace.exceptionEvent(event);
        }
    }

    private void vmDeathEvent(VMDeathEvent event) {
        vmDied = true;
        trace("-- The application exited --");
    }

    private void vmDisconnectEvent(VMDisconnectEvent event) {
        connected = false;
        if (!vmDied) {
            trace("-- The application has been disconnected --");
        }
    }

    private void trace(String s) {
        if (trace) {
            System.err.println(s);
        }
    }
}
