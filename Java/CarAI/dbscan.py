# -*- coding: utf-8 -*-
"""
===================================
Demo of DBSCAN clustering algorithm
===================================

Finds core samples of high density and expands clusters from them.

"""
#print(__doc__)
import sys
import numpy as np

from sklearn.cluster import DBSCAN
from sklearn import metrics
from sklearn.datasets.samples_generator import make_blobs
from sklearn.preprocessing import StandardScaler


##############################################################################
# Generate sample data
#centers = [[1, 1], [-1, -1], [1, -1]]
#X, labels_true = make_blobs(n_samples=750, centers=centers, cluster_std=0.4,
#                            random_state=0)
#
#X = StandardScaler().fit_transform(X)
Y=[]
i = 0
f = open('coords.csv','r')
#alllines = f.readlines()

for line in reversed(f.readlines()):
    lines = line.split()
    Y = Y  + [[float(lines[0]), float(lines[1])]]    
    i = i+1
    if(i > int(sys.argv[3])):
        break


#print np.array(Y)
Y = np.array(Y)
#Y = StandardScaler().fit_transform(Y)
#print Y    
##############################################################################
# Compute DBSCAN
db = DBSCAN(eps=float(sys.argv[1]), min_samples=int(sys.argv[2])).fit(Y)
core_samples_mask = np.zeros_like(db.labels_, dtype=bool)
core_samples_mask[db.core_sample_indices_] = True
labels = db.labels_
unique_labels = set(labels)
of = open('clusterFile.csv','wb')

output = ""
for l in unique_labels:
    class_member_mask = (labels == l)
    xy = Y[class_member_mask & core_samples_mask]
    for coord in xy:
        output = output + ('{0} {1} {2}\n'.format(l,coord[0],coord[1]))
        of.write('{0} {1} {2}\n'.format(l,coord[0],coord[1]))
    xy = Y[class_member_mask & ~core_samples_mask]
    for coord in xy:
        output = output + ('{0} {1} {2}\n'.format(l,coord[0],coord[1]))
        of.write('{0} {1} {2}\n'.format(l,coord[0],coord[1]))
    of.write('\n')

of.close()
print(output)

