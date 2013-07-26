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

package org.wikipediacleaner.api.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.Page;


/**
 * MediaWiki API page properties requests.
 */
public class ApiPagePropsRequest extends ApiPropertiesRequest {

  // ==========================================================================
  // API properties
  // ==========================================================================

  /**
   * Property for Properties.
   */
  public final static String PROPERTY_PROPERTIES = "ppprop";

  /**
   * Property value for Properties / Disambiguation.
   */
  public final static String PROPERTY_PROPERTIES_DISAMBIGUATION = "disambiguation";

  // ==========================================================================
  // Request management
  // ==========================================================================

  private final ApiPagePropsResult result;

  /**
   * @param wiki Wiki.
   * @param result Parser for result depending on chosen format.
   */
  public ApiPagePropsRequest(EnumWikipedia wiki, ApiPagePropsResult result) {
    super(wiki);
    this.result = result;
  }

  /**
   * Set disambiguation status of a list of pages.
   * 
   * @param pages List of pages.
   */
  public void setDisambiguationStatus(Collection<Page> pages) throws APIException {

    // Check for pages outside the main name space
    List<Page> tmpPages = new ArrayList<Page>();
    for (Page page : pages) {
      if (page.isInMainNamespace()) {
        if (!tmpPages.contains(page)) {
          tmpPages.add(page);
        }
      } else {
        page.setDisambiguationPage(Boolean.FALSE);
      }
    }

    // Search disambiguation categories for pages in the main name space
    List<Collection<Page>> splitPagesList = splitListPages(pages, MAX_PAGES_PER_QUERY);
    for (Collection<Page> splitPages : splitPagesList) {
      for (Page page : splitPages) {
        Iterator<Page> itPage = page.getRedirectIteratorWithPage();
        while (itPage.hasNext()) {
          itPage.next().setDisambiguationPage(null);
        }
      }
      Map<String, String> properties = getProperties(ACTION_QUERY, result.getFormat());
      properties.put(PROPERTY_PROP, PROPERTY_PROP_PAGEPROPS);
      properties.put(PROPERTY_PROPERTIES, PROPERTY_PROPERTIES_DISAMBIGUATION);
      properties.put(PROPERTY_REDIRECTS, "");
      properties.put(PROPERTY_TITLES, constructListTitles(splitPages));
      while (result.setDiambiguationStatus(properties, splitPages)) {
        //
      }
      for (Page page : splitPages) {
        Iterator<Page> itPage = page.getRedirectIteratorWithPage();
        while (itPage.hasNext()) {
          Page tmpPage = itPage.next();
          if (tmpPage.isDisambiguationPage() == null) {
            tmpPage.setDisambiguationPage(Boolean.FALSE);
          }
        }
      }
    }
  }
}
