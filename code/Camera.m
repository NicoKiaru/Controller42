classdef Camera < handle
    %Camera Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        % values for Guppy camera, in the 42 setup, Roux lab 
        adaptorname;
        deviceID;
        format;
        vid;
        src;
        vidRes;
    end
    
    methods
        function CAM=Camera(AN,dID,F) % set video input properties
            CAM.adaptorname=AN;
            CAM.deviceID=dID;
            CAM.format=F;
        end
        
        function init(CAM) % initialise camera device
            CAM.vid = videoinput(CAM.adaptorname, CAM.deviceID, CAM.format);
            CAM.src = getselectedsource(CAM.vid);
            CAM.vidRes = get(CAM.vid, 'VideoResolution');
        end
        
        function setGain(CAM,gain) %0 to 680 for Guppy Camera
            CAM.src.Gain=gain;
        end
        
        function setExpTime(CAM,exptime) %exposure time in millisec
            CAM.src.ExtendedShutter=exptime*1000; 
        end
    end
    
end

