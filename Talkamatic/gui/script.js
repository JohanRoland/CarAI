  var ws;
  var nrws;


  function connectToTDM()
  {
    ws = new WebSocket("ws://localhost:8888/tdm_gui"); 
    ws.onopen = function () {
      connectedOn();
    };
    ws.onclose = function () {
      connectedOff();
    };
    ws.onerror = function (evt) {
      connectedOff();
    };
    ws.onmessage = function (evt) {
      msg = JSON.parse(evt.data);
      console.log(JSON.stringify(msg,null,2));
      if(msg.length != 0)
      {
        for(var i in msg)
        {
          if("DeviceResult" in msg[i])
          {
            console.log("Action: "+ msg[i].DeviceResult.action)
          }
          if("Popup" in msg[i])
          {
            extra = "";
            m = msg[i].Popup.title.replace('-','');
            if(m != "")
            {
                if(msg[i].Popup.elements.length >0)
              {
                for(var j in msg[i].Popup.elements[0].Element.items)
                {
                  extra += parseElement( msg[i].Popup.elements[0].Element.items[j]);
                }
              }
              $(".contentArea").append("<div class=\"convo\"><p>"+m+"</p>"+extra+"</div>");
              $('.contentContainer').scrollTop($('.contentContainer')[0].scrollHeight);
            }
          }
          if("Screen" in msg[i])
          {
            extra = "";
            m = msg[i].Screen.title;
            if(msg[i].Screen.elements.length >0)
            {
              m = msg[i].Screen.elements[0].Element.title;
              for(var j in msg[i].Screen.elements[0].Element.items)
              {
                extra += parseElement( msg[i].Screen.elements[0].Element.items[j]);
              }
            }
            $(".contentArea").append("<div class=\"convo\"><p>"+m+"</p>"+extra+"</div>");
            $('.contentContainer').scrollTop($('.contentContainer')[0].scrollHeight);
          }
        }
      }
    };
  }
 
  function connectInput()
  {
    nrws = new WebSocket("ws://192.168.1.127:1880/gui");
 
    nrws.onmessage = function(evt) {
      m = JSON.parse(evt.data)
      if(m.ptt == 'on')
      {
        pttOn();
      }    
      else if( m.ptt == 'off')
      {
        pttOff();
      }

    };

  }
  function parseElement(e)
  {
      if(e.Item.title != null)
      {
        return  "<button class='tdmbutton' onclick=\"sendListToTDM('"+e.Item.value+"')\">"+e.Item.title+"</button>";
      }
    
  }

  function printMsg(s) {
    $(".contentArea").append("<div class=\"convo2\"><p>"+s+"</p></div>") 
    $('.contentContainer').scrollTop($('.contentContainer')[0].scrollHeight);
  }

  function sendToTDM(m) {
    ws.send("[{\"InputFromGui\":{\"type\":\"text\",\"name\":\"text\",\"value\":\""+m+"\",\"title\":\"text\"}}]")
  };

  function sendListToTDM(m) {
    ws.send("[{\"InputFromGui\":{\"type\":\"list\",\"name\":\"move\",\"value\":\""+m+"\",\"title\":\"text\"}}]")
  };


  function sendUtteranceToTDM(m) {
    //client.publish("talkamatic/input",m);
    waitForSocketConnection(nrws,function() {
    console.log("sent message");
    nrws.send(m);
    });
  }; 
 
  function sendPTTToTDM() {
    //client.publish("talkamatic/ptt","pttpushed")
    waitForSocketConnection(nrws,function() {
    console.log("mic activated");
    nrws.send("pttpushed");
    });
  };
  
  function waitForSocketConnection(socket, callback){
    setTimeout(
        function () {
            if (socket.readyState === 1) {
                console.log("Connection is made")
                if(callback != null){
                    callback();
                }
                return;

            } else {
                console.log("wait for connection...")
                waitForSocketConnection(socket, callback);
            }

        }, 5); // wait 5 milisecond for the connection...
  }

  function connect()
  {
    connectToTDM();
    connectInput();
  };

  function disconnect() {
    ws.send("[{\"Disconnect\":{}}]")
  };
  function cancel() {
    ws.send("[{\"InputFromGui\":{\"type\":\"key\",\"name\":\"cancel\",\"value\":\"cancel\",\"title\":\"text\"}}]") 
  }
  function pttOn()
  { 
    $('.mic-button').css("background","url(content/mic-icon.svg)");
    //$('#mic-button').prop('disabled',true);
  }
  function pttOff()
  {
    $('.mic-button').css("background","url(content/mic-icon-inactive.svg)");
    //$('#mic-button').prop('disabled',false);
  }

  function connectedOn()
  { 
    $('#cbutton').prop('disabled',true);
    $('#cbutton').css("background-color","green");
  }  
  function connectedOff()
  {
    $('#cbutton').css("background-color","red");
    $('#cbutton').prop('disabled',false);
  }

  function getLoc()
  {
    printMsg("Where are we?")
    sendListToTDM('ask(?X.gpsdata(X))')
  }
  
  function getDest()
  {
    printMsg("Where are we going?")
    sendListToTDM('ask(?X.destdata(X))')
  }
  
  function getEta()
  {
    printMsg("When will we arive?")
    sendListToTDM('ask(?X.timetodest(X))')
  }
  
  function getMeeting()
  {
    printMsg("What is my next scheduled event?")
    sendListToTDM('ask(?X.next_cal_event(X))')
  }
  
  $(document).ready(function() {
    //connect();
  });

  
  
  $(function() {
   $(".input-form").submit(function(e) {
      e.preventDefault();   
      var s = document.forms["inform"]["in"].value;
      printMsg(s);
      //sendUtteranceToTDM(s);
      document.forms["inform"]["in"].value = "";
      
    }); 
  });
  
  ///////////////RECORDING PART////////////////////
  
      var onFail = function(e) {
        console.log('Rejected!', e);
      };

      var onSuccess = function(s) {
        console.log("success!")
        var context = new webkitAudioContext();
        var mediaStreamSource = context.createMediaStreamSource(s);
        recorder = new Recorder(mediaStreamSource);
        recorder.record();

        // audio loopback
        // mediaStreamSource.connect(context.destination);
      }

      window.URL = window.URL || window.webkitURL;
      navigator.mediaDevices.getUserMedia  = navigator.mediaDevices.getUserMedia || navigator.mediaDevices.webkitGetUserMedia || navigator.mediaDevices.mozGetUserMedia || navigator.mediaDevices.msGetUserMedia;

      var recorder;
      var audio = document.querySelector('audio');

      function startRecording() {
        if (navigator.mediaDevices.getUserMedia) {
          navigator.mediaDevices.getUserMedia({audio: true}, onSuccess, onFail);
        } else {
          console.log('navigator.mediaDevices.getUserMedia not present');
        }
      }

      function stopRecording() {
        recorder.stop();
        recorder.exportWAV(function(s) {
          audio.src = window.URL.createObjectURL(s);
        });
      }
  
  
  ////////////////////////////////////////////////
