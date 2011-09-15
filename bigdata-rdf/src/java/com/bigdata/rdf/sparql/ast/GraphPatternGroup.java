/**

Copyright (C) SYSTAP, LLC 2006-2011.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Sep 5, 2011
 */

package com.bigdata.rdf.sparql.ast;

import java.util.Map;

import com.bigdata.bop.BOp;

/**
 * Join group or union.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
abstract public class GraphPatternGroup<E extends IGroupMemberNode> extends
        GroupNodeBase<E> implements IBindingProducerNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Required deep copy constructor.
     */
    public GraphPatternGroup(GraphPatternGroup<E> op) {

        super(op);
        
    }

    /**
     * Required shallow copy constructor.
     */
    public GraphPatternGroup(BOp[] args, Map<String, Object> anns) {

        super(args, anns);

    }
    
    /**
     * 
     */
    public GraphPatternGroup() {
    }

    /**
     * @param optional
     */
    public GraphPatternGroup(boolean optional) {
        super(optional);
    }

}
