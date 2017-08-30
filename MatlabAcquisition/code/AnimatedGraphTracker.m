classdef AnimatedGraphTracker < handle
    %AnimatedGraphTracker Summary of this class goes here
    %   Detailed explanation... maybe one day
    
    properties
         maxNumberOfPoints;
         xData;
         yData;  
         graphHandle;
         axisHandle;
         minIntervalYGraph;
         
         displayMinimalRangeInY;
         minYAxisValueDisplayed;
         maxYAxisValueDisplayed;
         
         displayMinimalRangeInX;
         minXAxisValueDisplayed;
         maxXAxisValueDisplayed;  
         
         scaleYGraph;
         scaleXGraph;
         expandMaxX;
         expandMaxY;
    end
    
    methods
        % ----- Constructor of the AnimatedGraph
        function self=AnimatedGraphTracker(xD, yD, maxNPts, dispMinYRange, miY, maY, dispMinXRange, miX, maX, scXGraph, scYGraph, eMaxX,eMaxY )
            self.xData=xD;
            self.yData=yD;
            self.maxNumberOfPoints=maxNPts;
            
            self.displayMinimalRangeInY = dispMinYRange;
            self.minYAxisValueDisplayed=miY;
            self.maxYAxisValueDisplayed=maY;
            
            self.displayMinimalRangeInX = dispMinXRange;
            self.minXAxisValueDisplayed=miX;
            self.maxXAxisValueDisplayed=maX;
            
            self.scaleYGraph=scYGraph;
            self.scaleXGraph=scXGraph;
            self.expandMaxX=eMaxX;
            self.expandMaxY=eMaxY;
        end
        
        function appendNewPt(self,x,y)
            if (length(self.xData)<self.maxNumberOfPoints)
                self.xData= [self.xData x*self.scaleXGraph];
                self.yData= [self.yData y*self.scaleYGraph];
            else
                self.xData= [self.xData(2:end) x*self.scaleXGraph];
                self.yData= [self.yData(2:end) y*self.scaleYGraph];
            end
        end
        
        function showGraph(self)
            if (length(self.xData))>2
                axis([min(self.xData),max(self.xData),min(self.yData),max(self.yData)])
                self.graphHandle=plot(self.xData,self.yData,'.');
            else
                self.graphHandle=plot([0 1],[0 1],'.');
            end
        end
        
        function updateGraph(self)
             if (length(self.xData))>2
                    minXData=min(self.xData);
                    maxXData=max(self.xData);            
                    rangeX=(maxXData-minXData)*self.expandMaxX;
                    centerX=(minXData+maxXData)/2;
                    minXData=centerX-rangeX/2;
                    maxXData=centerX+rangeX/2;

                    minYData=min(self.yData);
                    maxYData=max(self.yData);
                    rangeY=(maxYData-minYData)*self.expandMaxY;
                    centerY=(minYData+maxYData)/2;
                    minYData=centerY-rangeY/2;
                    maxYData=centerY+rangeY/2;   

                    if (self.displayMinimalRangeInY==1) 
                        if minYData>self.minYAxisValueDisplayed
                            minYData=self.minYAxisValueDisplayed;
                        end
                        if maxYData<self.maxYAxisValueDisplayed
                            maxYData=self.maxYAxisValueDisplayed;
                        end
                    end
                    
                    if (self.displayMinimalRangeInX==1) 
                        if minXData>self.minXAxisValueDisplayed
                            minXData=self.minXAxisValueDisplayed;
                        end
                        if maxXData<self.maxXAxisValueDisplayed
                            maxXData=self.maxXAxisValueDisplayed;
                        end
                    end           
                    axisSettings=[minXData,maxXData,minYData,maxYData];
                    if sum(isnan(axisSettings)) == 0
                        axis([minXData,maxXData,minYData,maxYData])
                        set(self.graphHandle,'xdata',self.xData,'ydata',self.yData);
                        drawnow;
                    end
            end
        end
        
    end
    
end