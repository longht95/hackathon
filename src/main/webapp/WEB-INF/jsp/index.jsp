<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <link rel="stylesheet" href= "css/indexx.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>
<body>
	<div id="form-login">
        <form action="/upload" method = "POST" enctype="multipart/form-data">
            <div class="form-group">
                <label for="exampleInputEmail1">User</label>
                <input type="text" class="form-control" id="exampleInputEmail1" aria-describedby="emailHelp"
                    placeholder="Enter email">
            </div>
            <div class="form-group">
                <label for="exampleInputPassword1">Password</label>
                <input type="password" class="form-control" id="exampleInputPassword1" placeholder="Password">
            </div>
            <div class="form-group">
                <label for="exampleInputPassword1">URL</label>
                <input type="text" class="form-control" id="exampleInputPassword1" placeholder="URL">
            </div>

            <select class="custom-select" id="inputGroupSelect01">
                <option selected>Choose...</option>
                <option value="1">Oracle</option>
                <option value="2">My SQL</option>
                <option value="3">SQL Server</option>
            </select>
            &nbsp;
            <select class="custom-select" id="inputGroupSelect02">
                <option selected>Choose...</option>
                <option value="1">Input file</option>
                <option value="2">Input query</option>
            </select>
            
            <div id="file" style="display: none;">
                <label id="select-file">Select a file:</label>
                <input type="file" id="myfile" name=file><br><br>
            </div>
            <div id="query" style="display: none;">
                <div id="query-non">
                    <textarea id="input-query" path="name" name="inputFile" cols="40" rows="1"></textarea>
                </div>
            </div>
            
            <button type="submit" class="btn btn-primary">Submit</button>
        </form>
    </div>

    <script>
        $(document).ready(function() {
            console.log("dsa");
            $( "#inputGroupSelect02" ).change(function() {
            var e = $('#inputGroupSelect02').val();
            console.log(e);
            if (e == 1) {
                $("#file").show();
                $("#query").hide();
                
            } else {
              $("#file").hide();
              $("#query").show();
            }
        });
        });
    </script>
</body>
</html>