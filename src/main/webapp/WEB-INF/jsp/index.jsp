<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>
<style>
body {
	margin: 0;
}

main {
	width: 100%;
	display: flex;
}

.left-layout {
	width: 30%;
	height: 100vh;
	display: flex;
	flex-direction: column;
	border-right: 2px solid #d1d1d1;
}

.text-statement {
	background: url(http://i.imgur.com/2cOaJ.png);
	background-attachment: local;
	background-repeat: no-repeat;
	width: 100%;
	padding-left: 27px;
	box-sizing: border-box;
	height: 100%;
}

textarea {
	padding: 15px;
	width: 100%;
	height: 100%;
	border: none;
	box-sizing: border-box;
	outline: none !important;
	resize: none;
}

.right-layout {
	width: 70%;
	padding: 15px;
	background-color: #eeeeee;
	box-sizing: border-box;
}

.select-database {
	width: 140px;
	height: 32px;
	border: 1px solid #d9d9d9
}

.box-button {
	padding: 15px;
	border-bottom: 1px solid #333;
}

.box-control {
	margin-bottom: 15px;
	display: flex;
}

input[type='submit'] {
	width: 140px;
	height: 32px;
	border: 1px solid #d9d9d9;
	background: #fff;
}

input[type='text'] {
	height: 20px;
}

input[type='text']:not(:last-child) {
	margin-bottom: 15px;
}

.box-input {
	display: flex;
	flex-direction: column;
}

h3 {
	margin: 5px;
}

.box-connect {
	display: flex;
}
table {
	border-spacing: 0;
	margin-top: 15px;
}
table th {
	border-bottom: 1px solid #333;
	border-right: 1px solid #333;
	padding:5px 10px;
	font-weight: normal;
}
table th:last-child {
	border-right:none;
}
table td:not(:last-child) {
	border-right: 1px solid #333;
}
table td {
	padding: 5px 10px;
}

table td:last-child {
	border-right:none;
}
table td.appDetails:nth-last-child(2) {
	border-right:none;
}

.delete-picker {
	width:50px;
}
</style>
<body>
	<main>
		<div class="left-layout">
			<div class="box-button">
				<div class="box-control">
					<select class="select-database" id="select-database" style="margin-right:2.5px;">
						<option value="Oracle">Oracle</option>
						<option value="Mysql">Mysql</option>
						<option value="H2">H2</option>
					</select>
					<input type="submit" value="Import SQL" id="importFile" style="margin-left:2.5px;">
					<input type="file" name="inputFile" id="inputFile" style="display: none;">
				</div>
				<div class="box-input">
					<input type="text" placeholder="URL" id="url">
					<input type="text" placeholder="Schema" id="schema">
					<input type="text" placeholder="User" id="user">
					<input type="text" placeholder="Password" id="pass">
					<div class="box-connect">
						<input type="submit" value="Test Connection" onclick="testConnection()" style="margin-right:2.5px;">
						<input type="submit" value="Update query" id="updateQuery" style="margin-left:2.5px;">
					</div>
				</div>
			</div>
			<div class="text-statement">
				<textarea id="inputQuerySQL" rows="10" cols="40">${errMess}</textarea>
			</div>
		</div>
		<div class="right-layout">
			<h4 style="margin-top:0px;">DATA SET</h4>
			<select id="selectBox" class="select-database"
				onchange="selectTable(this)">
				<option value="None" disabled="disabled" selected="selected">Select table</option>
			</select>
			<table class="table">
				<thead>
					
				</thead>
				<tbody>
					
				</tbody>
			</table>
			<h4>DATA PICK</h4>
				<div id="block-data-picker">
			</div>
			<h4>EXPORT TYPES</h4>
			<div class="box-generate">
				<select id="selectType"
					class="select-database">
					<option value="Excel">Excel</option>
					<option value="SQL">SQL</option>
				</select>
				<input type="submit" value="Generate" id="Generate">
			</div>
		</div>
	</main>
	<script>
	var dataPicker = [];
	
	function delPicker(inp) {
		let id = $(inp).attr('id');
		console.log('id', id);
		$('#block-data-picker').find('table tbody #'+id).remove();
		dataPicker = dataPicker.filter(item => item.id != id);
		if (dataPicker.length == 0) {
			console.log('xxxxxxx');
			$('#block-data-picker').find('table').remove();
		}
		console.log('xxx',dataPicker);
	}
		function checkedBoxChange(check) {
			console.log('xxxxxxxx');
			let tableSelected = $('#selectBox :selected').text();
			let isNotHeader = $('#block-data-picker').find('#'+tableSelected+' thead').length == 0;
			let isNotTable = $('#block-data-picker').find('#'+tableSelected).length == 0;
			let id = $(check).attr('id');
			let data = $(check).parent().parent().find("td");
			let htmlTable = '<tr id="'+id+'"><td><input id="'+id+'" class="delete-picker" style="width:50px" type="submit" value="Del" onClick="delPicker(this)"></td>';
			let findColumn = [];
			
			let current = dataPicker.find(tb => tb.tableName == tableSelected);
			let htmlColumn = "<thead><tr><th>#</th>";
			if (!check.checked) {
				console.log('false');
				$('#block-data-picker').find('table tbody #'+id).remove();
				dataPicker = dataPicker.filter(item => item.id != id);
				return;
			}
			$('.table').find('thead tr th').each(function(index) {
				if (index != 0) {
					htmlColumn+= '<th>'+$(this).html()+'</th>'
					findColumn.push($(this).html());
				}
			});
			htmlColumn+='</tr>';
			let objectPicker = {
				tableName : tableSelected,
				listColumn : findColumn,
				listData : [],
				id : id,
			}
			let dataP = [];
			
			for (let i = 1; i < data.length; i++) {
				if (!$(data[i].innerHTML).is('input')) {
					htmlTable+="<td>";
					htmlTable+=data[i].innerHTML;
					htmlTable+="</td>";
					dataP.push(data[i].innerHTML);
				}
			}
			htmlTable+="</tr>"
			if (current) {
				current.listData.push(dataP);
				$('#block-data-picker').find('table tbody').append(htmlTable);
			} else {
				//create table
				let tableHTML = '<table id="'+tableSelected+'"><tbody>' + htmlTable + '</tbody></table>'
				if (isNotTable) {
					$('#block-data-picker').append(tableHTML);
				} else {
					console.log('id', tableSelected);
					$('#block-data-picker #'+tableSelected).find('tbody').append(htmlTable);
				}
				objectPicker.listData.push(dataP);
				dataPicker.push(objectPicker);
				
			}
			if (isNotHeader) {
				$('#block-data-picker').find('#'+tableSelected).append(htmlColumn);
			}
		}
		function selectTable(select) {
			let url = $('#url').val();
			let schema = $('#schema').val();
			let user = $('#user').val();
			let pass = $('#pass').val();
			let tableSelected = $('#select-database :selected').val();
			let tableName = select.value;
					$.ajax({
						type : "GET",
						contentType : "application/json",
						url : "/selectTable",
						data : {
							"tableName" : tableName,
							"url" : url,
							"schema" : schema,
							"user" : user,
							"pass" :pass,
							"tableSelected" : tableSelected
						},
						dataType : 'json',
						timeout : 100000,
						success : function(data) {
							console.log("SUCCESS: ", data);
							let tbl = $(".table > tbody");
							let arrData = data.listData;
							let arrColumn = data.listColumnName;
							console.log(data);
							let tien = "";
							tien += "<tr><th>#</th>";
							let tbl1 = $(".table > thead");
							tbl1.empty();
							for (let j = 0; j < arrColumn.length; j++) {
								tien += "<th>" + arrColumn[j] + "</th>";
							}
							tien+="</tr>";
							tbl.empty();
							tbl1.append(tien);
							for (let i = 1; i < arrData.length; i++) {
								let abc = "<tr>";
								let tmp = 1;
								abc += '<td><input type="checkbox" class="checkbox" onchange="checkedBoxChange(this)" id="'+tableName+i+'"></td>';
								for (let k = 0; k < arrData[i].length; k++) {
									abc += "<td id ='row"+ tmp +"-col"+ tmp +"-p' class='appDetails'>";
									abc += arrData[i][k];
									abc += "</td>";
									abc += '<td id="row'+ tmp +'-col'+ tmp +'-p-input" style="display: none;"><input  type="text" placeholder="Nhập..."></td>'
									tmp++;
								}
								abc += "</tr>";
								tbl.append(abc);
							}
							console.log(data);
						},
						error : function(e) {
							alert("Connect Error");
						}
					});
		}
		
		
		
		$(document).on("click", ".checkbox", function() {
			if($(this).prop("checked") == true){
            	console.log($(this).parent());
            }
            else if($(this).prop("checked") == false){
                console.log("Checkbox is unchecked.");
            }
		});

		$(document).on("click", ".appDetails", function() {
			var clickedBtnID = $(this).attr('id'); // or var clickedBtnID = this.id
			var inputT = $(this).attr('id')+'-input';
			alert('you clicked on button #' + clickedBtnID);
			$("#" + clickedBtnID).hide();
			$("#" + inputT).show();
		});
		
		//import file
		$('#importFile').on("click", function(){
			$('#inputFile').trigger('click');
		});
		
		$('#updateQuery').on("click", function(){
			const queryInput = $('#inputQuerySQL').val();
			$.ajax({
			       url : '/updateQuery',
			       type : 'GET',
			       data : {
						"query" : queryInput
					},
					contentType : "application/json",
					dataType : 'json',
			       success : function(data) {
			    	   if (data.mess == null) {
			    		   alert("Update success");
			    		   $('textarea#inputQuerySQL').val(data.query);
				           $("#selectBox").empty();
				           $("#selectBox").append('<option value="None" disabled="disabled" selected="selected">Select table</option>');
				           for (const tbl in data.listTable) {
				        	   $("#selectBox").append(new Option(data.listTable[tbl], data.listTable[tbl]));
				           }
				           
			    	   } else {
			    		   console.log(data.mess);
			    		   $("#selectBox").append(data.mess);
			    		   alert(data.mess);
			    	   }
			           
			       }
			});
		});
		
		//import file
		$('#inputFile').on("change", function(e){
			var formData = new FormData();
			formData.append('file', $('#inputFile')[0].files[0]);
			$.ajax({
			       url : '/uploadFile',
			       type : 'POST',
			       data : formData,
			       processData: false,  // tell jQuery not to process the data
			       contentType: false,  // tell jQuery not to set contentType
			       success : function(data) {
			    	   console.log('data', data);
			    	   const dataSQL = JSON.parse(data);
			           console.log('dataSQL', dataSQL);
			           console.log('table', dataSQL.listTable);
			           console.log('query', dataSQL.query);
			           $('textarea#inputQuerySQL').val(dataSQL.query);
			           $("#selectBox").empty();
			           $("#selectBox").append('<option value="None" disabled="disabled" selected="selected">Select table</option>');
			           for (const tbl in dataSQL.listTable) {
			        	   console.log('tbl', tbl);
			        	   $("#selectBox").append(new Option(dataSQL.listTable[tbl], dataSQL.listTable[tbl]));
			           }
			       }
			});
		});
		
		function testConnection() {
			let url = $('#url').val();
			let schema = $('#schema').val();
			let user = $('#user').val();
			let pass = $('#pass').val();
			let tableSelected = $('#select-database :selected').val();
			$.ajax({
			       url : '/testConnection',
			       type : 'GET',
			       data : {
						"url" : url,
						"schema" : schema,
						"user" : user,
						"pass" :pass,
						"tableSelected" : tableSelected
					},
					contentType : "application/json",
					dataType : 'json',
			       success : function(data) {
			           alert(data);
			       }
					
			});
		}
		
		
	</script>
</body>
</html>