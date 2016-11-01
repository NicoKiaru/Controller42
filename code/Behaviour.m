classdef Behaviour < handle
    %BEHAVIOUR Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
        panel;
        toggleButton_Exec;
        behaviourName;
        hBehaviourGUI;
        isRunning;
    end
    
    methods
         function self=Behaviour(name,pos)
            self.behaviourName=name;
            self.initGUI(name,pos);
            self.isRunning=0;
            self.updateGUI;
         end
          function initGUI(self,name,pos)
              self.hBehaviourGUI=figure('Toolbar','none',...
                          'Menubar', 'none',...
                          'NumberTitle','Off',...
                          'Name',name,'NumberTitle','off','Position', pos);
               set(self.hBehaviourGUI,'CloseRequestFcn',@self.my_closereq);
               exec_panel=uipanel('Title','Execution','FontName','MS Sans Serif','FontSize',10,...
                                        'BackgroundColor','white',...
                                        'units','normalized',...
                                        'Position',[0 0 1 0.2]);
               self.toggleButton_Exec = uicontrol('Parent',exec_panel,'style','togglebutton',...
                                                    'units','normalized', 'position',[0 0 1 1],...
                                                    'callback',{@(src,event)self.toggleExecCB(src,event)}); %an edit box
               self.panel = uipanel('Title','Parameters','FontName','MS Sans Serif','FontSize',10,...
                                        'BackgroundColor','white',...
                                        'units','normalized',...
                                        'Position',[0 0.2 1 0.8]);
               self.buildPanel(self.panel);               
          end
          
         function my_closereq(self,src,evnt)
            % User-defined close request function 
            % to display a question dialog box 
            selection = questdlg(['Kill behaviour ' self.behaviourName ' ?'],...
                                'Close Request Function',...
                                'Kill','Cancel','Cancel'); 
            switch selection, 
            case 'Kill'                
                self.custom_delete();                
            case 'Cancel'
                return 
            end
         end
        
         function custom_delete(self)
            delete(self.hBehaviourGUI);
            if(self.isRunning==1)
                self.interrupt;
            end
            delete(self);
         end
         function updateGUI(self);
            % Initialize value
            if (self.isRunning==1) 
               set(self.toggleButton_Exec,'string','Executing...');
               set(self.toggleButton_Exec,'Value',0);
            else
               set(self.toggleButton_Exec,'string','Yes my lord ?');
               set(self.toggleButton_Exec,'Value',1);
            end
         end
          function toggleExecCB(self,src,event)
            if (self.isRunning==1)
                self.stop;
            else
                self.exec;
            end            
          end
          
          function exec(self)
              self.isRunning=1;
              self.execute;
              self.updateGUI();
          end
          
          function stop(self)
              self.interrupt;
              self.isRunning=0;
              self.updateGUI();
          end
          
    end
    
    methods (Abstract)
          execute(self)
          interrupt(self)
          buildPanel(self,panel)
    end
    
end

