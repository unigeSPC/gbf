% PROCESS_GBF
% 
% Name:       process_GBF.m
% Purpose:    Process the output files of the GBF model into probabilistic
%             hazard maps for ballistic impacts
% Author:     Sebastien Biass, Jean-Luc Falcone, Costanza Bonadonna
% Created:    April 2015
% Updated:    July 2015
% Copyright:  S Biass, JL Falcone, C Bonadonna - University of Geneva, 2015
% License:    GNU GPL3
% 
%         "You shake my nerves and you rattle my brain
%         Too much love drives a man insane
%         You broke my will, oh what a thrill
%         Goodness gracious great balls of fire"
%                                     -- J.L. Lewis
% 
% This is a free software: you can redistribute it and/or modify
%     it under the terms of the GNU General Public License as published by
%     the Free Software Foundation, either version 3 of the License, or
%     (at your option) any later version.
% 
%     It is distributed in the hope that it will be useful,
%     but WITHOUT ANY WARRANTY; without even the implied warranty of
%     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
%     GNU General Public License for more details.
% 
%     You should have received a copy of the GNU General Public License
%     along with it. If not, see <http://www.gnu.org/licenses/>.

function process_GBF

%% 1 Retrieve input data and pre-process
addpath('dependencies/');
% Retrieve output file
[FileName, PathName] = uigetfile('*.dat');
file    = [PathName, filesep, FileName];

if FileName == 0
    return
end

% Check if post-processing was run already
if exist('gbf.mat', 'file') == 2
    tmp       = load('gbf.mat');
    input_val = tmp.answer;
    %clear tmp;
else
    input_val = {'test_run','50', '100, 1000', '10, 25, 50, 75, 90', '1000', '22.5', '496670', '4250690', '33 S'};
end

% GUI
answer  = inputdlg({'Name:','Grid resolution (m):','Energy thresholds (J), comma delimited', 'Probability threshold (%), comma delimited', 'Distance interval (m)', 'Radial sector interval (degrees)', 'Vent easting (m)', 'Vent northing (m)', 'UTM Zone'},...
    'Input', [1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35],...
    input_val);

if isempty(answer)
    return
else
    clear tmp;
    save('gbf.mat', 'answer');
end

% Display
display(sprintf('\nRun started at %s', datestr(now, 'HH:MM:SS')));

% Retrieve data from GUI
res     = str2double(answer{2});                    % Resolution
Ethresh = str2double(strsplit(answer{3}, ','));     % Vector of energy thresholds
Pthresh = str2double(strsplit(answer{4}, ','));     % Vector of probability thresholds
distD   = str2double(answer{5});                    % Distance interval
angD    = str2double(answer{6});                    % Radial interval
vX      = str2double(answer{7});                    % Vent easting
vY      = str2double(answer{8});                    % Vent northing

% Create directory
if exist(answer{1}, 'dir') == 7
    choice = questdlg('The output name already exists, overwrite?', ...
	'Output name', ...
	'No','Yes','No');
    switch choice
        case 'No'
            return
        case 'Yes'
            rmdir(answer{1},'s');
    end
end
mkdir(answer{1});
pthout  = [answer{1}, filesep];         % Set output folder

% Load file
display(sprintf('\tLoading file'));
data    = dlmread(file, '', 1, 0);

% Get results from file
x       = data(:,1);                    % Easting
y       = data(:,2);                    % Northing
e       = data(:,6).*1000;              % Energy (J)
n       = size(data,1);                 % Number of bombs
d       = sqrt((x-vX).^2 + (y-vY).^2);  % Distance between vent and bombs
id      = zeros(size(x,1),2);           % Vectors of indices for cartesian grid

data    = data(:,3:end);                % Colums:
                                        % 1: Landing altitude (m a.s.l.)
                                        % 2: Mass (kg)
                                        % 3: Diameter (m)
                                        % 4: Kinetic energy (kJ)
                                        % 5: Landing angle (deg)
                                        % 6: Ejection andgle(deg)
                                        % 7: Flight time (sec)

                                                                               

% Create cartesian grid
display(sprintf('\tConverting coordinates'));
[east, north] = meshgrid(min(x)-res:res:max(x)+res, min(y)-res:res:max(y)+res);
north         = flipud(north);
lat           = zeros(size(east));
lon           = zeros(size(north));

% Convert to geographic coordinates
count = 1;
h = waitbar(0,'Converting projected to geographic coordinates...');
for i = 1:size(east,1)
    for j = 1:size(east,2)
        [tmp_lat, tmp_lon]  = utm2deg(east(i,j), north(i,j), answer{9});
        lat(i,j)    = tmp_lat;
        lon(i,j)    = tmp_lon;
        count = count+1;
        waitbar(count / (size(east,2)*size(east,1)));
    end
end
close(h)
[vLat,vLon]   = utm2deg(vX, vY, answer{9});


%% 2 Conversion to cartesian grid and probability calculations
display(sprintf('\tConverting to cartesian grid'));
% Storage matrices
stor_part = zeros(size(north,1), size(east,2));                    % Number of particles
stor_en   = zeros(size(north,1), size(east,2), length(Ethresh));   % Probability to exceed a given energy
stor_prb  = zeros(size(north,1), size(east,2), length(Pthresh));   % Energy for a probability of occurrence

% Retrieve the X position of each bomb in the grid
for iX = 1:size(east,2)-1
    id(x>=east(1,iX) & x<east(1,iX+1),1)    = iX;
end
% Retrieve the Y position of each bomb in the grid
for iY = 1:size(east,1)-1
    id(y<=north(iY,1) & y>north(iY+1,1),2)  = iY;
end

% Parse the data and calculate probabilities
count = 1;
h = waitbar(0,'Computing probabilities...');
for iX = 1:size(east,2)
    for iY = 1:size(north,1)
        tmpI = id(:,1)==iX & id(:,2)==iY;
        
        % Number of particles
        stor_part(iY, iX) = length(x(tmpI));
        
        % Number of particles in pixel with energy > Ethresh
        for j = 1:length(Ethresh)
            stor_en(iY, iX, j) = sum(e(tmpI)>=Ethresh(j));%     /n; <- modification: need to divide by n later
        end
        
        % Energy for a given probability of occurrence
        for j = 1:length(Pthresh)
            stor_prb(iY, iX, j) = prctile(e(tmpI), 100-Pthresh(j)); % Here, the probability used to calculate the energy is considered as 100-percentile
        end 

        count = count+1;
        waitbar(count / (size(east,2)*size(east,1)));
    end
end
close(h)

%% 3 Probability calculations per distance enveloppes and radial sectors
% First, define matrices used for histograms
% a - distance
d_mat       = sqrt((east-vX).^2 + (north-vY).^2);               % Euclidian distance from the vent
dist_vec    = 0:distD:ceil(max(d)/1000)*1000;                   % Distance vector
d_hist      = zeros(length(dist_vec), length(Ethresh),2);       % Matrix to plot histograms

% b - radial sector
a_mat       = atan2d(east-vX,north-vY);                         % Angle from the vent
idx         = a_mat<0;                                          % Angle correction to be in the interval [0 360]
a_mat(idx)  = 360+a_mat(idx);
a_vec       = 0:angD:360;
a_hist      = zeros(length(a_vec), length(Ethresh),2);          % Matrix to plot histograms

% Second, define matrices to plot distance/radial sectors on a map
% Dim 1 = lon
% Dim 2 = lat
% Dim 3 = Energy threshold
% Dim 4 = Prob over total number of particles (1) or Prob over particles
% within enveloppe (2)
p_dist  = zeros(size(d_mat,1), size(d_mat,2), length(Ethresh),2);            
p_angle = zeros(size(d_mat,1), size(d_mat,2), length(Ethresh),2); 

for iE = 1:length(Ethresh)  % Loop over energy thresholds
    tmp_storD1       = zeros(size(d_mat,1),size(d_mat,2));   % Temp 2D storage matrix
    tmp_storD2       = zeros(size(d_mat,1),size(d_mat,2));   % Temp 2D storage matrix
    tmp_storA1       = zeros(size(a_mat,1),size(a_mat,2));   % Temp 2D storage matrix
    tmp_storA2       = zeros(size(a_mat,1),size(a_mat,2));   % Temp 2D storage matrix
    
    % a - distance
    for iD = 1:length(dist_vec)-1
        idx_dist        = d_mat > dist_vec(iD) & d_mat <= dist_vec(iD+1);   % Index of pixels within the distance increment
        tmp_prb         = stor_en(:,:,iE);                                  % 2D matrix containing the number of particles per pixel for a given energy threshold
        sum_impact      = sum(tmp_prb(idx_dist));                           % Sum of particles > Ethresh and within distance increment
        nb_part_dist    = sum(stor_part(idx_dist));                         % Number of particles in the enveloppe
        
        tmp_storD1(idx_dist) = sum_impact/n.*100;                           % Probability over total number of bombs
        tmp_storD2(idx_dist) = sum_impact/nb_part_dist.*100;                % Probability over bombs in distance increment
        
        p_dist(:,:,iE,1)= tmp_storD1;                                       % Fill final storage matrix
        p_dist(:,:,iE,2)= tmp_storD2;
        
        d_hist(iD,iE,1) = sum_impact/n;
        d_hist(iD,iE,2) = sum_impact/nb_part_dist;  
    end
    
    % b - angle
    for iA = 1:length(a_vec)-1
        idx_angle       = a_mat > a_vec(iA) & a_mat <= a_vec(iA+1);         % Index of pixels within the distance increment
        tmp_prb         = stor_en(:,:,iE);                                  % 2D matrix containing the number of particles per pixel for a given energy threshold
        sum_impact      = sum(tmp_prb(idx_angle));                          % Sum of particles > Ethresh and within distance increment
        nb_part_angle   = sum(stor_part(idx_angle));                        % Number of particles in the enveloppe
        
        tmp_storA1(idx_angle) = sum_impact/n*100;                               % Probability over total number of bombs
        tmp_storA2(idx_angle) = sum_impact/nb_part_angle*100;                   % Probability over bombs in distance increment
        
        p_angle(:,:,iE,1)= tmp_storA1;                                      % Fill final storage matrix
        p_angle(:,:,iE,2)= tmp_storA2;
        
        a_hist(iA,iE,1) = sum_impact/n;
        a_hist(iA,iE,2) = sum_impact/nb_part_angle; 
    end
end




%% 3 Save variables
display(sprintf('\tProcessing results'));

stor_en                 = stor_en./n.*100;

stor_part(stor_part==0) = nan;
stor_en(stor_en==0)     = nan;
stor_prb(stor_prb==0)   = nan;


project.stor_en         = stor_en;
project.stor_part       = stor_part;
project.stor_prb        = stor_prb;
project.data            = data;
project.x               = x;
project.y               = y;
project.n               = n;
project.d               = d;
project.e               = e;
project.lat             = lat;
project.lon             = lon;
project.vLat            = vLat;
project.vLon            = vLon;
project.Ethresh         = Ethresh;
project.Pthresh         = Pthresh;
project.a_vec           = a_vec;
project.a_hist          = a_hist;
project.angD            = angD;
project.p_angle         = p_angle;
project.dist_vec        = dist_vec ;
project.d_hist          = d_hist;
project.distD           = distD;
project.p_dist          = p_dist;
project.answer          = answer;
project.res             = res;

save([pthout, answer{1}, '.mat'], 'project');

%% 4 Write results
display(sprintf('\tWriting results'));
% Number of particles
writeBAL([pthout, 'nb_part.txt'], east, north-res, stor_part);

% Probability of a given energy threshold
for i = 1:length(Ethresh)
    writeBAL([pthout, 'prob_pixel_', num2str(Ethresh(i)), 'J.txt'], east, north-res, stor_en(:,:,i));
    writeBAL([pthout, 'prob_distance_all', num2str(Ethresh(i)), 'J.txt'], east, north-res, p_dist(:,:,i,1));
    writeBAL([pthout, 'prob_distance_zone', num2str(Ethresh(i)), 'J.txt'], east, north-res, p_dist(:,:,i,2));
    writeBAL([pthout, 'prob_radial_all', num2str(Ethresh(i)), 'J.txt'], east, north-res, p_angle(:,:,i,1));
    writeBAL([pthout, 'prob_radial_zone', num2str(Ethresh(i)), 'J.txt'], east, north-res, p_angle(:,:,i,2));
end

% Energy for a given probability of occurrence
for i = 1:length(Pthresh)
    writeBAL([pthout, 'en_', num2str(Pthresh(i)), '%.txt'], east, north-res, stor_prb(:,:,i));
end



display(sprintf('Run finished at %s\n', datestr(now, 'HH:MM:SS')));



function writeBAL(out_name, X, Y, Z)
Z(isnan(Z)) = 0;
Z           = log10(Z);
Z(isinf(Z)) = 0;
Z(isnan(Z)) = -9999;

fid         = fopen(out_name,'w');
fprintf(fid,'%s\n',...
    ['ncols         ' num2str(size(X,2))],...
    ['nrows         ' num2str(size(X,1))],...
    ['xllcorner     ' num2str(min(X(1,:)))],...
    ['yllcorner     ' num2str(min(Y(:,1)))],...
    ['cellsize      ' num2str(X(1,2)-X(1,1))],...
    ['NODATA_value  ' num2str(-9999)]);
fclose(fid);

dlmwrite(out_name,Z,'-append','delimiter',' ', 'Precision', 10)