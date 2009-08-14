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

package org.wikipediacleaner.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.wikipediacleaner.api.base.API;
import org.wikipediacleaner.api.base.APIException;
import org.wikipediacleaner.api.base.APIFactory;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.execution.BacklinksWRCallable;
import org.wikipediacleaner.api.execution.ContentsCallable;
import org.wikipediacleaner.api.execution.DisambiguationStatusCallable;
import org.wikipediacleaner.api.execution.EmbeddedInCallable;
import org.wikipediacleaner.api.execution.ExpandTemplatesCallable;
import org.wikipediacleaner.api.execution.LinksWRCallable;
import org.wikipediacleaner.api.execution.ParseTextCallable;
import org.wikipediacleaner.i18n.GT;


/**
 * Centralisation of access to MediaWiki.
 */
public class MediaWiki extends MediaWikiController {

  /**
   * @param listener Listener to MediaWiki events.
   * @return Access to MediaWiki.
   */
  static public MediaWiki getMediaWikiAccess(MediaWikiListener listener) {
    MediaWiki mw = new MediaWiki(listener);
    return mw;
  }

  /**
   * @param listener Listener.
   */
  private MediaWiki(MediaWikiListener listener) {
    super(listener);
  }

  /**
   * Block until all tasks are finished. 
   * 
   * @throws APIException
   */
  public void block(boolean block) throws APIException {
    if (block) {
      while (hasRemainingTask() && !shouldStop()) {
        getNextResult();
      }
    }
    if (shouldStop()) {
      stopRemainingTasks();
    }
  }

  /**
   * Retrieve page contents.
   * 
   * @param page Page.
   * @param block Flag indicating if the call should block until completed.
   * @param returnPage Flag indicating if the page should be returned once task is finished.
   * @throws APIException
   */
  public void retrieveContents(Page page, boolean block, boolean returnPage) throws APIException {
    if (page == null) {
      return;
    }
    final API api = APIFactory.getAPI();
    addTask(new ContentsCallable(this, api, page, returnPage ? page : null));
    block(block);
  }

  /**
   * Replace text in a list of pages.
   * 
   * @param pages List of pages.
   * @param replacements List of text replacements
   *        Key: Additional comments used for the modification.
   *        Value: Text replacements.
   * @param wikipedia Wikipedia.
   * @param comment Comment used for the modification.
   * @param description Out: description of changes made.
   * @throws APIException
   */
  public int replaceText(
      Page[] pages, HashMap<String, Properties> replacements,
      EnumWikipedia wikipedia, String comment,
      StringBuffer description) throws APIException {
    if ((pages == null) || (replacements == null) || (replacements.size() == 0)) {
      return 0;
    }
    for (Page page : pages) {
      retrieveContents(page, false, true);
    }
    int count = 0;
    final API api = APIFactory.getAPI();
    StringBuffer details = new StringBuffer();
    while (hasRemainingTask() && !shouldStop()) {
      Object result = getNextResult();
      if ((result != null) && (result instanceof Page)) {
        boolean changed = false;
        Page page = (Page) result;
        String oldContents = page.getContents();
        if (oldContents != null) {
          String newContents = oldContents;
          details.setLength(0);
          for (Entry<String, Properties> replacement : replacements.entrySet()) {
            boolean replacementUsed = false;
            for (Entry<Object, Object> replacementValue : replacement.getValue().entrySet()) {
              String from = replacementValue.getKey().toString();
              String to = replacementValue.getValue().toString();
              String tmpContents = newContents;
              newContents = tmpContents.replaceAll(Pattern.quote(from), to);
              if (!newContents.equals(tmpContents)) {
                if (description != null) {
                  if (!changed) {
                    description.append(GT._("Page {0}:", page.getTitle()));
                    description.append("\n");
                    changed = true;
                  }
                  description.append(" - ");
                  description.append(from);
                  description.append(" => ");
                  description.append(to);
                  description.append("\n");
                }
                if (!replacementUsed) {
                  replacementUsed = true;
                  if (details.length() > 0) {
                    details.append(", ");
                  }
                  details.append(replacement.getKey());
                }
              }
            }
          }
          if (!oldContents.equals(newContents)) {
            count++;
            api.updatePage(page, newContents, wikipedia.createUpdatePageComment(comment, details.toString()));
          }
        }
      }
    }
    block(true);
    return count;
  }

  /**
   * Expand templates.
   * 
   * @param title Title of the page.
   * @param text Text of the page.
   * @throws APIException
   */
  public String expandTemplates(String title, String text) throws APIException {
    if (text == null) {
      return null;
    }
    final API api = APIFactory.getAPI();
    addTask(new ExpandTemplatesCallable(this, api, title, text));
    while (hasRemainingTask() && !shouldStop()) {
      Object result = getNextResult();
      if (result != null) {
        return result.toString();
      }
    }
    block(true);
    return null;
  }

  /**
   * Parse complete text.
   * 
   * @param title Title of the page.
   * @param text Text of the page.
   * @throws APIException
   */
  public String parseText(String title, String text) throws APIException {
    if (text == null) {
      return null;
    }
    final API api = APIFactory.getAPI();
    addTask(new ParseTextCallable(this, api, title, text));
    while (hasRemainingTask() && !shouldStop()) {
      Object result = getNextResult();
      if (result != null) {
        return result.toString();
      }
    }
    block(true);
    return null;
  }

  /**
   * Retrieve all links (with redirects) of a page.
   * 
   * @param page Page.
   * @throws APIException
   */
  public void retrieveAllLinks(Page page) throws APIException {
    if (page == null) {
      return;
    }
    final API api = APIFactory.getAPI();
    addTask(new LinksWRCallable(this, api, page));
    block(true);
  }

  /**
   * Retrieve all backlinks (with redirects) of a page.
   * 
   * @param page Page.
   * @throws APIException
   */
  public void retrieveAllBacklinks(Page page) throws APIException {
    if (page == null) {
      return;
    }
    retrieveAllBacklinks(new Page[] { page });
  }

  /**
   * Retrieve all backlinks (with redirects) of a list of pages.
   * 
   * @param pageList List of pages.
   * @throws APIException
   */
  public void retrieveAllBacklinks(Page[] pageList) throws APIException {
    if ((pageList == null) || (pageList.length == 0)) {
      return;
    }
    final API api = APIFactory.getAPI();
    for (final Page page : pageList) {
      addTask(new BacklinksWRCallable(this, api, page));
    }
    while (hasRemainingTask() && !shouldStop()) {
      Object result = getNextResult();
      if (result != null) {
        if (result instanceof Page) {
          final Page page = (Page) result;
          ArrayList<Page> backlinks = page.getBackLinks();
          Iterator<Page> iter1 = backlinks.iterator();
          while (iter1.hasNext()) {
            final Page p = iter1.next();
            if (!Page.areSameTitle(page.getTitle(), p.getTitle())) {
              Iterator<Page> iter = p.getRedirectIteratorWithPage();
              while (iter.hasNext()) {
                Page tmp = iter.next();
                if (Page.areSameTitle(page.getTitle(), tmp.getTitle())) {
                  addTask(new BacklinksWRCallable(this, api, p));
                }
              }
            }
          }
        }
      }
    }
    block(true);
  }

  /**
   * Retrieve all pages it is embedded in of a list of pages.
   * 
   * @param pageList List of pages.
   * @throws APIException
   */
  public void retrieveAllEmbeddedIn(ArrayList<Page> pageList) throws APIException {
    if ((pageList == null) || (pageList.size() == 0)) {
      return;
    }
    final API api = APIFactory.getAPI();
    for (final Page page : pageList) {
      addTask(new EmbeddedInCallable(this, api, page));
    }
    block(true);
  }

  /**
   * Retrieve disambiguation information for a list of pages.
   * 
   * @param pageList List of page.
   * @param disambiguations Flag indicating if possible disambiguations should be retrieved.
   * @throws APIException
   */
  public void retrieveDisambiguationInformation(
      ArrayList<Page> pageList,
      boolean disambiguations) throws APIException {
    if ((pageList == null) || (pageList.isEmpty())) {
      return;
    }
    final API api = APIFactory.getAPI();

    // Retrieving disambiguation status
    final int maxPages = api.getMaxPagesPerQuery();
    if (pageList.size() <= maxPages) {
      addTask(new DisambiguationStatusCallable(this, api, pageList));
    } else {
      int index = 0;
      while (index < pageList.size()) {
        ArrayList<Page> tmpList = new ArrayList<Page>(api.getMaxPagesPerQuery());
        for (int i = 0; i < maxPages && index < pageList.size(); i++, index++) {
          tmpList.add(pageList.get(index));
        }
        addTask(new DisambiguationStatusCallable(this, api, tmpList));
      }
    }
    block(true);

    // Retrieving possible disambiguations
    if (disambiguations) {
      for (Page p : pageList) {
        Iterator<Page> iter = p.getRedirectIteratorWithPage();
        while (iter.hasNext()) {
          p = iter.next();
          if (Boolean.TRUE.equals(p.isDisambiguationPage())) {
            addTask(new LinksWRCallable(this, api, p));
          }
        }
      }
    }
    block(true);
  }
}
