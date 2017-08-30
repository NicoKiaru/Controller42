lG=AnimatedGraphTracker([], [], 200, 1, -1, 1, 0, 0, 0, 1, 1, 1, 1.2 );
lG.showGraph();
for i=1:500
    lG.appendNewPt(rand,rand);
    lG.updateGraph();
end