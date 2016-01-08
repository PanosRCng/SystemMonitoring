<?php

namespace App\Http\Controllers;

use App\Report;
use Illuminate\Http\Request;
use App\Http\Requests;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB as DB;


class HomeController extends Controller
{

    /**
     * HomeController constructor.
     */
    public function __construct()
    {
        //$this->middleware('');
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


    /**
     * @return array
     */
    public function getMachines()
    {
        $ms = DB::table('reports')->lists('machine');

        return array_unique($ms, SORT_REGULAR);
    }


}
