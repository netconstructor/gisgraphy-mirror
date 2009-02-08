<#macro searchNavBar>
	<@s.url id="simpleFulltextSearchUrl" action="fulltextsearch" includeParams="all" namespace="" />
	<@s.url id="ajaxFulltextSearchUrl" action="ajaxfulltextsearch" includeParams="all" namespace="" />
	<@s.url id="simpleGeolocSearchUrl" action="geolocsearch" includeParams="all" namespace="" />
	<@s.url id="ajaxGeolocSearchUrl" action="ajaxgeolocsearch" includeParams="all"  namespace=""  />
	<@s.url id="streetSearchUrl" action="public/streetSearch" includeParams="none" namespace="" />
	
	<span id="searchlinks">
	<a href="${simpleFulltextSearchUrl}"  <#if request.getRequestURI().startsWith("/fulltextsearch")>style="color:#cc0000"</#if>  ><@s.text name="search.fulltext.breadcrumbs"/></a>
	 | <a href="${ajaxFulltextSearchUrl}" <#if request.getRequestURI().startsWith("/ajaxfulltextsearch")>style="color:#cc0000"</#if> ><@s.text name="search.fulltextDemo.breadcrumbs"/></a>
	 | <a href="${simpleGeolocSearchUrl}" <#if request.getRequestURI().startsWith("/geolocsearch")>style="color:#cc0000"</#if> ><@s.text name="search.geoloc.breadcrumbs"/></a> 
	 | <a href="${ajaxGeolocSearchUrl}" <#if request.getRequestURI().startsWith("/ajaxgeolocsearch")>style="color:#cc0000"</#if> ><@s.text name="search.geolocDemo.breadcrumbs"/></a>
	 |  <a href="${streetSearchUrl}" <#if request.getRequestURI().startsWith(streetSearchUrl)>style="color:#cc0000"</#if> ><@s.text name="search.street.breadcrumbs"/></a></span><br/>
</#macro>

<#macro paypalDonation>
<form action="https://www.paypal.com/cgi-bin/webscr" method="post">
<input type="hidden" name="cmd" value="_s-xclick"/>
<input type="hidden" name="hosted_button_id" value="1694440"/>
<input type="image" src="https://www.paypal.com/en_US/i/btn/btn_donate_SM.gif"  name="submit" alt="donate"/>
<img alt="pixel" src="https://www.paypal.com/fr_FR/i/scr/pixel.gif" width="1" height="1"/>
</form>
</#macro>

<#macro paypalDonationBig>
<form action="https://www.paypal.com/cgi-bin/webscr" method="post">
<input type="hidden" name="cmd" value="_s-xclick"/>
<input type="hidden" name="hosted_button_id" value="1694727"/>
<input type="image" src="https://www.paypal.com/en_US/i/btn/btn_donate_LG.gif" name="submit" alt="donate"/>
<img alt="pixel" border="0" src="https://www.paypal.com/fr_FR/i/scr/pixel.gif" width="1" height="1"/>
</form>
</#macro>

<#macro opensearchFulltext>
<#if request.getHeader('User-Agent')??>
	<#assign userAgent = request.getHeader('User-Agent')/>
	<#if userAgent.contains('Firefox') >
		<br/>
		<div class="center"><@s.text name="search.opensearch.tip.firefox.part1"/> <img src="/images/opensearch_mozilla.gif" class="imgAlign" alt="opensearch mozilla"/> <@s.text name="search.opensearch.tip.firefox.part2"/></div>
	<#elseif userAgent.contains('MSIE 7.0')>
		<div class="center"><@s.text name="search.opensearch.tip.ie.part1"/> <img src="/images/opensearch_internet_explorer.gif" class="imgAlign" alt="opensearch internet explorer"/> <@s.text name="search.opensearch.tip.ie.part2"/></div>
	</#if>
</#if>
</#macro>




<#macro fulltextSearchTooltip advancedSearchURLParam>
<div id="tooltip">
			 <@s.url id="advancedSearchUrl" action="${advancedSearchURLParam}" includeParams="all" >
			  <@s.param name="advancedSearch" value="true" />
			 </@s.url>
				<a href="${advancedSearchUrl}" onclick="$('advancedsearch').toggle();return false;"><@s.text name="search.advanced"/></a>
				<br/>
				<a href="http://www.gisgraphy.com/documentation/index.htm#fulltextservice" ><@s.text name="global.help"/></a>
				<br/>
				<a href="http://www.gisgraphy.com/"><@s.text name="global.more"/>...</a>
				<br/>
			</div>
</#macro>

<#macro geolocSearchTooltip advancedSearchURLParam>
<div id="tooltip">
			 <@s.url id="advancedSearchUrl" action="${advancedSearchURLParam}" includeParams="all" >
			  <@s.param name="advancedSearch" value="true" />
			 </@s.url>
				<a href="${advancedSearchUrl}" onclick="$('advancedsearch').toggle();return false;"><@s.text name="search.advanced"/></a>
				<br/>
				<a href="http://gisgraphy.com/documentation/index.htm#geolocservice" ><@s.text name="global.help"/></a>
				<br/>
				<a href="http://www.gisgraphy.com/"><@s.text name="global.more"/>...</a>
				<br/>
			</div>
</#macro>