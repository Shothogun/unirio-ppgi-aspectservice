/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;


/**
 * Receives event announcements from the engine and passes each of them to the
 *  custom service's appropriate handling method
 *
 * @author Lachlan Aldred
 * Date: 23/01/2004
 * Time: 13:26:04
 *
 * @author Michael Adams (refactored for v2.0, 12/2008)
 */

public class InterfaceB_EnvironmentBasedServer extends HttpServlet {
    private InterfaceBWebsideController _controller;
    private Logger _logger = LogManager.getLogger(InterfaceB_EnvironmentBasedServer.class);


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();

        // get the name of the custom service implementing this interface
        // (i.e. the name of the class that extends InterfaceBWebSideController)
        String controllerClassName = context.getInitParameter("InterfaceBWebSideController");

        try {
            Class<? extends InterfaceBWebsideController> controllerClass =
                    Class.forName(controllerClassName).asSubclass(InterfaceBWebsideController.class);

            // If the class has a getInstance() method, call that method rather than
            // calling a constructor (& thus instantiating 2 instances of the class)
            try {
                Method instMethod = controllerClass.getDeclaredMethod("getInstance");
                _controller = (InterfaceBWebsideController) instMethod.invoke(null);
            }
            catch (NoSuchMethodException nsme) {
                _controller = controllerClass.newInstance();
            }
            
            // retrieve the URL of the YAWL Engine from the web.xml file.
            String engineBackendAddress = context.getInitParameter("InterfaceB_BackEnd");
            _controller.setUpInterfaceBClient(engineBackendAddress);

            //If there is an auth proxy firewall and it has been configured it in the
            //web.xml file the settings be retrieved for use.
            String userName = context.getInitParameter("UserName");
            String password = context.getInitParameter("Password");
            String proxyHost = context.getInitParameter("ProxyHost");
            String proxyPort = context.getInitParameter("ProxyPort");
            _controller.setRemoteAuthenticationDetails(
                    userName, password, proxyHost, proxyPort);
            
            // if there are overridden engine logon & password, set them for the service
            String logonName = context.getInitParameter("EngineLogonUserName");
            String logonPassword = context.getInitParameter("EngineLogonPassword");
            if (logonName != null) _controller.setEngineLogonName(logonName);
            if (logonPassword != null) _controller.setEngineLogonPassword(logonPassword);

            context.setAttribute("controller", _controller);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        debug(request);
        _controller.doGet(request, response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();

    }


    private String processPostQuery(HttpServletRequest request) {
        debug(request);

        String action = request.getParameter("action");
        String caseID = request.getParameter("caseID");
        String workItemXML = request.getParameter("workItem");
        WorkItemRecord workItem = (workItemXML != null) ?
                Marshaller.unmarshalWorkItem(workItemXML) : null;

        // where there are two choices for 'action' below, those on the left are
        // passed from 2.2 or later engine versions, while those on the right come
        // from pre-2.2 engine versions
        if ("announceItemEnabled".equals(action) || "handleEnabledItem".equals(action)) {
            _controller.handleEnabledWorkItemEvent(workItem);
        }
        else if ("announceItemStatus".equals(action)) {
            String oldStatus = request.getParameter("oldStatus");
            String newStatus = request.getParameter("newStatus");
            _controller.handleWorkItemStatusChangeEvent(workItem, oldStatus, newStatus);
        }
        else if ("announceCaseStarted".equals(action)) {
            String launchingService = request.getParameter("launchingService");
            String delayedStr = request.getParameter("delayed");
            boolean delayed = delayedStr != null && delayedStr.equalsIgnoreCase("true");
            YSpecificationID specID = new YSpecificationID(
                    request.getParameter("specidentifier"),
                    request.getParameter("specversion"),
                    request.getParameter("specuri"));
            _controller.handleStartCaseEvent(specID, caseID, launchingService, delayed);
        }
        else if ("announceCaseCompleted".equals(action) || "announceCompletion".equals(action)) {
            String casedata = request.getParameter("casedata");
            _controller.handleCompleteCaseEvent(caseID, casedata);
        }
        else if ("announceItemCancelled".equals(action) || "cancelWorkItem".equals(action)) {
            _controller.handleCancelledWorkItemEvent(workItem);
        }
        else if ("announceCaseCancelled".equals(action)) {
            _controller.handleCancelledCaseEvent(caseID);
        }
        else if ("announceCaseDeadlocked".equals(action)) {
            String tasks = request.getParameter("tasks");
            _controller.handleDeadlockedCaseEvent(caseID, tasks);
        }
        else if ("announceTimerExpiry".equals(action) || "timerExpiry".equals(action)) {
            _controller.handleTimerExpiryEvent(workItem);
        }
        else if ("announceEngineInitialised".equals(action)) {
            _controller.handleEngineInitialisationCompletedEvent();
        }
        else if ("announceCaseSuspending".equals(action)) {
            _controller.handleCaseSuspendingEvent(caseID);
        }
        else if ("announceCaseSuspended".equals(action)) {
            _controller.handleCaseSuspendedEvent(caseID);
        }
        else if ("announceCaseResumed".equals(action)) {
            _controller.handleCaseResumedEvent(caseID);
        }
        else if ("ParameterInfoRequest".equals(action)) {
            YParameter[] params = _controller.describeRequiredParams();
            StringBuilder output = new StringBuilder();
            for (YParameter param : params) {
                if (param != null) output.append(param.toXML());
            }
            return output.toString();
        }
        return "<success/>";
    }


    public void destroy() {
        _controller.destroy();
    }


    private void debug(HttpServletRequest request) {
        if (_logger.isDebugEnabled()) {
            String verb = request.getMethod();
            _logger.debug("\nInterfaceB_EnvironmentBasedServer::do{}() " +
                    "request.getRequestURL = {}", verb, request.getRequestURL());
            _logger.debug("InterfaceB_EnvironmentBasedServer::do{}() request.parameters:",
                    verb);
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                _logger.debug("\trequest.getParameter({}) = {}", name,
                        request.getParameter(name));
            }
        }
    }

}
