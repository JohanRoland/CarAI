import sys

f = open(sys.argv[1],'r')
alllines = f.readlines()
Y =[]
for line in alllines:
    lines = line.split()
    Y = Y  + [[float(lines[0]), float(lines[1])]]

f.close()
    
of = open('testELKI.txt','wb')
for a in Y:
    of.write('{0},{1}\n'.format(a[0],a[1]))

of.close()