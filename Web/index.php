<?php

$file = fopen("admininterface.html","r");
echo fread($file,filesize("admininterface.html"));


?>
