classdef Controller_42 < handle
    %Controller_42 Summary
    %  Controls and synchronize 42 devices
    %  Detailed explanation goes here
    % DATA save to path: seriesFolderName\seriesName+currAcquNumber\
    % where seriesFolderName=workingFolder\currentUser\yy-mm-dd\
    
    % defaultFolder mostly unused
    
    properties
        devices;
        nDevices;
        acquiring;
        users;
        
        % Acquisition parameters
        defaultFolder;
        workingFolder;
        seriesFolderName;
        currentUser;
        newSeriesCreated;
        currAcquNumber;
        seriesName;
        
        chechBoxesDevices; % if ticked, device is saved
        
        % handle to the gui figure
        hControllerGUI;
        hDeviceListGUI;
        posGui;
        toggleButton_Acquire;
        popupmenuUsers;
        labelUsers;
        labelFolder;
        pushButtonChooseFolder;
        labelSeriesName;
        editTextSeriesName;
        toggleButtonShowDeviceList;
        labelCurAcquNumber;
        labelCurAcquName;
        
        
        deviceListShown;
    end
    
    methods
        % ----- Constructor for Controller class
        function Controller=Controller_42()
            Controller.nDevices=0;
            Controller.devices=[];
            Controller.chechBoxesDevices=[];
            Controller.acquiring=0;
            Controller.defaultFolder=pwd;
            Controller.newSeriesCreated=0;
            Controller.currentUser='DEFAULT';
            Controller.currAcquNumber=0;
            Controller.seriesName='Exp_';
            Controller.deviceListShown=0;
        end       
        
        % ---- Reads ini file and initialise devices according to this file
        function initCfg(self,filename)
                %removed for backward compatibility: [~,~,subsections] = inifile(filename,'readall');
                [unused_arg_1,unused_arg_2,subsections] = inifile(filename,'readall');
                % add all devices declared
                nsub=length(subsections);
                for i=1:nsub
                    if (strcmpi(subsections(i,1),'devices')==1)
                       currdevicename=upper(subsections{i,2});
                       queryKeys = {'devices',currdevicename,'type','s','';...
                                   'devices',currdevicename,'xGUI','i','50';...
                                   'devices',currdevicename,'yGUI','i','50';...
                                   'devices',currdevicename,'wGUI','i','200';...
                                   'devices',currdevicename,'hGUI','i','200'};
                       readSett = inifile(filename,'read',queryKeys);
                       pos=[readSett{2} readSett{3} readSett{4} readSett{5}];
                       switch upper(readSett{1})
                           case 'USER_NOTIFICATION'
                               disp(['User notification device: ' currdevicename]);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    self.addDevice(UserNotification_Device(currdevicename,pos));
                               end
                           case 'CAMERA'
                               disp(['Camera device: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'adapt','s','';...
                                   'devices',currdevicename,'dev','s','';...
                                   'devices',currdevicename,'format','s','';...
                                   'devices',currdevicename,'camtype','s',''};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    self.addDevice(Camera_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}, readSett{4}));
                               end
                           case 'TRACKER'
                              disp(['Tracker device: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'xStart','i','';...
                                            'devices',currdevicename,'xEnd','i','';...
                                            'devices',currdevicename,'yStart','i','';...
                                            'devices',currdevicename,'yEnd','i','';...
                                            'devices',currdevicename,'treshold','i','';...
                                            'devices',currdevicename,'above','i','';...
                                            'devices',currdevicename,'linkcam','s','none'};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    ht=Tracker_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}, readSett{4}, readSett{5}, readSett{6});
                                    self.addDevice(ht);
                                    if (strcmpi(readSett{7},'none')==0)
                                       h=self.getDeviceByName(readSett{7});
                                       h.addTrackerDevice(ht);
                                   end
                               end
                         case 'TRACKER_FOCUS'
                              disp(['Tracker device: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'xStart','i','';...
                                            'devices',currdevicename,'xEnd','i','';...
                                            'devices',currdevicename,'yStart','i','';...
                                            'devices',currdevicename,'yEnd','i','';...
                                            'devices',currdevicename,'linkcam','s','none';...
                                            'devices',currdevicename,'linkctrl','s','none';...
                                            'devices',currdevicename,'zrange','i','4';...
                                            'devices',currdevicename,'zstep','i','100'};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    ht=Tracker_Focus_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}, readSett{4},...
                                                            self.getDeviceByName(readSett{6}), readSett{7},readSett{8}/1000);
                                    self.addDevice(ht);
                                    if (strcmpi(readSett{5},'none')==0)
                                       h=self.getDeviceByName(readSett{5});
                                       h.addTrackerDevice(ht);
                                   end
                               end
                           case 'ZABER'
                               disp(['Zaber: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'port','s','';...
                                            'devices',currdevicename,'baudrate','i',''};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                  self.addDevice(Zaber_Device(currdevicename, pos, readSett{1}, readSett{2}));
                               end
                           case 'ALADDIN'
                               disp(['Aladdin pump: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'port','s','';...
                                            'devices',currdevicename,'baudrate','i','';...
                                            'devices',currdevicename,'ini_rate','s',''};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                  self.addDevice(Aladdin_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}));
                               end
                           case 'MP285'
                               disp(['MP285: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'port','s','';...
                                            'devices',currdevicename,'baudrate','i','';...
                                            'devices',currdevicename,'invX','i','';...
                                            'devices',currdevicename,'invY','i','';...
                                            'devices',currdevicename,'invZ','i',''};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    self.addDevice(MP_285_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}, readSett{4}, readSett{5}));
                               end
                           case 'SHUTTER'
                               disp(['Shutter: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'daq','s','';...
                                            'devices',currdevicename,'dev','s','';...
                                            'devices',currdevicename,'port','s','';...
                                            'devices',currdevicename,'inv','i','';...
                                            'devices',currdevicename,'ini','i',''};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                   self.addDevice(Shutter_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}, readSett{4}, readSett{5}));
                               end
                           case 'SHUTTER_LISTEN'
                               disp(['Shutter Listen: ' currdevicename]);
                               queryKeys = {'devices',currdevicename,'daq','s','';...
                                            'devices',currdevicename,'dev','s','';...
                                            'devices',currdevicename,'port','s',''};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1)                             
                                    self.addDevice(Shutter_Listen_Device(currdevicename, pos, readSett{1}, readSett{2}, readSett{3}));
                               end
                           case 'NIKON_CONTROLLER'
                               disp(['Nikon Controller: ' currdevicename]);
                               if (strcmpi(questdlg(['Initialize device: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                  self.addDevice(NikonTIControl_Device(currdevicename, pos));
                               end
                       end
                    end
                    if (strcmpi(subsections(i,1),'behaviours')==1)
                       currdevicename=upper(subsections{i,2});
                       queryKeys = {'behaviours',currdevicename,'type','s','';...
                                    'behaviours',currdevicename,'xGUI','i','50';...
                                    'behaviours',currdevicename,'yGUI','i','50';...
                                    'behaviours',currdevicename,'wGUI','i','200';...
                                    'behaviours',currdevicename,'hGUI','i','200'};
                       readSett = inifile(filename,'read',queryKeys);
                       pos=[readSett{2} readSett{3} readSett{4} readSett{5}];
                       switch upper(readSett{1})
                           case 'BF_SLICES'
                           disp(['Behaviour: ' currdevicename]);
                               queryKeys = {'behaviours',currdevicename,'linkctrl','s','none';...
                                            'behaviours',currdevicename,'linkshutBF','s','none';...
                                            'behaviours',currdevicename,'linkshuttrig','s','none';...
                                            'behaviours',currdevicename,'linkcam','s','none';...
                                            'behaviours',currdevicename,'delay','i','5';...
                                            'behaviours',currdevicename,'zrange','i','2';...
                                            'behaviours',currdevicename,'zstep','i','500'};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize behaviour: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    hB=BehaviourDICSlices(pos,...
                                                       self.getDeviceByName(readSett{1}),...
                                                       self.getDeviceByName(readSett{2}),...
                                                       self.getDeviceByName(readSett{3}),...
                                                       self.getDeviceByName(readSett{4}),...
                                                       readSett{5},readSett{6},readSett{7}/1000);
                                    if (strcmpi(readSett{4},'none')==0)
                                       h=self.getDeviceByName(readSett{4});
                                       h.addBehaviourToNotify(hB);
                                    end
                               end
                           case 'B_AUTOFOCUS'
                           disp(['Behaviour: ' currdevicename]);
                               queryKeys = {'behaviours',currdevicename,'linkctrl','s','none';...
                                            'behaviours',currdevicename,'linkAF','s','none';...                                            
                                            'behaviours',currdevicename,'linkshuttrig','s','none';...
                                            'behaviours',currdevicename,'delay','i','5'};
                               readSett = inifile(filename,'read',queryKeys);
                               if (strcmpi(questdlg(['Initialize behaviour: ' currdevicename],'Init','yes','no','no'),'yes')==1) 
                                    hB=BehaviourAutofocus(pos,...
                                                       self.getDeviceByName(readSett{1}),...
                                                       self.getDeviceByName(readSett{2}),...
                                                       self.getDeviceByName(readSett{3}),...
                                                       readSett{4});
                               end
                       end
                    end
                    if (strcmpi(subsections(i,1),'users')==1)
                        currUser=upper(subsections{i,2});
                        self.users{length(self.users)+1}=currUser;
                    end
                end
                % set controller properties
                queryKeys = {'controller','save','working_path','s',pwd};
                readSett = inifile(filename,'read',queryKeys);
                self.workingFolder=readSett{1};
                queryKeys = {'controller','gui','xGUI','i','50';...
                             'controller','gui','yGUI','i','50';...
                             'controller','gui','wGUI','i','200';...
                             'controller','gui','hGUI','i','200'};
                readSett = inifile(filename,'read',queryKeys);
                self.posGui=[readSett{1} readSett{2} readSett{3} readSett{4}];
        end
        
        % ---- Return handle to the device specified by its name
        function h=getDeviceByName(self, name)
            h=NaN;
            for i=1:length(self.devices)
                if (strcmp(self.devices{i}.deviceName,name)==1)
                    h=self.devices{i};
                end
            end
        end
        
        % ------ Displays controller GUI
        function showGui(self)
             self.hControllerGUI=figure('Toolbar','none',...
                          'Menubar', 'none',...
                          'NumberTitle','Off',...
                          'Name','42 Controller','NumberTitle','off','Position', self.posGui);
             set(self.hControllerGUI,'CloseRequestFcn',@self.my_closereq);
             
             self.toggleButton_Acquire = uicontrol('style','togglebutton',...
                                           'position',[2 2 196 28],...
                                           'string','START ACQUISITION',...
                                           'callback',{@(src,event)self.toggleButton_AcquireCB(src,event)}); %an edit box
             % Initialize value
             set(self.toggleButton_Acquire,'Value',self.acquiring);
             

             % Select User
             self.popupmenuUsers = uicontrol('style','popupmenu',...
                                           'position',[2 32 196 28],...
                                           'string',self.users,...
                                           'callback',{@(src,event)self.popupmenuUsersCB(src,event)}); %an edit box
             self.labelUsers=uicontrol('style','text',...
                                        'position',[2 62 196 20],...
                                        'string','Select user');
             % Folder selection
              self.labelFolder=uicontrol('style','text',...
                                        'position',[2 112 196 50],...
                                        'string',['Folder:' self.workingFolder]);
             % Choose Folder Button
              self.pushButtonChooseFolder = uicontrol('style','pushbutton',...
                                            'position',[2 87 196 20],...
                                            'string','Choose folder',...
                                            'callback',{@(src,event)self.pushButtonChooseFolderCB(src,event)}); %an edit box
                                        
             % Label CurrentAcquName

              self.labelCurAcquName = uicontrol('style','text',...
                                        'position',[2 184 196 20],...
                                        'string','Next Acquisition:',...
                                        'BackgroundColor', [1 1 1]);
             % Label CurrentAcquNumber
              %self.labelCurAcquNumber = 
              self.labelCurAcquNumber = uicontrol('style','text',...
                                        'position',[2 164 196 20],...
                                        'string','0',...
                                        'BackgroundColor', [1 1 1]);
                               
             % Label Series name
              self.labelSeriesName=uicontrol('style','text',...
                                        'position',[2 224 196 20],...
                                        'string','Series Name:');
             % Choose series Name
              self.editTextSeriesName = uicontrol('style','edit',...
                                            'position',[2 204 196 20],...
                                            'string',self.seriesName,...
                                            'callback',{@(src,event)self.editTextSeriesNameCB(src,event)}); %an edit box
             % Push button device list window
             self.toggleButtonShowDeviceList = uicontrol('style','togglebutton',...
                                                   'position',[2 246 196 20],...
                                                   'string','Show devices',...
                                                   'callback',{@(src,event)self.toggleButtonShowDeviceListCB(src,event)}); %an edit box
        end
        
        function initDevicesWindow(self)
            
            posDW=[self.posGui(1)+self.posGui(3)+10 self.posGui(2) self.posGui(3) (length(self.devices)+1)*20];
            
            self.hDeviceListGUI=figure('Toolbar','none',...
                          'Menubar', 'none',...
                          'NumberTitle','Off',...
                          'Name','Device List','NumberTitle','off','Position', posDW);
            
            %set(self.hControllerGUI,'CloseRequestFcn',@self.my_closereqdevicelist);
            set(self.hDeviceListGUI,'Visible','off');
            for i=1:length(self.devices)
                % Label device
                self.chechBoxesDevices{length(self.chechBoxesDevices)+1}=uicontrol('Style','checkbox',...
                'String',self.devices{i}.deviceName,...
                'Value',1,'position',[2 20*(i-1)+2 self.posGui(3) 18]);                    
                
            end
            i= length(self.devices)+1;
                            uicontrol('style','text',...
                                        'position',[2 20*(i-1)+2 0.2*self.posGui(3) 18],...
                                        'string','Save');
                            uicontrol('style','text',...
                                        'position',[0.2*self.posGui(3)+5 20*(i-1)+2 0.8*self.posGui(3)-2 18],...
                                        'string','Name');
                        
        end
        

        
        % ------------------------------
        % ------- GUI CALLBACKS --------
        % ------------------------------
        
        % ----- Toggle button acquire callback
        function toggleButton_AcquireCB(self,src,event)
            if (self.acquiring==1)
                self.stopAcquisition();
                set(self.toggleButton_Acquire,'Value',0);
                set(self.toggleButton_Acquire,'string','START ACQUISITION');
            else
                set(self.toggleButton_Acquire,'string','STOP ACQUISITION');
                self.startAcquisition();
            end
        end
        
        function r=isDeviceChecked(self,name)
            r=NaN;
            for i=1:length(self.chechBoxesDevices)
                st=get(self.chechBoxesDevices{i},'string');
                if (strcmp(st,name)==1)
                    r=get(self.chechBoxesDevices{i},'value');
                end
            end
        end
        
        % ----- Push button choose folder callback
        function pushButtonChooseFolderCB(self,src,event)
                selection = questdlg(['Changing folder requires to create new series. Are you sure?'],...
                                      'Choose folder request',...
                                      'Yes','Cancel','Yes'); 
                switch selection, 
                case 'Yes'
                     if (self.acquiring==0)
                         if (isdir(self.workingFolder))
                            temp=uigetdir(self.workingFolder);
                            if (temp==0)
                            else
                                self.setCurrentFolder(temp);
                            end
                         else
                            temp=uigetdir(pwd);
                            if (temp==0)
                            else
                                self.setCurrentFolder(temp);
                            end
                         end
                     else
                        disp('Cannot change folder while acquiring.');
                     end
                case 'Cancel'
                    return 
                end
        end
        
        % ----- Popup menu choose user callback
        function popupmenuUsersCB(self,src,event)
                selection = questdlg(['Changing user requires to create new series. Are you sure?'],...
                                'Choose user request',...
                                'Yes','Cancel','Yes'); 
                switch selection, 
                case 'Yes'
                    str = get(src , 'String');
                    val = get(src,'Value');
                    if (self.acquiring==0)
                        self.setCurrentUser(str{val});
                    else
                        disp('Cannot change user while acquiring.');
                        for i=1:length(self.users)
                            if (strcmpi(self.users{i},self.currentUser)==1)
                                set(src,'Value',i);
                            end
                        end
                    end
                case 'Cancel'
                        for i=1:length(self.users)
                            if (strcmpi(self.users{i},self.currentUser)==1)
                                set(src,'Value',i);
                            end
                        end
                    return
                end
        end
        
        % ----- Edit text series name callback
        function editTextSeriesNameCB(self,src,event)
                selection = questdlg('Changing series name requires to create new series. Are you sure?',...
                                'Set series name request',...
                                'Yes','Cancel','Yes'); 
                switch selection, 
                case 'Yes'
                    str = get(src , 'String');
                    if (self.acquiring==0)
                        self.setCurrentSeriesName(str);
                    else
                        disp('Cannot change user while acquiring.');
                        set(src,'string',self.seriesName);

                    end
                case 'Cancel'
                    set(src,'string',self.seriesName);
                    return
                end
        end
        
        function toggleButtonShowDeviceListCB(self,src,event)
            if (self.deviceListShown==1) % il est montre
                self.deviceListShown=0;
                set(self.toggleButtonShowDeviceList,'Value',0);
                set(self.toggleButtonShowDeviceList,'string','Show devices');
                
                set(self.hDeviceListGUI,'Visible','off');
            else
                self.deviceListShown=1;
                set(self.toggleButtonShowDeviceList,'Value',1);
                set(self.toggleButtonShowDeviceList,'string','Hide devices');
                
                set(self.hDeviceListGUI,'Visible','on');
            end
        end
        
        % ------------------------------
        % ---- END OF GUI CALLBACKS ----
        % ------------------------------
        
        % ----- set Current user name
        function setCurrentUser(self,nameUser)
            if (self.acquiring==0) % if not acquiring
                if (strcmpi(self.currentUser,nameUser)==0) % if current user if different from new user
                    self.currentUser=nameUser;
                    self.newSeriesCreated=0; % flag that a new series should be created
                end
            end
        end
        
        % ----- set Current series name
        function setCurrentSeriesName(self,nameSeries)
            if (self.acquiring==0) % if not acquiring
                if (strcmpi(self.seriesName,nameSeries)==0)% if current user if different from new series name
                    self.seriesName=nameSeries;
                    self.newSeriesCreated=0; % flag that a new series should be created
                end
            end
        end
        
        % ----- set Current working folder name
        function setCurrentFolder(self,nameF)
            if (self.acquiring==0) % if not acquiring
                if (strcmpi(self.workingFolder,nameF)==0)% if current user if different from new series name
                    self.workingFolder=nameF;
                    self.newSeriesCreated=0; % flag that a new series should be created
                end
            end
        end
        
        % ----- Custom delete function of the controller : custom delete
        % all devices
        function custom_delete(self)
            delete(self.hDeviceListGUI);
            delete(self.hControllerGUI);
            self.stopAcquisition();
            for i=1:length(self.devices)
                    self.devices{i}.custom_delete();
            end
            delete(self);
        end
        
        % ----- Launch upon closure of the controller window
        function my_closereq(self,src,evnt)
            % User-defined close request function 
            % to display a question dialog box 
            selection = questdlg(['Killing Controller will stop all acquisitions and close all devices. Are you sure to proceed ?'],...
                                'Close Request Function',...
                                'Yes','Cancel','Cancel'); 
            switch selection, 
            case 'Yes'
                 self.custom_delete();
            case 'Cancel'
                return 
            end
        end
        
        % ----- Auto Set the root folder where data will be acquired
        function autoSetWorkingFolder(self,UserName)
                  str=[self.defaultFolder '\' UserName '\' datestr(now, 'yy-mm-dd') '\'];
                  self.setWorkingFolder(str);
        end
        
        % ----- Set the root folder where data will be acquired
        function setWorkingFolder(self,folderName)
            if (self.acquiring==0)
                
                for i=1:length(self.devices)
                    self.devices{i}.setFolderName(folderName);
                end
            else
                disp('Acquisition in progress... Cannot modify save folder!');
            end
        end
        
        % ----- Add a device to the controller
        function addDevice(self,hDevice);
            self.devices{length(self.devices)+1}=hDevice;
        end

        % ----- Displays the list of all connected devices
        function listDevices(self)
            for i=1:length(self.devices)
                disp(self.devices{i}.deviceName);
            end
        end
        
        % ----- start acquisition : set current folder and start
        % acquisition for all devices
        function startAcquisition(self)
            if (self.newSeriesCreated==0)
                self.newSeriesCreated=self.createNewSeries();
            end
            if (self.newSeriesCreated==1)
                folderName=[self.seriesFolderName '\' self.seriesName num2str(self.currAcquNumber) '\'];
                status = mkdir(folderName);
                if (status==0)
                        disp('Could not create folder; Abort acquisition.');
                        return
                end
                set(self.labelCurAcquNumber,'BackgroundColor',[1 0 0 ]);
                set(self.labelCurAcquName,'string','Acquiring...');
                set(self.labelCurAcquName,'BackgroundColor',[1 0 0 ]);
                for i=1:length(self.devices)
                    if (self.isDeviceChecked(self.devices{i}.deviceName)==1)
                        self.devices{i}.setFolderName(folderName);
                        self.devices{i}.startRecording();
                    end
                end
                self.acquiring=1;
            else

                set(self.toggleButton_Acquire,'string','START ACQUISITION');
                set(self.toggleButton_Acquire,'value',0);
            end
        end
        
        % ----- Stop acquisition of all devices
        function stopAcquisition(self)
            if (self.acquiring==1)
                for i=1:length(self.devices)
                 self.devices{i}.stopRecording;
                end
                self.acquiring=0;
                self.currAcquNumber=self.currAcquNumber+1;
                set(self.labelCurAcquNumber,'BackgroundColor',[1 1 1]);
                set(self.labelCurAcquName,'BackgroundColor',[1 1 1]);
                set(self.labelCurAcquName,'string','Next Acquisition:');
                set(self.labelCurAcquNumber,'string',num2str(self.currAcquNumber));
            end
        end
        
        % ----- Creates new series
        function status=createNewSeries(self)
            % creates a new Series and corresponding folder
            fullFolderName=[self.workingFolder '\' self.currentUser '\' datestr(now, 'yy-mm-dd') '\' ];
            status=1;
            if (exist(fullFolderName,'dir')==0)
               selection = questdlg(['The folder :' fullFolderName ' does not exist. Create it ?'],...
                                'Create folder request',...
                                'Yes','Cancel','Yes'); 
                switch selection, 
                case 'Yes'
                    status = mkdir(fullFolderName);
                    if (status==0)
                        disp('Could not create folder; Abort creating new series');
                        return
                    end
                case 'Cancel'
                    status=0;
                    return 
                end
            end
            self.seriesFolderName=fullFolderName;
            self.currAcquNumber=0;
        end
    end
end

