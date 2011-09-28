__author__ = 'mikhail'
import sys

sys.path.append('dist/soofix3.jar')

class index:
    def __init__(self, documents):
        from soofix3 import StringTree
        self.st = StringTree(documents)

    def add(self, document):
        pass

    def find(self, query):
        print self.st.find(query);

    def clusters(self):
        return self.st.clusters();


#idx = index([['o','a','b','c'],['a','b'],['o','a','b','c']])
#idx.find(['a','b'])
#print 'done'