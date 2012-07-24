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

package org.wikipediacleaner.api.request.xml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.xpath.XPath;
import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.DataManager;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.request.ApiRawWatchlistResult;
import org.wikipediacleaner.api.request.ApiRequest;
import org.wikipediacleaner.api.request.ConnectionInformation;


/**
 * Class for MediaWiki API XML raw watch list results.
 */
public class ApiXmlRawWatchlistResult extends ApiXmlResult implements ApiRawWatchlistResult {

  /**
   * @param wiki Wiki on which requests are made.
   * @param httpClient HTTP client for making requests.
   * @param connection Connection information.
   */
  public ApiXmlRawWatchlistResult(
      EnumWikipedia wiki,
      HttpClient httpClient,
      ConnectionInformation connection) {
    super(wiki, httpClient, connection);
  }

  /**
   * Execute watch list raw request.
   * 
   * @param properties Properties defining request.
   * @param watchlist List of pages to be filled with the watch list.
   * @return True if request should be continued.
   * @throws APIException
   */
  public boolean executeWatchlistRaw(
      Map<String, String> properties,
      List<Page> watchlist)
          throws APIException {
    try {
      return constructWatchlist(
          getRoot(properties, ApiRequest.MAX_ATTEMPTS),
          properties,
          watchlist);
    } catch (JDOMParseException e) {
      log.error("Error loading watch list", e);
      throw new APIException("Error parsing XML", e);
    }
  }

  /**
   * Construct watch list.
   * 
   * @param root Root element.
   * @param properties Properties defining request.
   * @param watchlist List of pages to be filled with the watch list.
   * @return True if request should be continued.
   * @throws APIException
   */
  private boolean constructWatchlist(
      Element root, Map<String, String> properties,
      List<Page> watchlist)
      throws APIException {

    // Retrieve watch list
    try {
      XPath xpa = XPath.newInstance("/api/watchlistraw/wr");
      List results = xpa.selectNodes(root);
      Iterator iter = results.iterator();
      XPath xpaTitle = XPath.newInstance("./@title");
      while (iter.hasNext()) {
        Element currentNode = (Element) iter.next();
        Page page = DataManager.getPage(
            getWiki(), xpaTitle.valueOf(currentNode), null, null);
        if (page.isArticle()) {
          watchlist.add(page);
        }
      }
    } catch (JDOMException e) {
      log.error("Error watchlist raw", e);
      throw new APIException("Error parsing XML result", e);
    }

    // Retrieve continue
    return shouldContinue(
        root, "/api/query-continue/watchlistraw",
        properties);
  }
}