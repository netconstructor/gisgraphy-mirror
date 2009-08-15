<#import "macros/breadcrumbs.ftl" as breadcrumbs>
<#import "macros/gisgraphysearch.ftl" as gisgraphysearch>
<html>
<head>
<title><@s.text name="search.street.title"/></title>
<meta name="Description" content="free geoloc webservices demo for Geonames Data. Results are shown in an human readable way. Pagination, indentation, several languages are supported"/>
<meta name="heading" content="<@s.text name="search.street.title"/>"/>
<meta name="keywords" content="street search java openstreetMap ajax webservices postgis hibernate toponyms gazeteers"/>
</head>
<body>
<br/>
<div id="gissearch">
<noscript>
	<div class="tip yellowtip">
	<@s.text name="global.noscript"/>
	</div>
	<br/>
</noscript>
	<@s.url id="streetsearchFormUrl" action="streetSearch" includeParams="none" method="search" namespace="/public" />
			<@breadcrumbs.searchNavBar/>
<div class="clear"></div><div class="biggertext" style="line-height:1.5em;">The STREETSEARCH webservice is totally FREE and allow to find a street from a Lat/lng pair or a name via a REST interface. it uses (free) data from <span class="imgAlign"><a href="http://openstreetmap.org">OpenstreetMap <img src="/images/openstreetmap.png" alt="openstreetmap" class="imgAlign" style="width:30px"/></a> that are imported into a local database. <@s.text name="search.geocoding.notUsinggooglemap"/>. You can see an example of use bellow</span>. You can find documentation on <a href="http://www.gisgraphy.com/documentation/index.htm#streetservice" target="_blank">how to use the webservice</a> and see how to <a href="http://www.gisgraphy.com/documentation/installation/index.htm" target="_blank">download and install</a> Gisgraphy</div><br/><br/>
<div class="clear"></div>
	<@s.form action="${streetsearchFormUrl}" method="get" id="streetsearch">
		<div id="simplesearch">
			<@gisgraphysearch.latlongsearchbox/>
			<@breadcrumbs.streetsearchTooltip advancedSearchURLParam="ajaxstreetsearch"/>
	</div>
	<div class="clear"><br/></div>
	<div id="nonAjaxDisplayResults">
			<#if errorMessage!= ''>
			<div class="clear"><br/><br/></div>
				<div class="tip redtip">
					<div class="importantMessage">Error : ${errorMessage}</div>
				</div>
			<#elseif displayResults>
			<div class="clear"><br/><br/></div>
		 		<@gisgraphysearch.displaystreetResults streetResponseDTO=streetSearchResultsDto/>
		 	</#if>
		 </div>
	
		
		 <div class="clear"><br/></div>
	 <@s.if test="advancedSearch">
			<div id="advancedsearch" >
		</@s.if>
		<@s.else>
    		<div id="advancedsearch" style="display:none;" >
		</@s.else>
	<fieldset >
		<legend>&nbsp; <@s.text name="search.advanced"/> &nbsp; </legend>
		<span class="advancedsearchcat"><@s.text name="search.moreCriteria"/></span>
		<hr/>
		<span class="searchfield">
			<span class="searchfieldlabel"><@s.text name="global.radius"/> (<@s.text name="global.unit"/>) : </span>
			<@s.textfield name="radius" required="true" size="10" theme="simple" />
			<br/>
		</span>
		<div class="clear"></div>
		<span class="searchfield">
			<s:set name="personName" value="ee"/>
			<span class="searchfieldlabel"><@s.text name="global.name"/>  : </span>
			<@s.select  name="name"  list="nameOptions"   multiple="false" theme="simple"/>
			<br/>
		</span>


		<div class="clear"></div>
		<span class="searchfield">
			<span class="searchfieldlabel"><@s.text name="global.streettype"/> : </span>
			<@s.select headerKey="" headerValue="--all--"  name="streettype" list="streetTypes"  multiple="false" theme="simple"/>
			<br/>
		</span>
		<br/>
		<div class="clear"></div>
		<span class="advancedsearchcat"><@s.text name="search.paginationSpecs"/></span>
		<hr/>
		<span class="searchfield">
			<span class="searchfieldlabel"><@s.text name="search.pagination.from"/> : </span><@s.textfield size="5" maxlength="3" name="from" required="false"  theme="simple"/> 
		</span>
		<span class="searchfield">
			<span class="searchfieldlabel"><@s.text name="search.pagination.to"/> : </span><@s.textfield size="5"  name="to" maxlength="3" required="false"  theme="simple"/> 
		</span>
		<br/>
		<div class="clear"></div>
	</fieldset>
	</div>
	</@s.form>
</div>
<div id="popupResults"></div>

<script src="/scripts/prototype.js" type="text/javascript"></script>
<script src="/scripts/gisgraphyapi.js" type="text/javascript"></script>

<script type="text/javascript" >

	DEFAULT_NUMBER_OF_RESULTS_PER_PAGE=${defaultNumberOfResultsPerPage?c};

 	updatePaginationNext= function(){
    $('streetsearch')['from'].value=parseInt($('streetsearch')['from'].value)+DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;
    $('streetsearch')['to'].value=parseInt($('streetsearch')['to'].value)+DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;
    return updatePopupResults();
    }
    
     updatePaginationPrevious = function(){
    $('streetsearch')['from'].value=parseInt($('streetsearch')['from'].value)-DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;
    $('streetsearch')['to'].value=parseInt($('streetsearch')['to'].value)-DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;
    return updatePopupResults();
    }

 	 	displayPopupResults = function(transport){
	 	 	 if (transport.responseText){
	 	 	 	$('nonAjaxDisplayResults').update("");
		     	$('popupResults').update(transport.responseText);
		     	$('popupResults').show();
		     	 Event.observe('closePopupResultsPopupButton','click',closePopupResults);
		     	 Event.observe(document,'keydown',function(e){
		     	 	var code;
					if (!e) var e = window.event;
					if (e.keyCode) code = e.keyCode;
					else if (e.which) code = e.which;
					if (code=27) {
						closePopupResults();
					}
		     	 }
		     	 );
		     	 return false;
		   	 } else {
		      alert("No response from the server");
		      return true;
		     }
        }
        
        closePopupResults = function(){
        	$('popupResults').hide();
        	$('popupResults').update("");
        	$('streetsearch')['from'].value=1;
        	$('streetsearch')['to'].value=DEFAULT_NUMBER_OF_RESULTS_PER_PAGE
        }
        
 	doSearch = function(){
 		return updatePopupResults();
 	}
 	
    updatePopupResults = function(){
    try {
     if (!checkParameters('streetsearch'))
     {
 	    return false;
     }
	<@s.url id="streetSearchPopupUrl" action="streetSearch" includeParams="none" method="searchpopup" namespace="/public" />
    var savedAction = $('streetsearch').action;
    $('streetsearch').action='${streetSearchPopupUrl}';
    $('streetsearch').request(
    { onComplete: displayPopupResults ,onFailure : function(transport){
	  	alert("an error has occured");
	  } }
    );
    //restore overiden parameters
    $('streetsearch').action=savedAction;
    return false;
    }catch(e){
    alert("an error occured : " +e);
    return true;
    }
	}
	<#if autosubmit>
		doSearch();
	</#if>

</script>
</body>
</html>