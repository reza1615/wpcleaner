/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
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

package org.wikipediacleaner.api.check.algorithm;

import java.util.Collection;
import java.util.List;

import org.wikipediacleaner.api.check.CheckErrorResult;
import org.wikipediacleaner.api.data.PageAnalysis;
import org.wikipediacleaner.api.data.PageElementTitle;


/**
 * Algorithm for analyzing error 7 of check wikipedia project.
 * Error 7: Headlines all start with three "="
 */
public class CheckErrorAlgorithm007 extends CheckErrorAlgorithmBase {

  public CheckErrorAlgorithm007() {
    super("Headlines all start with three \"=\"");
  }

  /**
   * Analyze a page to check if errors are present.
   * 
   * @param pageAnalysis Page analysis.
   * @param errors Errors found in the page.
   * @return Flag indicating if the error was found.
   */
  public boolean analyze(
      PageAnalysis pageAnalysis,
      Collection<CheckErrorResult> errors) {
    if (pageAnalysis == null) {
      return false;
    }

    // Check level of each title
    List<PageElementTitle> titles = pageAnalysis.getTitles();
    if ((titles == null) || (titles.size() == 0)) {
      return false;
    }
    for (PageElementTitle title : titles) {
      if (title.getLevel() < 3) {
        return false;
      }
    }

    if (errors == null) {
      return true;
    }
    CheckErrorResult errorResult = createCheckErrorResult(
        pageAnalysis.getPage(),
        titles.get(0).getBeginIndex(), titles.get(0).getEndIndex());
    errorResult.addEditTocAction();
    errors.add(errorResult);
    return true;
  }

  /**
   * Bot fixing of all the errors in the page.
   * 
   * @param analysis Page analysis.
   * @return Page contents after fix.
   */
  @Override
  public String botFix(PageAnalysis analysis) {
    String contents = analysis.getContents();
    if (!analysis.areTitlesReliable()) {
      return contents;
    }

    // Compute minimum title level
    List<PageElementTitle> titles = analysis.getTitles();
    if ((titles == null) || (titles.size() == 0)) {
      return contents;
    }
    int minTitle = Integer.MAX_VALUE;
    for (PageElementTitle title : titles) {
      if (title.getLevel() < minTitle) {
        minTitle = title.getLevel();
      }
    }
    if (minTitle < 3) {
      return contents;
    }

    // Replace titles
    StringBuilder tmp = new StringBuilder();
    int lastIndex = 0;
    int offset = minTitle - 2;
    for (PageElementTitle title : titles) {
      if (lastIndex < title.getBeginIndex()) {
        tmp.append(contents.substring(lastIndex, title.getBeginIndex()));
        lastIndex = title.getBeginIndex();
      }
      tmp.append(PageElementTitle.createTitle(title.getLevel() - offset, title.getTitle()));
      if (title.getAfterTitle() != null) {
        tmp.append(title.getAfterTitle());
      }
      lastIndex = title.getEndIndex();
    }
    if (lastIndex < contents.length()) {
      tmp.append(contents.substring(lastIndex));
    }

    return tmp.toString();
  }
}
