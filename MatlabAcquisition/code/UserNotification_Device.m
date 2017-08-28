classdef UserNotification_Device < Device
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        % ------ For GUI
        et; %edit text
        message;
        defaultString;
    end
    
    events
           newUserNotification;
    end 
    methods
         % ---------- Constructor of the User Notification Device
         function self=UserNotification_Device(name_identifier, position)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,position);
            self.defaultString=' ';
            self.buildGui();
            
            % set listener for this device
            addlistener(self,'newUserNotification',@UserNotification_Device.listen_newUserNotification);
            % addlistener(self,'ZMoved',@Zaber_Device.listen_ZMoved);
                  
         end
        
        
         
        function closeDevice(self)
        end
        
        function buildGui(self)
            self.et = uicontrol(self.hDeviceGUI,'Style','edit',...
                        'String',self.defaultString,...
               'Position',[0 0 300 20],...
               'HorizontalAlignment','left',...
               'Callback', {@(src,event)self.newUNotification(src,event)}...
               );
            
        end
        
         function newUNotification(self,src,event)
             self.last_event_clock=clock;
             self.message=get(self.et,'String');
             notify(self,'newUserNotification');
             set(self.et,'String',self.defaultString);

         end
        
        function writeLogHeader(self)
        end
        
        function startDevAcqu(self)
        end
        
        function stopDevAcqu(self)
        end
    end
       methods(Static)
        function listen_newUserNotification(src,event)
               logline=[src.stringEventHeader  char(9) src.message];
               src.saveLog(logline);
        end
       end
    
end

