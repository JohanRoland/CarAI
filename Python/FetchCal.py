from __future__ import print_function
import httplib2
import os
import sys

from apiclient import discovery
import oauth2client
from oauth2client import client
from oauth2client import tools

import datetime

import paho.mqtt.client as mqtt
import json

try:
    import argparse
    flags = argparse.ArgumentParser(parents=[tools.argparser]).parse_args()
except ImportError:
    flags = None

# If modifying these scopes, delete your previously saved credentials
# at ~/.credentials/calendar-python-quickstart.json
SCOPES = 'https://www.googleapis.com/auth/calendar.readonly'
CLIENT_SECRET_FILE = 'client_secret.json'
APPLICATION_NAME = 'CarAI calendar fetch'


def get_credentials(userID):
    """Gets valid user credentials from storage.

    If nothing has been stored, or if the stored credentials are invalid,
    the OAuth2 flow is completed to obtain the new credentials.

    Returns:
        Credentials, the obtained credential.
    """
    home_dir = os.path.expanduser('~')
    project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    user_dir = os.path.join(project_dir,'Data/Users') 
    user_dir2 = os.path.join(user_dir,'{}'.format(userID))
    credential_dir = os.path.join(user_dir2, '.credentials') #os.path.join(home_dir, '.credentials')
    if not os.path.exists(credential_dir):
        os.makedirs(credential_dir)
    credential_path = os.path.join(credential_dir,
                                  'user{}-cred.json'.format(userID))
    store = oauth2client.file.Storage(credential_path)
    credentials = store.get()
    if not credentials or credentials.invalid:
        flow = client.flow_from_clientsecrets(CLIENT_SECRET_FILE, SCOPES)
        flow.user_agent = APPLICATION_NAME
        if flags:
            credentials = tools.run_flow(flow, store, flags)
        else: # Needed only for compatibility with Python 2.6
            credentials = tools.run(flow, store)
        print('Storing credentials to ' + credential_path)
    return credentials

def getNextEvent(user):
    credentials = get_credentials(1)
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('calendar', 'v3', http=http)

    now = datetime.datetime.utcnow().isoformat() + 'Z' # 'Z' indicates UTC time
    #print('Getting the upcoming 10 events')
    eventsResult = service.events().list(
        calendarId='primary', timeMin=now, maxResults=10, singleEvents=True,
        orderBy='startTime').execute()
    events = eventsResult.get('items', [])
    
    if len(events) < 1:
      return events[0]
    else:
      return {} 

    
def main():
    """Shows basic usage of the Google Calendar API.

    Creates a Google Calendar API service object and outputs a list of the next
    10 events on the user's calendar.
    """
    if len(sys.argv) < 1:
      raise(NameError('No user added to path'))      
    
    credentials = get_credentials(2)
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('calendar', 'v3', http=http)

    now = datetime.datetime.utcnow().isoformat() + 'Z' # 'Z' indicates UTC time
    print('Getting the upcoming 10 events')
    eventsResult = service.events().list(
        calendarId='primary', timeMin=now, maxResults=10, singleEvents=True,
        orderBy='startTime').execute()
    events = eventsResult.get('items', [])

    if not events:
        print('No upcoming events found.')
    
    client = mqtt.Client()
    client.connect("54.229.54.240", 1883, 60)
    for event in events:
        start = event['start'].get('dateTime', event['start'].get('date'))
        loc = 'unknown'
        if 'location' in event:
          loc = event['location']
        client.publish("carai/usr/cal",json.dumps(event))
        client.loop(1) #timeout 1 sec
        print(start, event['summary'],loc)


if __name__ == '__main__':
    main()
