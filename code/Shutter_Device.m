classdef Shutter_Device < Device
    % shutter device
    % Uses a digitalio output
    % usually:
    %   daqS='ni'; dev='Dev2'; port='Port0/Line7';
    % Line test:
% sd=Shutter_Device('DIC', [2150 950 400 200],'ni','Dev2','Port0/Line0',0,0);
    properties
        device;
        port;
        daqSession;
        ModeON; % 0 ou 1, user defined, 1 by default; ModeOFF is always 1-ModeON
        toggleButton_Shutter;
        currState;
    end
    
    events
        ShutterON;
        ShutterOFF;
    end
    
    methods
         % ---------- Constructor of the User Notification Device
         function self=Shutter_Device(name_identifier, position,daqS,dev,port,invert,iniState)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,position);
            %self.defaultString=' ';
           
            % initialise device with specified port
            self.daqSession = daq.createSession(daqS);
            self.daqSession.addDigitalChannel(dev, port, 'OutputOnly'); % Shutter is output
            
            self.ModeON=1-invert;
            
            self.buildGui();
            
            % set listener for this device
            addlistener(self,'ShutterON',@Shutter_Device.listen_ShutterON);
            addlistener(self,'ShutterOFF',@Shutter_Device.listen_ShutterOFF);
            self.currState=iniState;
            if (self.currState==1)
                self.shutterON;
            else
                self.shutterOFF; 
            end
         end
         
         % ---------------------------------------------
         % ------------- DEVICE COMMANDS ---------------
         % ---------------------------------------------
          % ---------- Initialise serial port communication
         
         % ---------- Close serial port communication upon device deletion
         function closeDevice(self)
                 self.daqSession.removeChannel(1);
         end
         
         function writeLogHeader(self)
         end
         
         function shutterON(self)
             self.currState=1;
             notify(self,'ShutterON');
             self.daqSession.outputSingleScan(self.ModeON);
         end
         function shutterOFF(self)
             self.currState=0;
             notify(self,'ShutterOFF');
             self.daqSession.outputSingleScan(1-self.ModeON);
         end
         
         % ---------------------------------------------
         % ------------- GUI COMMANDS ------------------
         % ---------------------------------------------
         
         % ---------- Builds GUI for Shutter
         function buildGui(self)
                figure(self.hDeviceGUI);
                % self.slider_ZA=uicontrol('Style', 'slider',...
                %           'Min',0,'Max',58,'Value',0,...
                %           'Position', [20 20 20 460],...
                %           'Callback', {@(src,event)self.sliderCB(src,event)});
                % 'Callback', {@surfzlim,hax}
                % FINE/COARSE Switch
                self.toggleButton_Shutter = uicontrol('style','togglebutton',...
                                                      'units','normalized', 'position',[0 0 1 1],...
                                                      'callback',{@(src,event)self.toggleShutterCB(src,event)});
                self.updateGUI;
         end
         function toggleShutterCB(self,src,event)
             if (self.currState==0)
                 self.shutterON;
             else
                 self.shutterOFF;
             end
         end
         function updateGUI(self) 
            % Initialize value
                if (self.currState==1)
                    set(self.toggleButton_Shutter,'string','ON');
                    set(self.toggleButton_Shutter,'Value',1);
                else
                    set(self.toggleButton_Shutter,'string','OFF');
                    set(self.toggleButton_Shutter,'Value',0);
                end
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
        function listen_ShutterON(src,event)
           %if (src.isRecording==1)
           %    logline=[src.stringEventHeader  char(9) 'SHUTTER ON'];
           %    src.saveLog(logline);
%
        %   end
           src.updateGUI();
        end
        
        function listen_ShutterOFF(src,event)
         %  if (src.isRecording==1)
        %       logline=[src.stringEventHeader  char(9) 'SHUTTER OFF'];
       %        src.saveLog(logline);
%
        %   end
           src.updateGUI();
        end
    end
    
end

