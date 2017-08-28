
% sequence calibrate
MP285L=c42.getDeviceByName('MP285_RIGHT');
% Set speed and displacement
MP285L.setV(200,'c');MP285L.stepDistance=200;
% Triangle Begin
MP285L.updateGUIDistance();MP285L.updateGUISpeed();
MP285L.doStepMove(1,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-1,0,0);MP285L.waitForIdle();
% Triangle End

% Set speed and displacement
MP285L.setV(400,'c');MP285L.stepDistance=400;
% Triangle Begin
MP285L.updateGUIDistance();MP285L.updateGUISpeed();
MP285L.doStepMove(1,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-1,0,0);MP285L.waitForIdle();
% Triangle End
% Set speed and displacement
MP285L.setV(600,'c');MP285L.stepDistance=600;
% Triangle Begin
MP285L.updateGUIDistance();MP285L.updateGUISpeed();
MP285L.doStepMove(1,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-1,0,0);MP285L.waitForIdle();
% Triangle End
% Set speed and displacement
MP285L.setV(800,'c');MP285L.stepDistance=800;
% Triangle Begin
MP285L.updateGUIDistance();MP285L.updateGUISpeed();
MP285L.doStepMove(1,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-1,0,0);MP285L.waitForIdle();
% Triangle End
% Set speed and displacement
MP285L.setV(1000,'c');MP285L.stepDistance=1000;
% Triangle Begin
MP285L.updateGUIDistance();MP285L.updateGUISpeed();
MP285L.doStepMove(1,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-2,0,0);MP285L.waitForIdle();MP285L.doStepMove(2,0,0);MP285L.waitForIdle();
MP285L.doStepMove(-1,0,0);MP285L.waitForIdle();
% Triangle End


%MP285L.triangle_X(200,200,2,'f');
%MP285L.triangle_Y(200,200,2,'f');
%MP285L.triangle_X(400,400,2,'f');
%MP285L.triangle_Y(400,400,2,'f');
%MP285L.triangle_X(600,600,2,'f');
%MP285L.triangle_Y(600,600,2,'f');
%MP285L.triangle_X(800,800,2,'f');
%MP285L.triangle_Y(800,800,2,'f');
%MP285L.triangle_X(1000,1000,2,'f');
%MP285L.triangle_Y(1000,1000,2,'f');



