from cls import C

class D(C):
    
    def __init__(self):
        # Python 3 style
        super().substitution(1, 5)
        
        
    
    # override
    def substitution(self, a, b):
        return (a - b) + 1
    
    def addition(self, a,b):
        add(a,b)
    
    def add (self, a, b): 
        return a + b