/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.tools.rma;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**
 * The model of the AMSTree
 */
public class AMSTreeModel extends DefaultTreeModel {

  /**
   * the Root of the Tree
   */
  protected static TreeData root = new TreeData ("JADE ",TreeData.SUPER_NODE);

  public AMSTreeModel () {
    super(root);
  }

  /* TreeModel methods */
  public Object getRoot() {
    return root;
  }

  /**
   * This method must be rewritten if we want
   * to make possible editing the tree
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}

	
  /**
   * This method must be rewritten if we want
   * to make possible editing the tree
   */
  protected void fireValueChanged(TreePath path,int[] ind,Object[] children)  {}

}

