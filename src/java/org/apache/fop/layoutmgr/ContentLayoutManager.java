/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.Resolvable;
import org.apache.fop.area.PageViewport;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ArrayList;
import org.apache.fop.traits.MinOptMax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content Layout Manager.
 * For use with objects that contain inline areas such as
 * leader use-content and title.
 */
public class ContentLayoutManager implements InlineLevelLayoutManager {
    private FOUserAgent userAgent;
    private AreaTreeHandler areaTreeHandler;
    private Area holder;
    private int stackSize;
    private LayoutManager parentLM;
    private InlineLevelLayoutManager childLM = null;

    /**
     * logging instance
     */
    protected static Log log = LogFactory.getLog(LayoutManager.class);

    /**
     * Constructs a new ContentLayoutManager
     *
     * @param area  The parent area
     */
    public ContentLayoutManager(Area area) {
        holder = area;
    }

    /**
     * Set the FO object for this layout manager
     *
     * @param fo the fo for this layout manager
     */
    public void setFObj(FObj fo) {
    }

    public void fillArea(LayoutManager curLM) {

        int ipd = 1000000;

        LayoutContext childLC = new LayoutContext(LayoutContext.NEW_AREA);
        childLC.setLeadingSpace(new SpaceSpecifier(false));
        childLC.setTrailingSpace(new SpaceSpecifier(false));
        // set stackLimit for lines
        childLC.setStackLimit(new MinOptMax(ipd));
        childLC.setRefIPD(ipd);

        int lineHeight = 14000;
        int lead = 12000;
        int follow = 2000;

        int halfLeading = (lineHeight - lead - follow) / 2;
        // height before baseline
        int lineLead = lead + halfLeading;
        // maximum size of top and bottom alignment
        int maxtb = follow + halfLeading;
        // max size of middle alignment below baseline
        int middlefollow = maxtb;

        stackSize = 0;

        LinkedList contentList =
            getNextKnuthElements(childLC, Constants.EN_START);
        ListIterator contentIter = contentList.listIterator();
        while (contentIter.hasNext()) {
            KnuthElement element = (KnuthElement) contentIter.next();
            if (element.isBox()) {
                KnuthBox box = (KnuthBox) element;
                if (box.getLead() > lineLead) {
                    lineLead = box.getLead();
                }
                if (box.getTotal() > maxtb) {
                    maxtb = box.getTotal();
                }
                // Is this needed? cf. LineLM.makeLineBreakPosition
                // if (box.getMiddle() > lineLead) {
                //     lineLead = box.getMiddle();
                // }
                if (box.getMiddle() > middlefollow) {
                    middlefollow = box.getMiddle();
                }
            }
        }

        if (maxtb - lineLead > middlefollow) {
            middlefollow = maxtb - lineLead;
        }

        LayoutContext lc = new LayoutContext(0);
        lc.setBaseline(lineLead);
        lc.setLineHeight(lineHeight);

        lc.setFlags(LayoutContext.RESOLVE_LEADING_SPACE, true);
        lc.setLeadingSpace(new SpaceSpecifier(false));
        lc.setTrailingSpace(new SpaceSpecifier(false));
        KnuthPossPosIter contentPosIter =
            new KnuthPossPosIter(contentList, 0, contentList.size());
        curLM.addAreas(contentPosIter, lc);
    }

    public void addAreas(PositionIterator posIter, LayoutContext context) {
        // add the content areas
        // the area width has already been adjusted, and it must remain unchanged
        // so save its value before calling addAreas, and set it again afterwards
        int savedIPD = ((InlineArea)holder).getIPD();
        // set to zero the ipd adjustment ratio, to avoid spaces in the pattern
        // to be modified
        LayoutContext childContext = new LayoutContext(context);
        childContext.setIPDAdjust(0.0);
        childLM.addAreas(posIter, childContext);
        ((InlineArea)holder).setIPD(savedIPD);
    }

    public int getStackingSize() {
        return stackSize;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean generatesInlineAreas() {
        return true;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public Area getParentArea(Area childArea) {
        return holder;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addChild(Area childArea) {
        holder.addChild(childArea);
    }

    /**
     * Set the user agent.
     *
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getUserAgent()
     */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setParent(LayoutManager lm) {
        parentLM = lm;
    }

    public LayoutManager getParent() {
        return this.parentLM;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean canBreakBefore(LayoutContext lc) {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        return null;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean isFinished() {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setFinished(boolean isFinished) {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void initialize() {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void resetPosition(Position position) {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void getWordChars(StringBuffer sbChars, Position bp1,
            Position bp2) { }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public String getCurrentPageNumber() {
        return parentLM.getCurrentPageNumber();
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public PageViewport resolveRefID(String ref) {
        return parentLM.resolveRefID(ref);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addIDToPage(String id) {
        parentLM.addIDToPage(id);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addUnresolvedArea(String id, Resolvable res) {
        parentLM.addUnresolvedArea(id, res);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addMarkerMap(Map marks, boolean start, boolean isfirst) {
        parentLM.addMarkerMap(marks, start, isfirst);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public Marker retrieveMarker(String name, int pos, int boundary) {
        return parentLM.retrieveMarker(name, pos, boundary);
    }

    /**
     * Set the AreaTreeHandler.
     * This is used by the PageSequenceLM for the Title,
     * because it does not set itself as the parentLM.
     * @param areaTreeHandler the area tree handler to add pages to
     */
    public void setAreaTreeHandler(AreaTreeHandler areaTreeHandler) {
        this.areaTreeHandler = areaTreeHandler;
    }

    /**
     * Either areaTreeHandler or parentLM should not be null.
     * If areaTreeHandler is null,
     * delegate getAreaTreeHandler to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     * @return the AreaTreeHandler object.
     */
    public AreaTreeHandler getAreaTreeHandler() {
        if (areaTreeHandler == null) {
            areaTreeHandler = parentLM.getAreaTreeHandler();
        }
        return areaTreeHandler;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#preLoadNext
     */
    public boolean preLoadNext(int pos) {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChildLMs
     */
    public List getChildLMs() {
        List childLMs = new ArrayList(1);
        childLMs.add(childLM);
        return childLMs;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildLM
     */
    public void addChildLM(LayoutManager lm) {
        if (lm == null) {
            return;
        }
        lm.setParent(this);
        lm.initialize();
        childLM = (InlineLevelLayoutManager)lm;
        log.trace(this.getClass().getName()
                  + ": Adding child LM " + lm.getClass().getName());
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildLMs
     */
    public void addChildLMs(List newLMs) {
        if (newLMs == null || newLMs.size() == 0) {
            return;
        }
        ListIterator iter = newLMs.listIterator();
        while (iter.hasNext()) {
            LayoutManager lm = (LayoutManager) iter.next();
            addChildLM(lm);
        }
    }

    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        LinkedList contentList = new LinkedList();
        LinkedList returnedList;

        while (!childLM.isFinished()) {
            // get KnuthElements from childLM
            returnedList = childLM.getNextKnuthElements(context, alignment);

            if (returnedList != null) {
                // move elements to contentList, and accumulate their size
               KnuthElement contentElement;
               while (returnedList.size() > 0) {
                    contentElement = (KnuthElement)returnedList.removeFirst();
                    stackSize += contentElement.getW();
                    contentList.add(contentElement);
                }
            }
        }

        setFinished(true);
        return contentList;
    }

    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        return element;
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
    }

    public void hyphenate(Position pos, HyphContext hc) {
    }

    public boolean applyChanges(List oldList) {
        return false;
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              int flaggedPenalty,
                                              int alignment) {
        return null;
    }
}

