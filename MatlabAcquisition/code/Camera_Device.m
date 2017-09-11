classdef Camera_Device < Device
    %Camera_Device Summary of this class goes here
    %   Detailed explanation goes here
    % ------- Trying an improvement on 28 November 2012: We want to 
    % limit the number of calls to addframe because it is very slow
    properties
        cam;                %   Camera linked to this device
        
        % Display options
        BG_image;           %   Background image that can be removed (live processing)
        meanBG;             %   Mean gray value of the background image
        hImage_unprocessed; %   Window where the image is displayed
        hImage_processed;
        imageSize;          %   Size of the image
        min_gray_level;     %   Minimal gray value displayed
        max_gray_level;     %   Maximal gray value displayed
        sub_BG;             %   Toggle BG substraction in preview
        
        % GUI controllers
        slider_min_gray;    % Slider setting min gray displayed value
        slider_max_gray;    % Slider setting max gray displayed value
        edit_gain;          % Edit text setting camera Gain
        edit_exp_time;      % Edit text setting camera exposure time
        edit_skp_image;     % Edit text setting number of skipped images for saving 
        toggle_sub_BG;      % Toggle button enabling/disabling live image background substraction
        button_getNewBG;    % Button triggering acquisition of a new background image
        toggle_save_Button;
        
        % For video acquisition
        video_acquisition_started;  % Flag set to 1 when acquisition is enabled, value=0 otherwise
        curr_file_number;           % index of current avi file because file cannot be larger two 2Go -> splitting files
        video_file_name;            % name of the current avi file
        video_file;                 % handle to the current avi file
        %nb_skipped_image;           % number of skipped image after one frame has been acquired
        %index_skipped_image;        % save current number of skipped images
        MAX_FRAMES_PER_FILE;        % maximum number of frames per avi file because file cannot be larger two 2Go -> splitting files
        MAX_SIZE_PER_FILE;          % maximum size of one avi file because file cannot be larger two 2Go -> splitting files
        
        NUMBER_OF_FRAME_PER_ADDFRAME;
        CURRENT_DATA;
        
        % This device allows to control Tracker_Devices...
        trackers;                   % list of tracker devices linked to this camera device

        % Guppy and pixelink supported
        cam_type;
        
        % MODIFICATION 21 January 2013: Top level control on acquisition :
        % Allows to save data only when needed
        toggle_save_data;       % if = 1, then we save data, if = 0, then data are dropped
        N_acquisition_needed;   % if = 0, then infinite acquisition needed, otherwise, the counter is decreased, if it reaches 0, toggle save data is toggled to 0
        N_images_acquired_since_acqu_start;
        
        behaviours_to_notify;
    end
    
    events
        %all_images_acquired; % triggered if N_acquisition_needed=N_images_acquired_since_acqu_start, and then N_images_acquired_since_acqu_start is set to 0
    end
    
    
    methods
        % ----- Constructor of the Camera device
        function self=Camera_Device(name_identifier, pos, adapt, dev, format,ctype)
            name=name_identifier; % Device name
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,pos);            
            self.deviceType='CAMERA';
            
            self.cam_type=upper(ctype);
            
            % Initialise camera hardware
            self.cam=Camera(adapt,dev,format); % creates object
            self.cam.init(); % initialise camera
  
            % Set default acuisition properties
            set(self.cam.vid,'LoggingMode','memory');       % Save to memory (to make possible real time image processing)
            set(self.cam.vid,'TriggerRepeat',inf);          % Undefined length of acquisition (camera runs until stop method is called)
            set(self.cam.vid,'FramesPerTrigger',1);         % Probably useless...
            set(self.cam.vid,'FramesAcquiredFcnCount',1);   % Call frame acquired callback function every one image is acquired (to make possible real time image processing)
          
            %if (strcmpi(self.cam_type,'PIXELINK')==1)
            %     set(self.cam.src,'ExposureMode','manual');
            %end
            % self.cam.src.Exposure=-5;
            % fetch image size from camera
            self.imageSize = fliplr(self.cam.vidRes);     
             
            % initialize background image to 0
            self.BG_image = double(zeros(self.imageSize));
            self.meanBG=double(0);
            % display parameters
            self.min_gray_level=0;
            self.max_gray_level=256; % for 8-bit image
            self.sub_BG=1; % enable background correction by default
            
            %self.nb_skipped_image=0;           % number of skipped image after one frame has been acquired
            %self.index_skipped_image=0; 

            % Display unprocessed image at the top left position of GUI
            subplot(5,3,1); %top left position
            self.hImage_unprocessed = imshow(uint8(zeros(self.imageSize))); % show image
            subplot(5,3,4:15); 
            self.hImage_processed = imshow(uint8(zeros(self.imageSize)));
            set(self.hImage_processed,'ButtonDownFcn',{@(src,event)self.buttonDownCB(src,event)});
            % Shows GUI: Interface to control display options and camera
            % properties
            self.toggle_save_data=1;
            self.buildGui();

            % Set preview callback function -> self.preview_display
             setappdata(self.hImage_unprocessed,'UpdatePreviewWindowFcn',@self.preview_display);
            % setappdata(self.hImage_unprocessed,'UpdatePreviewWindowFcn',{@(src,event)self.oneFrameAcquired(src,event)});
            % Set acquisition callback function -> self.oneFrameAcquired
            set(self.cam.vid,'FramesAcquiredFcn',{@(src,event)self.oneFrameAcquired(src,event)});
  
            % Start preview
            preview(self.cam.vid, self.hImage_unprocessed);

            % acquisition has not started
            self.video_acquisition_started=0;

            % During acquisition, one avi file cannot be larger than self.MAX_SIZE_PER_FILE
            % This comes from the fact that Avi file cannot be larger than
            % 2Go. Otherwise a new avi file is created which contains the
            % remaining acquisition
            self.MAX_SIZE_PER_FILE=1024*1024*1024*1.5;
            self.NUMBER_OF_FRAME_PER_ADDFRAME=10;
            nx=self.imageSize(1);
            ny=self.imageSize(2);
            self.CURRENT_DATA=uint8(zeros(nx,ny,self.NUMBER_OF_FRAME_PER_ADDFRAME)); % Buffer to store frame before saving
            % No listener for this device

            
            
            % --------- TRACKERS
            self.trackers=[];
            self.behaviours_to_notify=[];
            %--------- END OF TRACKERS

            self.N_acquisition_needed=0;
            self.N_images_acquired_since_acqu_start=0;

        end
        
        function addBehaviourToNotify(self,h)
                self.behaviours_to_notify{length(self.behaviours_to_notify)+1}=h;
        end
        % ----- [Derived from Device class]
        % ----- Build Graphic User Interface to control Camera properties
        % and display options
        function buildGui(self)
                figure(self.hDeviceGUI);
                
                % Slider to set displayed min gray value
                self.slider_min_gray=uicontrol('Style', 'slider',...
                          'Min',0,'Max',256,'Value',0,...
                          'Position', [20 20 20 460],...
                          'Callback', {@(src,event)self.sliderMinGrayCB(src,event)});
                
                % Slider to set displayed max gray value
                self.slider_max_gray=uicontrol('Style', 'slider',...
                          'Min',0,'Max',256,'Value',256,...
                          'Position', [50 20 20 460],...
                          'Callback', {@(src,event)self.sliderMaxGrayCB(src,event)});
                
                if (strcmpi(self.cam_type,'GUPPY')==1)
                    % Label for Gain control
                    uicontrol('style','text',...
                              'position',[100 20 100 20],...
                              'string','Gain (0..680)');
                    % Gain control (edit text)
                    self.edit_gain = uicontrol('style','edit',...
                                               'position',[200 20 50 20],...
                                               'string',num2str(self.cam.src.Gain),...
                                               'callback',{@(src,event)self.editGainCB(src,event)}); %an edit box
                    % Label for Exposure time control
                    uicontrol('style','text',...
                              'position',[260 20 100 20],...
                              'string','Exposure time (ms)');
                
                    % Exposure time control (edit text)
                    self.edit_exp_time = uicontrol('style','edit',...
                                               'position',[360 20 40 20],...
                                               'string',num2str(self.cam.src.ExtendedShutter/1000),...
                                               'callback',{@(src,event)self.editExpTimeCB(src,event)}); %an edit box
                end
                
%                 if (strcmpi(self.cam_type,'PIXELINK')==1)
%                      uicontrol('style','text',...
%                           'position',[260 20 100 20],...
%                           'string','Exposure (-14..1)');
%                 
%                     % Exposure time control (edit text)
%                     self.edit_exp_time = uicontrol('style','edit',...
%                                                'position',[360 20 40 20],...
%                                                'string',num2str(self.cam.src.Exposure),...
%                                                'callback',{@(src,event)self.editExpTimeCB(src,event)}); %an edit box
%                 end
                
                if (strcmpi(self.cam_type,'GUPPY_GENTL')==1)
                    % GAIN CANNOT BE SET WITH THIS ADAPTOR!!!!! Please USE
                    % AVT VIMBA TO SET IT. MATLAB WILL NOT MODIFY IT AFTER
                    % Label for Gain control
                    %uicontrol('style','text',...
                    %          'position',[100 20 100 20],...
                    %          'string','Gain (0..680)');
                    % Gain control (edit text)
                    %self.edit_gain = uicontrol('style','edit',...
                    %                           'position',[200 20 50 20],...
                    %                           'string',num2str(self.cam.src.Gain),...
                    %                           'callback',{@(src,event)self.editGainCB(src,event)}); %an edit box
                    % Label for Exposure time control
                    uicontrol('style','text',...
                              'position',[260 20 100 20],...
                              'string','Exposure time (ms)');
                
                    % Exposure time control (edit text)
                    self.edit_exp_time = uicontrol('style','edit',...
                                               'position',[360 20 40 20],...
                                               'string',num2str(self.cam.src.ExposureTime/1000),...
                                               'callback',{@(src,event)self.editExpTimeCB(src,event)}); %an edit box
                end
                
                % Toggle button to enable/disable live background
                % correction
                self.toggle_sub_BG = uicontrol('style','togglebutton',...
                                            'position',[410 20 80 20],...
                                           'string','BG correction',...
                                           'callback',{@(src,event)self.toggleSubBGCB(src,event)}); %an edit box
                % Initialize value
                set(self.toggle_sub_BG,'Value',self.sub_BG);
                
                %toggle_save_data; CONTROLLER
                %toggle_save_Button
                self.toggle_save_Button = uicontrol('style','togglebutton',...
                                            'position',[600 20 100 40],...
                                            'callback',{@(src,event)self.toggleSaveCB(src,event)}); %an edit box
                % Initialize value
                %'string','BG correction',...
                set(self.toggle_save_Button,'Value',self.toggle_save_data);
                if (self.toggle_save_data==1)
                    set(self.toggle_save_Button,'string','ACQUIRE');
                else
                    set(self.toggle_save_Button,'string','SKIP');
                end
                % Button that triggers acquisition of a new Background
                % image
                self.button_getNewBG = uicontrol('style','pushbutton',...
                                            'position',[500 20 80 20],...
                                           'string','get BG',...
                                           'callback',{@(src,event)self.buttonGetBGCB(src,event)}); %an edit box                                       
          
     

        end
        
        % --------- TRACKERS
        function buttonDownCB(self,src,event)
               % Get Mouse Position
               %mouse_pos = get(0,'CurrentPosition');
               mouse_pos=get(gca,'Currentpoint');

               % Update Box Position
               
               for i=1:length(self.trackers)
                p=mouse_pos(1,[1:2]);
                %p(2)=480-p(2); % because image display is flipped
                self.trackers{i}.clickNotify(p);
               end

        end
        %--------- END OF TRACKERS
        
        % ------- GUI Callbacks
        % Callback for min gray value slider
        function sliderMinGrayCB(self,src,event)
             Gvalue=get(src,'Value');
             self.setMinGray(Gvalue);
        end
        function toggleSaveCB(self,src,event)
            v=get(src,'Value');
            if (v==1)
                self.N_acquisition_needed=0; % may not be good
            end
            self.setSaveState(v);
        end
        % Callback for max gray value slider
        function sliderMaxGrayCB(self,src,event)
             Gvalue=get(src,'Value');
             self.setMaxGray(Gvalue);
        end
        % Callback for gain edit text
        function editGainCB(self,src,event)
             Gvalue=round(str2num(get(src,'String')));
             if (Gvalue == [])
             else
                 self.setGain(Gvalue);
             end
        end
        % Callback for exposure time edit text
        function editExpTimeCB(self,src,event)
             Evalue=round(str2num(get(src,'String')));
             if (Evalue == [])
             else
                 self.setExpTime(Evalue);
             end
        end
        % Callback for live sub background button
        function toggleSubBGCB(self,src,event)
            self.sub_BG=get(src,'Value');
        end
        % Callback for new Bakcground acquisition button
        function buttonGetBGCB(self,src,event)
            self.getBG();
        end
        
        % ---- Methods to modify live display options
        % Minimal Gray value displayed
        function setMinGray(self,v)
            self.min_gray_level=v;
        end
        % Maximal Gray value displayed
        function setMaxGray(self,v)
            self.max_gray_level=v;
        end
        % Enable/Disable live background image substraction
        function enableBGcorrection(self,flag)
            self.sub_BG=flag; % flag: 0=disabled, 1=enabled
        end
        
        % ---- Methods to modify camera acquisition properties
        % Gain of camera
        function setGain(self,v)
            self.cam.src.Gain=v;
        end
        % Exposure Time of Camera
        function setExpTime(self,v)
            if (strcmpi(self.cam_type,'GUPPY')==1)
                self.cam.src.ExtendedShutter=v*1000;
            end
            if (strcmpi(self.cam_type,'PIXELINK')==1)
                self.cam.src.Exposure=v;
            end
            if (strcmpi(self.cam_type,'GUPPY_GENTL')==1)
                self.cam.src.ExposureTime=v*1000;
            end
        end
                 
        % ---- Callback function for live preview display of Camera
        function preview_display(self,obj,event,hImage)
        %function preview_display(self,vid,event)
            % This callback function updates the displayed frame and the
            % live processed image

            set(self.hImage_unprocessed, 'CData', event.Data);      % gives data to the handle (I don t understand but compulsory)


            data=double(event.Data);                                % get data

            % Image processing before display: 
            %   - Scale image to show gray min_gray_level and max_gray_level
            %   - Substract BG image if self.sub_BG is set to 1
            scale=256/(self.max_gray_level-self.min_gray_level);
            if (self.sub_BG==1)
                 res=((self.meanBG+data-self.BG_image)-self.min_gray_level)*scale;    % computes processed image
            else
                 res=((data)-self.min_gray_level)*scale;                                % computes non processed image
            end

            set(0, 'CurrentFigure', self.hDeviceGUI);                % set camera GUI as active figure
            %subplot(5,3,4:15);                                       % set location to show prcessed image
            set(self.hImage_processed, 'CData', res);%flipdim(uint8(res),1));

            %imshow(flipdim(uint8(res),1));                           % show it and flip it 
            
            % --------- TRACKERS
            for i=1:length(self.trackers)
                set(0, 'CurrentFigure', self.hDeviceGUI);
 %              rectangle('position',self.trackers{i}.getRect()); % PB worknotifs only with one tracker!!!!
                self.trackers{i}.showPreview();
                %hold on
                %rect  = [ 150 40 80 70]

            end
            if (self.video_acquisition_started==0)
               % if acquisition is not started, we track, otherwise it is
               % done directly in the oneFrameAcquired function
               for i=1:length(self.trackers)
                self.trackers{i}.Track(event.Data);
               end
            end
            %--------- END OF TRACKERS
        end
        
        %----- Top level control on data saving
        function setSaveState(self,state)
            self.toggle_save_data=state;
            % should update GUI  
                           set(self.toggle_save_Button,'Value',self.toggle_save_data);
                if (self.toggle_save_data==1)
                    set(self.toggle_save_Button,'string','ACQUIRE');
                else
                    set(self.toggle_save_Button,'string','SKIP');
                end
        end
        
        function res=getSaveState(self)
            res=self.toggle_save_data;
        end
        
        % ---- Callback function for acquiring camera images
        % Called each time a new frame is acquired and when device
        % acquisition has been started
        function oneFrameAcquired(self,vid,event)
            NImage_in_Buffer=get(vid,'FramesAvailable'); % Gets number of frame available into the buffer
            %disp('image recue');
            if (NImage_in_Buffer > 0)
               [data, time, metadata] = getdata(vid,1); % Gets one image from the buffer and the metadata linked to it
               if (self.getSaveState()==1) % save?
                  % disp('on sauve');
                    realFrameNumber=metadata.FrameNumber;%-1)/(self.nb_skipped_image+1)+1;
                    str=[num2str(realFrameNumber) char(9) num2str(metadata.AbsTime(4)) char(9) num2str(metadata.AbsTime(5)) char(9) num2str(metadata.AbsTime(6))];
                    self.saveLog(str); % Save image date of acquisition into the log file linked to this device                   
                    NumImagesMovie = realFrameNumber+NImage_in_Buffer-1; % Number of images in movie.
                    fwrite(self.video_file, data);
                    if (NumImagesMovie<(self.curr_file_number+1)*self.MAX_FRAMES_PER_FILE) % If the current avi file is not too large
                    % Nothing special
                    else % otherwise, the current file is too large
                        self.curr_file_number=self.curr_file_number+1; % Increment the file number
                        fclose(self.video_file); % closes current file
                        % opens a new one whose name has been incremented by 1
                        self.video_file_name = sprintf('%s/%s_%d.raw',self.folderName,self.logFileName, self.curr_file_number); % concatenante file name
                        self.video_file = fopen(self.video_file_name, 'w'); % creates handle to the video file
                    end
                    if (self.N_acquisition_needed==0)
                    else
                        self.N_images_acquired_since_acqu_start=self.N_images_acquired_since_acqu_start+1; % Increment number of acquired frames
                        if (self.N_acquisition_needed==self.N_images_acquired_since_acqu_start)
                            self.N_images_acquired_since_acqu_start=0;
                            self.setSaveState(0);
                            %notify(self,'all_images_acquired');
                            %disp('je notifie');
                            %notify_imageAcquired
                            
                            for i=1:length(self.behaviours_to_notify)
                                self.behaviours_to_notify{i}.notify_imagesAcquired();
                            end
                        end
                    end
               end
               % Always track, even if we do not save the image
               for i=1:length(self.trackers)
                   self.trackers{i}.Track(data);
               end
            end
        end
        
        % ---- Starts live image display
        function startPreview(self)
            preview(self.cam.vid, self.hImage_unprocessed);
        end
        
        % ---- Stops live i;age display
        function stopPreview(self)
            stoppreview(self.cam.vid);
        end
        
        % ---- Acquire a new Background image
        function getBG(self)
            stoppreview(self.cam.vid);
            self.BG_image = double(getsnapshot(self.cam.vid));
            self.BG_image = self.BG_image+double(getsnapshot(self.cam.vid));
            self.BG_image = self.BG_image+double(getsnapshot(self.cam.vid));
            self.BG_image = self.BG_image/3;
            
            % Plot current BG in the upper right corner of the Camera GUI
            set(0, 'CurrentFigure', self.hDeviceGUI);
            subplot(5,3,3);
            imshow(uint8(self.BG_image));
            self.meanBG=mean(mean(self.BG_image));

            preview(self.cam.vid, self.hImage_unprocessed);
        end
        
        % ----- [Derived from Device class]
        % ---- Writing parameters specific to this device in the log file before acquisition starts 
        function writeLogHeader(self)
            fprintf(self.hLogFile, sprintf(self.get_Saver_Infos()));
        end
        
        % ---- Returns specific informations for this device
        function infos=get_Saver_Infos(self);
            infos='';
            infos=[infos 'Camera Informations\n'];
            tv = clock;
            infos=[infos sprintf('\tTIME_START=%d,%d,%d,%d,%d,%f\n', tv(1), tv(2), tv(3), tv(4), tv(5), tv(6))];
              if (strcmpi(self.cam_type,'GUPPY')==1)
                    infos=[infos sprintf('\tEXP_TIME (micros) =%d\n',self.cam.src.ExtendedShutter)];
                    infos=[infos sprintf('\tGAIN =%d\n',self.cam.src.Gain)];
              end
       %       if (strcmpi(self.cam_type,'PIXELINK')==1)
       %             infos=[infos sprintf('\tEXP_TIME =%d\n',self.cam.src.Exposure)];
       %       end
            infos=[infos sprintf('Image Size X (pixels) = %d\n', self.imageSize(2))];
            infos=[infos sprintf('Image Size Y (pixels) = %d\n', self.imageSize(1))];
          
            infos=[infos 'First Image is the current Background' char(13)];
            infos=[infos 'Data are written as follows:' char(13) ];
            infos=[infos 'FrameNumber' char(9) 'Hour' char(9) 'Minute' char(9) 'Seconds' char(13)];
        end
        
        % ----- [Derived from Device class]
        % Starts camera acquisition:
        %   - Creates first avi file
        %   - Start camera acquisition (enables frame acquired function
        %   callback -> set in constructor to oneFrameAcquired function
        function startDevAcqu(self)
%                self.index_skipped_image=self.nb_skipped_image;
                self.video_acquisition_started=0;
                self.curr_file_number=0;
                self.video_file_name = sprintf('%s/%s_%d.raw',self.folderName,self.logFileName, self.curr_file_number); % concatenante file name
                % self.video_file_name = sprintf('%s/%s_%d.avi',self.folderName,self.logFileName, self.curr_file_number); % concatenante file name
                if (exist(self.video_file_name, 'file')==0)
                    self.video_acquisition_started=1;
                else
                    % Construct a questdlg with two options
                    choice = questdlg('Video already exists, overwrite?','Choice Menu','Overwrite File','Cancel','Cancel');
                    % Handle response
                    switch choice
                        case 'Overwrite File'
                            self.video_acquisition_started=1;
                        case 'Cancel'
                            self.video_acquisition_started=0;
                    end
                end
                if (self.video_acquisition_started==1)
                            % Uncompressed AVI File
                            % self.video_file = avifile(self.video_file_name,'compression','none','Colormap', gray(256));
                            self.video_file = fopen(self.video_file_name, 'w');
                            % First frame= Current Background image
                            fwrite(self.video_file, uint8(self.BG_image));
                            self.MAX_FRAMES_PER_FILE=round(self.MAX_SIZE_PER_FILE/(780*582));
               
                end
            
            start(self.cam.vid);
        end
        
        % ----- [Derived from Device class]
        % Stop camera acquisition:
        %   - Stops camera acquisition (disable frameacquired callback)
        %   - Save all remaining images
        %   - Closes avi file 
        function stopDevAcqu(self)
            vid=self.cam.vid;
            stop(vid);
            Na = get(vid,'FramesAvailable');
            while (Na>0)
                    fprintf(1,'\n Still %d images to fit.', Na);
                    self.oneFrameAcquired(vid, []);
                    Na = get(vid,'FramesAvailable');
            end
            fprintf(1,'\n %d images were not fitted.', Na);
            % self.video_file = close(self.video_file);
            fclose(self.video_file);
            self.video_acquisition_started=0;
            % reset situation for standard acqu
            % self.setSaveState(1);
            self.N_acquisition_needed=0;
            self.N_images_acquired_since_acqu_start=0;
        end
        
        % ----- [Derived from Device class]
        % ----- Nothing specially required when closing this device
        function closeDevice(self)
            self.stopPreview();
        end
        
        % ----- Functions to handle Tracker Devices, to enable live object
        % Tracking
        function addTrackerDevice(self,hTD)
            hTD.setLinkedDeviceName(self.deviceName);
            self.trackers{length(self.trackers)+1}=hTD;
            set(0, 'CurrentFigure', self.hDeviceGUI);
            self.trackers{length(self.trackers)}.initDisplayedRect(self.imageSize(2),self.imageSize(1));
        end
        
    end
    
end

