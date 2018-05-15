classdef MFCS_Device < Device
    % Class for controlling Fluigent MFCS Vac device ( should be ok for non
    % vac device, but not tested)
    % Instructions before using this device:
    %    - Download Fluigent MFCS SDK  at https://www.fluigent.com/download-1/ 
    %    - INstall MINGW64 compiler to use the sdk (info message by matlab is fine)
    %    - If using a matlab version < 2017, see https://ch.mathworks.com/support/bugreports/1741173?s_tid=mwa_osa_a
    % Pressure unit is millibars
    properties
        % ----- MFCS device identification
        s_id; % serial number of MFCS
        mfcs_handle; % mfcs handle number
        n_channels; % number of channels available on the device
        
        % ----- GUI properties
        channel_id_gui; % channel handled in the GUI
        slider_PA;    % Used to set absolute Pressure
        edit_P0;      % Used to set reference pressure
        edit_PR;      % Used to set relative pressure
        show_PR;      % Used to display relative pressure
        edit_PStep;   % Used to set pressure step
        
        % ----- Device Status = 
        %     - Pressure measurements - (pressure set points are not
        %     stored)
        %     - Pressure origins (P0)
        % These are arrays because several channels can exist 
        P0; % Relative origin of the pressure device, set by user
            % should be accessed via setp0 and getp0 functions
        PA; % Measured
        PT; % Pressure Target - Set by user
        delayTrack; % sampling rate, in seconds, of the device. normally 0.1 is ok for most applications
        trackTimer; % sampling thread
        Pstep_Size; % Pressure step - same for all channels
        
        % CONSTANT but cannot be static because of Matlab
        MAXP; % Maximal pressure - here it is the same for each canal - which can be a problem. To check!
        MINP; % Minimal pressure (should be zero anyway)
        %CurrP; % for GUI only - relative for the displayed channel only
    end
    
    events
        P0redefined;
        PMoved;
        PTracked;
    end
    
    methods
         % ---------- Constructor of the MFCS Device
         function self=MFCS_Device(name_identifier, position, serial_number, channel_id_ini)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,position);            
            self.deviceType='MFCS';
           
            % set device properties : serial number
            self.s_id = serial_number;
            self.init; % Starts communication
            if self.mfcs_handle ~= 0 % should be initialized, or else skips the rest
                self.n_channels = mfcs_chan_number(self.mfcs_handle);

                self.P0 = zeros(self.n_channels,1);
                self.PA = zeros(self.n_channels,1);
                self.PT = zeros(self.n_channels,1);

                % GUI parameters
                % Initialize specific channel parameters
                % TODO but not sure the SDK allows to access these values
                self.channel_id_gui=channel_id_ini;
                self.MAXP=30;
                self.MINP=0;
                self.Pstep_Size=1;
                self.buildGui();
                self.updateGuiState();

                % set listener for this device
                addlistener(self,'P0redefined',@MFCS_Device.listen_P0redefined);
                addlistener(self,'PMoved',@MFCS_Device.listen_PMoved);
                addlistener(self,'PTracked',@MFCS_Device.listen_PTracked);

                self.delayTrack=0.2; % in s, a lower value couls lead to over-run error ?
                self.trackTimer=timer('Period',self.delayTrack,'ExecutionMode','fixedSpacing','TimerFcn',{@(src,event)self.trackMFCS(src,event)});
                start(self.trackTimer);
            else
                disp('Could not initialized MFCS');
            end
         end
         
         % ---------------------------------------------
         % ------------- DEVICE COMMANDS ---------------
         % ---------------------------------------------
         % ---------- Initialise serial port communication
         
         % ---------- Close serial port communication upon device deletion
         function closeDevice(self)
               stop(self.trackTimer);
               if (self.mfcs_handle~=0)
                    try
                         selection = questdlg(['Set all channels pressure of ' self.deviceName ' to zero ?'],...
                                               'Set pressures to zero',...
                                               'Yes','No','No'); 
                         switch selection, 
                               case 'Yes'
                                    self.home();
                               case 'No'
                         end
                         mfcs_close(self.mfcs_handle);
                    catch err % if error occured, display a warning box.
                        errString = getReport(err,'basic','hyperlinks','off');
                        warndlg(strcat('Error ocurred when initializing : ', errString));
                        %ensure giving the handle to the user in the comand window, before
                        %closing the window3
                        warning('DeviceManager:ErrorWhenWindowClosure',...
                            ['Error  occured when closing the Window!'...
                            'Handle value of the unclosed MFCS connection : ' num2str(self.mfcs_handle)]);
                    end
               end
         end
         
         % ---------- Set zero pressure on all channels to the device
         function home(self) % return to home
             % TODO
             for i=1:self.n_channels
                self.setPA(i,0);
                pause(0.1) % necessary ?
             end
         end
         
         % ---------- Initialises MFCS device
         function init(self) % initialise serial port communication device
             try
                self.mfcs_handle = mfcs_init(self.s_id);
             catch err % if error occured, display a warning box.
                errString = getReport(err,'basic','hyperlinks','off');
                warndlg(strcat('Error ocurred when initializing : ', errString));
             end
         end
         
         function writeLogHeader(self)
         end
         
         % ---------- Send command to set Absolute Pressure  (PA) value position
         % of the indexed device channel
         function setPA(self, channel_index, pressure) % set the absolute position of linear actuator      
            if (pressure>=self.MINP)&&(pressure<self.MAXP) % Check if command is in range
                self.last_event_clock=clock;
                mfcs_set_auto(self.mfcs_handle, pressure,channel_index);
                self.PT(channel_index)=pressure;
            else
                notify(self,'POutOfRange');
            end
         end
         
         % ---------- Send command to set relative (z0) Z value position of the device
         function setPR(self, channel_index, pressure) % set the relative position of linear actuator
             self.setPA(channel_index, pressure+self.P0(channel_index));
         end
         
         % --------- Redefine P0 of indicated channel
         function setP0(self,channel_index,pi)
             self.last_event_clock=clock;
             self.P0(channel_index)=pi;
             notify(self,'P0redefined');
         end
         
         % --------- Get P0 of indicated channel
         function res=getP0(self,channel_index)
             res = self.P0(channel_index);
         end
         
         % --------- Get PA of indicated channel
         function res=getPA(self,channel_index)
            res = self.PA(channel_index);
         end
         
         % --------- Get PR of indicated channel
         function res=getPR(self,channel_index)
             res = self.PA(channel_index)-self.P0(channel_index);
         end
         
         % ---------- Track MFCS, reads continuously its measures, for all
         % available channels
         function trackMFCS(mfcs,src,event)
            try
               for i=1:mfcs.n_channels
                   mfcs.PA(i) = mfcs_read_chan(mfcs.mfcs_handle,i);
               end
               % mfcs.CurrP = mfcs.PA(mfcs.channel_id_gui);
               % disp((mfcs.PA)')
               
            catch err
                    errString = getReport(err,'basic','hyperlinks','off');
                    warndlg(strcat('Error ocurred when measuring pressure: ', errString));
                    set(handles.measureButton,'Enable','off','value',0,'String','Start Measure')
            end
            notify(mfcs,'PTracked');
            %  end
         end
         
         
         % ---------------------------------------------
         % ------------- GUI COMMANDS ------------------
         % ---------------------------------------------
         
         % ---------- Builds GUI for Shutter
         function buildGui(self)
                figure(self.hDeviceGUI);
                % Absolute pressure (mBar)
                self.slider_PA=uicontrol('Style', 'slider',... 
                          'Min',self.MINP-1,'Max',self.MAXP+1,'Value',0,... // marging because of fluctuations near set point
                          'units','normalized',...
                          'Position', [0 0 0.2 1],...
                          'Callback', {@(src,event)self.sliderCB(src,event)});
                % Pressure label
                USY=0.10;
                
                iYButton=1;
                
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-iYButton*USY 0.2 USY],...
                          'string','P (mBar)');
                      
                % Edit Relative Pressure
                self.edit_PR=uicontrol('style','edit',...
                                       'units','normalized','position',[0.4 1-iYButton*USY 0.6 USY],...%,...
                                       'callback',{@(src,event)self.setPosCB(src,event)}); %an edit box
                iYButton=iYButton+1;          
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-iYButton*USY 0.2 USY],...
                          'string','P (mBar)');
                % Show current relative Pressure
                self.show_PR=uicontrol('style','text',...
                                       'units','normalized','position',[0.4 1-iYButton*USY 0.6 USY]); %show box
                
                iYButton=iYButton+1;
                %P0 label                
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-iYButton*USY 0.2 USY],...
                          'string','P0 (mBar)');
                      
                % Edit P0
                self.edit_P0=uicontrol('style','edit',...
                                       'units','normalized','position',[0.4 1-iYButton*USY 0.6 USY],...%,...
                                       'callback',{@(src,event)self.setP0CB(src,event)}); %an edit box
                iYButton=iYButton+1;
                % Set current pressure as P0
                uicontrol('Style','Push','String','Set current P as P0',...
                          'FontSize',14,'units','normalized', 'Position', [0.2 1-iYButton*USY 0.8 USY],...
                          'callback',{@(src,event)self.setCurrPasP0CB(src,event)}); % Plus one step Z
                iYButton=iYButton+1;
                % Goto P0
                uicontrol('Style','Push','String','Goto P0',...
                          'FontSize',14,'units','normalized', 'Position', [0.2 1-iYButton*USY 0.8 USY],...
                          'callback',{@(src,event)self.gotoP0CB(src,event)}); % Plus one step Z 
                iYButton=iYButton+1;
                % Pressure Step size
                uicontrol('style','text','BackgroundColor','white',...
                          'units','normalized','position',[0.2 1-iYButton*USY 0.2 USY],...
                          'string','Step (mBar)');
                      
                % Edit Pressure Step Size
                self.edit_PStep=uicontrol('style','edit',...
                                       'units','normalized','position',[0.4 1-iYButton*USY 0.6 USY],...%,...
                                       'callback',{@(src,event)self.setStepSizeCB(src,event)}); %an edit box
                iYButton=iYButton+1;
                % Label
                % Editor
                % Button UP
                uicontrol('Style','Push','FontName','symbol','String',char(173),...
                    'FontSize',14,'units','normalized', 'Position', [0.2 1-iYButton*USY 0.8 0.1],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,1)}); % Plus one step Z
                iYButton=iYButton+1;
                % Button DOWN
                uicontrol('Style','Push','FontName','symbol','String',char(175),...
                    'FontSize',14,'units','normalized', 'Position', [0.2 1-iYButton*USY 0.8 0.1],...
                    'callback',{@(src,event)self.doStepMovementCB(src,event,-1)});% Minus one step Z
                iYButton=iYButton+1;
                
                % selection of channel controlled by the GUI
               
                for i=1:self.n_channels
                    % TODO:  %select_channel_buttongroup = uibuttongroup(fig,'Position',[bla bla bla bla]);
                    uicontrol('Style','Push','String',['Ch-' num2str(i)],...
                              'FontSize',14,'units','normalized', 'Position', [0.2+0.8/self.n_channels*(i-1) 1-iYButton*USY 0.8/self.n_channels USY],...
                              'callback',{@(src,event)self.setGUIChannelCB(src,event,i)});% GUI works on channel i
                         
                end
                iYButton=iYButton+1;
                % ALL zero pressure button
                uicontrol('Style','Push','String','ZERO ALL!',...
                          'FontSize',14,'units','normalized', 'Position', [0.2 1-iYButton*USY 0.8 USY],...
                          'callback',{@(src,event)self.zerosCB(src,event)}); % Stop all movements
                self.updateGUI;
         end
         
         % ---------- Set working channel for GUI
         function setGUIChannelCB(self, src, event,index_channel)
             self.channel_id_gui=index_channel;
         end
         
         % ---------- ZERO ALL button callback
         function zerosCB(self, src, event)
             self.home();
         end
         
         % All commands below applies to channel self.channel_id_gui
         
         % ---------- Do a step movement for Zaber
         function doStepMovementCB(self,src,event,dir)
             %curPos=self.getZA();
             %self.CurrZ=curPos+dir*self.Zstep_Size;
             self.setPA(self.channel_id_gui,self.PT(self.channel_id_gui)+dir*self.Pstep_Size);
         end
                  
         % ---------- Set Current PressurePos as Z0
         function setCurrPasP0CB(self,src,event)
             self.setP0(self.channel_id_gui,self.PT(self.channel_id_gui));
         end
                  
         % ---------- Set Step Size CB
         function setStepSizeCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.Pstep_Size=H;
            end
            self.updateGuiState();   
         end
         
         % ---------- Goto P0 CB
         function gotoP0CB(self,src,event)             
                self.setPR(self.channel_id_gui,0);
         end
         
         % ---------- Slider CallBack: if the user moves the slider
         function sliderCB(self,src,event)
             Pvalue=get(src,'Value')
             self.setPA(self.channel_id_gui,Pvalue);
         end
         
         % ---------- Set Pos CB
         function setPosCB(self,src, event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setPR(self.channel_id_gui,H);
            end
            self.updateGuiState();
         end
         
         % ---------- Set P0 CB
         function setP0CB(self,src, event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setP0(self.channel_id_gui,H);
            end
            self.updateGuiState();
         end
         
         function updateGUI(self) 
         end
        
         function startDevAcqu(self)
         end
         
         function stopDevAcqu(self)
         end
         
         % ---------- Updates all the Gui according to the current device
         % state
         function updateGuiState(self)             
           set(self.slider_PA,'Value',self.PA(self.channel_id_gui));
           set(self.edit_PR,'String',num2str(self.PT(self.channel_id_gui)-self.P0(self.channel_id_gui)));
           set(self.show_PR,'String',num2str(self.PA(self.channel_id_gui)-self.P0(self.channel_id_gui)));
           set(self.edit_P0,'String',num2str(self.P0(self.channel_id_gui)));
           set(self.edit_PStep,'String',num2str(self.Pstep_Size));
           zs_pc=(self.Pstep_Size/(self.MAXP-self.MINP));
           set(self.slider_PA,'SliderStep',[zs_pc zs_pc]);
         end
   
    end
    
    % ---------------------------------------------
    % ------------- LISTENERS=static methods ------
    % ---------------------------------------------
    methods(Static)
        % ---- Called if P0 is redefined
        function listen_P0redefined(src,event)
           src.updateGuiState();
        end
        
        % ---- Called if P is moved (manually or through the slider or
        % through a command)
        function listen_PMoved(src,event)
           src.updateGuiState();
        end
        
        function listen_PTracked(src,event)
            src.updateGuiState();
            if (src.isRecording==1)
                t=clock;
                %mes=[num2str(t(1)) ';' num2str(t(2)) ';' num2str(t(3)) ';' num2str(t(4)) ';' num2str(t(5)) ';' num2str(t(6)) ';' ];
                %mes=[num2str(t(4)) char(9) num2str(t(5)) char(9) num2str(t(6))];
                % TODO!
                logline=[mes  char(9) 'Z = ' char(9)  num2str(src.CurrZ)];
                src.saveLog(logline);
            end           
            % Could be necessary src.updateGuiState();
        end
    end
    
end

