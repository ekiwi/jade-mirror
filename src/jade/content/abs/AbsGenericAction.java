/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.abs;

import jade.content.GenericAction;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsGenericAction extends AbsContentElement implements GenericAction {

    /**
     * Construct an Abstract descriptor to hold a generic action of
     * the proper type.
     * @param typeName The name of the type of the generic action held by 
     * this abstract descriptor.
     */
    public AbsGenericAction(String typeName) {
        super(typeName);
    }

    /**
     */
    //private AbsGenericAction() {
    //    super(GenericActionSchema.BASE_NAME);
    //}

    /**
     * Retrieves the name of the action.
     *
     * @return the name of the action.
     *
     */
    //public String getName() {
    //    return getTypeName();
    //} 

}

