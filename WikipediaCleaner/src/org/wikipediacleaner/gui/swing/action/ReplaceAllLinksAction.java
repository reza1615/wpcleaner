/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2007  Nicolas Vervelle
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wikipediacleaner.gui.swing.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.data.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementInternalLink;
import org.wikipediacleaner.gui.swing.component.MWPane;
import org.wikipediacleaner.gui.swing.component.MenuCreator;


/**
 * An action listener for replacing all internal links.
 */
public class ReplaceAllLinksAction implements ActionListener {

  private MWPane textPane;
  private Page from;
  private String to;

  public ReplaceAllLinksAction(MWPane textPane, Page from, String to) {
    this.textPane = textPane;
    this.from = from;
    this.to = to;
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
    String originalText = textPane.getText();
    PageAnalysis analysis = new PageAnalysis(textPane.getWikiPage(), originalText);
    StringBuilder buffer = new StringBuilder();
    int lastPosition = 0;
    Collection<PageElementInternalLink> links = analysis.getInternalLinks();
    for (PageElementInternalLink link : links) {
      if (Page.areSameTitle(from.getTitle(), link.getLink())) {
        buffer.append(originalText.substring(lastPosition, link.getBeginIndex()));
        lastPosition = link.getBeginIndex();
        buffer.append("[[");
        buffer.append(to);
        buffer.append("|");
        buffer.append(link.getDisplayedText());
        buffer.append("]]");
        lastPosition = link.getEndIndex();
      }
    }
    if (lastPosition > 0) {
      buffer.append(originalText.substring(lastPosition));
      textPane.changeText(buffer.toString());
    }
    MenuCreator.addLastReplacement(from.getTitle(), to);
  }
}
