/**
 * Created by Zhiguo.Chen on 15/7/3.
 */
$(function () {
    var dateFormat = 'YYYY-MM-DD';
    var rangePicker = $('#searchDateRange');
    var rangeSpan = rangePicker.find('span');
    var dateFrom = $('#invokeDateFrom');
    var dateTo = $('#invokeDateTo');
    //Date range picker
    rangePicker.daterangepicker({
        ranges: {
            'Today': [moment(), moment()],
            'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
            'Last 7 Days': [moment().subtract(6, 'days'), moment()],
            'Last 30 Days': [moment().subtract(29, 'days'), moment()],
            'This Month': [moment().startOf('month'), moment().endOf('month')],
            'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
        }, format: dateFormat
    }).on('apply.daterangepicker', function (ev, picker) {
        dateFrom.val(picker.startDate.format(dateFormat));
        dateTo.val(picker.endDate.format(dateFormat));
        rangeSpan.text(dateFrom.val() + ' ~ ' + dateTo.val());

    });
    dateFrom.val(moment().format(dateFormat));
    dateTo.val(moment().format(dateFormat));
    rangeSpan.text(dateFrom.val() + ' ~ ' + dateTo.val());

});

function loadChartsData() {
    var invokeDateFrom = new Date($('#invokeDateFrom').val().replace(new RegExp("-","gm"),"/") + ' 00:00:00');
    var invokeDateTo = new Date($('#invokeDateTo').val().replace(new RegExp("-","gm"),"/") + ' 23:59:59');
    var provider = $('#serviceRole').val();
    if( !provider ) {
        provider = "provider";
    }
    var size = $('#size').val();
    if( !size ) {
        size = 20;
    }
    var serviceType = $('#serviceType').val();
    if (!serviceType) {
        serviceType = "success";
    }
    $.ajax({
        type: "POST", url: "loadTopData", dataType: "json", data: {
            "invokeDateFrom": invokeDateFrom,
            "invokeDateTo": invokeDateTo,
            "type": provider,
            "size": size,
            "serviceType":serviceType
        }, error: function (req, status, err) {
            alert('Failed reason: ' + err);
        }, success: function (data) {
            if (data.success) {
                drawCharts(data);
            } else {
                alert('Failed reason: ' + data.message);
            }
        }
    });
}

function drawCharts(data) {
        drawChart(data.data[0]);
}

function drawChart(data) {
    $('#topCharts').highcharts({
        chart: {
            type: 'column',
            zoomType: 'x'
        }, title: {
            text: data.title, x: -20 //center
        }, subtitle: {
            text: data.subtitle, x: -20
        }, xAxis: {
            categories: data.xAxisCategories,
            labels: {
                rotation: -45,
                align: 'right',
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        }, yAxis: {
            min: 0, title: {
                text: data.yAxisTitle
            }, plotLines: [{
                value: 0, width: 1, color: '#808080'
            }]
        }, tooltip: {
            xDateFormat: '%Y-%m-%d, %A', valueSuffix: data.yAxisTitle,
            style: {
                fontSize: '13px',
                fontFamily: 'Verdana, sans-serif'
            }
        },plotOptions: {
            series: {
                dataLabels: {
                    enabled: true,

                    color: '#FFFFFF',

                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif',
                        textShadow: '0 0 3px black'
                    }
                }
            }
        },series: data.seriesData
    });
}
