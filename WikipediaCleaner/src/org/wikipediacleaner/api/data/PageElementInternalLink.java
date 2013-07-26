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

package org.wikipediacleaner.api.data;

import java.util.List;

import org.wikipediacleaner.api.constants.EnumWikipedia;


/**
 * Class containing information about a complete internal link ([[link#anchor|text]]). 
 */
public class PageElementInternalLink extends PageElement {

  private final String linkNotTrimmed;
  private final String link;
  private final String anchorNotTrimmed;
  private final String anchor;
  private final String textNotTrimmed;
  private final String text;
  private final int    textOffset;

  /**
   * Analyze contents to check if it matches an internal link.
   * 
   * @param wikipedia Wikipedia.
   * @param contents Contents.
   * @param index Block start index.
   * @return Block details it there's a block.
   */
  public static PageElementInternalLink analyzeBlock(
      EnumWikipedia wikipedia, String contents, int index) {
    // Verify arguments
    if (contents == null) {
      return null;
    }

    // Look for '[['
    int tmpIndex = index;
    if ((tmpIndex >= contents.length()) ||
        (!contents.startsWith("[[", tmpIndex))) {
      return null;
    }
    tmpIndex += 2;
    int beginIndex = tmpIndex;

    // Retrieve link
    int anchorIndex = -1;
    int pipeIndex = -1;
    int endIndex = -1;
    int level3CurlyBrackets = 0;
    while ((tmpIndex < contents.length()) &&
           (endIndex < 0) &&
           (pipeIndex < 0)) {
      switch (contents.charAt(tmpIndex)) {
      case '[':
        return null;
      case ']':
        if ((level3CurlyBrackets == 0) &&
            (contents.startsWith("]]", tmpIndex))) {
          endIndex = tmpIndex;
          tmpIndex++;
        } else {
          return null;
        }
        break;
      case '{':
        if (contents.startsWith("{{{", tmpIndex)) {
          level3CurlyBrackets++;
          tmpIndex += 2;
        } else {
          return null;
        }
        break;
      case '}':
        if ((contents.startsWith("}}}", tmpIndex)) &&
            (level3CurlyBrackets > 0)) {
          level3CurlyBrackets--;
          tmpIndex += 2;
        } else {
          return null;
        }
        break;
      case '#':
        if ((level3CurlyBrackets == 0) &&
            (anchorIndex < 0)) {
          anchorIndex = tmpIndex;
        }
        break;
      case '|':
        if (level3CurlyBrackets == 0) {
          pipeIndex = tmpIndex;
        }
      }
      tmpIndex++;
    }

    // Retrieve link text
    if (endIndex < 0) {
      int level2CurlyBrackets = 0;
      level3CurlyBrackets = 0;
      while ((tmpIndex < contents.length()) && (endIndex < 0)) {
        switch (contents.charAt(tmpIndex)) {
        case '[':
          if (contents.startsWith("[[", tmpIndex)) {
            return null;
          }
          break;
        case ']':
          if (contents.startsWith("]]", tmpIndex)) {
            endIndex = tmpIndex;
            tmpIndex++;
          }
          break;
        case '{':
          if (contents.startsWith("{{{", tmpIndex)) {
            level3CurlyBrackets++;
            tmpIndex += 2;
          } else if (contents.startsWith("{{", tmpIndex)) {
            level2CurlyBrackets++;
            tmpIndex++;
          }
          break;
        case '}':
          if ((contents.startsWith("}}}", tmpIndex)) &&
              (level3CurlyBrackets > 0)) {
            level3CurlyBrackets--;
            tmpIndex += 2;
          } else if ((contents.startsWith("}}", tmpIndex)) &&
                     (level2CurlyBrackets > 0)) {
            level2CurlyBrackets--;
            tmpIndex++;
          }
          break;
        }
        tmpIndex++;
      }
    }
    if (endIndex < 0) {
      return null;
    }

    // Extract link elements
    String link = null;
    String anchor = null;
    String text = null;
    int textOffset = -1;
    if ((pipeIndex >= 0) && (pipeIndex < endIndex)) {
      if ((anchorIndex >= 0) && (anchorIndex < pipeIndex)) {
        link = contents.substring(beginIndex, anchorIndex);
        anchor = contents.substring(anchorIndex + 1, pipeIndex);
      } else {
        link = contents.substring(beginIndex, pipeIndex);
      }
      text = contents.substring(pipeIndex + 1, endIndex);
      textOffset = pipeIndex + 1 - index;
    } else if ((anchorIndex >= 0) && (anchorIndex < endIndex)) {
      link = contents.substring(beginIndex, anchorIndex);
      anchor = contents.substring(anchorIndex + 1, endIndex);
    } else {
      link = contents.substring(beginIndex, endIndex);
    }

    // Check that it is really an internal link
    String linkTrimmed = link.trim();
    int colonIndex = linkTrimmed.indexOf(':');
    if (colonIndex > 0) {
      String namespaceName = linkTrimmed.substring(0, colonIndex);

      // Is it a category ?
      Namespace category = wikipedia.getWikiConfiguration().getNamespace(Namespace.CATEGORY);
      if ((category != null) && (category.isPossibleName(namespaceName))) {
        return null;
      }

      // Is it a file / image ?
      Namespace image = wikipedia.getWikiConfiguration().getNamespace(Namespace.IMAGE);
      if ((image != null) && (image.isPossibleName(namespaceName))) {
        return null;
      }

      // Is it an interwiki ?
      for (Interwiki iw : wikipedia.getWikiConfiguration().getInterwikis()) {
        if (iw.getPrefix().equals(namespaceName)) {
          return null;
        }
      }

      // Is it a language link ?
      if (Language.isLanguageCode(
          wikipedia.getWikiConfiguration().getLanguages(), namespaceName)) {
        return null;
      }
    }

    // Check that this is not an external link between double square brackets
    List<String> protocols = PageElementExternalLink.getProtocols();
    for (String protocol : protocols) {
      if (linkTrimmed.startsWith(protocol)) {
        return null;
      }
    }

    // Create internal link
    return new PageElementInternalLink(
        wikipedia,
        index, endIndex + 2,
        link, anchor, text, textOffset);
  }

  public String getLink() {
    return link;
  }

  public String getLinkNotNormalized() {
    return (linkNotTrimmed != null) ? linkNotTrimmed.trim() : null;
  }

  public String getAnchor() {
    return anchor;
  }

  public String getFullLink() {
    if (anchor == null) {
      return link;
    }
    return link + "#" + anchor;
  }

  public String getText() {
    return text;
  }

  public int getTextOffset() {
    return textOffset;
  }

  public String getDisplayedText() {
    if (text != null) {
      return text;
    }
    if (anchor == null) {
      return linkNotTrimmed;
    }
    return linkNotTrimmed + "#" + anchorNotTrimmed;
  }

  public String getDisplayedTextNotTrimmed() {
    if (textNotTrimmed != null) {
      return textNotTrimmed;
    }
    if (anchor == null) {
      return linkNotTrimmed;
    }
    return linkNotTrimmed + "#" + anchorNotTrimmed;
  }

  private PageElementInternalLink(
      EnumWikipedia wikipedia,
      int beginIndex, int endIndex,
      String link, String anchor,
      String text, int textOffset) {
    super(beginIndex, endIndex);
    this.linkNotTrimmed = link;
    this.link = (link != null) ? wikipedia.normalizeTitle(link.trim()) : null;
    this.anchorNotTrimmed = anchor;
    this.anchor = (anchor != null) ? anchor.trim() : null;
    this.textNotTrimmed = text;
    this.text = (text != null) ? text.trim() : null;
    this.textOffset = textOffset;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return createInternalLink(linkNotTrimmed, anchorNotTrimmed, textNotTrimmed);
  }

  /**
   * Create an internal link.
   * 
   * @param link Link.
   * @param text Displayed text.
   * @return Internal link.
   */
  public static String createInternalLink(String link, String text) {
    return createInternalLink(link, null, text);
  }

  /**
   * Create an internal link.
   * 
   * @param link Link.
   * @param anchor Anchor
   * @param text Displayed text.
   * @return Internal link.
   */
  public static String createInternalLink(String link, String anchor, String text) {
    StringBuilder sb = new StringBuilder();
    sb.append("[[");
    String fullLink = null;
    if ((link != null) || (anchor != null)) {
      fullLink =
          ((link != null) ? link.trim() : "") +
          ((anchor != null) ? ("#" + anchor.trim()) : "");
    }
    if (text != null) {
      if ((fullLink != null) && (!Page.areSameTitle(fullLink, text))) {
        sb.append(fullLink);
        sb.append("|");
      }
      sb.append(text.trim());
    } else {
      sb.append(fullLink);
    }
    sb.append("]]");
    return sb.toString();
  }
}
