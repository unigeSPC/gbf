
function  [Lat,Lon] = utm2deg(xx,yy,utmzone)

% Author: 
%   Rafael Palacios
%   Universidad Pontificia Comillas
%   Madrid, Spain
% Version: Apr/06, Jun/06, Aug/06
% Aug/06: corrected m-Lint warnings


% Argument checking
%
%error(nargchk(3, 3, nargin)); %3 arguments required
n1=length(xx);
n2=length(yy);
n3=size(utmzone,1);
if (n1~=n2 || n1~=n3)
   error('x,y and utmzone vectors should have the same number or rows');
end
c=size(utmzone,2);
if (c~=4)
   error('utmzone should be a vector of strings like "30 T"');
end

   
 
% Memory pre-allocation
%
Lat=zeros(n1,1);
Lon=zeros(n1,1);


% Main Loop
%
for i=1:n1
   if (utmzone(i,4)>'X' || utmzone(i,4)<'C')
      fprintf('utm2deg: Warning utmzone should be a vector of strings like "30 T", not "30 t"\n');
   end
   if (utmzone(i,4)>'M')
      hemis='N';   % Northern hemisphere
   else
      hemis='S';
   end

   x=xx(i);
   y=yy(i);
   zone=str2double(utmzone(i,1:2));

   sa = 6378137.000000 ; sb = 6356752.314245;
  
%   e = ( ( ( sa ^ 2 ) - ( sb ^ 2 ) ) ^ 0.5 ) / sa;
   e2 = ( ( ( sa ^ 2 ) - ( sb ^ 2 ) ) ^ 0.5 ) / sb;
   e2cuadrada = e2 ^ 2;
   c = ( sa ^ 2 ) / sb;
%   alpha = ( sa - sb ) / sa;             %f
%   ablandamiento = 1 / alpha;   % 1/f

   X = x - 500000;
   
   if hemis == 'S' || hemis == 's'
       Y = y - 10000000;
   else
       Y = y;
   end
    
   S = ( ( zone * 6 ) - 183 ); 
   lat =  Y / ( 6366197.724 * 0.9996 );                                    
   v = ( c / ( ( 1 + ( e2cuadrada * ( cos(lat) ) ^ 2 ) ) ) ^ 0.5 ) * 0.9996;
   a = X / v;
   a1 = sin( 2 * lat );
   a2 = a1 * ( cos(lat) ) ^ 2;
   j2 = lat + ( a1 / 2 );
   j4 = ( ( 3 * j2 ) + a2 ) / 4;
   j6 = ( ( 5 * j4 ) + ( a2 * ( cos(lat) ) ^ 2) ) / 3;
   alfa = ( 3 / 4 ) * e2cuadrada;
   beta = ( 5 / 3 ) * alfa ^ 2;
   gama = ( 35 / 27 ) * alfa ^ 3;
   Bm = 0.9996 * c * ( lat - alfa * j2 + beta * j4 - gama * j6 );
   b = ( Y - Bm ) / v;
   Epsi = ( ( e2cuadrada * a^ 2 ) / 2 ) * ( cos(lat) )^ 2;
   Eps = a * ( 1 - ( Epsi / 3 ) );
   nab = ( b * ( 1 - Epsi ) ) + lat;
   senoheps = ( exp(Eps) - exp(-Eps) ) / 2;
   Delt = atan(senoheps / (cos(nab) ) );
   TaO = atan(cos(Delt) * tan(nab));
   longitude = (Delt *(180 / pi ) ) + S;
   latitude = ( lat + ( 1 + e2cuadrada* (cos(lat)^ 2) - ( 3 / 2 ) * e2cuadrada * sin(lat) * cos(lat) * ( TaO - lat ) ) * ( TaO - lat ) ) * ...
                    (180 / pi);
   
   Lat(i)=latitude;
   Lon(i)=longitude;
   
end