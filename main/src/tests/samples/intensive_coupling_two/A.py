import sys

import os as operating
from B import B

#intensive coupling 
# Goal of this package is to have multiple II relations between the classes. A <-> B  C <-> D and B <-> D
class A:
    
    id = 25
    name = "skdgkg" 
    _namee = "hiddden" 
    numberA = 23232
    sister = "A'"
    b = B()

    
    def __init__(self):
        sister = b.name
        

    def stuff(self):
        name = b.name
        
        return name + " " 

    def more_stuff(self):
        
        return b.do_more_stuff() + b._protBInt