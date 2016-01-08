
@extends('layouts.base')

@section('body')

    @include('includes.header')

    <div class="panel panel-default">
        <div class="panel-body">


    @if ( ( isset($machines) ) && ( count($machines)>0 ) )

        @foreach($machines as $machine)

            <div class="panel panel-default">
                <div class="panel-heading">{{ $machine  }}</div>
                <div class="panel-body">
                    <h5>status
                        <span class="label label-default">info</span>
                        <span class="label label-success">on</span>
                        <span class="label label-danger">off</span>
                        <span class="label label-warning">Warning</span>
                    </h5>
                    infos and some small graphs here
                </div>
            </div>

        @endforeach

    @else
        <p> there are no machines </p>
    @endif

        </div>
    </div>

@stop