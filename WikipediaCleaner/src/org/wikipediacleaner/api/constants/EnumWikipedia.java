/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.constants;

import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.wikipediacleaner.Version;
import org.wikipediacleaner.api.API;
import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.constants.wiki.AbstractWikiSettings;
import org.wikipediacleaner.api.constants.wiki.Waze;
import org.wikipediacleaner.api.constants.wiki.WazeopediaUSA;
import org.wikipediacleaner.api.constants.wiki.WikiSkripta;
import org.wikipediacleaner.api.constants.wiki.WikimediaCommons;
import org.wikipediacleaner.api.constants.wiki.Wikipedia;
import org.wikipediacleaner.api.constants.wiki.Wikiquote;
import org.wikipediacleaner.api.constants.wiki.Wikisource;
import org.wikipediacleaner.api.constants.wiki.Wikiversity;
import org.wikipediacleaner.api.constants.wiki.Wiktionary;
import org.wikipediacleaner.api.data.DataManager;
import org.wikipediacleaner.api.data.Namespace;
import org.wikipediacleaner.api.data.Page;
import org.wikipediacleaner.api.impl.CommentDecorator;
import org.wikipediacleaner.api.impl.ProgramCommentDecorator;
import org.wikipediacleaner.utils.Configuration;
import org.wikipediacleaner.utils.ConfigurationValueBoolean;


/**
 * Encapsulate possible Wikipedia sites.
 */
public enum EnumWikipedia {

  // List of wikis WPCleaner is able to deal with.
  // For each wiki, a configuration object is required.
  AF(new Wikipedia("af", "Afrikaans Wikipedia")),
  ALS(new Wikipedia("als", "Alemannisch Wikipedia")),
  AR(new Wikipedia("ar", "Arabic Wikipedia", ComponentOrientation.RIGHT_TO_LEFT)),
  ARZ(new Wikipedia("arz", "Egyptian Arabic Wikipedia", ComponentOrientation.RIGHT_TO_LEFT)),
  BAR(new Wikipedia("bar", "Boarich Wikipedia")),
  BE(new Wikipedia("be", "Беларускай Вікіпедыяй")),
  BE_TARASK(new Wikipedia("be-tarask", "Беларуская Вікіпэдыя")),
  BG(new Wikipedia("bg", "Българоезична Уикипедия")),
  BN(new Wikipedia("bn", "Bengali Wikipedia")),
  CA(new Wikipedia("ca", "Viquipèdia en català")),
  CS(new Wikipedia("cs", "Czech Wikipedia")),
  CY(new Wikipedia("cy", "Welsh Wikipedia")),
  DA(new Wikipedia("da", "Danish Wikipedia")),
  DE(new Wikipedia("de", "Deutschsprachige Wikipedia")),
  EL(new Wikipedia("el", "Greek Wikipedia")),
  EN(new Wikipedia("en", "English Wikipedia")),
  EO(new Wikipedia("eo", "Esperanto Wikipedia")),
  ES(new Wikipedia("es", "Wikipedia en español")),
  FA(new Wikipedia("fa", "Persian Wikipedia", ComponentOrientation.RIGHT_TO_LEFT)),
  FI(new Wikipedia("fi", "Finnish Wikipedia")),
  FR(new Wikipedia("fr", "Wikipédia en Français")),
  FY(new Wikipedia("fy", "West Frisian Wikipedia")),
  GD(new Wikipedia("gd", "Scottish Gaelic Wikipedia")),
  GL(new Wikipedia("gl", "Galipedia")),
  HE(new Wikipedia("he", "ויקיפדיה העברית", ComponentOrientation.RIGHT_TO_LEFT)),
  HIF(new Wikipedia("hif", "Fiji Hindi Wikipedia")),
  HR(new Wikipedia("hr", "Croatian Wikipedia")),
  HU(new Wikipedia("hu", "Magyar Wikipedia")),
  ID(new Wikipedia("id", "Indonesian Wikipedia")),
  IS(new Wikipedia("is", "Wikipedia á íslensku")),
  IT(new Wikipedia("it", "Italian Wikipedia")),
  JA(new Wikipedia("ja", "Japanese Wikipedia")),
  Ko(new Wikipedia("ko", "Korean Wikipedia")),
  LA(new Wikipedia("la", "Latin Wikipedia")),
  LI(new Wikipedia("li", "Limburgish Wikipedia")),
  LV(new Wikipedia("lv", "Latvian Wikipedia")),
  ML(new Wikipedia("ml", "Malayalam Wikipedia")),
  NDS(new Wikipedia("nds", "Low Saxon Wikipedia")),
  NDS_NL(new Wikipedia("nds-nl", "Dutch Low Saxon Wikipedia")),
  NL(new Wikipedia("nl", "Nederlandstalige Wikipedia")),
  NO(new Wikipedia("no", "Norsk Wikipedia på bokmål og riksmål")),
  PA(new Wikipedia("pa", "ਪੰਜਾਬੀ ਵਿਕੀਪੀਡੀਆ")),
  PDC(new Wikipedia("pdc", "Pennsylvania German Wikipedia")),
  PL(new Wikipedia("pl", "Polska Wikipedia")),
  PT(new Wikipedia("pt", "Wikipédia em português")),
  RO(new Wikipedia("ro", "Romanian Wikipedia")),
  RU(new Wikipedia("ru", "Русская Википедия")),
  SCO(new Wikipedia("sco", "Scots wikipedia")),
  SIMPLE(new Wikipedia("simple", "Simple English Wikipedia")),
  SK(new Wikipedia("sk", "Slovak Wikipedia")),
  SL(new Wikipedia("sl", "Slovenska Wikipedija")),
  SQ(new Wikipedia("sq", "Albanian Wikipedia")),
  SV(new Wikipedia("sv", "Swedish Wikipedia")),
  TA(new Wikipedia("ta", "தமிழ் விக்கிப்பீடியா")),
  TR(new Wikipedia("tr", "Turkish Wikipedia")),
  UK(new Wikipedia("uk", "Ukrainian Wikipedia")),
  UR(new Wikipedia("ur", "Urdu Wikipedia", ComponentOrientation.RIGHT_TO_LEFT)),
  VI(new Wikipedia("vi", "Vietnamese Wikipedia")),
  YI(new Wikipedia("yi", "Yiddish Wikipedia", ComponentOrientation.RIGHT_TO_LEFT)),
  ZH(new Wikipedia("zh", "维基百科")),
  COMMONS(new WikimediaCommons()),

  WIKTIONARY_EN(new Wiktionary("en", "English Wiktionary")),
  WIKTIONARY_FR(new Wiktionary("fr", "Wiktionnaire en français")),
  WIKTIONARY_SV(new Wiktionary("sv", "Swedish Wiktionary")),

  WIKIQUOTE_CA(new Wikiquote("ca", "Viquidites")),

  WIKISOURCE_FR(new Wikisource("fr", "Wikisource en français")),

  WIKIVERSITY_FR(new Wikiversity("fr", "Wikiversité en français")),
  
  WAZE(new Waze()),
  WAZEOPEDIA_USA(new WazeopediaUSA()),
  WIKISKRIPTA(new WikiSkripta()),
  
  TEST(new Wikipedia("test", "Test Wikipedia"));

  private Set<String> disambiguationPages;
  private List<Page> disambiguationTemplates;

  /**
   * Configuration for Check Wiki project.
   */
  private final CWConfiguration CWConfiguration;

  /**
   * @param settigs Wiki settings.
   */
  EnumWikipedia(AbstractWikiSettings settings) {
    this.settings = settings;
    this.configPage = settings.getConfigurationPage();
    this.wikiConfiguration = new WikiConfiguration();
    this.WPCConfiguration = new WPCConfiguration(this);
    this.CWConfiguration = new CWConfiguration(settings.getCodeCheckWiki(), this);
  }

  /**
   * @return Vector of all Wikipedia.
   */
  public static List<EnumWikipedia> getList() {
    List<EnumWikipedia> list = new ArrayList<EnumWikipedia>(EnumWikipedia.values().length);
    for (EnumWikipedia e : EnumWikipedia.values()) {
      list.add(e);
    }
    return list;
  }

  /**
   * @param code Language code.
   * @return Corresponding Wikipedia.
   */
  public static EnumWikipedia getWikipedia(String code) {
    for (EnumWikipedia e : EnumWikipedia.values()) {
      if (e.getSettings().getCode().equals(code)) {
        return e;
      }
    }
    return getDefaultWikipedia();
  }

  /**
   * @return Default Wikipedia.
   */
  public static EnumWikipedia getDefaultWikipedia() {
    Locale locale = Locale.getDefault();
    if ((locale != null) && (locale.getLanguage() != null)) {
      for (EnumWikipedia e : EnumWikipedia.values()) {
        if (e.getSettings().getCode().equals(locale.getLanguage())) {
          return e;
        }
      }
    }
    return EN;
  }

  /**
   * @param pageTitle Title.
   * @return Normalized title.
   */
  public String normalizeTitle(String pageTitle) {
    if (pageTitle == null) {
      return null;
    }
    String result = pageTitle.replaceAll(" ", " ");
    result = result.replaceAll("_", " ");
    result = result.replaceAll(" +", " ");
    result = result.trim();
    result = Page.getStringUcFirst(result);
    return result;
  }

  // =========================================================================
  // Configuration
  // =========================================================================

  /** Wiki settings */
  private final AbstractWikiSettings settings;

  /** Configuration page name */
  private final String configPage;

  /** Wiki configuration */
  private final WikiConfiguration wikiConfiguration;

  /** WPCleaner configuration */
  private final WPCConfiguration WPCConfiguration;

  /**
   * @return Wiki settings.
   */
  public AbstractWikiSettings getSettings() {
    return settings;
  }

  /**
   * @return Wiki configuration.
   */
  public WikiConfiguration getWikiConfiguration() {
    return wikiConfiguration;
  }

  /**
   * @return WPCleaner configuration.
   */
  public WPCConfiguration getConfiguration() {
    return WPCConfiguration;
  }

  /**
   * @return Check Wiki project configuration.
   */
  public CWConfiguration getCWConfiguration() {
    return CWConfiguration;
  }

  /**
   * @return Configuration page.
   */
  public String getConfigurationPage() {
    return getUserConfigurationPage("NicoV");
  }

  /**
   * @param userName User name.
   * @return Configuration page.
   */
  public String getUserConfigurationPage(String userName) {
    Namespace userNamespace = wikiConfiguration.getNamespace(Namespace.USER);
    String userPrefix = (userNamespace != null) ? userNamespace.getTitle() : "User";
    return userPrefix + ":" + userName + "/" + configPage;
  }

  // =========================================================================
  // User management
  // =========================================================================

  /**
   * Connection information.
   */
  private final ConnectionInformation connection = new ConnectionInformation();

  /**
   * Contributions.
   */
  private final Contributions contributions = new Contributions(this);

  /**
   * @return Connection information.
   */
  public ConnectionInformation getConnection() {
    return connection;
  }

  /**
   * @return Contributions
   */
  public Contributions getContributions() {
    return contributions;
  }

  /**
   * @param text Comment.
   * @param details Details about the update.
   * @return Full comment.
   */
  public String createUpdatePageComment(String text, String details) {
    StringBuilder comment = new StringBuilder();
    if ((text != null) && (text.length() > 0)) {
      comment.append(text);
    }
    if ((details != null) && (details.length() > 0)) {
      if (comment.length() > 0) {
        comment.append(" - ");
      }
      comment.append(details);
    }
    return comment.toString();
  }

  /**
   * @return Comment decorator.
   */
  public CommentDecorator getCommentDecorator() {
    Configuration config = Configuration.getConfiguration();
    boolean showProgram = config.getBoolean(
        null,
        ConfigurationValueBoolean.WIKICLEANER_COMMENT);
    String link = WPCConfiguration.getString(WPCConfigurationString.HELP_PAGE);
    String tag = WPCConfiguration.getString(WPCConfigurationString.TAG);
    return new ProgramCommentDecorator(
        Version.PROGRAM, Version.VERSION,
        showProgram, link, tag);
  }

  // =========================================================================
  // Disambiguation
  // =========================================================================

  /**
   * Construct list of disambiguation pages.
   * 
   * @param api Wikipedia API
   * @return List of disambiguation pages
   * @throws APIException
   */
  public List<Page> constuctDisambiguationPages(API api) throws APIException {

    WPCConfiguration config = getConfiguration();

    // Use __DISAMBIG__ magic word if set
    boolean useDisambig = config.getBoolean(WPCConfigurationBoolean.DAB_USE_DISAMBIG_MAGIC_WORD);
    if (useDisambig) {
      List<Page> tmpResult = api.retrievePagesWithProp(this, "disambiguation", false);
      Iterator<Page> itPage = tmpResult.iterator();
      while (itPage.hasNext()) {
        Page page = itPage.next();
        if (!page.isInMainNamespace()) {
          itPage.remove();
        } else {
          page.setDisambiguationPage(Boolean.TRUE);
        }
      }
      return tmpResult;
    }

    // Use categories if they are defined
    List<Page> dabCategories = config.getDisambiguationCategories();
    if ((dabCategories != null) && (dabCategories.size() > 0)) {
      ArrayList<Page> tmpResult = new ArrayList<Page>();
      for (Page dabCategory : dabCategories) {
        api.retrieveCategoryMembers(
            this, dabCategory, 0, false, Integer.MAX_VALUE);
        List<Page> tmpPages = dabCategory.getRelatedPages(Page.RelatedPages.CATEGORY_MEMBERS);
        if (tmpPages != null) {
          tmpResult.ensureCapacity(tmpResult.size() + tmpPages.size());
          for (Page page : tmpPages) {
            if (page.isInMainNamespace()) {
              tmpResult.add(page);
            }
          }
        }
      }
      return tmpResult;
    }

    // Use disambiguation templates
    if (disambiguationTemplates != null) {
      ArrayList<Page> tmpResult = new ArrayList<Page>();
      for (Page dabTemplate : disambiguationTemplates) {
        api.retrieveEmbeddedIn(
            this, dabTemplate,
            Collections.singletonList(Namespace.MAIN),
            false);
        List<Page> tmpPages = dabTemplate.getRelatedPages(Page.RelatedPages.EMBEDDED_IN);
        if (tmpPages != null) {
          tmpResult.ensureCapacity(tmpResult.size() + tmpPages.size());
          for (Page page : tmpPages) {
            if (page.isInMainNamespace()) {
              tmpResult.add(page);
            }
          }
        }
      }
      return tmpResult;
    }

    return null;
  }

  /**
   * Load all disambiguation pages.
   * 
   * @param api Wikipedia API.
   * @throws APIException
   */
  public void loadDisambiguationPages(API api) throws APIException {
    try {
      List<Page> tmpPages = constuctDisambiguationPages(api);
      HashSet<String> tmpResult = new HashSet<String>();
      for (Page page : tmpPages) {
        tmpResult.add(page.getTitle());
      }
      disambiguationPages = tmpResult;
    } catch (APIException e) {
      disambiguationPages = null;
      throw e;
    }
  }

  /**
   * @param dabPages List of disambiguation pages.
   */
  public void setDisambiguationPages(Set<String> dabPages) {
    if (dabPages == null) {
      return;
    }
    HashSet<String> tmpResult = new HashSet<String>(dabPages.size());
    tmpResult.addAll(dabPages);
    disambiguationPages = tmpResult;
  }

  /**
   * @return true if disambiguation pages have been loaded.
   */
  public boolean isDisambiguationPagesLoaded() {
    return (disambiguationPages != null);
  }

  /**
   * Tell if a page is a disambiguation page.
   * 
   * @param page Page
   * @return TRUE if it's a disambiguation page.
   */
  public Boolean isDisambiguationPage(Page page) {
    if (page == null) {
      return null;
    }
    if (disambiguationPages == null) {
      return null;
    }
    if (disambiguationPages.contains(page.getTitle())) {
      return Boolean.TRUE;
    }
    if (page.isRedirect()) {
      List<Page> redirects = page.getRedirects();
      if (redirects != null) {
        for (Page redirect : redirects) {
          if ((redirect != null) &&
              (disambiguationPages.contains(redirect.getTitle()))) {
            return Boolean.TRUE;
          }
        }
      }
    }
    return Boolean.FALSE;
  }

  /**
   * Load all disambiguation templates.
   * 
   * @param api Wikipedia API.
   */
  public void initDisambiguationTemplates(API api) {
    if (disambiguationTemplates == null) {
      synchronized (api) {
        Page page = DataManager.getPage(
            this, "Mediawiki:Disambiguationspage",
            null, null, null);
        try {
          api.retrieveLinks(this, Collections.singletonList(page));
        } catch (APIException e) {
          // Error retrieving Disambiguation templates list
        }
        disambiguationTemplates = page.getLinks();
      }
    }
  }
  
  /**
   * @param pageName Page name.
   * @param api Wikipedia API.
   * @return Flag indicating if <code>page</code> is a disambiguation template.
   */
  public boolean isDisambiguationTemplate(String pageName, API api) {
    if (disambiguationTemplates == null) {
      initDisambiguationTemplates(api);
    }
    if (disambiguationTemplates != null) {
      for (Page page : disambiguationTemplates) {
        if (Page.areSameTitle(page.getTitle(), pageName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return List of disambiguation templates 
   */
  public List<Page> getDisambiguationTemplates() {
    return disambiguationTemplates;
  }

  // =========================================================================
  // Tools
  // =========================================================================

  /**
   * @return Description of the settings.
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return settings.toString();
  }
}
