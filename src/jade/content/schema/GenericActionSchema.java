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
package jade.content.schema;

import jade.content.abs.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class GenericActionSchema extends ContentElementSchema {
    public static final String         BASE_NAME = "GenericAction";
    private static GenericActionSchema baseSchema = new GenericActionSchema();

    /**
     * Construct a schema that vinculates an entity to be a generic
     * action
     */
    private GenericActionSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>GenericActionSchema</code> with a given type-name.
     *
     * @param typeName The name of this <code>GenericActionSchema</code>.
     */
    protected GenericActionSchema(String typeName) {
        super(typeName);
    }

    /**
     * Retrieve the generic base schema for generic actions.
     *
     * @return the generic base schema for generic actions.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold a generic action of
     * the proper type.
     */
    public AbsObject newInstance() {
        return new AbsGenericAction(getTypeName());
    } 
}
