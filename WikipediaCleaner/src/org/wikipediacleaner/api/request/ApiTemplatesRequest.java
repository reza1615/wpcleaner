/*
 *  WikipediaCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2012  Nicolas Vervelle
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.data.Page;


/**
 * MediaWiki API templates requests.
 */
public class ApiTemplatesRequest extends ApiPropertiesRequest {

  // ==========================================================================
  // API properties
  // ==========================================================================

  /**
   * Property for Name space.
   */
  public final static String PROPERTY_NAMESPACE = "tlnamespace";

  /**
   * Property for Limit.
   */
  public final static String PROPERTY_LIMIT = "tllimit";

  /**
   * Property for Templates.
   */
  public final static String PROPERTY_TEMPLATES = "tltemplates";

  /**
   * Maximum templates in a query.
   */
  public final static int MAX_TEMPLATES_PER_QUERY = 50;

  // ==========================================================================
  // Request management
  // ==========================================================================

  private final ApiTemplatesResult result;

  /**
   * @param result Parser for result depending on chosen format.
   */
  public ApiTemplatesRequest(ApiTemplatesResult result) {
    this.result = result;
  }

  /**
   * Set disambiguation status of a list of pages.
   * 
   * @param pages List of pages.
   */
  public void setDisambiguationStatus(List<Page> pages) throws APIException {

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

    // Search disambiguation templates for pages in the main name space
    List<List<Page>> splitPagesList = splitListPages(pages, MAX_PAGES_PER_QUERY);
    for (List<Page> splitPages : splitPagesList) {
      for (Page page : splitPages) {
        Iterator<Page> itPage = page.getRedirectIteratorWithPage();
        while (itPage.hasNext()) {
          itPage.next().setDisambiguationPage(null);
        }
      }
      List<List<Page>> splitTemplatesList = splitListPages(
          result.getWiki().getDisambiguationTemplates(), MAX_TEMPLATES_PER_QUERY);
      for (List<Page> templates : splitTemplatesList) {
        Map<String, String> properties = getProperties(ACTION_QUERY, result.getFormat());
        properties.put(
            PROPERTY_PROP,
            PROPERTY_PROP_TEMPLATES);
        properties.put(PROPERTY_LIMIT, LIMIT_MAX);
        properties.put(PROPERTY_REDIRECTS, "");
        properties.put(
            PROPERTY_TEMPLATES,
            constructListPages(templates));
        properties.put(PROPERTY_TITLES, constructListPages(splitPages));
        while (result.setDiambiguationStatus(properties, splitPages)) {
          //
        }
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