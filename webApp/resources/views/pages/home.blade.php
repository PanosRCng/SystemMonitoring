
@extends('layouts.base')

@section('body')

    <script src={{ URL::asset('js/jquery.canvasjs.min.js') }}></script>
    <script src={{ URL::asset('js/charts.js') }}></script>


    @include('includes.header')

    <div class="panel panel-default">
        <div class="panel-body">


    @if ( ( isset($machines) ) && ( count($machines)>0 ) )

        @foreach($machines as $machine)

            <div class="panel panel-default">
                <div class="panel-heading">{{ $machine  }}</div>
                <div class="panel-body">
                    <h5 id="{{ $machine  }}_sync_label">
                    <!--
                        <span class="label label-default">info</span>
                        <span class="label label-warning">Warning</span> -->
                    </h5>

                    <div class="row">
                        <div class="col-sm-6" id="{{ $machine  }}_cpuUsageChartContainer" style="height:100px; width:400px"></div>
                        <div class="col-sm-6" id="{{ $machine  }}_ramUsageChartContainer" style="height:100px; width:400px"></div>
                        <div class="col-sm-6" id="{{ $machine  }}_netUsageChartContainer" style="height:100px; width:400px"></div>
                    </div>

                </div>
            </div>

        @endforeach

    @else
        <p> there are no machines </p>
    @endif

        </div>
    </div>

@stop