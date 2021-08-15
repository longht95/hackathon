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
</head>
<body>
	<div id = "form-login">
        <form action="/input">
            <div class="form-group">
              <label for="exampleInputUser1">User</label>
              <input type="text" class="form-control" id="exampleInputUser1" aria-describedby="emailHelp" placeholder="Enter email">
            </div>
            <div class="form-group">
              <label for="exampleInputPassword1">Password</label>
              <input type="password" class="form-control" id="exampleInputPassword1" placeholder="Password">
            </div>
            <div class="form-group">
                <label for="exampleInputPassword1">Schema name</label>
                <input type="text" class="form-control" id="exampleInputPassword1" placeholder="Schema name">
            </div>
            <div class="form-group">
                <label for="exampleInputPassword1">Host name</label>
                <input type="text" class="form-control" id="exampleInputPassword1" placeholder="Host name">
            </div>
            <div class="form-group">
                <label for="exampleInputPassword1">Port</label>
                <input type="text" class="form-control" id="exampleInputPassword1" placeholder="Port">
            </div>
            <button type="submit" class="btn btn-primary">Submit</button>
        </form>
    </div>
</body>
</html>