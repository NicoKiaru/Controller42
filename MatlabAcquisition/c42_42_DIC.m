addpath('code');
javaclasspath({'C:/Micro-Manager/ij.jar','C:/Micro-Manager/plugins/Micro-Manager/MMCoreJ.jar','C:/Micro-Manager/plugins/Micro-Manager/MMJ_.jar','C:/Micro-Manager/plugins/Micro-Manager/bsh-2.0b4.jar','C:/Micro-Manager/plugins/Micro-Manager/swingx-0.9.5.jar'});
import mmcorej.*;
c42=Controller_42();
c42.initCfg('ini/spinning42_120509_DIC.ini');
c42.initDevicesWindow();
c42.showGui();