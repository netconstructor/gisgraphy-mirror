<%@ include file="/common/taglibs.jsp"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<head>
    <title><fmt:message key="mainMenu.title"/></title>
    <meta name="heading" content="<fmt:message key='mainMenu.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>


<p><fmt:message key="mainMenu.message"/></p>

<%
com.gisgraphy.domain.geoloc.service.fulltextsearch.FullTextSearchEngine fulltextSearchEngine = (com.gisgraphy.domain.geoloc.service.fulltextsearch.FullTextSearchEngine) org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()).getBean("fullTextSearchEngine");

com.gisgraphy.domain.geoloc.importer.ImporterManager importerManager = (com.gisgraphy.domain.geoloc.importer.ImporterManager) org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()).getBean("importerManager");

%>

<div class="separator"></div>

<% if (!fulltextSearchEngine.isAlive()) { %>
   <div class="tip redtip"> <fmt:message key="import.fulltextEngineNotReachable" ><fmt:param><%= fulltextSearchEngine.getURL() %></fmt:param></fmt:message></div>
<% } else {%>
<div class="tip greentip"> <fmt:message key="import.fulltextEngineReachable"/> ! </div>
<%  }%>

<% if (com.gisgraphy.domain.valueobject.GisgraphyConfig.googleMapAPIKey == null || "".equals(com.gisgraphy.domain.valueobject.GisgraphyConfig.googleMapAPIKey.trim())) { %>
   <div class="tip yellowtip"> <fmt:message key="config.googlemapapikey.empty" /></div>
<% } %>
<ul class="glassList">
<li><a href="http://www.gisgraphy.com/documentation/index.htm"><fmt:message key="global.read.docs" /></a></li>
	<% if (!importerManager.isAlreadyDone()) { %>
   <li>
        <a href="<c:url value='/admin/importconfirm.html'/>"><fmt:message key="menu.admin.import"/></a>
    </li>
<% } else {%>
 <li>
         <fmt:message key="import.already.done"/> 
    </li>
<%  }%>
  <li>
        <fmt:message key="global.gohome"/>
    </li>
</ul>

<iframe src="http://www.gisgraphy.com/news/getnews.php?version=<fmt:message key="gisgraphy.version"/>" 
width="100%" height="100%" id="iframenews" frameborder="0" vspace="0" hspace="0" marginwidth="0" marginheight="0" scrolling="no" noresize
> </iframe>
