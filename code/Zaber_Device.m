classdef Zaber_Device < Device
    %Zaber_Device Summary
    % Class for controlling TLA Zaber linear actuator device
    % Z is defined in mm
    %   Written by Nicolas Chiaruttini on 06/2011
    %   Modified on 12/04/2012 to implement 'Device' interface
    %   Height is defined in millimeters
    %   COM4=Right Zaber
    %   COM5=Left Zaber
    % Commands example :
    %       R_ZA=Zaber_Device('Right','COM4',9600);
    %       R_ZA=R_ZA.init;
    %       R_ZA.setZA(20);
    %       R_ZA.setZA(0);
    %       R_ZA.home;
    %       delete(R_ZA);
    %
    
    % I set the default resolution at 64
    
    
    properties
        % ----- Hardware Properties
            % ----- Communication with computer
            port; % Com port (serial port) connected to Zaber
            BaudRate; % 9600 for Zaber apparently
            s_id; % id of serial port communication
            dev_num=1; % Useful ?
            MicroStepResolution;
            Speed;

        
        % ----- Device Status = Z position
        Z0; % Relative origin of the micromanipulation device, set by user
            % should be accessed via setz0 and getz0 functions
        
        CurrZ;
        delayTrack;
        trackTimer;
        Zstep_Size; % To do zstep
        
        
        
        % ----- GUI properties
        slider_ZA; % Used to show current absolute Z position, and also to set it
        edit_Z0;   % Used to show current Z origin, and also to set it
        edit_ZR;   % Used to show current relative Z position, and also to set it
        edit_ZStep;   % Used to show current relative Z position, and also to set it
        edit_ZSpeed;
        
        % CONSTANT but cannot be static because of Matlab
            size_per_step=0.00009921875; % step size in millimeter according to TLA doc (http://www.zaber.com/documents/ZaberT-SeriesProductsTechnotes.pdf)
            MAXZ=58; % Maximal height size in mm
            MINZ=0;
            % 3072 Microsteps per revolution, 48 Steps per revolution
    end
    

    
    events
        Z0redefined;
        ZMoved;
        ZOutOfRange;
        ZTracked;
    end
    
    methods
         % ---------- Constructor of the device Zaber
         function self=Zaber_Device(name_identifier,pos,p,BR)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,pos);
            
            % set communication device properties
            self.BaudRate=BR;
            self.port=p;
            self.init;

            
            % reference Z position
            self.Z0=0;
            self.Zstep_Size=1; % 1 mm by default
            self.CurrZ=self.askZA(); % Initilyze Z pos
            self.MicroStepResolution=64;
            self.setResolution(self.MicroStepResolution);
            self.Speed=60; % in mm/min
            self.setSpeed(self.Speed);
            self.setDeviceMode(128+16);
            self.buildGui();
            self.updateGuiState();
            
            % set listener for this device
            addlistener(self,'Z0redefined',@Zaber_Device.listen_Z0redefined);
            addlistener(self,'ZMoved',@Zaber_Device.listen_ZMoved);
            addlistener(self,'ZTracked',@Zaber_Device.listen_ZTracked);
            
            self.delayTrack=0.1/2; % in s, a lower value couls lead to over-run error ?
            self.trackTimer=timer('Period',self.delayTrack,'ExecutionMode','fixedSpacing','TimerFcn',{@(src,event)self.trackZaber(src,event)});
            start(self.trackTimer);
         end
                  
         % ---------------------------------------------
         % ------------- DEVICE COMMANDS ---------------
         % ---------------------------------------------
         
         % ---------- Initialise serial port communication
         function init(self) % initialise serial port communication device
            s = serial(self.port);            
            set(s,'BaudRate',self.BaudRate,'terminator','CR');
            fopen(s);
            self.s_id=s;
         end
         
         % ---------- Really ask the current position
         function res=askZA(self)
                command=uint8(53); % Ask - Cmd 53
                %Zp=int32(z/self.size_per_step); % Pos translated in microstep number
                %To have the Zp in micron: z=NMicrosteps*self.size_per_step
                %if (Zp>-1)&&(Zp<(self.MAXZ/self.size_per_step)) % Check if command is in range, 58 is arbitrary\
                %self.CurrZ=z-self.Z0;
                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,uint32(45),'int32'); % current pos - Cmd 45
                     fread(self.s_id,1,'uint8');  % Device Number
                     fread(self.s_id,1,'uint8'); % Command Number
                     DATA=fread(self.s_id,1,'int32'); % Data
                 res=DATA*self.size_per_step;
         end
         
         % ---------- Stop all current movements
         function STOP(self)
                command=uint8(23); % Set Microstep Resolution Command
                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,uint32(0),'int32');
         end
         
         % ---------- Set microstep resolution
         function setResolution(self, value)
                command=uint8(37); % Set Microstep Resolution Command
                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,uint32(value),'int32');
         end
         
         % ----------- Set Speed
         %  - Speed in mm/s
         function setSpeed(self, value)
                speedcode=value/(9.375*self.size_per_step)/60;
                command=uint8(42); % Set Speed Command
                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,uint32(speedcode),'int32');
                self.Speed=round(speedcode)*(9.375*self.size_per_step)*60;
         end
         
         function res=askDeviceMode(self)
                command=uint8(53); % Ask - Cmd 53
                %Zp=int32(z/self.size_per_step); % Pos translated in microstep number
                %To have the Zp in micron: z=NMicrosteps*self.size_per_step
                %if (Zp>-1)&&(Zp<(self.MAXZ/self.size_per_step)) % Check if command is in range, 58 is arbitrary\
                %self.CurrZ=z-self.Z0;
                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,uint32(40),'int32');
                     fread(self.s_id,1,'uint8');  % Device Number
                     fread(self.s_id,1,'uint8'); % Command Number
                     DATA=fread(self.s_id,1,'int32'); % Data
                 res=DATA;
         end
         
         function setDeviceMode(self,value)
                command=uint8(40); % Set device mode - Cmd 40
                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,uint32(value),'int32');
         end
         
         % ---------- Track Zaber, reads continuously RS232 output
         function trackZaber(ZB,src,event)
         %   if (MP.wait_for_end_of_command==1)
                if (ZB.s_id.BytesAvailable>1)
                     ND=fread(ZB.s_id,1,'uint8');  % Device Number
                     CMD=fread(ZB.s_id,1,'uint8'); % Command Number
                     DATA=fread(ZB.s_id,1,'int32'); % Data
                     if (CMD==8)
                         
                         self.last_event_clock=clock;
                         % Move Tracking
                         ZB.CurrZ=DATA*ZB.size_per_step;
                         % disp(['Automated displacement, ZA=' num2str(ZB.CurrZ) ' mm ZR=' num2str(ZB.CurrZ-ZB.Z0) ' mm']);
                         notify(ZB,'ZMoved');
                     end
                     if (CMD==20)
                         
                         self.last_event_clock=clock;
                         % Automated displacement
                         ZB.CurrZ=DATA*ZB.size_per_step;
                         % disp(['Automated displacement, ZA=' num2str(ZB.CurrZ) ' mm ZR=' num2str(ZB.CurrZ-ZB.Z0) ' mm']);
                         notify(ZB,'ZMoved');
                     end
                     if (CMD==10)
                         
                         self.last_event_clock=clock;
                         % Manual displacement
                         ZB.CurrZ=DATA*ZB.size_per_step;
                         % disp(['Manual displacement, ZA=' num2str(ZB.CurrZ) ' mm ZR=' num2str(ZB.CurrZ-ZB.Z0) ' mm']);
                         notify(ZB,'ZMoved');
                     end
                     
                end
                notify(ZB,'ZTracked');
          %  end
         end
         
         % ---------- Close serial port communication upon device deletion
         function closeDevice(self)
                stop(self.trackTimer);
                selection = questdlg(['Return ' self.deviceName ' to home position ?'],...
                                'Return home function',...
                                'Yes','No','No'); 
                switch selection, 
                case 'Yes'
                           self.home();
                case 'No'
                end
                fclose(self.s_id);
         end
         
         % --------- Redefine Z0 of Zaber
         function setZ0(self,zi)
             self.last_event_clock=clock;
             self.Z0=zi;
             notify(self,'Z0redefined');
         end
         
         % --------- Get Z0 of Zaber
         function res=getZ0(self)
             res = self.Z0;
         end
         
         % --------- Get ZA of Zaber
         function res=getZA(self)
            res = self.CurrZ;
         end
         
         % --------- Get ZR of Zaber
         function res=getZR(self)
             res = self.CurrZ-self.Z0;
         end
   
         % ---------- Send command to set absolute Z value position of the device
         function setZA(self, z) % set the absolute position of linear actuator      
            command=uint8(20); % Move Absolute - Cmd 20
            Zp=int32(z/self.size_per_step); % Pos translated in microstep number
            if (Zp>-1)&&(Zp<(self.MAXZ/self.size_per_step)) % Check if command is in range, 58 is arbitrary\
                %self.CurrZ=z;%-self.Z0;
                self.last_event_clock=clock;

                fwrite(self.s_id,self.dev_num,'uint8');
                fwrite(self.s_id,command,'uint8');
                fwrite(self.s_id,Zp,'int32');              
            else
                notify(self,'ZOutOfRange');
            end
         end
         
         % ---------- Send command to set relative (z0) Z value position of the device
         function setZR(self, z) % set the relative position of linear actuator
             self.setZA(z+self.Z0);
         end
         
         % ---------- Send command to return to home position to the device
         function home(self) % return to home
             command=uint8(1); % Home - Cmd 1
             data=int32(0); % data = 0 for home position
             fwrite(self.s_id,self.dev_num,'uint8');
             fwrite(self.s_id,command,'uint8');
             fwrite(self.s_id,data,'int32');
             % empty buffer
         end
         
         % ---------------------------------------------
         % ------------- GUI COMMANDS ------------------
         % ---------------------------------------------
         
         % ---------- Builds GUI for Zaber
         function buildGui(self)
                figure(self.hDeviceGUI);
                self.slider_ZA=uicontrol('Style', 'slider',...
                          'Min',self.MINZ,'Max',self.MAXZ,'Value',0,...
                          'units','normalized',...
                          'Position', [0 0 0.2 1],...
                          'Callback', {@(src,event)self.sliderCB(src,event)});
                % Z label
                USY=0.11;
                
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-USY 0.2 USY],...
                          'string','Z (mm)');
                      
                % Edit Z Pos
                self.edit_ZR=uicontrol('style','edit',...
                                       'units','normalized','position',[0.4 1-USY 0.6 USY],...%,...
                                       'callback',{@(src,event)self.setPosCB(src,event)}); %an edit box
                %Z0 label                
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-2*USY 0.2 USY],...
                          'string','Z0 (mm)');
                      
                % Edit Z0
                self.edit_Z0=uicontrol('style','edit',...
                                       'units','normalized','position',[0.4 1-2*USY 0.6 USY],...%,...
                                       'callback',{@(src,event)self.setZ0CB(src,event)}); %an edit box
                % Set current Z as Z0
                uicontrol('Style','Push','String','Set current Z as Z0',...
                          'FontSize',14,'units','normalized', 'Position', [0.2 1-3*USY 0.8 USY],...
                          'callback',{@(src,event)self.setCurrZasZ0CB(src,event)}); % Plus one step Z
                % Goto Z0
                uicontrol('Style','Push','String','Goto Z0',...
                          'FontSize',14,'units','normalized', 'Position', [0.2 1-4*USY 0.8 USY],...
                          'callback',{@(src,event)self.gotoZ0CB(src,event)}); % Plus one step Z 
                % Step size
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-5*USY 0.2 USY],...
                          'string','Step Size (mm)');
                      
                % Edit Step Size
                self.edit_ZStep=uicontrol('style','edit',...
                                       'units','normalized','position',[0.4 1-5*USY 0.6 USY],...%,...
                                       'callback',{@(src,event)self.setStepSizeCB(src,event)}); %an edit box
                % Label
                % Editor
                % Button UP
                uicontrol('Style','Push','FontName','symbol','String',char(173),...
                    'FontSize',14,'units','normalized', 'Position', [0.2 1-6*USY 0.8 0.1],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,1)}); % Plus one step Z
                % Button DOWN
                uicontrol('Style','Push','FontName','symbol','String',char(175),...
                    'FontSize',14,'units','normalized', 'Position', [0.2 1-7*USY 0.8 0.1],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,-1)});% Minus one step Z
                % Current SPEED
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-8*USY 0.5 USY],...
                          'string','Speed (mm/min)');                      
                % Edit Speed
                self.edit_ZSpeed=uicontrol('style','edit',...
                                        'units','normalized','position',[0.7 1-8*USY 0.3 USY],...%,...
                                        'callback',{@(src,event)self.setSpeedCB(src,event)}); %an edit box
                % STOP button
                uicontrol('Style','Push','String','STOP',...
                          'FontSize',14,'units','normalized', 'Position', [0.2 1-9*USY 0.8 USY],...
                          'callback',{@(src,event)self.stopCB(src,event)}); % Stop all movements
                
         end
         
         % ---------- Stop button callback
         function stopCB(self, src, event)
             self.STOP();
         end
         
         % ---------- Do a step movement for Zaber
         function doStepMovementCB(self,src,event,dir)
             curPos=self.getZA();
             %self.CurrZ=curPos+dir*self.Zstep_Size;
             self.setZA(curPos+dir*self.Zstep_Size);
         end
                  
         % ---------- Set Current ZPos as Z0
         function setCurrZasZ0CB(self,src,event)
             curPos=self.getZA();
             self.setZ0(curPos);
         end
                  
         % ---------- Set Step Size CB
         function setStepSizeCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.Zstep_Size=H;
            end
            self.updateGuiState();   
         end
         
         % ---------- Set Speed CB
         function setSpeedCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
               %self.Speed=H;
               self.setSpeed(H);
            end
            self.updateGuiState();   
         end
         
         % ---------- Goto Z0 CB
         function gotoZ0CB(self,src,event)             
                self.setZR(0);
         end
         
         % ---------- Slider CallBack: if the user moves the slider
         function sliderCB(self,src,event)
             Zvalue=get(src,'Value');
             self.setZA(Zvalue);
         end
         
         % ---------- Set Pos CB
         function setPosCB(self,src, event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setZR(H);
            end
            self.updateGuiState();
         end
         
         % ---------- Set Z0 CB
         function setZ0CB(self,src, event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setZ0(H);
            end
            self.updateGuiState();
         end
         
         % ---------- Write Log Header file
         function writeLogHeader(self)
             fprintf(self.hLogFile, ['Height is in mm.' char(13)]);
         end
         
         function startDevAcqu(self)
         end
         
         function stopDevAcqu(self)
         end
         
         % ---------- Updates all the Gui according to the current device
         % state
         function updateGuiState(self)             
           set(self.slider_ZA,'Value',self.CurrZ);
           set(self.edit_ZR,'String',num2str(self.CurrZ-self.Z0));
           set(self.edit_Z0,'String',num2str(self.Z0));
           set(self.edit_ZStep,'String',num2str(self.Zstep_Size));
           set(self.edit_ZSpeed,'String',num2str(self.Speed));
           zs_pc=(self.Zstep_Size/(self.MAXZ-self.MINZ));
           set(self.slider_ZA,'SliderStep',[zs_pc zs_pc]);
         end
         
    end
         % ---------------------------------------------
         % ------------- LISTENERS=static methods ------
         % ---------------------------------------------
    methods(Static)
        % ---- Called if Z0 is redefined
        function listen_Z0redefined(src,event)
           %disp([src.stringEventHeader  char(9) 'Z0 redefined to' char(9)  num2str(src.Z0)]);
         %  if (src.isRecording==1)
         %      logline=[src.stringEventHeader  char(9) 'Z0 redefined to' char(9)  num2str(src.Z0)];
         %      src.saveLog(logline);
         %  end
           src.updateGuiState();
        end
        
        % ---- Called if Z is moved (manually or through the slider or
        % through a command)
        function listen_ZMoved(src,event)
           %disp([src.stringEventHeader  char(9) 'Z moved to' char(9)  num2str(src.CurrZ)]);
        %   if (src.isRecording==1)
        %        t=clock;
                %mes=[num2str(t(1)) ';' num2str(t(2)) ';' num2str(t(3)) ';' num2str(t(4)) ';' num2str(t(5)) ';' num2str(t(6)) ';' ];
        %        mes=[num2str(t(4)) char(9) num2str(t(5)) char(9) num2str(t(6))];
        %        logline=[mes  char(9) 'Z moved to' char(9)  num2str(src.CurrZ)];
        %       src.saveLog(logline);
        %   end
           src.updateGuiState();
        end
        
        function listen_ZTracked(src,event)
            if (src.isRecording==1)
                t=clock;
                %mes=[num2str(t(1)) ';' num2str(t(2)) ';' num2str(t(3)) ';' num2str(t(4)) ';' num2str(t(5)) ';' num2str(t(6)) ';' ];
                mes=[num2str(t(4)) char(9) num2str(t(5)) char(9) num2str(t(6))];
                logline=[mes  char(9) 'Z = ' char(9)  num2str(src.CurrZ)];
                src.saveLog(logline);
            end            
        end
    end
end

