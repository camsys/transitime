<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">

    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
    <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>

    <link href="params/reportParams.css" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css"  href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>
    <style>

        .wrapper {
            background: #f1f1f1f1;
            font-family: 'Montserrat', sans-serif !important;
            height: 100vh;
            width: 100vw;
            position: fixed;
            display: flex;
            flex-flow: row;
        }

        .wrapper.split {
            flex-flow: row;
        }

        #title {
            margin-top: 40px;
            margin-bottom: 2px;
            font-weight: normal;
            text-align: center;
            background: #019932;
            color: white;
            padding: 8px;
            font-size: 24px;
            width: -webkit-fill-available;
            display: inline-block !important;
        }

        #routesDiv {
            font-family: 'Montserrat', sans-serif !important;
        }

        input {
            -webkit-appearance: none;
            width: -webkit-fill-available;
            border: 1px solid #c1c1c1c1;
            background-color: #fff;
            line-height: 1.5;
            box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.33);
            color: #444;
            padding: 0px 6px;
            font-family: 'Montserrat', sans-serif;
            font-size: 16px;
        }

        input::placeholder {
            color: #44444469;
        }

        select:not(.datepick-month-year):not(.datepick-month-year) lect {
            width: -webkit-fill-available;
            border: 1px solid #c1c1c1c1;
            background-color: #fff;
            line-height: 1.5;
            box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.33);
            color: #444;
            padding: 0px 6px;
            font-family: 'Montserrat', sans-serif;
            font-size: 16px;
        }

        label {
            text-align: left;
            width: auto;
        }

        hr {
            height: 2px;
            background-color: darkgray;
            margin-right: 5px;
        }

        .paramsWrapper {
            width: 100%;
            height: 100vh;
            transition: width .75s ease-in-out, max-width .75s ease-in-out;
            font-size: 16px;
            background-color: #fff;
            border: #969696 solid 1px;
            box-shadow: 3px 3px 4px rgba(0, 0, 0, 0.3);
            /* align-self: center; */
            position: relative;
            z-index: 8;
        }

        .split .paramsWrapper {
            width: 22%;
        }

        #paramsSidebar {
            height: 100vh;
            max-width: 420px;
            width: 100%;
            margin: auto;
            display: flex;
            align-items: center;
            flex-flow: column;
            background-color: #fff;
            z-index: 2;
        }

        .split #paramsSidebar {
        }

        #paramsSidebar > * {
            display: flex;
        }

        #paramsFields {
            flex-flow: column;
            width: 90%;
            max-width: 30vw;
        }

        .param {
            display: flex;
            flex-flow: row;
            justify-content: space-between;
            margin-top: 6%;
        }

        .param-modal {
            margin: 10px 0px;
        }

        .param, .param-modal > * {
            font-size: 14px;
        }

        .param > label, .param-modal > label {
            width: 130px;
        }


        .param > span {
            font-weight: 500;
            padding-bottom: 12px;
        }

        .param > input, .param > select {
            height: 30px;
        }

        .param-modal > input, .param-modal > select {
            height: 30px;
            width: 200px;
        }

        .pair {
            display: flex;
            flex-flow: row;
            justify-content: space-between;
            margin-bottom: 6px;
        }

        .vertical {
            flex-flow: column;
            margin-top: 8%;
            /* background-color: #f1f1f1f1; */
            padding: 10px 0px;
        }


        /*#paramsSidebar {*/
        /*width: 25%;*/
        /*height: 100vh;*/
        /*margin-left: 10px;*/
        /*float:left;*/
        /*border-right: 1px solid black;*/
        /*}*/

        #mainPage {
            visibility: hidden;
            opacity: 0;
            display: none;
            margin-left: 2vw;
            margin-top: 20vh;
            height: 100vh;
            width: 90%;
            max-width: 1250px;
            padding: 10px 30px;
            background-color: #fff;
            border-radius: 4px;
            box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.3);
            transition: visibility .25s .75s ease-in-out, opacity .25s .75s ease-in-out;
        }

        .split #mainPage {
            display: inline-block;
            position: relative;
            visibility: visible;
            opacity: 1;
            margin-top: 2vh;
        }

        .inactive {
            filter: blur(2px) grayscale(100%);
        }

        #comparisonModal {
            width: 40%;
            height: 40%;
            z-index: 999;
            border: 1px solid black;
            background-color: white;
            transition: all .5s ease;
            position: absolute;
            left: 44%;
        }

        #serviceDayType {
            width: 100%;
            height: 30px;
            margin-top: 0px;
            box-shadow: 0px 1px 4px #69696969;
            font-family: 'Montserrat', sans-serif;

        }

        #beginTime, #endTime {
            width: 50%;
        }


        .submit {
            margin: 40px 24px;
            background-color: #029932;
            cursor: pointer;
            width: 210px;
            padding: 5px 70px;
            color: #fff;
            font-family: 'Montserrat', sans-serif;
            box-shadow: 0 4px rgba(127, 127, 127, 0.8);
        }

        .submit:hover {
            background-color: #02772c;
        }

        .submit:active {
            box-shadow: 0 1px rgba(127, 127, 127, 0.33);
            transform: translateY(3px);
            outline: none;
        }

    </style>

</head>
<body>
<%@include file="/template/header.jsp" %>


<div class="wrapper">
    <div class="paramsWrapper">
        <div id="paramsSidebar">
            <div id="title">
                Run Time Analysis
            </div>

            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <jsp:include page="params/routeAllOrSingle.jsp"/>

                <div class="param individual-route-only">
                    <label for="direction">Direction:</label>
                    <select id="direction" name="direction" disabled="true"></select>
                </div>

                <div class="param">
                    <label for="serviceDayType">Service Day:</label>
                    <select id="serviceDayType" name="serviceDayType">
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                        <span class="select2-selection__arrow">
                                <b role="presentation"></b>
                        </span>
                    </select>
                </div>

                <div class="param">
                    <label for="timeband">Time Band:</label>
                    <select id="timeband" name="timeband"></select>
                </div>
            </div>
            <div class="submitDiv">
                <input type="button" id="submit" class="submit" value="Analyze">
            </div>


        </div>
    </div>


    <div id="mainPage" class="scrollable-element inner-spacing">

        <div class="perceptive-header-container"> </div>
        <div class="perceptive-summary-container">
            <div class="individual-route">
                <h3>Trip Run Time Summary</h3>
                <table class="border-table">
                    <tbody><tr>
                        <th>Average</th>
                        <th>Fixed</th>
                        <th>Variable</th>
                        <th>Dwell</th>
                    </tr>
                    <tr class="average-time-details"></tr>
                    </tbody></table>
            </div>

        </div>
<%--        <div class="perceptive-table-container">Table</div>--%>
        </div>
</div>
</body>
</html>

<script>

    var stops = {};
    function generateTimeBands(){

        var timebandOptions = [{
            name: "Morning",
            value:"morning"
        },{
            name: "Morning Rush",
            value:"morning-rush"
        },{
            name: "Mid-Day",
            value:"mid-day"
        },{
            name: "Afternoon",
            value:"afternoon"
        },{
            name: "Evening Rush",
            value:"evening Rush"
        },{
            name: "Evening",
            value:"evening"
        },{
            name: "Late Night",
            value:"night"
        }];


        timebandOptions.forEach(function (eachTime) {
            $("#timeband").append("<option value='" + eachTime.value + "'>" + eachTime.name + "</option>");
        })

        $("#timeband").append(' <span class="select2-selection__arrow"><b role="presentation"></b> </span>');


    }


    $("#route").attr("style", "width: 200px");

    $("#route").change(function () {
        if ($("#route").val().trim() != "") {
            $(".individual-route-only").show();

            populateDirection();
        } else {
            $(".individual-route-only").hide();
            $("#direction").empty();
            $("#direction").attr("disabled", true);
        }
    })


    var highestPoints = [];

    function msToMin(data) {
        var highest = 0;

        for (var i = 0 in data) {
            data[i] = parseFloat((data[i] / 60000).toFixed(1));
            if (data[i] > highest) {
                highest = data[i];
            }
        }

        highestPoints.push(highest);
        return data;
    }
    $("#submit").click(function () {

        // $("#submit").attr("disabled", "disabled");
        $(".wrapper").addClass("split");
        $("#mainResults").hide();


        var request = getParams(true);

        var callBack = function(response){

            console.log(response);

            if (jQuery.isEmptyObject(response)) {
                alert("No run time information available for selected parameters.");
            } else {

                var beginDateArray = request.beginDate.split("-");
                var endDateArray = request.endDate.split("-");

                [beginDateArray[0], beginDateArray[1], beginDateArray[2]] = [beginDateArray[1], beginDateArray[2], beginDateArray[0]];
                [endDateArray[0], endDateArray[1], endDateArray[2]] = [endDateArray[1], endDateArray[2], endDateArray[0]];

                var beginDateString = beginDateArray.join("/");
                var endDateString = endDateArray.join("/");

                $(".perceptive-header-container").html(
                    "<p>" +
                    (request.r == "" ? "All routes" : "Route " + request.r) + " to " +
                    (request.headsign == "" ? "All directions" : request.headsign) + " | " +
                    beginDateString + " to " + endDateString +
                    "</p>"
                );

                var avgRunTime = typeof (response.avgRunTime) == 'undefined' ? "N/A" : (response.avgRunTime / 60000).toFixed(1) + " min";
                var avgFixed = typeof (response.fixed) == 'undefined' ? "N/A" : (response.fixed / 60000).toFixed(1) + " min";
                var avgVar = typeof (response.variable) == 'undefined' ? "N/A" : (response.variable / 60000).toFixed(1) + " min";
                var avgDwell = typeof (response.dwell) == 'undefined' ? "N/A" : (response.dwell / 60000).toFixed(1) + " min";

                var tableTD = "<td>"+avgRunTime+"</td>";
                tableTD += "<td>"+avgFixed+"</td>";
                tableTD += "<td>"+avgVar+"</td>";
                tableTD += "<td>"+avgDwell+"</td>";

                $(".average-time-details").html(tableTD);

            }


        };

        serviceCall(request,  callBack);



    });


    function populateDirection() {

        // $("#submit").attr("disabled", true);


        $("#direction").removeAttr('disabled');
        $("#direction").empty();


        $.ajax({
            url: apiUrlPrefix + "/command/headsigns",
            // Pass in query string parameters to page being requested
            data: {
                r: $("#route").val(),
                formatLabel: false
            },
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                response.headsigns.forEach(function (headsign) {
                    $("#direction").append("<option value='" + headsign.headsign + "'>" + headsign.label + "</option>");
                })
            },
            error: function (response) {
                alert("Error retrieving directions for route " + response.r);
                // $("#submit").attr("disabled", false);
            }
        })
    }


    function getParams(flag) {


        var routeName = $("#route").val().trim() == "" ? "" : $("#route").val();
        var directionName = $("#direction").val() == null ? "" : $("#direction").val();

        var date = new Date();

        params = {};

        /** beginDate: 2021-05-03
         endDate: 2021-05-17
         beginTime: 00:00:00
         endTime: 23:59:59
         r: 22759
         headsign: 704 PARKLAND HOSPITAL
         serviceType: */

       //  params.timeBand = $("#timeband").val();

        var firstDay = new Date(date.getFullYear(), date.getMonth()-1, 1);
        var lastDay = new Date(date.getFullYear(), date.getMonth(), 0);

        params.beginDate =  firstDay.getFullYear() + "-"
            + (firstDay.getMonth() <= 10 ? "0" + (firstDay.getMonth() + 1) : (firstDay.getMonth() + 1))
            + "-" + (firstDay.getDate() < 10 ? "0" + firstDay.getDate() : firstDay.getDate());

        params.endDate =  lastDay.getFullYear() + "-"
            + (lastDay.getMonth() <= 10 ? "0" + (lastDay.getMonth() + 1) : (lastDay.getMonth() + 1))
            + "-" + (lastDay.getDate() < 10 ? "0" + lastDay.getDate() : lastDay.getDate());
        params.beginTime = "00:00:00";
        params.endTime = "23:59:59";
        params.r = routeName
        params.headsign = directionName;
        params.serviceType = $("#serviceDayType").val();
        params.tripPattern = "";


        return params;
    }

    function serviceCall(request, callBack){

        $.ajax({
            url: apiUrlPrefix + "/report/runTime/avgRunTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {


                callBack(response);

            },
            error: function () {
                // $("#submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })

    }
    generateTimeBands();

</script>