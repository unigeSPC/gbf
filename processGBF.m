% PROCESS_GBF
% 
% Name:       process_GBF.m
% Purpose:    Process the output files of the GBF model into probabilistic
%             hazard maps for ballistic impacts
% Author:     Sebastien Biass, Jean-Luc Falcone, Costanza Bonadonna
% Created:    April 2015
% Updated:    November 2017
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

function VBP = processGBF(varargin)
addpath('dependencies/');

%{ 
Debug
    fl = 'vulcano_blocks.dat';
    inBal = struct('name', 'test',...
        'gridRes', [100,10],...             % Resolution
        'subset', 10,...
        'eT', [60,100,4000,8000],...     % Vector of energy thresholds
        'pT', [10, 25, 50, 75, 90],...       % Vector of probability thresholds
        'dI', 250,...                      % Distance interval
        'rI', 20,...                    % Radial interval
        'vE', 496670,...                      % Vent easting
        'vN', 4250690,...                    % Vent easting
        'vZ', 33);                         % Vent UTM zone 
%}


%% Retrieve input parameters
% Case using the GUI
if nargin == 0
    
    [FileName, PathName] = uigetfile('*.*'); % Choose file
    if FileName == 0; return; end   % Check non-null value
    
    fl2load     = [PathName, filesep, FileName];    % Define file path
	input_val   = {'test_run','50', '100', '100, 1000', '10, 25, 50, 75, 90', '1000', '22.5', '496670', '4250690', '33'}; % Default GUI values

    % GUI
    answer      = inputdlg({'Name:','Grid resolution (m):', 'Subset of total number of VBPs (%):', 'Energy thresholds (J), comma delimited', 'Probability threshold (%), comma delimited', 'Distance interval (m)', 'Radial sector interval (degrees)', 'Vent easting (m)', 'Vent northing (m)', 'UTM Zone'},...
        'Input', [1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35; 1 35],...
        input_val);
    
    % Check non-null value
    if isempty(answer); return; end
    
    % Fillup the input structure
    inBal = struct('name', answer{1},...
        'gridRes', str2double(answer{2}),...                % Resolution
        'subset', str2double(answer{3}),...                 % Subset of the total number of VBPs
        'eT', str2double(strsplit(answer{4}, ',')),...      % Vector of energy thresholds
        'pT', str2double(strsplit(answer{5}, ',')),...      % Vector of probability thresholds
        'dI', str2double(answer{6}),...                     % Distance interval
        'rI', str2double(answer{7}),...                     % Radial interval
        'vE', str2double(answer{8}),...                     % Vent easting
        'vN', str2double(answer{9}),...                     % Vent easting
        'vZ', str2double(answer{10}));                      % Vent UTM zone
        
% Case using a structure    
elseif nargin == 2
    % Case second argument is not a structure
    if ~isstruct(varargin{2})
        error('The second input argument should be a structure')
    end
    
    fl2load = varargin{1};
    inBal   = varargin{2};
    
% Case entering all input parameters separately    
elseif nargin == 11
    fl2load = varargin{1};
    inBal = struct('name', varargin{2},...
        'gridRes', varargin{3},...                          % Resolution
        'subset', varargin{4},...                           % Subset of the total number of VBPs
        'eT', varargin{5},...                               % Vector of energy thresholds
        'pT', varargin{6},...                               % Vector of probability thresholds
        'dI', varargin{7},...                               % Distance interval
        'rI', varargin{8},...                               % Radial interval
        'vE', varargin{9},...                               % Vent easting
        'vN', varargin{10},...                               % Vent easting
        'vZ', varargin{11});                                % Vent UTM zone
% Else error
else
    error('Wrong number of input arguments')
end

%% Load file
% Create directory
if exist(inBal.name, 'dir') == 7
    choice = questdlg('The output name already exists, overwrite?', ...
	'Output name', ...
	'No','Yes','No');
    switch choice
        case 'No'
            return
        case 'Yes'
            rmdir(inBal.name,'s');
    end
end
mkdir(inBal.name);
pthout  = [inBal.name, filesep];         % Set output folder

fprintf('\tLoading file\n');
data    = dlmread(fl2load, '', 1, 0);

if size(data,2) < 6
    error('The input file should have at least 6 columns organized as easting (m), northing (m), altitude (m asl), mass (kg), diameter (m) and kinetic energy (kJ)') 
end

% Extract a subset of the whole VBP population
randIdx     = randperm(size(data,1),round(size(data,1)*inBal.subset/100));
data        = data(randIdx,:);

% Get results from file
bal.x       = data(:,1);                    % Easting
bal.y       = data(:,2);                    % Northing
[bal.lat, bal.lon] ...
            = utm2ll(bal.x, bal.y, ones(size(bal.x)).*inBal.vZ); 
bal.e       = data(:,6).*1000;              % Energy (J)
bal.n       = size(data,1);                 % Number of bombs
bal.d       = sqrt((bal.x-inBal.vE).^2 + (bal.y-inBal.vN).^2);  % Distance between vent and bombs
bal.r       = atan2d(bal.x-inBal.vE,bal.y-inBal.vN); bal.r(bal.r<0)  = 360+bal.r(bal.r<0);
%bal.id      = zeros(size(bal.x,1),2);           % Vectors of indices for cartesian grid

bal.data    = data(:,3:end);            % Colums:
                                        % 1: Landing altitude (m a.s.l.)
                                        % 2: Mass (kg)
                                        % 3: Diameter (m)
                                        % 4: Kinetic energy (kJ)
                                        % 5: Landing angle (deg)
                                        % 6: Ejection andgle(deg)
                                        % 7: Flight time (sec)

% Convert vent coordinates to geographic
[inBal.lat, inBal.lon] ...
            = utm2ll(inBal.vE, inBal.vN, inBal.vZ);

%% Pixel approach
pixel.eastEdge  = min(bal.x):inBal.gridRes(1):max(bal.x);           % Vector along easting
pixel.northEdge = min(bal.y):inBal.gridRes(1):max(bal.y);           % Vector along northing
[pixel.east, pixel.north, pixel.lat, pixel.lon] = ...
                  getGrid(pixel.eastEdge, pixel.northEdge, inBal);  % Meshgrid and convert to geographic coordinates
                        
% Define storage
pixel.E     = zeros(size(pixel.east,1), size(pixel.east,2), length(inBal.pT));  % Energy per probability threshold
pixel.N     = zeros(size(pixel.east,1), size(pixel.east,2), length(inBal.eT));  % Number of VBP per energy threshold
pixel.Pabs  = zeros(size(pixel.east,1), size(pixel.east,2), length(inBal.eT));  % Absolute probability
pixel.Prel  = zeros(size(pixel.east,1), size(pixel.east,2), length(inBal.eT));  % Relative probability

% Bin the total number of particles
[pixel.Nt,~,~,bal.xi,bal.yi] = histcounts2(bal.x,bal.y,pixel.eastEdge, pixel.northEdge);
pixel.Nt = flipud(pixel.Nt');   % Translate the results of histcounts2

% Calculate the number of particles per pixel per energy threshold and the
% absolute and relative probabilities (in %)
for iE = 1:length(inBal.eT)
    pixelTmp            = histcounts2(bal.x(bal.e>inBal.eT(iE)), bal.y(bal.e>inBal.eT(iE)), pixel.eastEdge, pixel.northEdge);   % Bin VBP per energy threshold
    pixel.N(:,:,iE)     = flipud(pixelTmp');
    pixel.Pabs(:,:,iE)  = pixel.N(:,:,iE) ./ bal.n .* 100;      % Absolute probability (%)
    pixel.Prel(:,:,iE)  = pixel.N(:,:,iE) ./ pixel.Nt .* 100;   % Relative probability (%)
end

% Calculate the energy for a given exceedance probability
for iE = 1:size(pixel.N, 2)
    for iN = 1:size(pixel.N, 1)
        for iP = 1:length(inBal.pT)
            pixel.E(iN, iE, iP) = prctile(bal.e(bal.xi==iE & bal.yi==iN), 100-inBal.pT(iP));
        end
    end
end

%% Concentric approach
% Storage
concentric.bin      = 0:inBal.dI:ceil(max(bal.d)/1000)*1000;                % Vector of distances
concentric.Nt       = zeros(length(concentric.bin),1);                      % Total number of VBP per distance
concentric.N        = zeros(length(concentric.bin), length(inBal.eT));      % Number of VBP per distance per energy threshold
concentric.E        = zeros(length(concentric.bin), length(inBal.pT));      % Energy per distance per probability threshold
concentric.Pabs     = zeros(length(concentric.bin), length(inBal.eT));      % Absolute probability
concentric.Prel     = zeros(length(concentric.bin), length(inBal.eT));      % Relative probability

% Define a grid for the concentric approach, only for mapping purpose
% If a second grid resolution is not specified in gridRes, the original grid for the pixel approach is arbitrarily resampled to be 10% finer than the initial grid
if length(inBal.gridRes) == 1   
    concentric.eastEdge  = min(bal.x):inBal.gridRes(1)/10:max(bal.x);       % Vector along easting
    concentric.northEdge = min(bal.y):inBal.gridRes(1)/10:max(bal.y);       % Vector along northing
else
    concentric.eastEdge  = min(bal.x):inBal.gridRes(2):max(bal.x);          % Vector along easting
    concentric.northEdge = min(bal.y):inBal.gridRes(2):max(bal.y);          % Vector along northing
end
[concentric.east, concentric.north, concentric.lat, concentric.lon] = ...
                      getGrid(concentric.eastEdge, concentric.northEdge, inBal);  % Meshgrid and convert to geographic coordinates

% Define matrix storage (denoted by a "M")
concentric.binM     = sqrt((concentric.east-inBal.vE).^2 + (concentric.north-inBal.vN).^2);         % Euclidian distance from the vent
concentric.NtM      = zeros(size(concentric.east,1), size(concentric.east,2));                      % Total number of VBPs
concentric.NM       = zeros(size(concentric.east,1), size(concentric.east,2), length(inBal.eT));    % Number of VBPs per energy threshold
concentric.EM       = zeros(size(concentric.east,1), size(concentric.east,2), length(inBal.pT));    % Energy per distance per probability threshold
concentric.PabsM    = zeros(size(concentric.east,1), size(concentric.east,2), length(inBal.eT));    % Absolute probability
concentric.PrelM    = zeros(size(concentric.east,1), size(concentric.east,2), length(inBal.eT));    % Relative probability

                  
for iC = 1:length(concentric.bin)-1     % Loop over concentric distances
    concentric.Nt(iC)    = nnz(bal.d >= concentric.bin(iC) & bal.d < concentric.bin(iC+1));
    idxM                 = find(concentric.binM >= concentric.bin(iC) & concentric.binM < concentric.bin(iC+1));    % Extract indices of particles
    concentric.NtM(idxM) = concentric.Nt(iC);
    
    for iE = 1:length(inBal.eT)         % Loop over energy thresholds
        concentric.N(iC,iE)     = nnz(bal.d >= concentric.bin(iC) & bal.d < concentric.bin(iC+1) & bal.e > inBal.eT(iE));
        concentric.Pabs(iC,iE)  = concentric.N(iC,iE) ./bal.n .*100;
        concentric.Prel(iC,iE)  = concentric.N(iC,iE) ./concentric.Nt(iC) .*100;
        tmp                     = concentric.NM(:,:,iE);
        tmp(idxM)               = concentric.N(iC,iE);
        concentric.NM(:,:,iE)   = tmp;
    end
    for iP = 1:length(inBal.pT)
        concentric.E(iC,iP)     = prctile(bal.e(bal.d >= concentric.bin(iC) & bal.d < concentric.bin(iC+1)), 100-inBal.pT(iP));
        tmp                     = concentric.EM(:,:,iE);
        tmp(idxM)               = concentric.E(iC,iE);
        concentric.EM(:,:,iE)   = tmp;
    end
end
concentric.PabsM = concentric.NM ./ bal.n .* 100;
concentric.PrelM = concentric.NM ./ repmat(concentric.NtM,1,1,length(inBal.eT)) .* 100;

%% Radial approach
% Define storage for plots as histograms
radial.bin      = 0:inBal.rI:360; %atan2d(coorRef.crEast-inBal.vE,coorRef.crNorth-inBal.vN);
radial.Nt       = zeros(length(radial.bin),1);
radial.N        = zeros(length(radial.bin), length(inBal.eT));
radial.E        = zeros(length(radial.bin), length(inBal.pT));
radial.Pabs     = zeros(length(radial.bin), length(inBal.eT));
radial.Prel     = zeros(length(radial.bin), length(inBal.eT));

% Define coordinates to plot on a map
radial.eastEdge     = concentric.eastEdge;
radial.northEdge    = concentric.northEdge;
radial.east         = concentric.east;
radial.north        = concentric.north;       
radial.lat          = concentric.lat;
radial.lon          = concentric.lon;

% Define storage to plot on a map
radial.binM     = atan2d(radial.east-inBal.vE,radial.north-inBal.vN); radial.binM(radial.binM<0) = 360+radial.binM(radial.binM<0);
radial.NtM      = zeros(size(radial.east,1), size(radial.east,2));
radial.NM       = zeros(size(radial.east,1), size(radial.east,2), length(inBal.eT));
radial.EM       = zeros(size(radial.east,1), size(radial.east,2), length(inBal.pT));
radial.PabsM    = zeros(size(radial.east,1), size(radial.east,2), length(inBal.eT));
radial.PrelM    = zeros(size(radial.east,1), size(radial.east,2), length(inBal.eT));

for iR = 1:length(radial.bin)-1
    radial.Nt(iR)    = nnz(bal.r >= radial.bin(iR) & bal.r < radial.bin(iR+1));
    idxM             = find(radial.binM >= radial.bin(iR) & radial.binM < radial.bin(iR+1));
    radial.NtM(idxM) = radial.Nt(iR);
    for iE = 1:length(inBal.eT)
        radial.N(iR,iE)     = nnz(bal.r >= radial.bin(iR) & bal.r < radial.bin(iR+1) & bal.e > inBal.eT(iE));
        radial.Pabs(iR,iE)  = radial.N(iR,iE) ./bal.n .*100;
        radial.Prel(iR,iE)  = radial.N(iR,iE) ./radial.Nt(iR) .*100;
        tmp                 = radial.NM(:,:,iE);
        tmp(idxM)           = radial.N(iR,iE);
        radial.NM(:,:,iE)   = tmp;
    end
    for iP = 1:length(inBal.pT)
        radial.E(iR,iP)     = prctile(bal.e(bal.r >= radial.bin(iR) & bal.r < radial.bin(iR+1)), 100-inBal.pT(iP));
        tmp                 = radial.EM(:,:,iE);
        tmp(idxM)           = radial.E(iR,iE);
        radial.EM(:,:,iE)   = tmp;
    end
end
radial.PabsM = radial.NM ./ bal.n .* 100;
radial.PrelM = radial.NM ./ repmat(radial.NtM,1,1,length(inBal.eT)) .* 100;


%% Save variables
% Start by removing some data
pixel             = rmfield(pixel, {'eastEdge', 'northEdge'});
concentric        = rmfield(concentric, {'eastEdge', 'northEdge'});
radial            = rmfield(radial, {'eastEdge', 'northEdge'});

VBP.inBal         = inBal;
VBP.bal           = bal;
VBP.pixel         = pixel;
VBP.concentric    = concentric;
VBP.radial        = radial;

save([pthout, inBal.name, '.mat'], 'VBP');

%% Write results
display(sprintf('\tWriting results'));
% Number of particles
%writeBAL([pthout, 'nb_part.txt'], pixelEast, pixelNorth-inBal.gridRes, stor_part);

% Probability of a given energy threshold
for i = 1:length(inBal.eT)
    writeBAL([pthout, 'pixel_prob_abs_', num2str(inBal.eT(i)), 'J.txt'], pixel.east, pixel.north, pixel.Pabs(:,:,i));
    writeBAL([pthout, 'pixel_prob_rel_', num2str(inBal.eT(i)), 'J.txt'], pixel.east, pixel.north, pixel.Prel(:,:,i));
    writeBAL([pthout, 'concentric_prob_abs_', num2str(inBal.eT(i)), 'J.txt'], concentric.east, concentric.north, concentric.PabsM(:,:,i));
    writeBAL([pthout, 'concentric_prob_rel_', num2str(inBal.eT(i)), 'J.txt'], concentric.east, concentric.north, concentric.PrelM(:,:,i));
    writeBAL([pthout, 'radial_prob_abs', num2str(inBal.eT(i)), 'J.txt'], radial.east, radial.north, radial.PabsM(:,:,i));
    writeBAL([pthout, 'radial_prob_rel', num2str(inBal.eT(i)), 'J.txt'], radial.east, radial.north, radial.PrelM(:,:,i));
end

% Energy for a given probability of occurrence
for i = 1:length(inBal.pT)
    writeBAL([pthout, 'pixel_en_', num2str(inBal.pT(i)), '%.txt'], pixel.east, pixel.north, pixel.E(:,:,i));
    writeBAL([pthout, 'concentric_en_', num2str(inBal.pT(i)), '%.txt'], concentric.east, concentric.north, concentric.EM(:,:,i));
    writeBAL([pthout, 'radial_en_', num2str(inBal.pT(i)), '%.txt'], radial.east, radial.north, radial.EM(:,:,i));
end

%display(sprintf('Run finished at %s\n', datestr(now, 'HH:MM:SS')));

function [east,north,lat,lon] = getGrid(eastEdge, northEdge, inBal)
[east, north] = meshgrid(eastEdge(1:end-1), northEdge(1:end-1));
north         = flipud(north);
[lat, lon]    = utm2ll(east, north, ones(size(east)).*inBal.vZ);

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
