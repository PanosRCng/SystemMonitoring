<!DOCTYPE html>
<html>

<head>
    @include('includes.head')
</head>


<body>
{{-- Javascript --}}
<script src={{ URL::asset('js/jquery-1.11.3.min.js') }}></script>
<script src={{ URL::asset('bootstrap/js/bootstrap.min.js') }}></script>


@yield('body')

</body>

</html>