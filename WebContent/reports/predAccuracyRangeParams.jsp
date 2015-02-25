<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>

  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  
  <!-- Load in JQuery UI javascript and css to set general look and feel -->
  <script src="/api/jquery-ui/jquery-ui.js"></script>
  <link rel="stylesheet" href="/api/jquery-ui/jquery-ui.css">
  
  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="/api/select2/select2.css" rel="stylesheet"/>
  <script src="/api/select2/select2.min.js"></script>
  
  <!-- Load in general transitime javascript library -->
  <script src="/api/javascript/transitime.js"></script>

  <link rel="stylesheet" href="/api/css/general.css">
  
  
  <style>
  label {width: 200px; float: left; text-align: right; margin-top: 4px; margin-right: 10px;}
  .param {margin-top: 10px;}
  #route {width:300px;}
  #submit {margin-top: 40px; margin-left: 200px;}
  .note {font-size: small;}
  </style>
  
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">
   Select Parameters for Prediction Accuracy Range Chart
</div>

<div id="mainDiv">
<form action="predAccuracyRangeChart.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/fromToDateTime.jsp" />
   
   <jsp:include page="params/route.jsp" />
 
   <div class="param">
     <label for="source">Prediction Source:</label> 
     <select id="source" name="source" 
     	title="Specifies which prediction system to display data for. Selecting
     	'Transitime' means will only show prediction data generated by Transitime. 
     	If there is another prediction source then can select 'Other'. ">
       <option value="Transitime">Transitime</option>
       <option value="Other">Other</option>
     </select>
   </div>
 
   <div class="param">
     <label for="predictionType">Prediction Type:</label> 
     <select id="predictionType" name="predictionType" 
     	title="Specifies whether or not to show prediction accuracy for 
     	predictions that were affected by a layover. Select 'All' to show
     	data for predictions, 'Affected by layover' to only see data where
     	predictions affected by when a driver is scheduled to leave a layover, 
     	or 'Not affected by layover' if you only want data for predictions 
     	that were not affected by layovers.">
       <option value="">All</option>
       <option value="AffectedByWaitStop">Affected by layover</option>
       <option value="NotAffectedByWaitStop">Not affected by layover</option>
     </select>
   </div>
 
   <div class="param">
    <label for="allowableEarly">Allowable Early:</label>
    <input id="allowableEarly" name="allowableEarly"
    	title="How early a vehicle can arrive compared to the prediction
    	and still be acceptable. Must be a negative number to indicate
    	early." 
    	size="1"
    	value="-1.0" /> <span class="note">minutes</span>
  </div>
 
   <div class="param">
    <label for="allowableLate">Allowable Late:</label>
    <input id="allowableLate" name="allowableLate"
    	title="How late a vehicle can arrive compared to the prediction
    	and still be acceptable. Must be a positive number to indicate
    	late." 
    	size="1" 
    	value=" 4.0"/> <span class="note">minutes</span>
  </div>
    
    <input id="submit" type="submit" value="Run Report" />
  </form>
</div>

</body>
</html>