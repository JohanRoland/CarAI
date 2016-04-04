
import MySQLdb


class User:
  def __init__(self,i= 0,n= "unknown"):
    self.UID = i
    self.Name = n
  
  def __str__ (self):
    return ("ID: %i, Name: %s" % (self.UID, self.Name)) 

  def getName(self):
    return self.Name

  def getId(self):
    return self.UID

###
#        Public methods
#


def getUser(ID):  
  data = execdb("CALL getUser(%s)" % ID,False)
  u = User(data[0],data[1])
  return u 

def getAllUsers():
  data = execdb("CALL getAllUsers()",True)
  users = {}
  for row in data:
    users[str(row[0])] = User(row[0],row[1]) 
  return users 
  
def execdb(cmd,getAll):
  db = MySQLdb.connect("54.229.54.240","johan","knarkapan","mydb")
  cursor = db.cursor()
  cursor.execute(cmd)
  if getAll:
    data = cursor.fetchall()
  else:
    data = cursor.fetchone()
  cursor.close()
  db.close()
  return data
  

def main():
  u = getAllUsers()
  for usr in u.keys():
    print(u[usr])

if __name__ == "__main__":
  main()

  
