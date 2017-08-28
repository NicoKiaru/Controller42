classdef Tracker_Focus_Device < Device
    %Tracker_Device Summary of this class goes here
    %   Software device doing Live tracking of object in image flux
    %   In particular made to track bead position for optical tweezer
    %   This tracker is based on measuring the center of mass of an object
    
    properties
        xPos;
        yPos;
        zPos;
        
        % Position of the tracking rectangle
        startX;
        endX;
        startY;
        endY;
        nikonController;
        
        % working image to track
        cutImg;
        % dispImg;
        % maskImg; USELESS
        
        % ---- GUI 
        % slider_Treshold; % gray treshold slider
        % toggleButton_above; % toggle level above or not
        toggleButton_setBoxPos; % wait for click notification to recenter box
        waitForSetBoxPos;
        % pushButton_autoRecenterBox;
       
        % labelYPos;
        % toggleButton_setAutoFollowBoxPos;
        editWBox;
        editHBox;
        editZRange;
        editZStep;
        labelZPos;
        pushButton_getCurPos;
        pushButton_acquZImgs;
        
        % ---- tracking parameter
        % tr_value;       % treshold in gray value
        % level_above;    % defines if mask = value above or below treshold?
        % Xr0;            %  
        % Yr0;            %
        % followObject;   % if set to 0 live displacement of the tracking square
        
        % ---- Bondary conditions for tracking
        minX;
        minY;
        maxX;
        maxY;
        
        % ---- Focus parameter
        zRange; % range over which to acquire data
        zStep; % steps performed
        storedZRange;
        storedZStep;
        Range;
        NZ;
        storedNZ;
        TP; % trackpos object
        toggleImagesAcquired; % for image acquisition
        
        % ---- distant GUI
        hDisplayedRectangle;
    end
    
    methods
        
        % ----- Constructor of the Tracker device
        function self=Tracker_Focus_Device(name_identifier, pos,xS,xE,yS,yE,nC,zr,zs)
            name=name_identifier; % Device name

            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,pos);            
            self.deviceType='TRACKER_FOCUS';
            % Sets bounding rectangle for tracking device
            self.startX=xS;
            self.endX=xE;
            self.startY=yS;
            self.endY=yE;
            
            % Initialise mask image
            % self.maskImg=uint8(zeros(self.endX-self.startX+1,self.endY-self.startY+1));
            % Initialise cutimg
            self.cutImg=uint8(zeros(self.endX-self.startX+1,self.endY-self.startY+1));
            
            % self.tr_value=tr;       % treshold value for trakcing
            % self.level_above=ab;    % mask = value above or below treshold?
            % self.followObject=0;    % auto recenter box over time
            self.zRange=zr;
            self.zStep=zs;           
            % Builds GUI of this device
            self.buildGui();        
            
            % Current tracked position
            self.zPos=0;

            % Limits of tracked rectangle
            self.minX=1;
            self.minY=1;
            self.maxX=640;
            self.maxY=480;
            
            self.waitForSetBoxPos=0;
            self.nikonController=nC;
            

        end
        
        % ----- [Derived from Device class]
        %    - Nothing specially required to close Tracker Device
        function closeDevice(self)
        end
        
        % Function called by devices that signals a mouse click on the
        % displayed image
        % pure GUI function
        function clickNotify(self,pos)
            if (self.waitForSetBoxPos==1)
                self.setBoxPos(pos(1),pos(2));
                set(self.toggleButton_setBoxPos,'Value',0);
                self.waitForSetBoxPos=0;
            end
        end
        
        % ----- [Derived from Device class]
        %    - Builds GUI for Tracker Device
        function buildGui(self)
                % Button to acquire zImages
                self.pushButton_acquZImgs = uicontrol('style','pushbutton','units','normalized',...
                                            'position',[0 0.7 0.5 0.1],...
                                            'string','Acquire Z Images',...
                                            'callback',{@(src,event)self.getZImagesCB(src,event)}); %an edit box
                % Button to get current Pos
                self.pushButton_getCurPos = uicontrol('style','pushbutton','units','normalized',...
                                            'position',[0 0.6 0.5 0.1],...
                                            'string','Get Z Pos',...
                                            'callback',{@(src,event)self.getZCB(src,event)}); %an edit box
                % Label for ZPos
                uicontrol('style','text','units','normalized',...
                          'position',[0 0.5 0.25 0.1],...
                          'string','ZPos (um)');
                
                self.labelZPos=uicontrol('style','text','units','normalized',...
                          'position',[0.25 0.5 0.25 0.1],...
                          'string','0');
                % Label for ZRange
                uicontrol('style','text','units','normalized',...
                          'position',[0 0.4 0.25 0.1],...
                          'string','ZRange (um)');
                % Zrange
                self.editZRange=uicontrol('style','edit','units','normalized',...
                          'position',[0 0.3 0.25 0.1],...
                          'string',num2str(self.zRange),...
                          'callback',{@(src,event)self.editZRangeCB(src,event)});       
                % Label for ZStep
                uicontrol('style','text','units','normalized',...
                          'position',[0.25 0.4 0.25 0.1],...
                          'string','ZStep (um)');
                % ZStep
                self.editZRange=uicontrol('style','edit','units','normalized',...
                          'position',[0.25 0.3 0.25 0.1],...
                          'string',num2str(self.zStep),...
                          'callback',{@(src,event)self.editZStepCB(src,event)});                  
                % toggleButton setBoxPos
                self.toggleButton_setBoxPos = uicontrol('style','togglebutton','units','normalized',...
                                            'position',[0 0 0.5 0.1],...
                                           'string','Set Box Position',...
                                           'callback',{@(src,event)self.toggleButtonSetBoxPosCB(src,event)}); %an edit box
                % Initialize value
                set(self.toggleButton_setBoxPos,'Value',0);     
         
                % Label for WBox
                uicontrol('style','text','units','normalized',...
                          'position',[0 0.2 0.25 0.1],...
                          'string','Width (pix)');
              
                % Edit Text Choose Width
                self.editWBox = uicontrol('style','edit','units','normalized',...
                                            'position',[0 0.1 0.25 0.1],...
                                            'string',num2str(self.endX-self.startX),...
                                            'callback',{@(src,event)self.editWBoxCB(src,event)}); %an edit box
                % Label for HBox
                uicontrol('style','text','units','normalized',...
                          'position',[0.25 0.2 0.25 0.1],...
                          'string','Heigth (pix)');
                
                % Edit Text Choose Height
                self.editHBox = uicontrol('style','edit','units','normalized',...
                                            'position',[0.25 0.1 0.25 0.1],...
                                            'string',num2str(self.endY-self.startY),...
                                            'callback',{@(src,event)self.editHBoxCB(src,event)}); %an edit box
             
            
        end
        
        function [p] = quadint(ym1,y0,yp1) 
        %QINT - quadratic interpolation of three adjacent samples
        %
        % [p,y,a] = qint(ym1,y0,yp1) 
        %
        % returns the extremum location p, height y, and half-curvature a
        % of a parabolic fit through three points. 
        % Parabola is given by y(x) = a*(x-p)^2+b, 
        % where y(-1)=ym1, y(0)=y0, y(1)=yp1. 
        p = (yp1 - ym1)/(2*(2*y0 - yp1 - ym1)); 
        %y = y0 - 0.25*(ym1-yp1)*p;
        %a = 0.5*(ym1 - 2*y0 + yp1);
        end
        
        function [zans,zp,corcoef,listcoef]=getMPZ(self) % returns most probable z with coeff cor
            [zp,corcoef,listcoef]=self.TP.findMaxCorPosZ_Global(self.cutImg);
            if (zp>1) && (zp<self.storedNZ-1)
                ym1=listcoef(zp-1);
                y0=listcoef(zp);
                yp1=listcoef(zp+1);
                zp=zp+(yp1 - ym1)/(2*(2*y0 - yp1 - ym1));%quadint(listcoef(zp-1),listcoef(zp),listcoef(zp+1));                
            end            
            zans=(zp*self.storedZStep)-self.storedZRange;
            if (zans>self.storedZRange)
                zans=self.storedZRange;
            end
            if (zans<-self.storedZRange)
                zans=-self.storedZRange;
            end
            set(self.labelZPos,'string',num2str(zans));
        end
        
        function goto(self,ztarget)
            % ztarget should be in micron
            % measure current position
            zmAF=(self.getMPZ());
            zcur=self.nikonController.getZ();
            displacement=ztarget-zmAF;
            self.nikonController.setZ(zcur+displacement);
            %zcur=self.nikonController.getZ();
            %set(self.labelZPos,'string',num2str(ztarget));
        end
        
        function acquireZImages(self)
            self.Range=-self.zRange:self.zStep:self.zRange;
            self.storedZRange=self.zRange;
            self.storedZStep=self.zStep;
            self.NZ=length(self.Range);
            self.storedNZ=self.NZ;
            disp('Initialisation de la serie d images');
            self.TP=TrackPos(self.endY-self.startY+1,self.endX-self.startX+1,self.NZ);
                zi=self.nikonController.getZ();
                zslices=(zi-self.zRange):self.zStep:(zi+self.zRange);
                zind=0;
                    for z=zslices
                        zind=zind+1;
                        self.nikonController.setZ(z);
                        pause(0.3);
                        self.toggleImagesAcquired=0;
                        Ntry=0; % 5 sec max                        
                        while (self.toggleImagesAcquired==0)&&(Ntry<50)
                            pause(0.1);
                            Ntry=Ntry+1;
                            %disp(Ntry);
                            % ca pause
                        end
                        disp('Image acquise');
                        self.TP.SetZImage(zind,self.cutImg);
                        %disp(mean(self.cutImg));
                    end
               self.nikonController.setZ(zi);
            
        end
        
        function initDisplayedRect(self)
            self.hDisplayedRectangle=rectangle('position',self.getDisplayRect()); 
        end
        
        function editWBoxCB(self,src,event)
            [W, status] = str2num(get(src,'string'));
            if (status==1)
                self.setWidthBox(W);
            else
                set(src,'string',num2str(self.endX-self.startX));
            end
        end
        
        function editHBoxCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setHeightBox(H);
            else
                set(src,'string',num2str(self.endY-self.startY));
            end
        end
        
        function toggleButtonSetBoxPosCB(self,src,event)
            val=get(src,'Value');
            if (val==1)
                self.waitForSetBoxPos=1;
            else
                self.waitForSetBoxPos=0;
            end
        end
        function getZCB(self, src, event)
            self.getMPZ();
        end
        function getZImagesCB(self, src, event)
            self.acquireZImages();
        end
        function editZRangeCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setZRange(H);
            else
                set(src,'string',self.zRange);
            end
        end
        function editZStepCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                self.setZStep(H);
            else
                set(src,'string',self.zStep);
            end
        end
        function setZRange(self,v)
            self.zRange=v;
        end
        function setZStep(self,v)
            self.zStep=v;
        end
        function setWidthBox(self,v)
            dx=(v-(self.endX-self.startX))/2;
            self.endX=round(self.endX+dx);
            self.startX=round(self.startX-dx);
            self.checkBounds();
            set(self.hDisplayedRectangle,'position',self.getDisplayRect());
        end
        
        function setHeightBox(self,v)
            dy=(v-(self.endY-self.startY))/2;
            self.endY=round(self.endY+dy);
            self.startY=round(self.startY-dy);
            self.checkBounds();
            set(self.hDisplayedRectangle,'position',self.getDisplayRect());
        end
        
        function setBoxPos(self,xr,yr)
            w=self.endX-self.startX;
            h=self.endY-self.startY;
            self.startX=round(xr);
            self.startY=round(yr);
            self.endX=round(xr+w);
            self.endY=round(yr+h);
            self.checkBounds();
            set(self.hDisplayedRectangle,'position',self.getDisplayRect());
        end
        
        function checkBounds(self)
            w=self.endX-self.startX;
            h=self.endY-self.startY;
            if (self.endX>self.maxX)
                self.endX=self.maxX;
                self.startX=self.endX-w;
            end
            if (self.startX<self.minX)
                self.startX=1;
                self.endX=self.startX+w;
            end
            if (self.endY>self.maxY)
                self.endY=self.maxY;
                self.startY=self.endY-h;
            end
            if (self.startY<self.minY)
                self.startY=1;
                self.endY=self.startY+h;
            end
        end
        
        function rect=getRect(self)
            rect=[self.startX self.startY (self.endX-self.startX) (self.endY-self.startY)];
        end
        
        function rect=getDisplayRect(self)
            rect=[self.startX (480-self.endY) (self.endX-self.startX) (self.endY-self.startY)];
        end
        
        % ----- [Derived from Device class]
        %   - Writes
        function writeLogHeader(self)
        end
        
        % ----- [Derived from Device class]
        function startDevAcqu(self)
        end
        
        % ----- [Derived from Device class]
        function stopDevAcqu(self)
        end
        
        function showPreview(self)
            set(0, 'CurrentFigure', self.hDeviceGUI);
            subplot(1,2,2);
            imshow(flipdim(self.cutImg,1));
        end
        
        function Track(self,data)
            % data is a 2D matrix
            % it is very important to not modify data, so that matlab does
            % not have to copy the whole data each time this function is
            % called

            self.cutImg = data(self.startY:self.endY,self.startX:self.endX);
            self.toggleImagesAcquired=1;
            %if (self.level_above==0)
            %    self.maskImg=(self.cutImg<self.tr_value);
            %else
            %    self.maskImg=(self.cutImg>=self.tr_value);
            %end
            %Area=sum(sum(self.maskImg));
            %if (Area~=0)

            %    stats=regionprops(double(self.maskImg), 'Centroid');
            %    tamp=stats.Centroid;
            %    self.xPos=(tamp(1)-self.Xr0);
            %    self.yPos=(tamp(2)-self.Yr0);

            %else
            %    self.xPos=NaN;
            %    self.yPos=NaN;
            %end
            
            %str=['Track Pos' char(9) num2str(self.xPos) char(9) num2str(self.yPos) char(9) num2str(self.xPos+self.Xr0+self.startX) char(9) num2str(self.yPos+self.Yr0+self.startY) ];
            
            
            %self.saveLog(str); % Save tracking position into the log file linked to this device
            
            %if (self.followObject==1)
            %    self.autoRecenterBox();
            %end
        end
        
    end
    
end

