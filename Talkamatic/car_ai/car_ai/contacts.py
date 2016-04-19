import json
import math
from decimal import Decimal
import sys,os
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))))

from Python.DBConnection import User, getAllUsers

CONTACT_NUMBERS = {
    "John": "0701234567",
    "Lisa": "0709876543",
    "Mary": "0706574839",
    "Andy": None,
    }

LOCATIONS = {
	"HOME": ("57.533704","11.931163"),
	"WORK": ("57.696998","11.975205"),
}

USERS = {
  "" : "",
  "0" : "Unknown",
	"1" : "William",
	"2" : "Johan",
}


POI = {
  (57.53361,11.93116) : "home",
  (57.697032, 11.976045) : "techno creatives",
  (57.465172, 11.994394) : "gym",
  (57.674033, 11.936127) : "marklandsgatan",
  (57.489372, 12.073605) : "kungsbacka",
#  (57.562569, 11.949050) : "snipen",
  (57.685289,11.946477) : "home2",
  (57.699042,11.977489) : "mc donalds",
  (57.706636,11.979626) : "max",
  (57.699489,11.952700) : "burger king",
  (57.703559,11.964807) : "gym2",
  (57.700774,11.950863) : "store",
  (57.714492,11.972654) : "Tegner"
}

class State: 
  def __init__(self):
    self.INITED = False
    self.usersDic = getAllUsers()

  global CARSTATE	  
  CARSTATE = { 
    "DRIVER": ("",1),
    "PASSENGER": ("",1),
    "BACKSEAT0": ("",1),
    "BACKSEAT1": ("",1),
  }

  DESTINATION =""
  GPSDEST = (0,0)
  LOCATION = (0,0)


  def isInited(self): 
    if self.INITED:
      return True
    else:
      self.INITED = True
      return False    
  
  def updateSeat(self,seat,name):
    self.CARSTATE[seat] = name

  def getSeatUser(self,seat):
    return CARSTATE[seat]

  def getAllUsers(self):
    result = []
    for seat in CARSTATE.keys():
      if CARSTATE[seat][0] != "":
        result.append((self.usersDic[CARSTATE[seat][0]],CARSTATE[seat][1]))
    return result

  def getUsersDic(self):
    return self.usersDic

  def getCarstate(self):
    return CARSTATE

#	returns a list of all new users
  def importFromJSON(self,js_string):
    parsed_json = json.loads(js_string,parse_float=Decimal)
    for seat in CARSTATE.keys():
      x = parsed_json[seat]
      if x == [] and (not (CARSTATE[seat][0] == "")):
        print("emptied "+ seat)
        CARSTATE[seat] = ("",1.0)
      elif not (x == []):
        CARSTATE[seat] = (x[0],float(x[1]))
        if CARSTATE[seat][0] != "0":
          print("changed " + seat + " to " + self.usersDic[CARSTATE[seat][0]].getName() +" with confidence " + str(CARSTATE[seat][1]))
        else:
          print("changed " + seat + " to 'unknown' with confidence " + str(CARSTATE[seat][1]))


  def importDest(self,js_string):
    parsed_json = json.loads(js_string,parse_float=Decimal)
    out = ""
    comp =sys.maxint # 100000
    for loc in POI.keys():
      dist = self.distToPoint(loc,parsed_json)
      if dist< comp:
        comp = dist  
        out = POI[loc]
    self.DESTINATION = out
    self.GPSDEST = (float(parsed_json['lat']),float(parsed_json['lon']))
    return out

  def importGPS(self,js_string):
    parsed_json = json.loads(js_string,parse_float=Decimal)
    pos = (float(parsed_json['lat']),float(parsed_json['lon']))
    self.LOCATION = pos
    return pos

  def distToPoint(self,p1,p2):
    dx = float(p2["lon"])-p1[1]
    dy = float(p2["lat"])-p1[0]
    return math.sqrt(dx*dx+dy*dy)

