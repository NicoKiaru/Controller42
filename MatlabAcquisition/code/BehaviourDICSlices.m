classdef BehaviourDICSlices < Behaviour
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    %   behave=BehaviourDICSlices(  c42.getDeviceByName('NIKON_TI'),...
    %                               c42.getDeviceByName('SHUTTER_DIC'),...
    %                               c42.getDeviceByName('SHUTTER_CAM'),...
    %                               c42.getDeviceByName('CAMERA_GUPPY'),5,2,0.34);
 
    
    properties
        und;
        und_listener;
        nikonController;
        dicShutter;
        camShutter;
        cam_Device;
        cam_Device_listener;
        t;
        timer_launched;
        message;
        cancel;
        zrange;
        zstep;
        delayBeforeDICAcqu;
        editZRange;
        editZStep;
        editDelay; 
        toggleImagesAcquired;
    end
    
    methods
         function self=BehaviourDICSlices(pos,nC,dS,cS,cD,deltime,zr,zs)
            self = self@Behaviour('DIC Slices',pos);
            self.nikonController=nC;
            self.dicShutter=dS;
            self.camShutter=cS;
            self.cam_Device=cD;
            self.timer_launched=0;
            self.message='Starting DIC Acquisition...';
            self.cancel=0;
            self.zrange=zr; % + and minus
            self.zstep=zs;
            self.delayBeforeDICAcqu=deltime;
            self.updateGUIChild;
            %listen_imagesAcquired
            %addlistener(self.cam_Device,'all_images_acquired',@self.listen_imagesAcquired);
         end
         
         function execute(self)
             self.cam_Device.setSaveState(0);
             self.und_listener=addlistener(self.camShutter,'Shutter_Pos_Increased',@self.listen_newUserNotification);
             self.t = timer('TimerFcn', {@(src,event)self.exec_after_delay(src,event)},'StartDelay',self.delayBeforeDICAcqu);
             self.cancel=0;
             self.cam_Device.N_acquisition_needed=1;
         end
         %------------------------- GUI
         function buildPanel(self,p)
              % zrange;
              % zstep;
              % delayBeforeDICAcqu;
              % Label ZRange
              uicontrol('Parent',p,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                        'units','normalized','position',[0 0 0.5 0.3],'string','ZRange (µm)');
              % Edit ZRange
              self.editZRange=uicontrol('Parent',p,'style','edit',...
                        'units','normalized','position',[0.5 0 0.5 0.3],...
                        'callback',{@(src,event)self.setZRangeCB(src,event)}); %an edit box
              % Label ZStep
              uicontrol('Parent',p,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                        'units','normalized','position',[0 0.3 0.5 0.3],'string','ZStep (µm)');
              % Edit ZStep
              self.editZStep=uicontrol('Parent',p,'style','edit',...
                        'units','normalized','position',[0.5 0.3 0.5 0.3],...
                        'callback',{@(src,event)self.setZStepCB(src,event)}); %an edit box
              % Label Delay
              uicontrol('Parent',p,'style','text','BackgroundColor',[0.9 0.9 0.9], 'HorizontalAlignment','left',...
                        'units','normalized','position',[0 0.6 0.5 0.3],'string','Delay (s)');
              % Edit Delay
              self.editDelay=uicontrol('Parent',p,'style','edit',...
                        'units','normalized','position',[0.5 0.6 0.5 0.3],...
                        'callback',{@(src,event)self.setDelayCB(src,event)}); %an edit box
             
         end
         function setZRangeCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                if (H>0)
                self.zrange=H;
                end
            else
                self.updateGUIChild;
            end
         end
         function setZStepCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                if (H>0)
                self.zstep=H;
                end
            else
                self.updateGUIChild;
            end
         end
         function setDelayCB(self,src,event)
            [H, status] = str2num(get(src,'string'));
            if (status==1)
                if (H>0)
                    self.delayBeforeDICAcqu=H;
                    if (self.isRunning==1)
                        self.stop;
                        self.exec;
                    end
                end
            else
                self.updateGUIChild;
            end
         end
         function updateGUIChild(self)
             set(self.editDelay,'string',num2str(self.delayBeforeDICAcqu));
             set(self.editZStep,'string',num2str(self.zstep));
             set(self.editZRange,'string',num2str(self.zrange));
         end
         %-------------------------- END OF GUI
         
         function interrupt(self)
             delete(self.und_listener);
             if (self.timer_launched==1)
                stop(self.t);
                delete(self.t);
                self.timer_launched=0;
             end
             self.cancel=1;
             self.cam_Device.setSaveState(1);
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
         function notify_imagesAcquired(self)
               self.toggleImagesAcquired=1;
               disp('notif recue');
         end
        
         function exec_after_delay(self,src,event)
             disp(self.message);
             if (self.cam_Device.video_acquisition_started==1)
                self.nikonController.saveState();
                self.nikonController.setFB2(2);
                self.dicShutter.shutterON();
                zi=self.nikonController.getZ();
                zslices=(zi-self.zrange):self.zstep:(zi+self.zrange);
                    for z=zslices
                        self.nikonController.setZ(z);
                        pause(0.3);
                        if (self.cancel==1)
                            disp('DIC acquisition cancelled!');
                            return
                        end
                        self.toggleImagesAcquired=0;
                        self.cam_Device.setSaveState(1);
                        Ntry=0; % 5 sec max                        
                        while (self.toggleImagesAcquired==0)&&(Ntry<50)
                            pause(0.1);
                            Ntry=Ntry+1;
                            %disp(Ntry);
                            if (self.cancel==1)
                               disp('DIC acquisition cancelled!');
                               return
                            end
                            % ca pause
                        end
                    end
                self.dicShutter.shutterOFF();
                self.nikonController.restoreState();
                disp('DIC acquisition finished!');
                %self.cam_Device.setSaveState(1);
             else
                 disp('Acquisition not started!');
             end
             stop(self.t);
             self.timer_launched=0;
         end
    end
    

    
end

