
var map;


$(function() {
  $("#iform").submit(function(e) {
	  e.preventDefault();   
	  var s = document.forms["inform"]["in"].value;
	  var res;
		var latlongs = s.split("\n")
		$.ajax(
		{
			type:"POST",
			url:'cullocs.php',
			dataType:'json',
 		  data: {functionname:'cull',arguments:[s]},
			success: function(obj,textstatus) {
				if(!('error' in obj))
				{
					 res = obj.result;
					 console.log(res);
				}
				else {
					console.log(obj.error);
				}
			}
		});
		for(var l = 0; l <latlongs.length; l++)
		{
		 	 var coords = latlongs[l].split(",");
			 //var coords = results.features[i].geometry.coordinates;
			 var latLng = new google.maps.LatLng(Number(coords[0]),Number(coords[1]));
			 var marker = new google.maps.Marker({
		 		 position: latLng,
				 map: map
			});	                                                
	  }
	});
	for(var i = 0; i < stats.length; i++)
	{
		$("#statData").append(stats[i]+"<br>");
	}
});

function initMap() {
	var myLatLng = {lat: 57.697032, lng: 11.976045};

	map = new google.maps.Map(document.getElementById('map'), {
		zoom: 11,
		center: {lat: 57.697032, lng: 11.976045}
	});
	
	for(var i = 1; i < marks.length; i++)
	{
		var res = marks[i].substring(1,marks[i].length-1);
		var coords = res.split(",");
		var latLng = new google.maps.LatLng(Number(coords[0]),Number(coords[1]));
		var marker = new google.maps.Marker({
			position: latLng,
			map: map,
			});
	}
}


