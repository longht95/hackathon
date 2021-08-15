<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <link rel="stylesheet" href= "css/input.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>
<body>
	<div id = "form-login">
        <div class="input-group mb-3">
            <div class="input-group-prepend">
              <label class="input-group-text" for="inputGroupSelect01">Options</label>
            </div>
            <select class="custom-select" id="inputGroupSelect01" >
              <option selected>Choose...</option>
              <option value="1">Input file</option>
              <option value="2">Input query</option>
            </select>
          </div>
          <form action="/action_page.php" id = "input-file">
            <label for="myfile">Select a file:</label>
            <input type="file" id="myfile" name="myfile"><br><br>
            <input type="submit">
          </form>
          <textarea name="file" id="input-query" cols="40" rows="1" style="display: none;"></textarea>
    </div> 
    <script>
        $( document ).ready(function() {
        console.log( "ready!" );
            $( "#inputGroupSelect01" ).change(function() {
            var e = $('#inputGroupSelect01').val();
            if (e == 1) {
                $("#input-file").show();
                $("#input-query").hide();
            } else {
                $("#input-file").hide();
                $("#input-query").show();
            }
        });
    });
        
    </script>
</body>
</html>