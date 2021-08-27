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
	margin-top:15px;
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
#rowGen {
	width: 36px;
    border: 1px solid #d9d9d9;
    height: 28px;
}
</style>
<body>
	<main>
		<div class="left-layout">
			<div class="box-button">
				<div class="box-control">
					<select class="select-database" id="select-database" style="margin-right:2.5px;">
						<option value="No database">No database</option>
						<option value="Oracle">Oracle</option>
						<option value="Mysql">Mysql</option>
						<option value="H2">H2</option>
					</select>
					<input type="submit" value="Import SQL" id="importFile" style="margin-left:2.5px;">
					<input type="file" name="inputFile" id="inputFile" style="display: none;">
				</div>
				<div class="box-input" style="display:none;">
					<input type="text" placeholder="URL" id="url">
					<input type="text" placeholder="Schema" id="schema">
					<input type="text" placeholder="User" id="user">
					<input type="text" placeholder="Password" id="pass">
				</div>
				<div class="box-connect">
					<input type="submit" value="Update query" id="updateQuery" style="margin-right:2.5px;">
					<input type="submit" value="Test Connection" id="testConnection" onclick="testConnection()" style="margin-left:2.5px; display:none;">
				</div>
			</div>
			<div class="text-statement">
				<textarea id="inputQuerySQL" rows="10" cols="40"></textarea>
			</div>
		</div>
		<div class="right-layout">
			<h4 style="margin-top:0px;">DATA SET</h4>
			<select id="selectBox" class="select-database"
				onchange="selectTable(this)">
				<option value="None" disabled="disabled" selected="selected">Select table</option>
			</select>
			<input type="submit" value="Add column" onclick="addColumnDataSet()">
			<input type="submit" value="Add row" onclick="addRowDataSet()">
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
				<input type="text" value="1" id="rowGen" >
				<input type="submit" value="Generate" id="Generate" onclick="genate()">
			</div>
		</div>
	</main>
	<script>
	var dataPicker = [];
	
	$('#select-database').on('change', function() {
		if ($(this).val() == 'No database') {
			$('#testConnection').hide();
			$('.box-input').hide();
		} else {
			$('#testConnection').show();
			$('.box-input').show();
		}
	});
	
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
			console.log('check', check);
			let data = $(check).parent().parent().find("td");
			
			let htmlTable = '<tr id="'+id+'"><td><input id="'+id+'" class="delete-picker" style="width:50px" type="submit" value="Del" onClick="delPicker(this)"></td>';
			let findColumn = [];
			
			let current = dataPicker.find(tb => tb.tableName == tableSelected);
			console.log(current);
			let htmlColumn = "<thead><tr><th>#</th>";
			if (!check.checked) {
				console.log('false');
				console.log('find data', id);
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
			
			for (let i = 0; i < data.length; i++) {
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
				$('#block-data-picker').find('table#'+ tableSelected+' tbody').append(htmlTable);
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
		var stateId = 0;
		function delRowDataPicker() {
			
		}
		
		function addRowDataSet() {
			let tableName = $('#selectBox :selected').val();
			let tableSelected = $('#select-database :selected').val();
			let listColumn = $('table.table thead tr th');
			let tbl = $("table.table > tbody");
			let rowHTML = '<tr id="row'+stateId+'">';
			for (let i = 0; i < listColumn.length; i++) {
				if (i == 0) {
					let rowId = "row"+stateId;
					rowHTML += '<td><input type="submit" value="Add" onclick="addRowDataPicker(this)" id="'+tableName+'"></td>';
				} else {
					let ind = i - 1;
					rowHTML += '<td id="row'+ stateId +'-col'+ ind +'-p-input" ><input  type="text" value="" placeholder="Nhập..."></td>'
					
				}
			}
			stateId++;
			rowHTML += "</tr>";
			tbl.prepend(rowHTML);
		}
		function addRowDataPicker(rowId) {
			let listCell = $(rowId).parent().parent().find('td');
			
			let listColumn = $('table.table').find('thead tr th');
			
			let table = $('#selectBox :selected').text();
			
			let listData = [];
			
			let listDataHeader = [];
			
			let dataPick = dataPicker.find(item => item.tableName == table);
			
			let tdHTML = "<tr>";
			
			for (let i = 1 ; i <listCell.length; i++) {
				tdHTML += "<td>"
				listData.push($(listCell[i]).children().val());
				tdHTML += $(listCell[i]).children().val();
				tdHTML += "</td>"
			}
			
			tdHTML += "</tr>"
			
			console.log('list header', listColumn.length);
			
			let tableHTML = '<table id="'+table+'">';
			let theadHTML = "<thead><tr>";
			if (!dataPick) {
				//chua co get column
				for (let i = 1 ; i < listColumn.length; i++) {
					theadHTML += "<th>";
					if ($(listColumn[i]).children().prop("tagName") == "INPUT") {
						theadHTML += $(listColumn[i]).children().val();
						listDataHeader.push($(listColumn[i]).children().val());
					} else {
						listDataHeader.push($(listColumn[i]).html());
						theadHTML += $(listColumn[i]).html();
					}
					theadHTML += "</th>";
				}
				theadHTML += "</tr>";
				theadHTML += "</thead>"
				
			}
			
			if (dataPick) {
				$('#block-data-picker').find('table#'+dataPick.tableName+' tbody').prepend(tdHTML);
				dataPick.listData.push(listData);
			} else {
				let resultHTML = tableHTML +"<caption>" + table + "</caption>" + theadHTML + "<tbody>" + tdHTML + "</tbody" + "</table>";
				$('#block-data-picker').append(resultHTML);
				const dataPush = {
						tableName : table,
						listColumn : listDataHeader,
						listData : [listData],
				}
				dataPicker.push(dataPush);
			}
			console.log('dataPicker', dataPicker);
			console.log('listData', listData);
			console.log('length td', parent.length);
			console.log('list data header', listDataHeader);
			
			console.log('rowid', rowId);
		}
		
		function addColumnDataSet() {
			let tableDataSet = $('table.table');
			let headerDataSet = $(tableDataSet).find('thead tr th');
			let rowDataSet = $(tableDataSet).find('tbody');
			let listRowDataSet = $(rowDataSet).find('tr');
			console.log('length tr', listRowDataSet.length);
			for (let i = 0 ; i < listRowDataSet.length ; i++) {
				let listTD = $(listRowDataSet[i]).find('td');
				let trId = $(listRowDataSet[i]).attr('id');
				console.log('trId', trId);
				let lengthTD = (listTD.length - 1)/2;
				console.log('lengtd', lengthTD);
				let rowHTML = "";
				rowHTML += "<td id='"+trId+"-col"+lengthTD+"-p-input' ><input  type='text' value='' placeholder='Nhập...'></td>"
				$(listTD).first().after(rowHTML);
			}
			$(headerDataSet).first().after('<th><input type="text"></th>');
		}
		
		function selectTable(select) {
			let url = $('#url').val();
			let schema = $('#schema').val();
			let user = $('#user').val();
			let pass = $('#pass').val();
			let query = $('#inputQuerySQL').val();
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
							"tableSelected" : tableSelected,
							"query" : query,
						},
						dataType : 'json',
						timeout : 10000,
						success : function(data) {
							if("Table not exit" == data) {
								alert("Table not exit");
								return;
							}
							if("Connect error" == data) {
								alert("Connect error");
								return;
							} 
							console.log("SUCCESS: ", data);
							let tbl = $(".table > tbody");
							let arrData = data.listData;
							let arrColumn = data.listColumnName;
							let tien = "";
							tien += "<tr><th>#</th>";
							let tbl1 = $(".table > thead");
							tbl1.empty();
							$(".table > tbody").empty();
							for (let j = 0; j < arrColumn.length; j++) {
								tien += "<th>" + arrColumn[j] + "</th>";
							}
							tien+="</tr>";
							tbl.empty();
							tbl1.append(tien);
							if (arrData && arrData.length != 0) {
								for (let i = 0; i < arrData.length; i++) {
									let abc = "<tr>";
									abc += '<td><input type="checkbox" class="checkbox" onchange="checkedBoxChange(this)" id="'+tableName+i+'"></td>';
									for (let k = 0; k < arrData[i].length; k++) {
										abc += "<td id ='row"+ i +"-col"+ k +"-p' class='appDetails'>";
										abc += arrData[i][k];
										abc += "</td>";
										abc += '<td id="row'+ i +'-col'+ k +'-p-input" style="display: none;" ><input  type="text" onblur="changeDataInput(this)" value='+ arrData[i][k]+' placeholder="Nhập..."></td>'
										
									}
									abc += "</tr>";
									tbl.append(abc);
								}
							}
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
			$("#" + inputT).show().find('input').focus();
			$("#" + clickedBtnID).hide();
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
			console.log('xxx');
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
		
		function changeDataInput(cur) {
			console.log();
			let curParentInput = $(cur).parent();
			
			let valInput = $(cur).val();
			let curId = $(cur).parent().attr('id');
			let label = curId.substring(0, curId.length - 6);
			curParentInput.hide();
			$("#"+label).show();
			$("#"+label).html(valInput);
			
		}
		
		function genate() {
			let typeGen = $('#selectType :selected').text();
			let queryInput = $('#inputQuerySQL').val();
			let url = $('#url').val();
			let schema = $('#schema').val();
			let user = $('#user').val();
			let pass = $('#pass').val();
			let row =  $('#rowGen').val();
			let tableSelected = $('#select-database :selected').val();
			const infoDatabase = {
					type : tableSelected,
					url : url,
					schema : schema,
					user : user,
					password :pass,
			}
			const inputGenerate = {
					dataPicker : dataPicker,
					queryInput : queryInput,
					typeGen : typeGen,
			}
			
			
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/generate', true);
			xhr.responseType = 'blob';
			xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
			xhr.onload = function(e) {
			    if (this.status == 200) {
			    	var blob = new Blob([xhr.response], {type: xhr.getResponseHeader("Content-Type")});
			        var downloadUrl = URL.createObjectURL(blob);
			        var a = document.createElement("a");
			        a.href = downloadUrl;
			        if (xhr.getResponseHeader("Content-Type") == 'application/vnd.ms-excel') {
			        	a.download = "data.xls";
			        } else {
			        	a.download = "data.txt";
			        }
			        document.body.appendChild(a);
			        a.click();
			    } else {
			        alert('Unable to download excel.')
			    }
			};
			const jsonData = {
					queryInput : queryInput,
					typeGen : typeGen,
					dataPicker : dataPicker,
					infoDatabase : infoDatabase,
					row :row,
			}
			xhr.send(JSON.stringify(jsonData));
			
		}
		
	</script>
</body>
</html>