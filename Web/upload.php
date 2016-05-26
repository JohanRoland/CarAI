<?php

function isTuple($var)
{
	if($var[0] == "(")
	{
		return true;
	}
	return false;
}

function isNotTuple($var)
{
	if($var[0] != "(")
	{
		return true;
	}
	return false;
}

$target_dir =  "uploads/";
$target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
$uploadOK =1;

$imageFileType = pathinfo($target_file,PATHINFO_EXTENSION);
/*$ext = strtolower(substr(strrchr($_FILES['file']['name'],'.'),1)); */
if(isset($_POST["submit"]))
{
	$uploadOK = 1;
}

if($_FILES["fileToUpload"]["size"] > 500000000) 
{
	echo "Sorry, your file is to big. ";
	$uploadOK = 0;
}

/*
if($imageFileType != "kml")
{
	echo (" is not accepted, only kml files are accepted ");
	$uploadOK = 0;
}*/


if ($uploadOK == 0) 
{
	echo "sorry your file was not uploaded";
}
else
{
	if(move_uploaded_file($_FILES["fileToUpload"]["tmp_name"],$target_file))
	{
		echo "The File has been uploaded and is now being processed<br>";
		$descriptorspec = array(
			0 => array("pipe","r"),
			1 => array("pipe","w"),
			2 => array("file","/tmp/error-output.txt","a")
		);

		$process = proc_open("sh run.sh ".$target_file,$descriptorspec,$pipes);

		if (is_resource($process))
		{
			fclose($pipes[0]);
			$retVal = stream_get_contents($pipes[1]);
			fclose($pipes[1]);

			$return_value = proc_close($process);
			/*echo "command returned $return_value\n";*/
			$retVals = explode("\n",$retVal);
			$retVals2 = array_filter($retVals,"isTuple"); 
			$stats = array_filter($retVals,"isNotTuple");
			echo "<script>var stats = [\"".implode("\",\"",$stats)."\",\"Clusters:".(count($retVals2)-1)."\"];
				var marks = [\"".implode("\",\"",$retVals2)."\"];</script>";
		}



	}
	else
	{
		echo "An error has occured";
		print_r($_FILES);
	}

}

		$file = fopen("mapinterface.html","r");
		echo fread($file,filesize("mapinterface.html"));

?>
