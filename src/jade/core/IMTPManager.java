/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;


import jade.util.leap.List;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public interface IMTPManager {

    public static final String SERVICE_MANAGER_NAME = "ServiceManager";


    /**
     * Initialize this IMTPManager
     */
    void initialize(Profile p, CommandProcessor cp) throws IMTPException;

    /**
       Connects the local container to the rest of the platform, over
       the IMTP managed by this <code>IMTPManager</code> object.

       @param id The container ID for the local container. It may be
       modified by this call.
       @throws IMTPException If something goes wrong during the
       container registration.
    */
    void connect(ContainerID id) throws IMTPException;

    /**
       Disconnects the local container from the rest of the platform,
       over the IMTP managed by this <code>IMTPManager</code> object.

       @param id The container ID for the local container.
       @throws IMTPException If something goes wrong during the
       container deregistration.
    */
    void disconnect(ContainerID id) throws IMTPException;

    /**
       Access the node that represents the local JVM.

       @return A <code>Node</code> object, representing the local node
       of this platform.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    Node getLocalNode() throws IMTPException;

    /**
       Makes the platform <i>Service Manager</i> available through
       this IMTP.
       @param mgr The <code>ServiceManager</code> implementation that
       is to be made available across the network.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    void exportServiceManager(ServiceManager mgr) throws IMTPException;

    /**
       Stops making the platform <i>Service Manager</i> available
       through this IMTP.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    void unexportServiceManager(ServiceManager sm) throws IMTPException;

    /**
       Builds a proxy object for the (possibly remote) platform
       service manager.
       @param proc The local Command Processor to which the service
       manager proxy can be attached.
       @return The newly created <code>ServiceManager</code>
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    ServiceManager createServiceManagerProxy(CommandProcessor proc) throws IMTPException;

    /**
       Builds a proxy object for the (possibly remote) platform
       service finder.
       @return The newly created <code>ServiceFinder</code>
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    ServiceFinder createServiceFinderProxy() throws IMTPException;

    /**
       Exports the locally installed slice of a service, so that it
       can be accessed across the network.

       @param serviceName The name of the service this slice is part of.
       @param localSlice The locally installed slice, that will answer
       to remote invocations made through the horizontal interface of
       the service.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
       @see jade.core.Service#getLocalSlice()
       @see jade.core.Service#getHorizontalInterface()
    */
    void exportSlice(String serviceName, Service.Slice localSlice) throws IMTPException;

    /**
       Unexports the locally installed slice of a service, so that it
       cannot be accessed across the network anymore.

       @param serviceName The name of the service this slice is part of.
       @param localSlice The locally installed slice, that answers
       to remote invocations made through the horizontal interface of
       the service.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
       @see jade.core.Service#getLocalSlice()
       @see jade.core.Service#getHorizontalInterface()
    */
    void unexportSlice(String serviceName, Service.Slice localSlice) throws IMTPException;

    /**
       Builds a proxy object for a remote service slice.

       @param itfs The array of all the interfaces that have to be
       implemented by the returned proxy. The first element of the
       array must be an interface derived from
       <code>Service.Slice</code>.
       @return A proxy object that can be safely casted to any of the
       interfaces in the <code>itfs</code> array.
       @throws IMTPException If something goes wrong in the underlying
       network transport.

       @see jade.core.Service
    */
    Service.Slice createSliceProxy(String serviceName, Class itf, Node where) throws IMTPException;

    /**
     * Release all resources of this IMTPManager
     */
    void shutDown();

    /**
       Return the the List of TransportAddress where this IMTP is 
       waiting for intra-platform remote calls.
     */
    List getLocalAddresses() throws IMTPException;
}

