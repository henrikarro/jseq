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

package th.co.edge.jseq.argouml.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import ru.novosoft.uml.MFactory;
import ru.novosoft.uml.behavior.collaborations.MCollaboration;
import ru.novosoft.uml.behavior.common_behavior.MCallAction;
import ru.novosoft.uml.behavior.common_behavior.MLink;
import ru.novosoft.uml.behavior.common_behavior.MLinkEnd;
import ru.novosoft.uml.behavior.common_behavior.MObject;
import ru.novosoft.uml.behavior.common_behavior.MStimulus;
import ru.novosoft.uml.foundation.core.MClass;
import ru.novosoft.uml.foundation.core.MNamespace;
import ru.novosoft.uml.foundation.data_types.MVisibilityKind;
import ru.novosoft.uml.model_management.MModel;

import th.co.edge.jseq.Activation;
import th.co.edge.jseq.argouml.SequenceDiagram;
import th.co.edge.jseq.argouml.UUID;

/**
 * A utility class to work with <code>MModel</code> objects.
 */
public class MModelUtil {
    private static MFactory factory = MFactory.getDefaultFactory();

    /**
     * This class only contains static methods, so we hide the constructor.
     */
    private MModelUtil() {
    }

    /**
     * Returns a new <code>MModel</code> object with the given name.
     *
     * @param name
     *            the name of the model to create
     *
     * @return a new <code>MModel</code> object
     */
    public static MModel createMModel(String name) {
        MModel model = factory.createModel();
        model.setName(name);
        model.setUUID(UUID.getID());
        return model;
    }

    /**
     * Creates a new <code>SequenceDiagram</code> based on an
     * <code>Activation</code> and adds it to the given <code>MModel</code>.
     *
     * @param model
     *            the <code>MModel</code> to which to add the sequence diagram
     * @param activation
     *            the <code>Activation</code> on which to base the sequence
     *            diagram
     *
     * @return the newly created <code>SequenceDiagram</code>
     *
     * @throws ParserConfigurationException
     *             if there is some serious error in the XML configuration
     *             (should normally not occur)
     */
    public static SequenceDiagram addSequenceDiagram(MModel model,
            Activation activation) throws ParserConfigurationException {
        MCollaboration collaboration = factory.createCollaboration();
        model.addOwnedElement(collaboration);
        collaboration.setName("newCollaboration");
        collaboration.setUUID(UUID.getID());
        model.addOwnedElement(collaboration);
        SequenceDiagramBuilder builder =
                new SequenceDiagramBuilder(collaboration, activation);
        return builder.getDiagram();
    }

    private static class SequenceDiagramBuilder {
        private MNamespace namespace;
        private SequenceDiagram diagram;
        private Map<String, MClass> addedClasses =
                new HashMap<String, MClass>();
        private Map<String, MObject> addedObjects =
                new HashMap<String, MObject>();

        public SequenceDiagramBuilder(MNamespace namespace,
                Activation activation) throws ParserConfigurationException {
            this.namespace = namespace;
            this.diagram = new SequenceDiagram(namespace, activation);
            fillSequenceDiagram(activation);
        }

        public SequenceDiagram getDiagram() {
            return diagram;
        }

        private void fillSequenceDiagram(Activation activation) {
            MObject receiver = addObject(activation.getClassName(), "");
            Activation parent = activation.getParent();
            if (parent != null) {
                MObject sender = addObject(parent.getClassName(), "");
                addCall(sender, receiver, activation.getMethod().name());
            }
            for (Activation nestedActivation : activation
                    .getNestedActivations()) {
                fillSequenceDiagram(nestedActivation);
            }
            getDiagram().deactivate(receiver);
        }

        private MObject addObject(String className, String name) {
            MClass cls = createClass(className);
            namespace.addOwnedElement(cls);
            MObject object = createObject(cls, name);
            namespace.addOwnedElement(object);
            return object;
        }

        private void addCall(MObject sender, MObject receiver, String name) {
            MCallAction call = createCall(name);
            namespace.addOwnedElement(call);

            MStimulus stimulus = createStimulus(sender, receiver, call);
            namespace.addOwnedElement(stimulus);

            MLink link = createLink(sender, receiver, name, stimulus);
            namespace.addOwnedElement(link);

            getDiagram().addCall(sender, receiver, link);
        }

        private MClass createClass(String name) {
            MClass cls;
            if (addedClasses.containsKey(name)) {
                cls = addedClasses.get(name);
            } else {
                cls = MModelUtil.factory.createClass();
                addedClasses.put(name, cls);
                if (name != null) {
                    cls.setName(name);
                } else {
                    cls.setName("");
                }
                cls.setUUID(UUID.getID());
                cls.setVisibility(MVisibilityKind.PUBLIC);
            }
            return cls;
        }

        private MObject createObject(MClass cls, String name) {
            MObject object;
            String fullName = cls.getName() + "." + name;
            if (addedObjects.containsKey(fullName)) {
                object = addedObjects.get(fullName);
            } else {
                object = MModelUtil.factory.createObject();
                addedObjects.put(fullName, object);
                object.addClassifier(cls);
                if (name != null) {
                    object.setName(name);
                } else {
                    object.setName("");
                }
                object.setUUID(UUID.getID());
                getDiagram().addObject(object);
            }
            return object;
        }

        private MLink createLink(MObject fromObject, MObject toObject,
                String name, MStimulus stimulus) {
            MLink link = MModelUtil.factory.createLink();
            link.setUUID(UUID.getID());
            MLinkEnd linkFrom = MModelUtil.factory.createLinkEnd();
            linkFrom.setInstance(fromObject);
            linkFrom.setUUID(UUID.getID());
            link.addConnection(linkFrom);
            MLinkEnd linkTo = MModelUtil.factory.createLinkEnd();
            linkTo.setInstance(toObject);
            linkTo.setUUID(UUID.getID());
            link.addConnection(linkTo);
            link.addStimulus(stimulus);
            return link;
        }

        private MCallAction createCall(String name) {
            MCallAction call = MModelUtil.factory.createCallAction();
            if (name != null) {
                call.setName(name);
            } else {
                call.setName("");
            }
            call.setUUID(UUID.getID());
            return call;
        }

        private MStimulus createStimulus(MObject fromObject, MObject toObject,
                MCallAction call) {
            MStimulus stimulus = MModelUtil.factory.createStimulus();
            stimulus.setName("");
            stimulus.setSender(fromObject);
            stimulus.setReceiver(toObject);
            stimulus.setDispatchAction(call);
            stimulus.setUUID(UUID.getID());
            return stimulus;
        }
    }
}
