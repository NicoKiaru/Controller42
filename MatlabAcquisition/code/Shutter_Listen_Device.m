classdef Shutter_Listen_Device < Device
    % shutter device
    % Uses a digitalio output
    % usually:
    %   daqS='ni'; dev='Dev2'; port='Port0/Line7';
    % Line test:
    % sd=Shutter_Device('DIC', [2150 950 400 200],'ni','Dev2','Port0/Line0',0,0);
    properties
        % Device Properties
        device;
        port;
        daqSession;
        % Shows current listener state
        statusGUI;
        % Current State
        currState;
        % DelayBetweenTwoAcquisition
        delayBetweenAcqu;
        trackTimer;
    end
    
    events
        Shutter_Pos_Increased;
        Shutter_Pos_Decreased;
    end
    
    methods
         % ---------- Constructor of the User Notification Device
         function self=Shutter_Listen_Device(name_identifier, position,daqS,dev,port)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,position);            
            self.deviceType='SHUTTER_LISTEN';
            %self.defaultString=' ';
           
            % initialise device with specified port
            self.daqSession = daq.createSession(daqS);
            self.daqSession.addDigitalChannel(dev, port, 'InputOnly'); % Shutter is output
            self.currState=self.daqSession.inputSingleScan;
            self.buildGui();
            
            self.delayBetweenAcqu=0.02; % In s; = 50Hz
            %self.delayTrack=0.2; % in s, a lower value will lead to SP over-run error (see MP285 Manual)
            self.trackTimer=timer('Period',self.delayBetweenAcqu,'ExecutionMode','fixedSpacing','TimerFcn',{@(src,event)self.trackShutter(src,event)});
            start(self.trackTimer);
            % ---------- Timer CB
            % function trackMP285(MP,src,event)
            % end
            % set listener for this device
            addlistener(self,'Shutter_Pos_Increased',@Shutter_Listen_Device.listen_Shutter_Pos_Increased);
            addlistener(self,'Shutter_Pos_Decreased',@Shutter_Listen_Device.listen_Shutter_Pos_Decreased);
         end
         
         function trackShutter(self,src,event)
             tamp=self.daqSession.inputSingleScan;
             if (tamp>self.currState)
                 self.currState=tamp;
                 notify(self,'Shutter_Pos_Increased');
             end
             if (tamp<self.currState)
                 self.currState=tamp;
                 notify(self,'Shutter_Pos_Decreased');
             end
         end
         
         % ---------------------------------------------
         % ------------- DEVICE COMMANDS ---------------
         % ---------------------------------------------
          % ---------- Initialise serial port communication
         
         % ---------- Close serial port communication upon device deletion
         function closeDevice(self)
                 stop(self.trackTimer);
                 delete(self.trackTimer);
                 self.daqSession.removeChannel(1);
                 
         end
         
         function writeLogHeader(self)
         end
         
      
         % ---------------------------------------------
         % ------------- GUI COMMANDS ------------------
         % ---------------------------------------------
         
         % ---------- Builds GUI for Shutter
         function buildGui(self)
                figure(self.hDeviceGUI);
                self.updateGUI;
         end

         function updateGUI(self) 
         end
        
         function startDevAcqu(self)
         end
         
         function stopDevAcqu(self)
         end
   
    end
    
    % ---------------------------------------------
    % ------------- LISTENERS=static methods ------
    % ---------------------------------------------
    methods(Static)        
        function listen_Shutter_Pos_Increased(src,event)
            src.updateGUI();
        end        
        function listen_Shutter_Pos_Decreased(src,event)
            src.updateGUI();
        end
    end
    
end

