classdef Tracker_Device < Device
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
        
        % working image to track
        cutImg;
        % dispImg;
        maskImg;
        
        % ---- GUI 
        slider_Treshold; % gray treshold slider
        toggleButton_above; % toggle level above or not
        toggleButton_fillHoles; % toggle fill holes or not
        toggleButton_setBoxPos; % wait for click notification to recenter box
        waitForSetBoxPos;
        pushButton_autoRecenterBox;
        labelXPos;
        labelYPos;
        toggleButton_setAutoFollowBoxPos;
        editWBox;
        editHBox;
        liveGraph;
        
        % ---- tracking parameter
        linkedDeviceName; % linked device name normally camera
        tr_value;       % treshold in gray value
        level_above;    % defines if mask = value above or below treshold?
        fillHoles;      % fill Holes within mask before tracking?
        Xr0;            %  
        Yr0;            %
        subPixX;        % subpixel resolution for centering
        subPixY;        % subpixel resolution for centering
        followObject;   % if set to 0 live displacement of the tracking square
        
        % ---- Bondary conditions for tracking
        minX;
        minY;
        maxX;
        maxY;
        
        % ---- distant GUI
        hDisplayedRectangle;
        timeStep;
    end
    
    methods
        
        % ----- Constructor of the Tracker device
        function self=Tracker_Device(name_identifier, pos,xS,xE,yS,yE,tr,ab)
            name=name_identifier; % Device name
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,pos);            
            self.deviceType='TRACKER';
            % Sets bounding rectangle for tracking device
            self.startX=xS;
            self.endX=xE;
            self.startY=yS;
            self.endY=yE;
            self.subPixX=0;
            self.subPixY=0;
            % Initialise mask image
            self.maskImg=uint8(zeros(self.endX-self.startX+1,self.endY-self.startY+1));
            % Initialise cutimg
            self.cutImg=uint8(zeros(self.endX-self.startX+1,self.endY-self.startY+1));
            
            self.tr_value=tr;       % treshold value for trakcing
            self.level_above=ab;    % mask = value above or below treshold?
            self.followObject=0;    % auto recenter box over time
            self.fillHoles=0;       % do not fill image holes by default
            
            % Live display of force
            self.liveGraph=AnimatedGraphTracker([], [], 200, 1, -1, 1, 0, 0, 0, 1, 1, 1, 1.2 );
            self.timeStep=0;
            
            % Builds GUI of this device
            self.buildGui();        
            
            % Current tracked position
            self.xPos=0;
            self.yPos=0;
            % Value of middle pixel position -> substracted from tracked
            % value
            self.Xr0=round((self.endX-self.startX)/2+1);            
            self.Yr0=round((self.endY-self.startY)/2+1);
            % Limits of tracked rectangle
            self.minX=1;
            self.minY=1;
            self.maxX=752; % default values, udpated by the camera call
            self.maxY=480;
            self.waitForSetBoxPos=0;
            
            
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
                pos(1)
                pos(2)
                self.setBoxPos(pos(1),pos(2));
                set(self.toggleButton_setBoxPos,'Value',0);
                self.waitForSetBoxPos=0;
            end
        end
        
        % ----- [Derived from Device class]
        %    - Builds GUI for Tracker Device
        function buildGui(self)
                curYPos=10;
                % Slider to set displayed min gray value
                self.slider_Treshold=uicontrol('Style', 'slider',...
                          'Min',0,'Max',256,'Value',self.tr_value,...
                          'Position', [10 curYPos 180 20],...
                          'Callback', {@(src,event)self.sliderTresholdCB(src,event)});
                
                curYPos=curYPos+30;
                % Toggle button to fill holes in tracked image or not
                self.toggleButton_fillHoles = uicontrol('style','togglebutton',...
                                                        'position',[10 curYPos 180 20],...
                                                        'string','FillHoles',...
                                                        'callback',{@(src,event)self.toggleFillHolesCB(src,event)}); %an edit box      
                      
                curYPos=curYPos+20;
                % Toggle button to measure gray level above
                % or under treshold
                % toggleButton measure above
                self.toggleButton_above = uicontrol('style','togglebutton',...
                                            'position',[10 curYPos 180 20],...
                                           'string','Above',...
                                           'callback',{@(src,event)self.toggleLevelAboveCB(src,event)}); %an edit box
                % Initialize value
                set(self.toggleButton_above,'Value',self.level_above);
                
                curYPos=curYPos+50;
                % Label for XPos
                uicontrol('style','text',...
                          'position',[10 curYPos 85 20],...
                          'string','Pos X (pix)');
                % XPos
                self.labelXPos=uicontrol('style','text',...
                          'position',[10 curYPos-20 85 20],...
                          'string','0');
                      
                % Label for YPos
                uicontrol('style','text',...
                          'position',[105 curYPos 90 20],...
                          'string','Pos Y (pix)');
                
                %YPos
                self.labelYPos=uicontrol('style','text',...
                          'position',[105 curYPos-20 90 20],...
                          'string','0');
                
                curYPos=curYPos+25;
                % pushButton recenter box
                self.pushButton_autoRecenterBox = uicontrol('style','pushbutton',...
                                            'position',[10 curYPos 180 20],...
                                            'string','Recenter Box',...
                                            'callback',{@(src,event)self.pushButtonAutoRecenterBoxCB(src,event)}); %an edit box
                curYPos=curYPos+20;
                % toggleButton setBoxPos
                self.toggleButton_setBoxPos = uicontrol('style','togglebutton',...
                                            'position',[10 curYPos 180 20],...
                                           'string','Set Box Position',...
                                           'callback',{@(src,event)self.toggleButtonSetBoxPosCB(src,event)}); %an edit box
                % Initialize value
                set(self.toggleButton_setBoxPos,'Value',0);
                
                curYPos=curYPos+20;
                % toggleButton setAutoFollowBoxPos
                self.toggleButton_setAutoFollowBoxPos = uicontrol('style','togglebutton',...
                                            'position',[10 curYPos 180 20],...
                                           'string','Follow Object',...
                                           'callback',{@(src,event)self.toggleButtonAutoFollowBoxPosCB(src,event)}); %an edit box
                % Initialize value
                set(self.toggleButton_setBoxPos,'Value',0);
                
                curYPos=curYPos+40;
                % Label for WBox
                uicontrol('style','text',...
                          'position',[10 curYPos 85 15],...
                          'string','Width (pix)');
              
                % Edit Text Choose Width
                self.editWBox = uicontrol('style','edit',...
                                            'position',[10 curYPos-15 85 15],...
                                            'string',num2str(self.endX-self.startX),...
                                            'callback',{@(src,event)self.editWBoxCB(src,event)}); %an edit box
                % Label for HBox
                uicontrol('style','text',...
                          'position',[105 curYPos 90 15],...
                          'string','Heigth (pix)');
                
                % Edit Text Choose Height
                self.editHBox = uicontrol('style','edit',...
                                            'position',[105 curYPos-15 90 15],...
                                            'string',num2str(self.endY-self.startY),...
                                            'callback',{@(src,event)self.editHBoxCB(src,event)}); %an edit box
                
                set(0, 'CurrentFigure', self.hDeviceGUI);
                subplot(1,3,3);
                self.liveGraph.showGraph();
             
            
        end
        
        function initDisplayedRect(self, maxX, maxY)
            self.hDisplayedRectangle=rectangle('position',self.getDisplayRect()); 
            self.maxX=maxX;
            self.maxY=maxY;
        end
        
        function sliderTresholdCB(self,src,event)
             TRvalue=get(src,'Value');
             self.setTreshold(TRvalue);
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
        
        function toggleLevelAboveCB(self,src,event)
             self.setAbove(get(src,'Value'));
        end
        
        function toggleFillHolesCB(self,src,event)
             self.setFillHoles(get(src,'Value'));
        end
        
        function toggleButtonSetBoxPosCB(self,src,event)
            val=get(src,'Value');
            if (val==1)
                self.waitForSetBoxPos=1;
            else
                self.waitForSetBoxPos=0;
            end
        end
        
        function toggleButtonAutoFollowBoxPosCB(self,src,event)
            val=get(src,'Value');
            if (val==1)
                self.followObject=1;
            else
                self.followObject=0;
            end
        end
        
        function pushButtonAutoRecenterBoxCB(self,src,event)
            self.autoRecenterBox();
        end
        
        function setTreshold(self,v)
            self.tr_value=v;
        end
        
        function setAbove(self,v)
            self.level_above=v;
        end
        
        function setFillHoles(self,v)
            self.fillHoles=v;
        end
        
        function setFollow(self,v)
            self.followObject=v;
        end
        
        function setWidthBox(self,v)
            dx=(v-(self.endX-self.startX))/2;
            self.endX=round(self.endX+dx);
            self.startX=round(self.startX-dx);
            self.checkBounds();
            self.Xr0=round((self.endX-self.startX)/2+1);
            self.Yr0=round((self.endY-self.startY)/2+1);
            set(self.hDisplayedRectangle,'position',self.getDisplayRect());
        end
        
        function setHeightBox(self,v)
            dy=(v-(self.endY-self.startY))/2;
            self.endY=round(self.endY+dy);
            self.startY=round(self.startY-dy);
            self.checkBounds();
            self.Xr0=round((self.endX-self.startX)/2+1);
            self.Yr0=round((self.endY-self.startY)/2+1);
            set(self.hDisplayedRectangle,'position',self.getDisplayRect());
        end
        
        function autoRecenterBox(self)
            if (isnan(self.xPos)||isnan(self.yPos))
            else
            self.startX=round(self.startX+self.xPos);
            self.startY=round(self.startY+self.yPos);
            self.subPixX=self.xPos;
            self.subPixY=self.yPos;
            self.endX=round(self.endX+self.xPos);
            self.endY=round(self.endY+self.yPos);
            self.checkBounds();
            end
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
            %rect=[self.startX (self.maxY-self.endY) (self.endX-self.startX) (self.endY-self.startY)];
            rect=[self.startX self.startY (self.endX-self.startX) (self.endY-self.startY)];
        end
        
        function setLinkedDeviceName(self,name)
            self.linkedDeviceName=name;
        end
        
        % ----- [Derived from Device class]
        %   - Writes
        function writeLogHeader(self)
            fprintf(self.hLogFile, ['Linked to ' self.linkedDeviceName char(13)]);
            fprintf(self.hLogFile, ['Position is in pixel size.' char(13)]);
            fprintf(self.hLogFile, ['Above:' num2str(self.level_above) char(13)]);
            fprintf(self.hLogFile, ['Treshold:' num2str(self.tr_value) char(13)]);
            fprintf(self.hLogFile, ['Data:' char(9) 'Xr ' char(9) 'Yr ' char(9) 'X absolute' char(9) 'Y absolute' char(13)]);
           
        end
        
        % ----- [Derived from Device class]
        function startDevAcqu(self)
        end
        
        % ----- [Derived from Device class]
        function stopDevAcqu(self)
        end
        
        function showPreview(self)
            set(0, 'CurrentFigure', self.hDeviceGUI);
            subplot(1,3,2);
            %imshow(flipdim(flipdim(self.maskImg,2),1));
            imshow(self.maskImg);
            xP=self.xPos-self.subPixX;
            yP=self.yPos-self.subPixY;
            set(self.labelXPos,'String',num2str(xP));
            set(self.labelYPos,'String',num2str(yP));
            self.timeStep=self.timeStep+1;
            self.liveGraph.appendNewPt(self.timeStep,xP);
            subplot(1,3,3);
            self.liveGraph.updateGraph();
        end
        
        function Track(self,data)
            % data is a 2D matrix
            % it is very important to not modify data, so that matlab does
            % not have to copy the whole data each time this function is
            % called

            self.cutImg = data(self.startY:self.endY,self.startX:self.endX);
            
            if (self.level_above==0)
                self.maskImg=(self.cutImg<self.tr_value);
            else
                self.maskImg=(self.cutImg>=self.tr_value);
            end
            
            if (self.fillHoles==0)
            else
                self.maskImg=imfill(self.maskImg,'holes');
            end
            
            Area=sum(sum(self.maskImg));
            if (Area~=0)

                stats=regionprops(double(self.maskImg), 'Centroid');
                tamp=stats.Centroid;
                self.xPos=(tamp(1)-(self.Xr0));
                self.yPos=(tamp(2)-(self.Yr0));

            else
                self.xPos=NaN;
                self.yPos=NaN;
            end
            
            str=['Track Pos' char(9) num2str(self.xPos) char(9) num2str(self.yPos) char(9) num2str(self.xPos+self.Xr0+self.startX) char(9) num2str(self.yPos+self.Yr0+self.startY) ];
            
            
            self.saveLog(str); % Save tracking position into the log file linked to this device
            
            if (self.followObject==1)
                self.autoRecenterBox();
            end

        end
        
    end
    
end

