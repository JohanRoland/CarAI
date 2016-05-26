<?php
$file = fopen("index.html","r");
echo fread($file,filesize("index.html"));



?>
