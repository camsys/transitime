<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Specify Parameters</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.1/css/select2.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.1/js/select2.min.js"></script>

    <link href="params/reportParams.css" rel="stylesheet"/>
</head>
<body>
<%@include file="/template/header.jsp" %>

<h4>
    Select Parameters for Schedule Adherence by Stop Chart
</h4>

<div id="mainDiv">
    <div class="params-description">
        Checks the schedule adherence of every stop (including time-points) for both Arrival and Departure records
        and breaks down schedule adherence by stop and direction for the selected route and time period.
    </div>
    <form action="schAdhByStopChart.jsp" method="POST">
        <%-- For passing agency param to the report --%>
        <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

        <jsp:include page="params/routeSingle.jsp" />

        <jsp:include page="params/fromDateNumDaysTime.jsp" />

        <jsp:include page="params/numDays.jsp"/>

        <jsp:include page="params/allowableEarlyLate.jsp" />

        <jsp:include page="params/submitReport.jsp" />
    </form>
</div>

</body>
</html>