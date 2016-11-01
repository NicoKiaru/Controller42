classdef BehaviourAutofocus < Behaviour
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    %   behave=BehaviourAutofocus(  c42.getDeviceByName('NIKON_TI'),...
    %                               c42.getDeviceByName('SHUTTER_DIC'),...
    %                               c42.getDeviceByName('SHUTTER_CAM'),...
    %                               c42.getDeviceByName('CAMERA_GUPPY'),5,2,0.34);
    
    properties
        und_listener;           % Shutter UP listener
        nikonController;        % Handle to microscope controller
        camShutter;             % Handle to cam spinning disc lsitener
        af_Device;              % Handle to autofocus device
        t;                      % timer
        timer_launched;         % Flag if the timer is enabled or not
        message;                % Message displayed during acquisition
        delayBeforeAutofocus;   % Delay in s between starting acqu in spinning and autofocus
        editDelay;              % GUI edit delay box
        zTarget;
        editZTarget;
        pushButton_GetZ;
        pushButton_GotoZ;
    end
    
    methods
         function self=BehaviourAutofocus(pos,nC,afD,cS,deltime)
            self = self@Behaviour('Autofocus',pos);     % Calls super constructor
            self.nikonController=nC;                    % Stored handle Nikon
            self.af_Device=afD;                        % Store handle autofocus
            self.camShutter=cS;                         % store handle cam shutter
            self.timer_launched=0;                      % Listener not enabled by default
            self.message='Starting Autofocus...';       % Message displayed
            self.delayBeforeAutofocus=deltime;          % Stored input delay time
            self.zTarget=0;
            self.updateGUIChild;                        % Initilize GUI            
         end
         
         function execute(self)     
             
             self.und_listener=addlistener(self.camShutter,'Shutter_Pos_Increased',@self.listen_newUserNotification);
             self.t = timer('TimerFcn', {@(src,event)self.exec_after_delay(src,event)},'StartDelay',self.delayBeforeAutofocus);
         end
         %------------------------- GUI
         function buildPanel(self,p)
              % delayBeforeDICAcqu;
              % Label ZTarget
              uicontrol('Parent',p,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                        'units','normalized','position',[0 0.3 0.5 0.3],'string','ZTarget (µm)');
              % Edit ZTarget
              self.editZTarget=uicontrol('Parent',p,'style','edit',...
                        'units','normalized','position',[0.5 0.3 0.5 0.3],...
                        'callback',{@(src,event)self.setZTargetCB(src,event)}); %an edit box
              % Label Delay
              uicontrol('Parent',p,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                        'units','normalized','position',[0 0.6 0.5 0.3],'string','Delay (s)');
              % Edit Delay
              self.editDelay=uicontrol('Parent',p,'style','edit',...
                        'units','normalized','position',[0.5 0.6 0.5 0.3],...
                        'callback',{@(src,event)self.setDelayCB(src,event)}); %an edit box
              % Button Get Current Z
              self.pushButton_GetZ = uicontrol('Parent',p,'style','pushbutton','string','GetZ',...
                                                    'units','normalized', 'position',[0 0 0.5 0.3],...
                                                    'callback',{@(src,event)self.getZCB(src,event)}); %an edit box                                                
              % Button Get Current Z
              self.pushButton_GotoZ = uicontrol('Parent',p,'style','pushbutton','string','Goto Ztarget',...
                                                    'units','normalized', 'position',[0.5 0 0.5 0.3],...
                                                    'callback',{@(src,event)self.gotoZCB(src,event)}); %an edit box
             
         end
         function listen_newUserNotification(self,src,event)
             if (self.timer_launched==1)
              % disp('Nouvelle notification, arrive trop tot!');
             else
              % disp('Vous avez recu une nouvelle notification');
               self.timer_launched=1;
               start(self.t);               
             end
         end
         function setDelayCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                if (H>0)
                    self.delayBeforeAutofocus=H;
                    if (self.isRunning==1)
                        self.stop;
                        self.exec;
                    end
                end
            else
                self.updateGUIChild;
            end
         end
         function getZCB(self,src,event)
             self.getZ();
         end
         function gotoZCB(self,src,event)
             self.gotoZTarget();
         end
         function setZTargetCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
               
                self.zTarget=H;

            else
                self.updateGUIChild;
            end
         end
         function updateGUIChild(self)
             set(self.editDelay,'string',num2str(self.delayBeforeAutofocus));
             set(self.editZTarget,'string',num2str(self.zTarget));
         end
         %-------------------------- END OF GUI
         
         function interrupt(self)
             delete(self.und_listener);
             if (self.timer_launched==1)
                stop(self.t);
                delete(self.t);
                self.timer_launched=0;
             end
%             self.cancel=1;
%             self.cam_Device.setSaveState(1);
         end
         
         function getZ(self)
             iFB2=self.nikonController.getFB2();%saveState();
             self.nikonController.setFB2(2);
             pause(0.5);
                 self.zTarget=self.af_Device.getMPZ;
             pause(0.5);
             self.nikonController.setFB2(iFB2);
             %self.nikonController.restoreState();
             self.updateGUIChild;
         end
         function gotoZTarget(self)
             iFB2=self.nikonController.getFB2();%saveState();
             self.nikonController.setFB2(2);
             pause(0.8);
                self.af_Device.goto(self.zTarget);
             pause(0.8);
                self.af_Device.goto(self.zTarget);
             pause(0.8);
                self.af_Device.goto(self.zTarget);
             pause(0.8);
             self.nikonController.setFB2(iFB2);
             self.updateGUIChild;
         end
     
         function exec_after_delay(self,src,event)
            disp(self.message);
            self.gotoZTarget();         
            disp('Autofocus finished!');
            stop(self.t);
            self.timer_launched=0;
         end
    end
    

    
end

