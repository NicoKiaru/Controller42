classdef NikonTIControl_Device < Device
    properties
        mmc;
        cState; % current state
        sState; % saved state
        % initial state variable
        iZ;   % initial Z position (TiZDrive)
        iFB1; % initial FilterBlock1 Position
        iFB2; % initial FilterBlock2 Position
        % current state variable
        cZ;   % initial Z position (TiZDrive)
        cFB1; % initial FilterBlock1 Position
        cFB2; % initial FilterBlock2 Position
    end
    
    events 
    end
    
    methods
          function self=NikonTIControl_Device(name_identifier, position)
            name=name_identifier;
            
            % Calls superclass constructor with GUI size as an input
            self = self@Device(name,position);            
            self.deviceType='NIKONTICONTROL';
            %pause(1);
            %javaclasspath({'C:/Micro-Manager/ij.jar','C:/Micro-Manager/plugins/Micro-Manager/MMCoreJ.jar','C:/Micro-Manager/plugins/Micro-Manager/MMJ_.jar','C:/Micro-Manager/plugins/Micro-Manager/bsh-2.0b4.jar','C:/Micro-Manager/plugins/Micro-Manager/swingx-0.9.5.jar'});
            %pause(1);
             import mmcorej.*;
             self.mmc=CMMCore;
             self.mmc.loadSystemConfiguration ('C:\Micro-Manager-1.4\Nikon_ZDrive.cfg');
             self.buildGui();
            % set listener for this device
            
          end
          
          function setZ(self,zpos)
              % set ZDrive position
              %['On set à ' num2str(zpos) ' um']
              self.mmc.setPosition('TIZDrive',zpos);
          end
          
          function zpos=getZ(self)
              % get ZDrive position
              %disp('On mesure Zpos');
              zpos=self.mmc.getPosition('TIZDrive');
          end
          
          function setFB1(self,pFB1)
              % set FilterBlock1 position
              %disp('On set FB1');     
               self.mmc.setState('TIFilterBlock1',pFB1);
          end
          
          function pFB1=getFB1(self)
              % set FilterBlock1 position
              %disp('On mesure FB1');
              pFB1=self.mmc.getState('TIFilterBlock1');
          end
          
          function setFB2(self,pFB2)
              % set FilterBlock1 position
              %disp('On set FB2');
              self.mmc.setState('TIFilterBlock2',pFB2);
          end
          
          function pFB2=getFB2(self)
              % set FilterBlock1 position
              %disp('On mesure FB2');
              pFB2=self.mmc.getState('TIFilterBlock2');;
          end
          
          function saveState(self)
              self.sState.z=self.getZ();
              self.sState.pFB1=self.getFB1();
              self.sState.pFB2=self.getFB2();
          end
          
          function restoreState(self)
              self.setZ(self.sState.z);
              self.setFB1(self.sState.pFB1);
              self.setFB2(self.sState.pFB2);
          end
          
          function closeDevice(self)
          end         
          function writeLogHeader(self)
          end
          function buildGui(self)
          end
          function startDevAcqu(self)
          end
          function stopDevAcqu(self)
          end          
    end
    
    methods(Static)
    end
end
    