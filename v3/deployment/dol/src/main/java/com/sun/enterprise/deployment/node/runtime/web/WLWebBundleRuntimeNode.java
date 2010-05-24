/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.deployment.node.runtime.web;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.node.runtime.RuntimeBundleNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.web.ClassLoader;
import com.sun.enterprise.deployment.xml.DTDRegistry;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/**
 * This node is responsible for handling all WebLogic runtime information for 
 * web bundle.
 */
public class WLWebBundleRuntimeNode extends RuntimeBundleNode<WebBundleDescriptor> {

    public final static String SCHEMA_ID = "weblogic-web-app.xsd";

    private final static List<String> systemIDs = initSystemIDs();

    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<String>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }

    WebBundleDescriptor descriptor=null;
        
    /** Creates new WLWebBundleRuntimeNode */
    public WLWebBundleRuntimeNode(WebBundleDescriptor descriptor) {
        super(descriptor);
        this.descriptor = descriptor;        
    }
    
    /** Creates new WebBundleRuntimeNode */
    public WLWebBundleRuntimeNode() {
        super(null);    
    }

    /**
     * Initialize the child handlers
     */
    protected void Init() {
        registerElementHandler(new XMLElement(RuntimeTagNames.CONTAINER_DESCRIPTOR),
                WLContainerDescriptorNode.class);
    }
    
    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return new XMLElement(RuntimeTagNames.WL_WEB_RUNTIME_TAG);
    }    
    
    /** 
     * @return the DOCTYPE that should be written to the XML file
     */
    public String getDocType() {
        return null;
    }
    
    /**
     * @return the SystemID of the XML file
     */
    public String getSystemID() {
        return SCHEMA_ID;
    }

    /**
     * @return the list of SystemID of the XML schema supported
     */
    public List<String> getSystemIDs() {
        return systemIDs;
    }
    
   /**
    * @return the web bundle descriptor instance to associate with this XMLNode
    */    
    public WebBundleDescriptor getDescriptor() {    
        return descriptor;               
    }

    /**
     * receives notification of the value for a particular tag
     * 
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (element.getQName().equals(RuntimeTagNames.CONTEXT_ROOT)) {
            // only set the context root for standalone war;
            // for embedded war, the context root will be set 
            // using the value in application.xml
            Application app = descriptor.getApplication();
            if ( (app == null) || (app!=null && app.isVirtual()) ) {
                descriptor.setContextRoot(value);
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, WebBundleDescriptor bundleDescriptor) {
        Element root = appendChildNS(parent, getXMLRootTag().getQName(),
                    TagNames.WL_WEB_APP_NAMESPACE);

        // context-root?
        appendTextChild(root, RuntimeTagNames.CONTEXT_ROOT, bundleDescriptor.getContextRoot());

        // container-descriptor/prefer-web-inf-classes
        ClassLoader clBean = bundleDescriptor.getSunDescriptor().getClassLoader();
        if (clBean != null) {
            Node containerDescriptorNode = appendChild(root, RuntimeTagNames.CONTAINER_DESCRIPTOR);
            appendTextChild(containerDescriptorNode,
                    RuntimeTagNames.PREFER_WEB_INF_CLASSES,
                    clBean.getAttributeValue(ClassLoader.DELEGATE));
        }

        return root;
    }
}
