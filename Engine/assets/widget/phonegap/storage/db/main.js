

	// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false); 
	
	// 填充数据库
	function populateDB(tx) {
		//tx.executeSql('DROP TABLE DEMO IF EXISTS');
		tx.executeSql('CREATE TABLE IF NOT EXISTS DEMO (id unique, data)');
		tx.executeSql('INSERT INTO DEMO (id, data) VALUES (1, "Firest row")');
		tx.executeSql('INSERT INTO DEMO (id, data) VALUES (2, "Sec3eond12 row")',[],insertSuccess);
	}
	function testDB(){
		var db = window.openDatabase("Database7", "1.0", "PhoneGap Demo", 200000);
		db.transaction(populateDB, errorCB, successCB);
	};
	function insertSuccess(te,results){
		console.log("Insert ID = " + results.insertId);
		console.log("Rows Affected = " + results.rowAffected);
	}
	// 查询数据库
	function queryDB(tx) {
		tx.executeSql('SELECT * FROM DEMO', [], querySuccess, errorCB);
	}
	
	// 查询成功后调用的回调函数
	function querySuccess(tx, results) {
		//alert("Insert ID = " + results.insertId);
		//alert("Rows Affected = " + results.rowAffected);
		//alert("Insert ID = " + results.rows.length);
		var len = results.rows.length;
		console.log("DEMO table: " + len + " rows found.");
		for (var i=0; i<len; i++){
			alert("插入数据之后的查询记录:Row = " + i + " ID = " + results.rows.item(i).id + " Data =  " + results.rows.item(i).data);
		}
	}
	
	// 事务执行出错后调用的回调函数
	function errorCB(err) {
		console.log("Error processing SQL: "+err.message);
	}
	
	// 事务执行成功后调用的回调函数
	function successCB() {
		var db = window.openDatabase("Database7", "1.0", "PhoneGap Demo", 200000);
		db.transaction(queryDB, errorCB);
	}
	
	// PhoneGap加载完毕
	//function onDeviceReady() {
	//	var db = window.openDatabase("Database9", "1.0", "PhoneGap Demo", 200000);
	//	db.transaction(populateDB, errorCB, successCB);
	//}