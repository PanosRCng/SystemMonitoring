$(document).ready(function () {

    var cpuUsageChartTemplate = {
        theme: "theme2",
        title: {
            text: "cpu usage",
            fontSize: 12,
            fontStyle: "italic",
        },
        animationEnabled: true,
        backgroundColor : "#F6F6F6",
        axisX: {
            valueFormatString: "HH:mm",
            interval: 3,
            intervalType: "hour",
            labelFontSize: 10
        },
        axisY: {
            includeZero: false,
            maximum : 100,
        },
        data: [
            {
                xValueType: "dateTime",
                type: "area",
                fillOpacity: .8,
                dataPoints: [],
                color: "red",
            }
        ]
    };

    var ramUsageChartTemplate = {
        theme: "theme2",
        title: {
            text: "ram usage",
            fontSize: 12,
            fontStyle: "italic",
        },
        animationEnabled: true,
        backgroundColor : "#F6F6F6",
        axisX: {
            valueFormatString: "HH:mm",
            interval: 3,
            intervalType: "hour",
            labelFontSize: 10
        },
        axisY: {
            includeZero: false,
            maximum : 100,
        },
        data: [
            {
                xValueType: "dateTime",
                type: "area",
                fillOpacity: .8,
                dataPoints: [],
                color: "blue",
            }
        ]
    };

    var netUsageChartTemplate = {
        theme: "theme2",
        title: {
            text: "network usage",
            fontSize: 12,
            fontStyle: "italic",
        },
        animationEnabled: true,
        backgroundColor : "#F6F6F6",
        axisX: {
            valueFormatString: "HH:mm",
            interval: 3,
            intervalType: "hour",
            labelFontSize: 10
        },
        axisY: {
            includeZero: false,
        },
        data: [
            {
                xValueType: "dateTime",
                type: "area",
                fillOpacity: .1,
                dataPoints: [],
                color: "green",
            },
            {
                xValueType: "dateTime",
                type: "area",
                fillOpacity: .1,
                dataPoints: [],
                color: "orange",
            }
        ]
    };


    $.getJSON('machinesList', null, function(data) {
        var machinesList = data.results;

        $.each(machinesList, function(i, machine){
            isSynced(machine);
            loadCpuChart(machine);
            loadRamChart(machine);
            loadNetChart(machine);
        }); // end each

    }); // end get JSON


    function isSynced(machine)
    {
        $.getJSON('isSynced', {'machine' : machine}, function (data) {

            if(data.result == 'sync')
            {
                $('#' + machine + '_sync_label').html('<span class="label label-success">sync</span>');
            }
            else
            {
                $('#' + machine + '_sync_label').html('<span class="label label-danger">no sync</span>');
            }

        }); // end get JSON
    }


    function loadCpuChart(machine)
    {
        $.getJSON('charts/cpuUsage', {'machine' : machine}, function (data) {

            var cpuUsageChart = new CanvasJS.Chart(machine+"_cpuUsageChartContainer", cpuUsageChartTemplate);

            var dataPoints = [];
            $.each(data.results.cpuUsagePoints, function (i, cpuUsageDataPoint) {
                dataPoints.push({
                    x: new Date(cpuUsageDataPoint.x),
                    y: cpuUsageDataPoint.y
                });
            }); // end each
            cpuUsageChart.options.data[0].dataPoints = dataPoints;

            cpuUsageChart.render();

        }); // end get JSON
    }


    function loadRamChart(machine)
    {
        $.getJSON('charts/ramUsage', {'machine' : machine}, function (data) {

            var ramUsageChart = new CanvasJS.Chart(machine+"_ramUsageChartContainer", ramUsageChartTemplate);

            var dataPoints = [];
            $.each(data.results.ramUsagePoints, function (i, ramUsageDataPoint) {
                dataPoints.push({
                    x: new Date(ramUsageDataPoint.x),
                    y: ramUsageDataPoint.y
                });
            }); // end each
            ramUsageChart.options.data[0].dataPoints = dataPoints;

            ramUsageChart.render();

        }); // end get JSON

    }


    function loadNetChart(machine)
    {
        $.getJSON('charts/netUsage', {'machine' : machine}, function (data) {

            var netUsageChart = new CanvasJS.Chart(machine+"_netUsageChartContainer", netUsageChartTemplate);

            var dataPointsNetIn = [];
            var dataPointsNetOut = [];
            $.each(data.results.netUsagePoints, function (i, netUsageDataPoint) {
                dataPointsNetIn.push({
                    x: new Date(netUsageDataPoint.netIn.x),
                    y: netUsageDataPoint.netIn.y
                });
                dataPointsNetOut.push({
                    x: new Date(netUsageDataPoint.netOut.x),
                    y: netUsageDataPoint.netOut.y
                });
            }); // end each
            netUsageChart.options.data[0].dataPoints = dataPointsNetIn;
            netUsageChart.options.data[1].dataPoints = dataPointsNetOut;

            netUsageChart.render();

        }); // end get JSON
    }

}); // end ready