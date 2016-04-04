#-*- coding: UTF-8 -*-
import json
import time
import urllib
import urllib2


maps_key = 'AIzaSyDYR5CdzexcBSV2a6aPWsd5JeIMt_S2lqo'

def dist(lat,lng,dlat,dlng):
  dist_base_url = 'https://maps.googleapis.com/maps/api/distancematrix/json'
   
  url = dist_base_url+ '?' + urllib.urlencode({
  'origins':"%s,%s" % (lat,lng),
  'destinations':"%s,%s" % (dlat,dlng),
  'key': maps_key,
  })
  result = send(url)
  return result['rows'][0]['elements'][0]['distance']['text'],result['rows'][0]['elements'][0]['duration']['text']

def locInfo(lat,lng): 
  dist_base_url = 'https://maps.googleapis.com/maps/api/geocode/json'
   
  url = dist_base_url+ '?' + urllib.urlencode({
  'latlng':"%s,%s" % (lat,lng),
  'key': maps_key,
  })
  result = send(url)
  temp =  result['results'][0]['formatted_address']
  temp = temp.replace(u'å','a')
  temp = temp.replace(u'ä','a')
  temp = temp.replace(u'ö','o')
  temp = temp.replace(u'Å','A')
  temp = temp.replace(u'Ä','A')
  temp = temp.replace(u'Ö','O')
  return temp

def send(url):
  current_delay = 0.1  # Set the initial retry delay to 100ms.
  max_delay = 3600  # Set the maximum retry delay to 1 hour.
  while True:
      try:
          # Get the API response.
          response = str(urllib2.urlopen(url).read())
      except IOError:
          pass  # Fall through the to the retry loop.
      else:
          # If we didn't get an IOError then parse the result.
          result = json.loads(response.replace('\\n', ''))
          if result['status'] == 'OK':
              return result 
          elif result['status'] != 'UNKNOWN_ERROR':
              # Many API errors cannot be fixed by a retry, e.g. INVALID_REQUEST or
              # ZERO_RESULTS. There is no point retrying these requests.
              raise Exception(result['error_message'])
      if current_delay > max_delay:
          raise Exception('Too many retry attempts.')
      print 'Waiting', current_delay, 'seconds before retrying.'
      time.sleep(current_delay)
      current_delay *= 2  # Increase the delay each time we retry.
  
 
def main():
    #d,t =  dist(57.696806, 11.976348,57.533629, 11.931114)
    #print("dist = %s" % d)
    #print("time = %s" % t)
    s = locInfo(57.696806,11.976348)
    print("street = %s" %s)
    
if __name__ == '__main__':
    main()
