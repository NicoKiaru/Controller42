classdef TrackPos < handle
    % TrackPos
    %
    %   Written by Nicolas Chiaruttini on 10/2011
    %   The purpose of this class is to track position of various devices
    %   thanks to image correlation.
    %
    %   Originally designed for two purpose in microscopy:
    %       - 1 creating an autofocus thanks to the reflection of an
    %       optical tweezer on a glass slide
    %       - 2 Keeping micromanipulators at fixed X Y Z position for a
    %       long period of time
    %   
    %   In both case displacement of micromanipulators or glass surface
    %   creates a characteristic variation in wide field images.
    %   
    %   Thanks to the capture of images of these devices at several
    %   position and to correlation calculation of the live image, the
    %   actual position of the device can be deduced
    %   
    %   The definition of a function controlling the device position can be
    %   used to set a feedback and to keep focus or to maintain the
    %   position of the device for a long period of time
    %
    %   Image collection is 8 bit grayscale
    %   
    %   Z position is tracked according to Z_image_collection
    %   X,Y position is tracked according to translated Z_image_collection
    
    properties
        currX;                      % Current X position
        currY;                      % Current Y position
        currZ;                      % Current Z position
        Z_image_collection;         % Collection of z images of trackobject
        int2_Z_image_collection;    % Precomputed integral signal squared for correlation coefficient computation
        Z_step_size;                % step size between Z images -> in microns
        X_pix_size;                 % Size of one X pixel
        Y_pix_size;                 % Size of one Y pixel
        SX;                         % Number of pixels in X
        SY;                         % Number of pixels in Y
        SZ;                         % Number of Z slices
        CurZ;
        
    end
   % methods (Static)
      %function obj = loadobj(obj)
   %       disp 'loading';
        % if isstruct(obj)
    %                   disp 'loading2';
            % Call default constructor
           % newObj = TrackPos(obj.TX,obj.TY,obj.TZ);
            % Assign property values from struct
           % newObj.currX = obj.currX;
           % newObj.currY = obj.currY;
           % newObj.currZ = obj.currZ;
           % newObj.Z_step_size=obj.Z_step_size;                % step size between Z images -> in microns
           % newObj.X_pix_size=obj.X_pix_size;                 % Size of one X pixel
           % newObj.Y_pix_size=obj.Y_pix_size;
           % newObj.Z_image_collection=zeros(obj.TX,obj.TY,obj.TZ);
           % newObj.int2_Z_image_collection=zeros(1,obj.TZ);
           % newObj.Z_image_collection=obj.Z_image_collection;         % Collection of z images of trackobject
           % newObj.int2_Z_image_collection=obj.int2_Z_image_collection;
           % newObj.SX=obj.SX;                         % Number of pixels in X
           % newObj.SY=obj.SY;                         % Number of pixels in Y
           % newObj.SZ=obj.SZ;                         % Number of Z slices
           % newObj.CurZ=obj.CurZ;
           % obj = newObj;
        % end
     % end
   %end
    methods
        
        function TP=TrackPos(TX,TY,TZ) % default constructor
            TP.SX=TX;
            TP.SY=TY;
            TP.SZ=TZ;
            TP.Z_image_collection=zeros(TX,TY,TZ);
            TP.int2_Z_image_collection=zeros(1,TZ);
            TP.CurZ=0;
        end
        
        function TP=SetZImage(TP,Z,Img)
            %TP.CurZ=TP.CurZ+1;
            Img=double(Img);
            TP.Z_image_collection(:,:,Z)=Img;
            TP.int2_Z_image_collection(1,Z)=sum(sum(Img.*Img));
        end
        
        function [POS,R,LC]=findMaxCorPosZ_Global(TP,ImgTest)
            % Search for the Z slice in collection which correlates the
            % best with the ImgTest, along Z axis
            % Global = test all images
            LC=zeros(1,TP.SZ);
            ImgTest=double(ImgTest);
            int2_ImgTest=sum(sum(ImgTest.*ImgTest)); % Computed only once
            MaxCor=0;
            for i=1:TP.SZ
                TestCor=ImgTest.*TP.Z_image_collection(:,:,i);
                TestCor=sum(sum(TestCor));
                TestCor=TestCor*TestCor;
                TestCor=TestCor/(int2_ImgTest*TP.int2_Z_image_collection(1,i));
                LC(1,i)=TestCor;
                if (TestCor>MaxCor) 
                    MaxCor=TestCor;
                    R=MaxCor;
                    POS=i;
                end
            end
        end
      %function obj = saveobj(obj)
        %            disp 'saving';
        % s.currX = obj.currX;
        % s.currY = obj.currY;
        % s.currZ = obj.currZ;
        %    s.Z_step_size=obj.Z_step_size;                % step size between Z images -> in microns
        %    s.X_pix_size=obj.X_pix_size;                 % Size of one X pixel
        %    s.Y_pix_size=obj.Y_pix_size; 
        %    s.Z_image_collection=obj.Z_image_collection;         % Collection of z images of trackobject
        %    s.int2_Z_image_collection=obj.int2_Z_image_collection;
        %            s.SX=obj.SX;                         % Number of pixels in X
        %s.SY=obj.SY;                         % Number of pixels in Y
        %s.SZ=obj.SZ;                         % Number of Z slices
        %s.CurZ=obj.CurZ;
        % obj = s;
      %end
       
       
        
        
    end
    
end

