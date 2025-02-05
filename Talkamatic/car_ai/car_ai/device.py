import os, sys
from datetime import datetime,timedelta
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))))

from tdm.lib.device import DddDevice,EntityRecognizer,DeviceAction,DeviceWHQuery,Validity
from tdm.tdmlib import *#EntityRecognizer,DeviceAction,DeviceWHQuery,Validity,DeviceMethod
from car_ai.contacts import CONTACT_NUMBERS,LOCATIONS, State,POI
from tdm.device_handler import send_to_frontend_device

import paho.mqtt.client as mqtt

sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))) 
from Python.DBConnection import createUser


from Python.GeoData import dist,locInfo
from Python.FetchCal import getNextEvent,getEvent,parseCal,formatDateDiff
#print(sys.path)
class CaraiDevice(DddDevice):
    class Call(DeviceAction):
        PARAMETERS = ["selected_contact.grammar_entry"]
        def perform(self, selected_contact):
            number = CONTACT_NUMBERS.get(selected_contact)
            # TODO: Implement calling
            success = True
            return success
    class phone_number_of_contact(DeviceWHQuery):
        PARAMETERS = ["selected_contact.grammar_entry"]
        def perform(self, selected_contact):
            number = CONTACT_NUMBERS.get(selected_contact)
            number_entity = {
                "grammar_entry": number
            }
            return [number_entity]
    class all_contacts(DeviceWHQuery):
        PARAMETERS = []
        def perform(self):
            out = []
            for con in CONTACT_NUMBERS.keys():
                number_entity = {
                    "grammar_entry": con
                }
                out.append(number_entity)
            return out
    class ContactRecognizer(EntityRecognizer):
        def recognize_entity(self, string):
            result = []
            words = string.lower().split()
            for contact in CONTACT_NUMBERS.keys():
                if contact.lower() in words:
                    recognized_entity = {
                        "sort": "contact",
                        "grammar_entry": contact
                    }
                    result.append(recognized_entity)
            return result
    class PhoneNumberAvailable(Validity):
        PARAMETERS = ["selected_contact.grammar_entry"]
        def is_valid(self, selected_contact):
            number = CONTACT_NUMBERS.get(selected_contact)
            if number:
                return True
            return False
#
#       navigate
#
    class Navigate(DeviceAction):
        PARAMETERS = ["location.grammar_entry"]
        def perform(self, location):
            pos = LOCATIONS[location]
            # Send GPS INFO
            client = mqtt.Client()
            client.connect("54.229.54.240", 1883, 60)
            client.publish("carai/car/gps","{\"a\": \""+pos[0]+"\",\"b\":\""+pos[1]+"\"}")
            client.loop(1) #timeout 1 sec
            success = True
            return success

#    class LocationRecognizer(EntityRecognizer):
#        def recognize_entity(self, string):
#            result = []
#            words = string.lower().split()
#            for contact in LOCATIONS.keys():
#                if contact.lower() in words:
#                    recognized_entity = {
#                        "sort": "locs",
#                        "grammar_entry": contact
#                    }
#                    result.append(recognized_entity)
#            return result

######################## GPS ###################

#
#     POS
#

    class gpsdata(DeviceWHQuery):
      def perform(self):
        gps = ACTIVE_STATE.LOCATION
        road = locInfo(gps[0],gps[1]).split(',')[0]
        gps_entity = {
          "grammar_entry" : road,
          }
        return [gps_entity]
      
    
#
#     DEST
#
    class setDest(DeviceAction):
      PARAMETERS = []
      def perform(self):
        return True

    class SetDest(DeviceAction):
      PARAMETERS = ["destdata.grammar_entry","timetodest.grammar_entry"]
      def perform(self,gps,time):
        return True

    class destdata(DeviceWHQuery):  
      def perform(self):
        out = []
        #for d in ACTIVE_STATE.GPSDEST:
        d = ACTIVE_STATE.GPSDEST[0] 
        gps_entity = {
          "grammar_entry":  ACTIVE_STATE.convertToPOI(d),
          "confidence": str(d[2])
          }
        out.append(gps_entity)
        return out 


    class GpsRecognizer(EntityRecognizer):
        def recognize_entity(self, string):
            result = []
            words = string.lower().split()
            for word in words:
                for loc in POI.keys(): 
                  if word == POI[loc]:
                    recognized_entity = {
                      "sort": "locs",
                      "grammar_entry": word
                    }
                    result.append(recognized_entity)
            return result

#
#       ETA
#
    class timetodest(DeviceWHQuery):
      def perform(self):
        pos = ACTIVE_STATE.LOCATION
        dest = ACTIVE_STATE.GPSDEST[0]
        eta = "-1"
        if pos != (0,0):
          if dest != (0,0):
            di,eta = dist(pos[0],pos[1],dest[0],dest[1])
            eta = eta.replace("mins","minutes")
        entity = {
          "grammar_entry": eta
          }
        return [entity]

#################### END GPS #####################

#
#       Airconditioning
#       

    class SetTemp(DeviceAction):
        PARAMETERS = ["temperature.grammar_entry"]
        def perform(self,temperature):
            client = mqtt.Client()
            client.connect("54.229.54.240", 1883, 60)
            client.publish("carai/car/ac",temperature)
            client.loop(1) #timeout 1 sec
            success = True
            return success

    class DegreeRecognizer(EntityRecognizer):
        def recognize_entity(self, string):
            result = []
            words = string.lower().split()
            for word in words:
                if word.isdigit():
                    recognized_entity = {
                        "sort": "deg",
                        "grammar_entry": word
                    }
                    result.append(recognized_entity)
            return result

#
#     Calendar
#
    class next_cal_event(DeviceWHQuery):
        def perform(self):
            ev = parseCal(getNextEvent(1))
            timeLeft = formatDateDiff(ev[1],datetime.now())
            event = {
                "grammar_entry": (ev[0] + timeLeft)
            }
            return [event]

    class cal_event(DeviceWHQuery):
        PARAMETERS =["day.grammar_entry"]
        def perform(self,day):
            days = ["monday","tuesday","wednesday","thursday","friday","saturday","sunday"]
            weekday = days.index(day)
            tnow = datetime.now()
            daysahead = weekday - tnow.weekday()
            if daysahead <= 0: 
              daysahead += 7
            date = tnow + timedelta(daysahead)
            date = date.replace(hour=0,minute=0,second=0)
            evs = getEvent(1,date)
            outstring = ""
            c = 0
            for es in evs:
              ev = parseCal(es)
              #ev = parseCal(getNextEvent(1))
              #timeLeft = formatDateDiff(ev[1],datetime.now())
              timeofday = ev[1].strftime('%H:%M')
              if c == len(evs) -1:
                outstring += ev[0] + " at " + timeofday 
              else: 
                outstring += ev[0] + " at " + timeofday + "and " 
              c = c+1
            event = {
                "grammar_entry":outstring
            }
            return [event]
    class DayNameRecognizer(EntityRecognizer):
        def recognize_entity(self, string):
            result = []
            words = string.lower().split()
            days = ["monday","tuesday","wednesday","thursday","friday","saturday","sunday"]
            for u in days:
                if u in words:
                    recognized_entity = {
                        "sort": "day_name",
                        "grammar_entry": u
                    }
                    result.append(recognized_entity)
            return result

#
#      USER RECOGNITION
#

    class user_name(DeviceWHQuery):
        def perform(self):
            result = []
            #uName = ACTIVE_STATE.getName()
            for users, conf in ACTIVE_STATE.getAllUsers():
                user_entity = {
                    "grammar_entry": users,
                    "confidence": conf
                }
                result.append(user_entity)
            return result

    class UserRecognizer(EntityRecognizer):
        def recognize_entity(self, string):
            result = []
            words = string.lower().split()
            for u in ACTIVE_STATE.getUsersDic().keys():
                name = ACTIVE_STATE.getUsersDic()[u].getName()
                if name.lower() in words:
                    recognized_entity = {
                        "sort": "u_name",
                        "grammar_entry": name
                    }
                    result.append(recognized_entity)
            return result
        
  
    class incar(DeviceWHQuery):
        PARAMETERS = ["seat0.grammar_entry","seat1.grammar_entry","seat2.grammar_entry","seat3.grammar_entry"]
        def perform(self,s0,s1,s2,s3):
            res = [s0,s1,s2,s3]
            res =filter(None,res)
            ACTIVE_STATE.updateSeat("DRIVER",s0)
            ACTIVE_STATE.updateSeat("PASSENGER",s1)
            ACTIVE_STATE.updateSeat("BACKSEAT0",s2)
            ACTIVE_STATE.updateSeat("BACKSEAT1",s3)
            ACTIVE_STATE.sendCarState()
            output = ""
            if(len(res) > 1):
              res.insert(-1,"and")
            c = 0
            for r in res:
              if c >= (len(res) -3):
                 output += (" " +r)
              else:
                output += (" " +r+ ",")
              c = c+1
            #output  = ", ".join(res) 
            car_entity = {
                "grammar_entry": output,           
            }
            return [car_entity]

        
    class ulist(DeviceWHQuery):
      def perform(self):
        ret = []
        for u in  ACTIVE_STATE.getUsersDic().keys():
          name = ACTIVE_STATE.getUsersDic()[u].getName()
          if not name.isdigit():  #Filtering out all samlple data to just get a list of real users
            t = {
              "grammar_entry":name,
#              "value":name
            }
            ret.append(t) 
        return ret

#  SEAT VERIFICATION

    class seat0(DeviceWHQuery):
      def perform(self):
        return self.device.seatver("DRIVER")
          
    class seat1(DeviceWHQuery):
      def perform(self):
        return self.device.seatver("PASSENGER")

    class seat2(DeviceWHQuery):
      def perform(self): 
        return self.device.seatver("BACKSEAT0")

    class seat3(DeviceWHQuery):
      def perform(self):
        return self.device.seatver("BACKSEAT1")

    def seatver(self,seat): 
      u,conf = ACTIVE_STATE.getSeatUser(seat)
      if u == "0":
        ent = {
          "grammar_entry":"",
          "confidence":0,
        }
      elif u =="":
        ent = {
          "grammar_entry":"",
          "confidence":1.0,
        }
      else:
        ent = {
          "grammar_entry":ACTIVE_STATE.getUsersDic()[u].getName(),
          "confidence":conf,
        } 
      return [ent]

#    class car_seatRecognizer(EntityRecognizer):
#        def recognize_entity(self, string):
#            result = []
#            words = string.lower().split()
#            for word in words:
#                if not( word == "none"):
#                    recognized_entity = {
#                        "sort": "car_seat",
#                        "grammar_entry": word.title()
#                    }
#                    result.append(recognized_entity)
#                else:
#                    recognized_entity = {
#                        "sort": "car_seat",
#                        "grammar_entry": ""
#                    }
#                    result.append(recognized_entity)
#            return result


# END SEAT VERIFICATION

#
#   Create User
#
    class CreateUser(DeviceAction):
      PARAMETERS = ["user_name.grammar_entry"]
      def perform(self,usr):
        createUser(usr)
        return True


#   Greet user device
    class GreetUser(DeviceAction):
        PARAMETERS = ["incar.grammar_entry"]
        #@send_to_frontend_device
        def perform(self,inc):
            client = mqtt.Client()
            client.connect("54.229.54.240", 1883, 60)
            client.publish("carai/car/dest","")
            client.loop(1) #timeout 1 sec
            return True
#   So Notify works
    class greetUser(DeviceAction):
        PARAMETERS = []
        def perform(self):
            return True

#   Connection to the MQTT network
    class MQTTInterface(DeviceAction):
        
        def __init__(self, dev,ont):
            self.device = dev
            self._ontology = ont
            global ACTIVE_STATE
            ACTIVE_STATE = State()

        parameters= []
        def perform(self):
            if not ACTIVE_STATE.isInited():
              def on_connect(client, userdata, rc):
                  print("Connected with result code "+str(rc))
                  client.subscribe("carai/talkamatic/user")
                  client.subscribe("carai/talkamatic/gps")
                  client.subscribe("carai/car/gps")
              def on_message(client, userdata, msg):
                  if msg.topic == "carai/talkamatic/user":
                    ACTIVE_STATE.importFromJSON(msg.payload) 
                    self.device.handler.notify_started("greetUser")
                  if msg.topic == "carai/talkamatic/gps":
                    ACTIVE_STATE.importDest(msg.payload)
                    self.device.handler.notify_started("setDest")
                  if msg.topic == "carai/car/gps":
                    ACTIVE_STATE.importGPS(msg.payload)
                       
              client = mqtt.Client()
              client.on_connect = on_connect
              client.on_message = on_message
              client.connect_async("54.229.54.240", 1883, 60)
              client.loop_start()
            return True

