classdef Device < handle
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        deviceName;
        hDeviceGUI; % handle to the GUI figure
        
        % ----- Event timing
        last_event_clock; % not sure of this one...
        
        % ----- Link with Exp_Saver
        isRecording; % 1 if an acquisition is enabled, else 0
        
        % ----- Recording Properties
        folderName;
        logFileName;
        fullLogFileName;
        hLogFile;
    end
    
    methods
        function self=Device(name,pos)
            self.deviceName=name;
            self.initGUI(name,pos);
            self.isRecording=0;
            self.logFileName=[self.deviceName];
        end
        
        function initGUI(self,name,pos)
            self.hDeviceGUI=figure('Toolbar','none',...
                          'Menubar', 'none',...
                          'NumberTitle','Off',...
                          'Name',name,'NumberTitle','off','Position', pos);
            set(self.hDeviceGUI,'CloseRequestFcn',@self.my_closereq);
        end
        
        function showGUI(self)
            set(self.hDeviceGUI,'Visible','on');
        end
        
        function hideGUI(self)
            set(self.hDeviceGUI,'Visible','off');
        end
        
        function custom_delete(self)
            delete(self.hDeviceGUI);
            self.closeDevice();
            if (self.isRecording==1)
                self.stopRecording();
            end
            delete(self);
        end
        
        function my_closereq(self,src,evnt)
            % User-defined close request function 
            % to display a question dialog box 
            selection = questdlg(['Kill device ' self.deviceName ' or hide GUI ?'],...
                                'Close Request Function',...
                                'Kill','Hide','Cancel','Cancel'); 
            switch selection, 
            case 'Hide',
                self.hideGUI();
                %delete(self.hDeviceGUI)
                % set(self.hDeviceGUI,'Visible','off');
                %delete(self)
                %clear self
            case 'Kill'
                
                self.custom_delete();
                
            case 'Cancel'
                return 
            end
        end
        
        function mes=stringEventHeader(self)
            t=self.last_event_clock;
            %mes=[num2str(t(1)) ';' num2str(t(2)) ';' num2str(t(3)) ';' num2str(t(4)) ';' num2str(t(5)) ';' num2str(t(6)) ';' ];
            mes=[num2str(t(4)) char(9) num2str(t(5)) char(9) num2str(t(6))];
        end
        
        % ------ Functions to records events in a log file
        function startRecording(self)
            % Opens a log file
            self.fullLogFileName = sprintf('%s/%s.log',self.folderName,self.logFileName); % concatenante file name

            % Checks if recording is already enable
            if (self.isRecording==1)
                disp(['Recording for device ' self.deviceName ' has already started.']);
                return;
            end

            % Checks that file does not already exist
            if (exist(self.fullLogFileName, 'file')~=0)
                  disp(['Error, could not create log file for Device ' self.deviceName '.']);
                  disp('Log File already exists');
                  return;
            end

            % Try to open new file
            try
                 self.hLogFile = fopen(self.fullLogFileName,'w'); % Text file describing series.
            catch err
                 disp(['Error, could not create log file for Device ' self.deviceName '.']);
                 return
            end

            % All checks performed, recording occurs
            self.isRecording=1;
            str=['============' char(13)];
            str=[str 'Log file for device ' self.deviceName char(13)];
            t=clock;
            str=[str 'File created on (yy-mm-dd) ' datestr(now, 'yy-mm-dd') ' at ' num2str(t(4)) 'h' num2str(t(5)) 'm' num2str(t(6)) 's' char(13)];
            fwrite(self.hLogFile,str);
            self.writeLogHeader(); % Writes log file header
            self.startDevAcqu();
            str=['============' char(13)];
            fwrite(self.hLogFile,str);
            
        end
        
        function setFolderName(self,fN)
            if (self.isRecording==0)
                self.folderName=fN;
            end
        end
        
        function deviceEvent(self, description)
            disp([self.deviceName '; ' description]);
        end
        
        function saveLog(self,strline)
            if (self.isRecording==1)
                str=[strline char(13)];
                fwrite(self.hLogFile,str);
            end
        end
        
        function stopRecording(self)
           if (self.isRecording==1)
                self.stopDevAcqu();
                self.isRecording=0;             
                self.hLogFile = fclose(self.hLogFile);  
           end
        end
    end
    
    methods (Abstract)
          closeDevice(self)
          buildGui(self)
          writeLogHeader(self)
          startDevAcqu(self)
          stopDevAcqu(self)
          
    end
    
end

