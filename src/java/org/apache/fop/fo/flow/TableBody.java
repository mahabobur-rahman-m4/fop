/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.flow;

// Java
import java.util.Iterator;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:table-body object.
 * @todo implement validateChildNode()
 */
public class TableBody extends FObj {
    // The value of properties relevant for fo:table-body.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonRelativePosition commonRelativePosition;
    // private ToBeImplementedProperty borderAfterPrecedence;
    // private ToBeImplementedProperty borderBeforePrecedence;
    // private ToBeImplementedProperty borderEndPrecedence;
    // private ToBeImplementedProperty borderStartPrecedence;
    private int visibility;
    // End of property values
    
    private PropertyList savedPropertyList;
    
    /**
     * @param parent FONode that is the parent of the object
     */
    public TableBody(FONode parent) {
        super(parent);
    }

    /**
     * @see FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonRelativePosition = pList.getRelativePositionProps();
        // borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE);
        // borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE);
        // borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE);
        // borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE);
        visibility = pList.get(PR_VISIBILITY).getEnum();
        
        //Used by convertCellsToRows()
        savedPropertyList = pList;
    }
    
    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        getFOEventHandler().startBody(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        getFOEventHandler().endBody(this);
        convertCellsToRows();
    }

    /**
     * If table-cells are used as direct children of a table-body|header|footer
     * they are replace in this method by proper table-rows.
     * @throws FOPException if there's a problem binding the TableRows properties.
     */
    private void convertCellsToRows() throws FOPException {
        try {
            if (childNodes.size() == 0 || childNodes.get(0) instanceof TableRow) {
                return;
            }
            //getLogger().debug("Converting cells to rows...");
            List cells = (List)childNodes.clone();
            childNodes.clear();
            Iterator i = cells.iterator();
            TableRow row = null;
            while (i.hasNext()) {
                TableCell cell = (TableCell)i.next();
                if (cell.startsRow() && (row != null)) {
                    childNodes.add(row);
                    row = null;
                }
                if (row == null) {
                    row = new TableRow(this);
                    PropertyList pList = new StaticPropertyList(row, savedPropertyList);
                    pList.setWritingMode();
                    row.bind(pList);
                }
                row.addReplacedCell(cell);
                if (cell.endsRow()) {
                    childNodes.add(row);
                    row = null;
                }
            }
            if (row != null) {
                childNodes.add(row);
            }
        } finally {
            savedPropertyList = null; //Release reference
        }
    }
    
    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-body";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_BODY;
    }
}

