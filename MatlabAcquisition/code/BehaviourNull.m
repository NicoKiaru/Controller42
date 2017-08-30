classdef BehaviourNull < Behaviour

    
    properties
 
    end
    
    methods
         function self=BehaviourNull(pos)
            self = self@Behaviour('Corse Behaviour',pos);
           
         end
         
         function execute(self)
            disp('Je me reveille ?');
         end
         
         function buildPanel(self,p)
            disp('Yopla, je vais te le builder ton panel;');
         end
         
         function interrupt(self)
            disp('ZZzzzzzz');
         end
 
    end
    

    
end

