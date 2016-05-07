<?php

/*
|--------------------------------------------------------------------------
| Routes File
|--------------------------------------------------------------------------
|
| Here is where you will register all of the routes in an application.
| It's a breeze. Simply tell Laravel the URIs it should respond to
| and give it the controller to call when that URI is requested.
|
*/

Route::get('/', 'HomeController@index');

Route::get('home', 'HomeController@index');

Route::get('charts/cpuUsage', 'HomeController@cpuUsageChart');

Route::get('charts/ramUsage', 'HomeController@ramUsageChart');

Route::get('charts/netUsage', 'HomeController@netUsageChart');

Route::get('machinesList', 'HomeController@machinesList');

Route::get('isSynced', 'HomeController@isSynced');

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| This route group applies the "web" middleware group to every route
| it contains. The "web" middleware group is defined in your HTTP
| kernel and includes session state, CSRF protection, and more.
|
*/

Route::group(['middleware' => ['web']], function () {
    //
});
