classdef BehaviourTest < Behaviour
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        und;
        und_listener;
        t;
        timer_launched;
        message;
    end
    
    methods
         function self=BehaviourTest(userND)
            self.und=userND; % gives usernotification device
            self.timer_launched=0;
            self.message='Et Voila! Ca fait une seconde!';           
         end
         
         function exec(self)
             self.und_listener=addlistener(self.und,'newUserNotification',@self.listen_newUserNotification);
             self.t = timer('TimerFcn', {@(src,event)self.exec_after_delay(src,event)},'StartDelay',1);
         end
         
         function stop(self)
             delete(self.und_listener);
             if (self.timer_launched==1)
                stop(self.t);
                delete(self.t);
             end             
         end
         
         function listen_newUserNotification(self,src,event)
             if (self.timer_launched==1)
               disp('Nouvelle notification, arrive trop tot!');
             else
               disp('Vous avez recu une nouvelle notification');
               self.timer_launched=1;
               start(self.t);               
             end
         end
         
         function exec_after_delay(self,src,event)
             self.timer_launched=0;
             disp(self.message);
             stop(self.t);
         end
    end
    

    
end

