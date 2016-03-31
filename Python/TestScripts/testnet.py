import paho.mqtt.client as mqtt
import time


Days = [1,2,3,4,5,6,7]
Times = ["08:00","12:00","17:00"]

client = mqtt.Client()
client.connect("54.229.54.240", 1883, 60)

for d in Days:
  client.publish("carai/day/set",d)
  client.loop(1)
  for t in Times:
    client.publish("carai/time/set",t)
    time.sleep(1)
    client.loop(5)
    client.publish("carai/car/dest","")
    time.sleep(1)
