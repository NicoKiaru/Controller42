classdef Aladdin_Device < Device
    %Aladdin Device class
    %   Basic communication mode should be enabled
    % SAFE COMMUNICATION MODE UNSUPPORTED
    % Unsecured sent values
    % rate only supported in microliter per min
    
    properties
      % ----- Hardware Properties
            % ----- Communication with computer
            port; % Com port (serial port) connected to Zaber
            BaudRate; % 9600 for Zaber apparently
            s_id; % id of serial port communication
            status; % char: 
                    % -S=stalled; 
                    % -I=infusing;
                    % -W=withdrawing;
                    % -other unsupported by this program
            diameter; % diameter of the syringe
            
            % ----- Current device value (correct only if user does not
            % change values manually on the device)
            currDIA;
            currVOL;
            currRATE;
            
            
            % ----- GUI
            pushButton_start;
            pushButton_stop;
            editVol;
            editRate;
            editDia;
            
            % ----- EVENTS
    end
    
    events
        DIAredefined;
        VOLredefined;
        RATEredefined;
        STARTpressed;
        STOPpressed;
    end
    
    methods
        function self=Aladdin_Device(name_identifier,pos,p,BR,ini_rate)
             name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,pos);
            self.deviceType='ALADDIN';
            
            % set communication device properties
            self.BaudRate=BR; % set serial port communication properties
            self.port=p;
            self.init(); % initialise serial port communication
            self.mockCMD();
            self.currDIA=self.getDia();
            self.currVOL=self.getDir()*self.getVol();
            self.setRate(ini_rate); % just to be sure to be in ul/min
            self.currRATE=self.getRate();
            
            % Builds GUI of this device
            self.buildGui();
            
            % set listener for this device
            addlistener(self,'DIAredefined',@Aladdin_Device.listen_DIAredefined);
            addlistener(self,'VOLredefined',@Aladdin_Device.listen_VOLredefined);
            addlistener(self,'RATEredefined',@Aladdin_Device.listen_RATEredefined);
            addlistener(self,'STARTpressed',@Aladdin_Device.listen_STARTpressed);
            addlistener(self,'STOPpressed',@Aladdin_Device.listen_STOPpressed);
        end
        
         % ---------------------------------------------
         % ------------- DEVICE COMMANDS ---------------
         % ---------------------------------------------
         
         % ---------- Initialise serial port communication
         function init(self) % initialise serial port communication device
            s = serial(self.port);            
            set(s,'BaudRate',self.BaudRate,'terminator',3); %char(3) terminator for device response
            fopen(s);
            self.s_id=s;
         end
         
         % ---------- Start current sequence
         function run(self)
             self.last_event_clock=clock;
             notify(self,'STARTpressed');
             fwrite(self.s_id,['RUN' char(13)],'uchar'); % char(13) terminator for device query
             a=fscanf(self.s_id); % read position, 3 signed long int
         end
         
         % ---------- Stop current sequence
         function stop(self)
             self.last_event_clock=clock;
             notify(self,'STOPpressed');
             fwrite(self.s_id,['STP' char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
         end
         
         % ---------- Return current syringe diameter
         function d=mockCMD(self)
             fwrite(self.s_id,['DIA' char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
         end
         
         % ---------- Return current syringe diameter
         function d=getDia(self)
             fwrite(self.s_id,['DIA' char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
             d=str2double(a(5:9));
         end
         
         % ---------- Set current syringe diameter (mm)
         function setDia(self,dia)
             self.currDIA=dia;
             self.last_event_clock=clock;
             notify(self,'DIAredefined');
             st=num2str(dia,4);
             fwrite(self.s_id,['DIA' st char(13)],'uchar');
             fscanf(self.s_id); % read position, 3 signed long int
             
         end
         
         % ---------- Return current defined volume
         function v=getVol(self)
             fwrite(self.s_id,['VOL' char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
             v=str2double(a(5:9));
             unit=a(10:11);
             switch unit
                 case 'UL'
                     
                 case 'ML'
                     v=v*1000; % result in microliter, always
                 otherwise
                     warning('Unexpected unit type.');
             end
         end

         % ----------- Set volume to be withdrawn/infused
         function setVol(self,vol) % VOL IN uL
             st=num2str(vol/1000,4);
             fwrite(self.s_id,['VOL' st char(13)],'uchar');
             fscanf(self.s_id); % read position, 3 signed long int
         end
         
         % ----------- Set rate of liquid
         function setRate(self,rat) % rate in microliter per minute
             self.currRATE=rat;
             self.last_event_clock=clock;
             notify(self,'RATEredefined');
             st=num2str(rat,4);
             fwrite(self.s_id,['RAT' st 'UM' char(13)],'uchar');
             fscanf(self.s_id); % empty buffer
         end
         
         % ----------- Get rate of liquid
         function r=getRate(self) % rate in microliter per minute
             %st=num2str(rat,4);
             fwrite(self.s_id,['RAT' char(13)],'uchar');
             a=fscanf(self.s_id); % empty buffer
             r=str2double(a(5:9));
         end
         
         % ----------- Get current volume dispensed, in microliter
         function vd=getVolDis(self)
             fwrite(self.s_id,['DIS' char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
             vd=str2double(a(6:10))-str2double(a(12:16));
             unit=a(17:18);
             switch unit
                 case 'UL'
                     % should never happen... except according to the
                     % documentation, depending on the syringe diameter
                     warning('Unexpected unit type.');
                 case 'ML'
                     vd=vd*1000; % result in microliter, always
                 otherwise
                     warning('Unexpected unit type.');
             end             
         end
         
         % ------------- Get direction of infusion : +1 = infuse, -1=
         % withdraw
         function direct=getDir(self)
             fwrite(self.s_id,['DIR' char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
             switch a(5:7)
                 case  'WDR'
                     direct=-1;
                 case 'INF'
                     direct=+1;
             end
         end
         
         % ----------- Higher level command: signed volume, set vol and
         % direction: negative value = withdraw liquid
         % positive value = infuse liquid
         function setStep(self,sVol) %signed volume
             self.currVOL=sVol;
             self.last_event_clock=clock;
             notify(self,'VOLredefined');
             self.setDir(sign(sVol));
             self.setVol(abs(sVol));
         end
         
         % ------------- S direction of infusion : +1 = infuse, -1=
         % withdraw
         function setDir(self,dir)
             st='';
             switch dir
                 case  -1
                     st='WDR';
                 case +1
                     st='INF';

             end
             fwrite(self.s_id,['DIR' st char(13)],'uchar');
             a=fscanf(self.s_id); % read position, 3 signed long int
         end
         
        % ----- [Derived from Device class]          
        % ---------- Close serial port communication upon device deletion
        function closeDevice(self)
            fclose(self.s_id);% closes serial port communication
        end
        % ----- [Derived from Device class]
        function buildGui(self)
                % Label for Volume
                uicontrol('style','text',...
                          'position',[10 62 88 18],...
                          'string','Vol (uL)');
                
                % Edit Text Choose Volume
                self.editVol = uicontrol('style','edit',...
                                            'position',[102 62 88 18],...
                                            'string',num2str(self.currVOL),...
                                            'callback',{@(src,event)self.editVolCB(src,event)}); %an edit box      
                
                % Label for Rate
                uicontrol('style','text',...
                          'position',[10 42 88 18],...
                          'string','Rate (uL/min)');
                
                % Edit Text Choose Rate
                self.editRate = uicontrol('style','edit',...
                                            'position',[102 42 88 18],...
                                            'string',num2str(self.currRATE),...
                                            'callback',{@(src,event)self.editRateCB(src,event)}); %an edit box      
                % Label for Diameter
                uicontrol('style','text',...
                          'position',[10 22 88 18],...
                          'string','Diameter (mm)');
                
                % Edit Text Choose Diameter
                self.editDia = uicontrol('style','edit',...
                                            'position',[102 22 88 18],...
                                            'string',num2str(self.currDIA),...
                                            'callback',{@(src,event)self.editDiaCB(src,event)}); %an edit box  
                      
                % pushButton start
                self.pushButton_start = uicontrol('style','pushbutton',...
                                            'position',[10 02 88 18],...
                                            'string','Start',...
                                            'callback',{@(src,event)self.pushButton_startCB(src,event)}); %an edit box
                
                % pushButton stop
                self.pushButton_stop = uicontrol('style','pushbutton',...
                                            'position',[102 2 88 18],...
                                            'string','Stop',...
                                            'callback',{@(src,event)self.pushButton_stopCB(src,event)}); %an edit box
               
                      
        end
        
        function pushButton_startCB(self,src,event)
            self.run();
        end
        
        function pushButton_stopCB(self,src,event)
            self.stop();
        end
        
        function editVolCB(self,src,event)
            [W, status] = str2num(get(src,'string'));
            if (status==1)
                self.setStep(W);
            else
                set(src,'string',num2str(self.currVOL));
            end
        end
        
        function editRateCB(self,src,event)
            [W, status] = str2num(get(src,'string'));
            if (status==1)
                self.setRate(W);
            else
                set(src,'string',num2str(self.currRATE));
            end
        end
        
        function editDiaCB(self,src,event)
            [W, status] = str2num(get(src,'string'));
            if (status==1)
                self.setDia(W);
            else
                set(src,'string',num2str(self.currDIA));
            end
        end
        
        % ----- [Derived from Device class]
        % ---- Writing parameters specific to this device in the log file before acquisition starts 
        function writeLogHeader(self)
            fprintf(self.hLogFile, sprintf(self.get_Saver_Infos()));
        end
        
        % ---- Returns specific informations for this device
        function infos=get_Saver_Infos(self);
            infos='';
            infos=[infos 'Aladdin Syringe Pump Informations\n'];
            tv = clock;
            infos=[infos sprintf('\tTIME_START=%d,%d,%d,%d,%d,%f\n', tv(1), tv(2), tv(3), tv(4), tv(5), tv(6))];
            infos=[infos sprintf('\tDIAMETER SYRINGE (mm) =%d\n',self.currDIA)];
            infos=[infos sprintf('\tRATE (uL/min) =%d\n',self.currRATE)];
            infos=[infos sprintf('\tVOL (uL) =%d\n',self.currVOL)];
            infos=[infos 'Data are written as follows:' char(13) ];
            infos=[infos 'Hour' char(9) 'Minute' char(9) 'Seconds' char(9) 'Type of event' char(9) 'optional data' char(13)];
        end
        
        % ----- [Derived from Device class]
        function startDevAcqu(self)
        end
        % ----- [Derived from Device class]
        function stopDevAcqu(self)
        end
        
    end
         % ---------------------------------------------
         % ------------- LISTENERS=static methods ------
         % ---------------------------------------------
    methods(Static)
        function listen_DIAredefined(src,event)
               logline=[src.stringEventHeader  char(9) 'DIA redefined to' char(9)  num2str(src.currDIA)];
               src.saveLog(logline);
               
               set(src.editDia,'string',num2str(src.currDIA)); % Update GUI in case command was send from command line
        end
        
        function listen_VOLredefined(src,event)

               logline=[src.stringEventHeader  char(9) 'VOL redefined to' char(9)  num2str(src.currVOL)];
               src.saveLog(logline);

               set(src.editVol,'string',num2str(src.currVOL)); % Update GUI in case command was send from command line
        end
        
        function listen_RATEredefined(src,event)

               logline=[src.stringEventHeader  char(9) 'RATE redefined to' char(9)  num2str(src.currRATE)];
               src.saveLog(logline);

                set(src.editRate,'string',num2str(src.currRATE)); % Update GUI in case command was send from command line
        end
        
        function listen_STARTpressed(src,event)

               logline=[src.stringEventHeader  char(9) 'START' char(9)];
               src.saveLog(logline);

        end
        
        function listen_STOPpressed(src,event)

               logline=[src.stringEventHeader  char(9) 'STOP' char(9) ];
               src.saveLog(logline);

        end
    end
end

