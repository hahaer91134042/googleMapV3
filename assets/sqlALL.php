<?php
	//MySQL資料庫	MySQL用戶	MySQL主機	磁碟空間使用情況，MB
//   u327232703_gcm	u327232703_tcnr6	mysql.hostinger.com.hk	0.03
$hostname = "mysql.hostinger.com.hk";
$username = "u327232703_tcnr6";
$password = "a91134042";
$dbname = "u327232703_gcm";
$dbtable="member";

// Create connection
$db = new mysqli($hostname, $username, $password, $dbname);
// Check connection
if ($db->connect_error) {
die("Connection failed: " . $db->connect_error);
}
//----------------------------------------------------------------
$db->query("SET NAMES utf8");
$db->query("SET CHARACTER SET 'UTF8';");
$db->query('SET CHARACTER_SET_CLIENT=UTF8;');
$db->query('SET CHARACTER_SET_RESULTS=UTF8;');
//----------------------------------------------------------------
//$sql = "SELECT * FROM member";
$getorder = $_REQUEST['query_string'];
//$getorder ="delete";
switch ($getorder) {
	case 'import':
		$sql = sprintf("SELECT * FROM ".$dbtable);
        $result = $db->query($sql);
        if ($result->num_rows > 0) {
        // output data of each row
         while($row = $result->fetch_assoc()) {
         $output[] = $row;
         }
        } else {
        echo "0 results";
        }
       //echo $output;
       print(json_encode($output));
		break;
	case 'delete':
	   $ch_id = $_REQUEST['id'];
       //$ch_id = "4";
       $id_num = (int)$ch_id;
       $sql = "DELETE From ".$dbtable." WHERE id = $id_num" ;
       
       $flag['code']=0;
       if($row=$db->query($sql)) {
        echo"delete ok";
       $flag['code']=1;
         } else {
        echo"delete missing";
       $flag['code']=0;
       }
       print(json_encode($flag));
		break;

	case 'update':
	    $ch_id = $_REQUEST['id'];
        $ch_name=$_REQUEST['name'];
        $ch_grp=$_REQUEST['grp'];
        $ch_address=$_REQUEST['address'];
        $id_num = (int)$ch_id;
        $sql = "UPDATE  ".$dbtable." SET name = '$ch_name'  , grp = '$ch_grp' , address = '$ch_address'   WHERE id = $id_num ";
        $flag['code']=0;
        if($row=$db->query($sql)) {
        $flag['code']=1;
        }else {
         $flag['code']=0;
        }
        print(json_encode($flag));

	break;
	case 'insert':
	    $name=$_REQUEST['name'];
        $grp=$_REQUEST['grp'];
        $address=$_REQUEST['address'];
      //$name="路人甲";
      //$grp="第3組";
      //$address="台中市南屯區";
        $sql = "INSERT INTO ".$dbtable." VALUES(DEFAULT,'$name','$grp','$address') ";
        $flag['code']=0;
        if($row=$db->query($sql))
          {
        $flag['code']=1;
        echo"hi ok";
        }
        print(json_encode($flag));
	break;
	
}



$db->close();
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>無標題文件</title>
</head>

<body>


</body>
</html>