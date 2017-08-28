classdef MP_285_Device < Device
    % Class for controlling MP-285 Sutter micromanipulation device
    %   Written by Nicolas Chiaruttini on 05/2011
    % Commands example :
    %   R_MP=MP_285('COM8',9600);R_MP=R_MP.init; // Initialise rigth
    %   micormanipulation
    %   L_MP=MP_285('COM9',9600);L_MP=L_MP.init; // Initialise left
    %   micormanipulation
    %   R_MP.P0=R_MP.getPosA;L_MP.P0=L_MP.getPosA; // actual position=new
    %   origin
    %   L_MP.setV(10,'f'); R_MP.setV(10,'f'); // 10 micron per sec
    %   R_MP.setPosR(0,35,0);L_MP.setPosR(0,35,0); // synchronous command
    %   displacement
    %   R_MP.empty_buffer;L_MP.empty_buffer; // empty buffer
    %   R_MP.close;L_MP.close; // close serial communication
    % Currently COM8 and COM10: Left and Right
    
    properties
        port;       %='COM1';
        BaudRate;   %='9600';
        
        V;          % Current speed displacement of micromanipulation device
        Vmode;
        s_id;       % serial port id
        available;  % flag (software handled) defined that mark is the device can receive commands
        wait_for_end_of_command;    % flag equals 1 if a displacement is going on
        trackTimer; % track MP changes every delayTrack seconds
        delayTrack;
        
        P0=[0;0;0]; % Relative origin of the micromanipulation device
        RP=[0;0;0];  % relative position
        AP=[0;0;0];  % absolute position in microns
        stepDistance; % Distance of one step (software handled), in micron
        
        % ___________ GUI Objects ________________
        toggleButton_FineCoarse;
        editSpeedBox;
        labelStatusFineCoarse;
        labelStatusIdleBusy;
        listBoxPos;
        buttonGoto;
        buttonDel;
        buttonStore;
        relativePosGUI;
        absolutePosGUI;
        editSpeedStatusGUI;
        editDistanceBox;
        invertXBox;invertYBox;invertZBox;
        iX,iY,iZ;
    end
    
    events
        P0redefined;    % Redefinition of the micromanipulator origin 
        PMoved;         % Request for changing position
        VRedefined;     % Redefinition of current speed
        PMeasured;      % Current position measured
        IsBusy;         % Event request : is busy
        IsAvailable;    % End of event : is available
        Stop;           % Stop movement requested
    end
    
    methods
        
         % ---------- Constructor of the device MP285
         function self=MP_285_Device(name_identifier,pos,p,BR,inX,inY,inZ)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,pos);            
            self.deviceType='MP_285';
            self.P0 = [0;0;0];
            self.iX=inX;
            self.iY=inY;
            self.iZ=inZ;
            % set communication device properties
            self.BaudRate=BR; % set serial port communication properties
            self.port=p;
            self.init(); % initialise serial port communication
            self.available=1;
            self.V=10;
            self.Vmode='c';
            self.stepDistance=10; %(microns
            % reference Z position
            % self.Z0=0;
            self.delayTrack=0.2; % in s, a lower value will lead to SP over-run error (see MP285 Manual)
            self.trackTimer=timer('Period',self.delayTrack,'ExecutionMode','fixedSpacing','TimerFcn',{@(src,event)self.trackMP285(src,event)});
            start(self.trackTimer);
            
            % draws GUI
            self.buildGui();
            
            % set listener for this device
             addlistener(self,'P0redefined',@MP_285_Device.listen_P0redefined);
             addlistener(self,'PMoved',@MP_285_Device.listen_PMoved);
             addlistener(self,'VRedefined',@MP_285_Device.listen_VRedefined);
             addlistener(self,'PMeasured',@MP_285_Device.listen_PMeasured);
             addlistener(self,'IsAvailable',@MP_285_Device.listen_IsAvailable);
             addlistener(self,'IsBusy',@MP_285_Device.listen_IsBusy);
             addlistener(self,'Stop',@MP_285_Device.listen_Stop);       
             self.setV(self.V,self.Vmode);
             notify(self,'IsAvailable');
         end        
        
        % ---------------------------------------------
        % ------------- DEVICE COMMANDS ---------------
        % ---------------------------------------------
        
        % ---------- Initialise serial port communication        
        function init(MP)
            s = serial(MP.port);            
            set(s,'BaudRate',MP.BaudRate,'terminator','CR');
            fopen(s);
            MP.s_id=s;
        end        
        % ---------- Closes serial port communication 
        function close(MP) % closes serial port
            stop(MP.trackTimer);
            fclose(MP.s_id);
        end
        % ---------- Returns absolute position of the micromanipulator
        % no input
        % output= absolute position (X,Y,Z) in microns
        function [POS]=getPosA(MP) % returns absolute position of the micromanipulator
            if (MP.available==1)
                fprintf(MP.s_id,'c');          
                POS=fread(MP.s_id,3,'int32'); % read position, 3 signed long int
                fread(MP.s_id,1,'uchar'); % Read End of line character = empty buffer
                POS=POS*0.04; % convert position from microsteps -> microns
                MP.AP=[POS(1);POS(2);POS(3)];
                MP.RP=MP.AP-MP.P0;
                MP.last_event_clock=clock;
                notify(MP,'PMeasured');
            else
                disp('Device not available, command dropped.');
            end
        end
        % ---------- Set displacement speed of the micromanipulator 
                              % -> V is the velocity in micron/s,
                              % -> m is the mode of displacement ('f' =
                              % fine, 'c' = coarse)
        function setV(MP,V,m)
           if (MP.available==1)
                command=uint8(86); % 86=ASCII Code for 'V'
                Velocity=uint16(V); % signed int 32 velocity in micron/s (=> minimal speed=1 micron/s?)
                end_command=uint8(13);
                if (m=='f') % coarse mode : highest bit set to 1
                    Velocity=bitor(Velocity,uint16(32768));
                end
                fwrite(MP.s_id,command,'uint8');
                fwrite(MP.s_id,Velocity,'uint16');
                fwrite(MP.s_id,end_command,'uint8');
                fread(MP.s_id,1,'uchar'); % Read End of line character = empty buffer
                MP.last_event_clock=clock;
                MP.V=V;
                MP.Vmode=m;
                notify(MP,'VRedefined');
           else
                disp('Device not available, command dropped.');
           end
        end
        % ---------- Set the absolute position of the micromanipulator with respect to the previous defined velocity (setV)
        % must use empty buffer afterwards
        % (not included in the function for synchronization's sake)
        function setPosA(MP, x, y, z)
           if (MP.available==1)
                notify(MP,'IsBusy');
                command=uint8(109); % 109=ASCII Code for 'm'
                Xp=int32(x/0.04);Yp=int32(y/0.04);Zp=int32(z/0.04); % Pos translated in microstep number
                end_command=uint8(13);
                fwrite(MP.s_id,command,'uint8');
                fwrite(MP.s_id,Xp,'int32');fwrite(MP.s_id,Yp,'int32');fwrite(MP.s_id,Zp,'int32');
                fwrite(MP.s_id,end_command,'uchar');
                MP.wait_for_end_of_command=1;
                MP.last_event_clock=clock;
                notify(MP,'PMoved');
           else
                disp('Device not available, command dropped.');
           end
        end
        % ---------- Wait For availability
        function waitForIdle(MP)
            delay = 0.200;  % 10 milliseconds
            while (MP.available==0)  % set by the callback
                pause(delay);  % a slight pause to let all the data gather
            end
            pause(delay);
        end
        % ---------- Read End of line character = empty buffer
        function empty_buffer(MP)
            fread(MP.s_id,1,'uchar'); % Read End of line character = empty buffer
        end
        % ---------- Timer CB
        % Main fucntion is to set MP285 available when the current movement
        % is finished
        function trackMP285(MP,src,event)
         %   if (MP.wait_for_end_of_command==1)
                if (MP.s_id.BytesAvailable==1)

                     fread(MP.s_id,1,'uchar'); % Read End of line character = empty buffer
                     notify(MP,'IsAvailable');
                end
          %  end
        end
        % ---------- Read End of line character = empty buffer
        % retries every 0.1s
        function empty_buffer_async(MP)
            while (MP.s_id.BytesAvailable==0) % good function to know is device is ready 
                pause(0.1);
            end
            fread(MP.s_id,1,'uchar'); % Read End of line character = empty buffer
            MP.wait_for_end_of_command=0;
            MP.available=1;
        end
        
        function doStepMove(self,x,y,z)
             disp('fetching current position');
             self.AP=self.getPosA();
             disp('Ok');
             pause(0.1);
             invX=get(self.invertXBox,'Value');
             invX=(0.5-invX)/0.5;
             invY=get(self.invertYBox,'Value');
             invY=(0.5-invY)/0.5;
             invZ=get(self.invertZBox,'Value');
             invZ=(0.5-invZ)/0.5;
             %self.AP(1)+x*self.stepDistance
             %self.AP(2)+y*self.stepDistance
             %self.AP(3)+z*self.stepDistance
             disp('Sending order for next position');
             self.setPosA(self.AP(1)+x*self.stepDistance*invX,...
                          self.AP(2)+y*self.stepDistance*invY,...
                          self.AP(3)+z*self.stepDistance*invZ);
             disp('Ok');
        end
        % ---------- Stops micromanipulator movement
        function stop(MP)
            MP.last_event_clock=clock;
            notify(MP,'Stop');
            command=uint8(3); % stop code
            fwrite(MP.s_id,command,'uint8');
            fread(MP.s_id,1,'uchar'); % Read End of line character = empty buffer
        end
        
        % ---------------------------------------------
        % --------- END OF DEVICE COMMANDS ------------
        % ---------------------------------------------
        
        % Functions added on 12/04/20
        function status=isAvailable(MP)
            status=self.available;
        end   
        % ----- [Derived from Device class]
        function closeDevice(self)
            self.close(); % closes serial port communication
        end
        % ----- [Derived from Device class]
        function buildGui(self)
      
            % PANEL STEPPED MOVEMENTS
                hp_StepMoves = uipanel('Title','Step Moves','FontName','MS Sans Serif','FontSize',10,...
                            'BackgroundColor','white',...
                            'Position',[0 0 .5 1]);
                % ARROW BOX
                smp_SX=.22;smp_SY=.12; % Size of one arrow button
                smp_offsetPX=.3*smp_SX;smp_offsetPY=.3*smp_SY; % Bottom right position of the 'arrow button' box
                smp_offsetArrowY=1.5;
                uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String',char(173),...
                    'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+smp_SX smp_offsetPY+(2+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,0,1,0)}); % Plus one step Y
                uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String',char(175),...
                    'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+smp_SX smp_offsetPY+(0+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,0,-1,0)});% Minus one step Y
                uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String',char(172),...
                    'FontSize',14,'units','normalized', 'Position', [smp_offsetPX smp_offsetPY+(1+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,-1,0,0)});% Minus one step X
                uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String',char(174),...
                    'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+2*smp_SX smp_offsetPY+(1+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,1,0,0)});% Plus one step X
                uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String',char(173),...
                    'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+3*smp_SX smp_offsetPY+(1.5+smp_offsetArrowY)*smp_SY smp_SX 1.5*smp_SY],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,0,0,1)});% Plus one step Z
                uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String',char(175),...
                    'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+3*smp_SX smp_offsetPY+(0+smp_offsetArrowY)*smp_SY smp_SX 1.5*smp_SY],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,0,0,-1)});% Minus one step Z
                % FINE/COARSE Switch
                self.toggleButton_FineCoarse = uicontrol('Parent',hp_StepMoves,'style','togglebutton',...
                                                    'units','normalized', 'position',[smp_offsetPX smp_offsetPY+(3+smp_offsetArrowY)*smp_SY 4*smp_SX smp_SY],...
                                                    'callback',{@(src,event)self.toggleFineCoarseCB(src,event)}); %an edit box
                % Label speed
                uicontrol('Parent',hp_StepMoves,'style','text','BackgroundColor','white',...
                          'units','normalized','position',[smp_offsetPX smp_offsetPY+(1)*smp_SY 4*smp_SX 0.5*smp_SY],...
                          'string','Speed (µm/s)');
                % Increase/Decrease Speed buttons
                 hMV = uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String','-',...
                        'FontSize',14,'units','normalized', 'Position', [smp_offsetPX smp_offsetPY smp_SX smp_SY],...
                        'callback',{@(src,event)self.subVButtonCB(src,event)});           % Plus one micron/s
                 hPV = uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String','+',...
                        'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+3*smp_SX smp_offsetPY smp_SX smp_SY],...
                        'callback',{@(src,event)self.addVButtonCB(src,event)});           % Plus one micron/s
                % Edit speed box
                self.editSpeedBox=uicontrol('Parent',hp_StepMoves,'style','edit',...
                                           'units','normalized','position',[smp_offsetPX+smp_SX smp_offsetPY 2*smp_SX smp_SY],...%,...
                                           'callback',{@(src,event)self.setVButtonCB(src,event)}); %an edit box            
                % Label Distance
                uicontrol('Parent',hp_StepMoves,'style','text','BackgroundColor','white',...
                          'units','normalized','position',[smp_offsetPX smp_offsetPY+(5+smp_offsetArrowY)*smp_SY 4*smp_SX 0.5*smp_SY],...
                          'string','Distance (µm)');                           
                % Increase/Decrease Distance buttons
                 uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String','-',...
                        'FontSize',14,'units','normalized', 'Position', [smp_offsetPX smp_offsetPY+(4+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...           % Plus one micron/s
                        'callback',{@(src,event)self.subDButtonCB(src,event)});
                 uicontrol('Parent',hp_StepMoves,'Style','Push','FontName','symbol','String','+',...
                        'FontSize',14,'units','normalized', 'Position', [smp_offsetPX+3*smp_SX smp_offsetPY+(4+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...           % Plus one micron/s
                        'callback',{@(src,event)self.addDButtonCB(src,event)});
                    % Edit Distance box
                self.editDistanceBox = uicontrol('Parent',hp_StepMoves,'style','edit',...
                                           'units','normalized','position',[smp_offsetPX+smp_SX smp_offsetPY+(4+smp_offsetArrowY)*smp_SY 2*smp_SX smp_SY],...
                                            'callback',{@(src,event)self.setDButtonCB(src,event)}); %an edit box
                uicontrol('Parent',hp_StepMoves,'style','text','BackgroundColor','white',...
                          'units','normalized','position',[smp_offsetPX smp_offsetPY+(5.25+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                          'string','Invert');                        
                self.invertXBox = uicontrol('Parent',hp_StepMoves,'Style','checkbox','BackgroundColor','white','String','X',...
                    'units','normalized','position',[smp_offsetPX+smp_SX smp_offsetPY+(5.5+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'Value',self.iX);
                self.invertYBox = uicontrol('Parent',hp_StepMoves,'Style','checkbox','BackgroundColor','white','String','Y',...
                    'units','normalized','position',[smp_offsetPX+2*smp_SX smp_offsetPY+(5.5+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'Value',self.iY);
                self.invertZBox = uicontrol('Parent',hp_StepMoves,'Style','checkbox','BackgroundColor','white','String','Z',...
                    'units','normalized','position',[smp_offsetPX+3*smp_SX smp_offsetPY+(5.5+smp_offsetArrowY)*smp_SY smp_SX smp_SY],...
                    'Value',self.iZ);
            % END OF PANEL STEP MOVEMENTS
            % STATUS PANEL
                hp_Status = uipanel('Title','Status','FontName','MS Sans Serif','FontSize',10,...
                            'BackgroundColor','white',...
                            'Position',[.5 .3 .5 .7]);
                % Update Status Button
                sst_SX=.1;sst_SY=.1; % Size of one arrow button
                sst_offsetPX=sst_SX;sst_offsetPY=sst_SY; % Bottom right position of the 'arrow button' box
                uicontrol('Parent',hp_Status,'Style','Push','FontName','MS Sans Serif','String','UPDATE',...
                    'FontSize',10,'units','normalized', 'Position', [sst_offsetPX sst_offsetPY 0.4 sst_SY],...
                    'callback',{@(src,event)self.updateButtonCB(src,event)}); 
                % STOPS Button
                uicontrol('Parent',hp_Status,'Style','Push','FontName','MS Sans Serif','String','STOP',...
                    'FontSize',10,'units','normalized', 'Position', [0.4+sst_offsetPX sst_offsetPY 0.4 sst_SY],...
                    'callback',{@(src,event)self.stopButtonCB(src,event)});
                % Label Relative Pos
                self.relativePosGUI =  uicontrol('Parent',hp_Status,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                          'units','normalized','position',[sst_offsetPX sst_offsetPY+(1.1)*sst_SY 4*sst_SX 2.9*sst_SY]);
                % Label Absolute Pos
                self.absolutePosGUI = uicontrol('Parent',hp_Status,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                          'units','normalized','position',[sst_offsetPX+4*sst_SX sst_offsetPY+(1.1)*sst_SY 4*sst_SX 2.9*sst_SY]);
                 % Label Speed
                self.editSpeedStatusGUI = uicontrol('Parent',hp_Status,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                          'units','normalized','position',[sst_offsetPX sst_offsetPY+4.1*sst_SY 0.8 0.9*sst_SY]);
                % Label Fine/Coarse
                 self.labelStatusFineCoarse = uicontrol('Parent',hp_Status,'style','text','BackgroundColor',[0.9 0.9 0.9],...
                          'units','normalized','position',[sst_offsetPX sst_offsetPY+5.1*sst_SY 0.8 0.9*sst_SY]);
                % Set New Origin Button
                uicontrol('Parent',hp_Status,'Style','Push','FontName','MS Sans Serif','String','SET ORIGIN',...
                    'FontSize',10,'units','normalized', 'Position', [sst_offsetPX sst_offsetPY+6*sst_SY 0.8 sst_SY],...
                    'callback',{@(src,event)self.setOriginButtonCB(src,event)}); 
                % IDLE/BUSY LABEL
                 self.labelStatusIdleBusy = uicontrol('Parent',hp_Status,'style','text','BackgroundColor',[0.9 0.9 0.9],'FontSize',13,...
                          'units','normalized','position',[sst_offsetPX sst_offsetPY+7.1*sst_SY 0.8 1.9*sst_SY]);%
           % END OF STATUS PANEL
           % MAP PANEL
                hp_Map = uipanel('Title','Map','FontName','MS Sans Serif','FontSize',10,...
                            'BackgroundColor','white',...
                            'Position',[0.5 0 0.5 .3]);
                % Update Status Button
                sma_SX=.2;sma_SY=.2; % Size of one arrow button
                sma_offsetPX=0.2*sma_SX;sma_offsetPY=0.2*sma_SY; 
                % StoredPositionList
                self.listBoxPos = uicontrol('Parent',hp_Map,'style','listbox','BackgroundColor',[0.9 0.9 0.9],'FontSize',13,...
                   'units','normalized','position',[sma_offsetPX sma_offsetPY+2*sst_SY 4*sma_SX 4*sma_SY]);%  
                % Goto Button
                self.buttonGoto = uicontrol('Parent',hp_Map,'Style','Push','FontName','MS Sans Serif','String','GO',...
                   'FontSize',9,'units','normalized', 'Position', [sma_offsetPX sma_offsetPY 0.9*sma_SX 2*sst_SY] ); % Minus one step Z
                % Delete Button
                self.buttonDel = uicontrol('Parent',hp_Map,'Style','Push','FontName','MS Sans Serif','String','DEL',...
                   'FontSize',9,'units','normalized', 'Position', [sma_offsetPX+2*sst_SX sma_offsetPY 0.9*sma_SX 2*sst_SY] ); % Minus one step Z
                % Store Button
                self.buttonStore = uicontrol('Parent',hp_Map,'Style','Push','FontName','MS Sans Serif','String','STO',...
                   'FontSize',9,'units','normalized', 'Position', [sma_offsetPX+4*sst_SX sma_offsetPY 0.9*sma_SX 2*sst_SY] ); % Minus one step Z
           % END OF MAP PANEL
           self.updateGUIStatus();
        end
        % ----- [Derived from Device class]
        function writeLogHeader(self)
        end
        % ----- [Derived from Device class]
        function startDevAcqu(self)
        end
        % ----- [Derived from Device class]
        function stopDevAcqu(self)
        end
        
        % ----- GUI CALLBACKS
        function doStepMovementCB(self,src,event,x,y,z)
             self.doStepMove(x,y,z);
        end
        function updateButtonCB(self,src,event)
            self.AP=self.getPosA();
        end
        function setOriginButtonCB(self,src,event)
            self.P0=self.getPosA();
            notify(self,'P0redefined');
        end
        function stopButtonCB(self,src,event)
            self.stop();
        end
        function setVButtonCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.V=H;
                self.setV(H,self.Vmode);
            end
            self.updateGUISpeed();
        end
        function addVButtonCB(self,src,event)
                self.V=self.V+1;
                self.setV(self.V,self.Vmode);
                self.updateGUISpeed();
        end
        function subVButtonCB(self,src,event)
                if (self.V>1)
                    self.V=self.V-1;
                    self.setV(self.V,self.Vmode);
                    self.updateGUISpeed();
                end
        end
        function setDButtonCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                if (self.stepDistance>0.039)
                self.stepDistance=H;
                end
            end
            self.updateGUIDistance();
        end
        function addDButtonCB(self,src,event)
                self.stepDistance=self.stepDistance+1;
                self.updateGUIDistance();
        end
        function subDButtonCB(self,src,event)
                if (self.stepDistance>1)
                    self.stepDistance=self.stepDistance-1;

                    self.updateGUIDistance();
                end
        end
        function toggleFineCoarseCB(self,src,event)
            if (strcmpi(self.Vmode,'f')==1)
                self.Vmode='c';
            else
                self.Vmode='f';
            end
            self.setV(self.V,self.Vmode);
            self.updateGUIFineCoarse();
        end
        % ----- END OF GUI CALLBACKS
        
        % --- UPDATE GUI Status according to current variable (no hardware
        % update
        function updateGUIStatus(self)
            % Update speed
            self.updateGUISpeed();
            % Update distance step
            self.updateGUIDistance();
            % Update fine coarse value
            self.updateGUIFineCoarse();
            % Update pos
            self.updateGUIPosStatus();
            % Update idle
            self.updateGUIIdleStatus();
        end
        function updateGUIFineCoarse(self) 
            % Initialize value
                if (strcmpi(self.Vmode,'f')==1)
                    set(self.labelStatusFineCoarse,'string','FINE');
                    set(self.toggleButton_FineCoarse,'string','FINE');
                    set(self.toggleButton_FineCoarse,'Value',0);
                else
                    set(self.labelStatusFineCoarse,'string','COARSE');
                    set(self.toggleButton_FineCoarse,'string','COARSE');
                    set(self.toggleButton_FineCoarse,'Value',1);
                end
        end
        function updateGUIIdleStatus(self)
                if (self.available==1)
                    set(self.labelStatusIdleBusy,'BackgroundColor',[0.5 1 0.5]);
                    set(self.labelStatusIdleBusy,'string','IDLE');
                else
                    set(self.labelStatusIdleBusy,'BackgroundColor',[1 0.5 0.5]);
                    set(self.labelStatusIdleBusy,'string','BUSY');
                end
        end
        function updateGUIPosStatus(self)
            set(self.relativePosGUI,'string',['XR=' num2str(self.RP(1)) char(13) char(10) 'YR=' num2str(self.RP(2)) char(13) char(10) 'ZR=' num2str(self.RP(3))]);
            set(self.absolutePosGUI,'string',['XA=' num2str(self.AP(1)) char(13) char(10) 'YA=' num2str(self.AP(2)) char(13) char(10) 'ZA=' num2str(self.AP(3))]);
        end
        function updateGUISpeed(self)
            set(self.editSpeedBox,'string',num2str(self.V));
            set(self.editSpeedStatusGUI,'string',['speed=' num2str(self.V) ' µm/s']);
        end
        function updateGUIDistance(self)
            set(self.editDistanceBox,'string',num2str(self.stepDistance));%,...
        end
    end
        methods(Static)
        function listen_P0redefined(src,event)
                src.RP=[src.AP(1)-src.P0(1);src.AP(2)-src.P0(2);src.AP(3)-src.P0(3)];
               logline=[src.stringEventHeader  char(9) 'P0 redefined to' char(9) num2str(src.P0(1)) char(9) num2str(src.P0(2)) char(9) num2str(src.P0(3)) ];
               %disp(logline);
               src.saveLog(logline);
               src.updateGUIPosStatus();
        end
        function listen_PMoved(src,event)
               logline=[src.stringEventHeader  char(9) 'P moved to' char(9) num2str(src.RP(1)) char(9) num2str(src.RP(2)) char(9) num2str(src.RP(3)) ];
               %disp(logline);
               src.saveLog(logline);
               src.wait_for_end_of_command=1;
               src.updateGUIPosStatus();
        end
        function listen_VRedefined(src,event)
               logline=[src.stringEventHeader  char(9) 'V set to' char(9) num2str(src.V) char(9) src.Vmode ];
               %disp(logline);
               src.saveLog(logline);
               src.updateGUISpeed();
        end
        function listen_PMeasured(src,event)
               logline=[src.stringEventHeader  char(9) 'P measured' char(9) num2str(src.RP(1)) char(9) num2str(src.RP(2)) char(9) num2str(src.RP(3))];
               %disp(logline);
               src.saveLog(logline);
               src.updateGUIPosStatus();
        end
        function listen_IsAvailable(src,event)
               logline=[src.stringEventHeader  char(9) 'Is Available'];
               %disp(logline);
               src.saveLog(logline);
               src.available=1;
               src.wait_for_end_of_command=0;
               src.updateGUIIdleStatus();
               src.getPosA(); % updates position after movement
        end
        function listen_IsBusy(src,event)
               logline=[src.stringEventHeader  char(9) 'Is Busy'];
               %disp(logline);
               src.saveLog(logline);
               src.available=0;
               src.wait_for_end_of_command=1;
               src.updateGUIIdleStatus();
        end
        function listen_Stop(src,event)
               logline=[src.stringEventHeader  char(9) 'Stopped'];
               %disp(logline);
               src.saveLog(logline);
               src.available=1;
               src.wait_for_end_of_command=0;
               src.updateGUIIdleStatus();
        end
    end
end

