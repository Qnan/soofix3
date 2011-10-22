__author__ = 'mikhail'

import sys
import index

if len(sys.argv) < 2:
    print 'not enough arguments'
    sys.exit(-1)

inputFile = sys.argv[1]
if len(sys.argv) > 2:
    num = int(sys.argv[2])
else:
    num = -1

f = open(inputFile, 'rt')
line = ""
doc = None
#docs = []
#ids = []
cnt = 0
id = -1
outf = open("topics.txt",'wt')
idf = open("topic-ids.txt",'wt')
for line in f:
    if num > 0 and cnt >= num:
        break
    if line[:2] == '.I':
        if id > 0:
            outf.write('\n')
            idf.write(str(id)+'\n')
            cnt += 1

        id = line[2:].strip()
    else:
        if len(line.strip()) > 0:
            word = line.split()[2] + '.' + line.split()[5]
            #word = line.split()[1].lower()
            outf.write(word + ' ')
f.close()
outf.close()
idf.close()

#idx = index.index(docs)
#
#clusters = idx.clusters()
#print type(clusters)
#f = open(outputFile,'wt')
#for cluster in clusters.keySet():
#    split = cluster.split('\n')
#    if len(split) > 2 and len(clusters[cluster]) > 2:
#        line = '.C '
#        line += ' &&'.join(split)
#        f.write(line + '\n')
#        f.write(' '.join([str(ids[i]) for i in clusters[cluster]]) + '\n')

f.close()
