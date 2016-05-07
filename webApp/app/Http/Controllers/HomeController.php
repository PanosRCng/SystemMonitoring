<?php

namespace App\Http\Controllers;

use App\Report;
use Illuminate\Http\Request;
use App\Http\Requests;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB as DB;
use Response;
use Carbon\Carbon;


class HomeController extends Controller
{
    private $sampling_interval;
    private $samplingPoints;

    /**
     * HomeController constructor.
     */
    public function __construct()
    {
        //$this->middleware('');

        $this->sampling_interval = 1;
        $this->getSamplingPoints();
    }


    /**
     * @return \Illuminate\Contracts\View\Factory|\Illuminate\View\View
     */
    public function index()
    {
        $data = [];

        if( Report::count() > 0 )
        {
            $data['machines'] = $this->getMachines();
        }

        return view('pages.home', $data);
    }


    public function machinesList()
    {
        $response = [];

        if( Report::count() > 0 )
        {
            $response['results'] = $this->getMachines();
        }

        return Response::json( $response );
    }


    public function isSynced(Request $request)
    {
        $response = [];

        if( $request->ajax() )
        {
            $machine = $request->input('machine');

            $date_time = Carbon::now()->subMinutes(10)->timestamp * 1000;

            $reports = Report::where('machine', $machine)
                ->where('date_time', '>', $date_time)->take(1)->get();

            if(count($reports) > 0)
            {
                $response['result'] = 'sync';
            }
            else
            {
                $response['result'] = 'not sync';
            }
        }

        return Response::json( $response );
    }


    public function cpuUsageChart(Request $request)
    {
        $response = [];

        if( $request->ajax() )
        {
            $response['results'] = $this->getCpuUSageData( $request->input('machine') );
        }

        return Response::json( $response );
    }

    public function ramUsageChart(Request $request)
    {
        $response = [];

        if( $request->ajax() )
        {
            $response['results'] = $this->getRamUSageData( $request->input('machine') );
        }

        return Response::json( $response );
    }


    public function netUsageChart(Request $request)
    {
        $response = [];

        if( $request->ajax() )
        {
            $response['results'] = $this->getNetUSageData( $request->input('machine') );
        }

        return Response::json( $response );
    }


    private function getCpuUsageData($machine)
    {
        $reportsSamples = $this->getReportsSamples($machine);

        $data = [];
        $cpuUsageDataPoints = [];

        if( count($reportsSamples) )
        {
            foreach($reportsSamples as $key => $reportSample)
            {
                $cpu_usage = 0.0;
                $reports_counter = 0;
                foreach($reportSample as $report)
                {
                    $cpu_usage += $report->cpu_usage;
                    $reports_counter++;
                }

                $avg_cpu_usage = 0;
                if($reports_counter > 0)
                {
                    $avg_cpu_usage = ($cpu_usage / $reports_counter);
                }

                $cpuUsageDataPoints[] = ['x' => $key, 'y' => $avg_cpu_usage];
            }
        }

        $data['cpuUsagePoints'] = $cpuUsageDataPoints;

        return $data;
    }


    private function getRamUsageData($machine)
    {
        $reportsSamples = $this->getReportsSamples($machine);

        $data = [];
        $ramUsageDataPoints = [];

        if( count($reportsSamples) )
        {
            foreach($reportsSamples as $key => $reportSample)
            {
                $ram_usage = 0.0;
                $reports_counter = 0;
                foreach($reportSample as $report)
                {
                    $ram_usage += ($report->used_mem/$report->total_mem)*100;
                    $reports_counter++;
                }

                $avg_ram_usage = 0;
                if($reports_counter > 0)
                {
                    $avg_ram_usage = ($ram_usage / $reports_counter);
                }

                $ramUsageDataPoints[] = ['x' => $key, 'y' => $avg_ram_usage];
            }
        }

        $data['ramUsagePoints'] = $ramUsageDataPoints;

        return $data;
    }


    private function getNetUsageData($machine)
    {
        $reportsSamples = $this->getReportsSamples($machine);

        $data = [];
        $netUsageDataPoints = [];

        if( count($reportsSamples) )
        {
            foreach($reportsSamples as $key => $reportSample)
            {
                $net_in_usage = 0.0;
                $net_out_usage = 0.0;
                $reports_counter = 0;

                foreach($reportSample as $report)
                {
                    $net_in_usage += $report->net_rx_Kbps;
                    $net_out_usage += $report->net_tx_Kbps;
                    $reports_counter++;
                }

                $avg_net_in_usage = 0;
                $avg_net_out_usage = 0;
                if($reports_counter > 0)
                {
                    $avg_net_in_usage = ($net_in_usage / $reports_counter);
                    $avg_net_out_usage = ($net_out_usage / $reports_counter);
                }

                $netIn = ['x' => $key, 'y' => $avg_net_in_usage];
                $netOut = ['x' => $key, 'y' => $avg_net_out_usage];

                $netUsageDataPoints[] = ['netIn' => $netIn, 'netOut' => $netOut];
            }
        }

        $data['netUsagePoints'] = $netUsageDataPoints;

        return $data;
    }


    private function getSamplingPoints()
    {
        $date = Carbon::now()->subDay();

        // calculate the sampling points
        while( Carbon::now()->gte( $date ) )
        {
            $this->samplingPoints[] = new Carbon($date);

            $date->addHours($this->sampling_interval);
        }
    }


    private function getReportsSamples($machine)
    {
        $reportsSamples = [];

        foreach ($this->samplingPoints as $samplingPoint)
        {
            $date = new Carbon($samplingPoint);
            // handle the difference between PHP and Java Unix Time Stamps
            $left_limit = $date->subHour()->timestamp * 1000;
            $right_limit = $samplingPoint->timestamp * 1000;

            $reportsSamples[$right_limit] = Report::where('machine', $machine)
                ->whereBetween('date_time', [$left_limit, $right_limit])->get()->all();
        }

        return $reportsSamples;
    }


    /**
     * @return array
     */
    private function getMachines()
    {
        $ms = DB::table('reports')->lists('machine');

        return array_unique($ms, SORT_REGULAR);
    }


}
