/**********************************************************
 *  Key Code Utility
 *  @author: joycelyn@orangetowers.com
 * 
 *  Utility to retrieve the identification of the keycode. 
 **********************************************************/
 
ZERO = '0';
ONE = '1';
TWO = '2';
THREE = '3';
FOUR = '4';
FIVE = '5';
SIX = '6';
SEVEN = '7';
EIGHT = '8';
NINE = '9';
A = 'A';
B = 'B';
C = 'C';
D = 'D';
E = 'E';
F = 'F';
G = 'G';
H = 'H';
I = 'I';
J = 'J';
K = 'K';
L = 'L';
M = 'M';
N = 'N';
O = 'O';
P = 'P';
Q = 'Q';
R = 'R';
S = 'S';
T = 'T';
U = 'U';
V = 'V';
W = 'W';
X = 'X';
Y = 'Y';
Z = 'Z';
F1 = 'F1';
F2 = 'F2';
F3 = 'F3';
F4 = 'F4';
F5 = 'F5';
F6 = 'F6';
F7 = 'F7';
F8 = 'F8';
F9 = 'F9';
F10 = 'F10';
F11 = 'F11';
F12 = 'F12';
NP_ZERO = 'Number Pad 0';
NP_ONE = 'Number Pad 1';
NP_TWO = 'Number Pad 2';
NP_THREE = 'Number Pad 3';
NP_FOUR = 'Number Pad 4';
NP_FIVE = 'Number Pad 5';
NP_SIX = 'Number Pad 6';
NP_SEVEN = 'Number Pad 7';
NP_EIGHT = 'Number Pad 8';
NP_NINE = 'Number Pad 9';
BACKSPACE = 'BACKSPACE';
TAB = 'TAB';
ENTER = 'ENTER';
SHIFT = 'SHIFT';
CTRL = 'CTRL';
ALT = 'ALT';
PAUSE_BREAK = 'PAUSE/BREAK';
CAPS_LOCK = 'CAPS LOCK';
ESCAPE = 'ESCAPE';
PAGE_UP = 'PAGE UP';
PAGE_DOWN = 'PAGE DOWN';
END = 'END';
HOME = 'HOME';
LEFT_ARROW = 'LEFT ARROW';
UP_ARROW = 'UP ARROW';
RIGHT_ARROW = 'RIGHT ARROW';
DOWN_ARROW = 'DOWN ARROW';
INSERT = 'INSERT';
DELETE = 'DELETE';
LEFT_WINDOW_KEY = 'LEFT WINDOW KEY';
RIGHT_WINDOW_KEY = 'RIGHT WINDOW KEY';
SELECT_KEY = 'SELECT KEY';
MULTIPLY = 'MULTIPLY';
ADD = 'ADD';
SUBTRACT = 'SUBTRACT';
DECIMAL_POINT = 'DECIMAL POINT';
DIVIDE = 'DIVIDE';
NUM_LOCK = 'NUM LOCK';
SCROLL_LOCK = 'SCROLL LOCK';
SEMICOLON = 'SEMI-COLON';
EQUAL_SIGN = 'EQUAL SIGN';
COMMA = 'COMMA';
DASH = 'DASH';
PERIOD = 'PERIOD';
FORWARD_SLASH = 'FORWARD SLASH';
GRAVE_ACCENT = 'GRAVE ACCENT';
OPEN_BRACKET = 'OPEN BRACKET';
BACK_SLASH = 'BACK SLASH';
CLOSE_BRAKET = 'CLOSE BRACKET';
SINGLE_QUOTE = 'SINGLE QUOTE';

function keyname (eventObj) { 
  var keyEvent =  eventObj.keyCode ? eventObj.keyCode :
                  window.event ? window.event.keyCode : 0;
  
                  /*document.getElementById ? window.event.keyCode :
                  document.layers ? eventObj.which :   // May need it if supporting IE4/5
                  document.all ? eventObj.keyCode : 0;*/
  
  //alert("eventObj = " + eventObj + "\nkeyEvent = " + keyEvent);
    
  switch (keyEvent) {  
    case 48:
      return '0';
    case 49:
     return '1';
    case 50:
     return '2';
    case 51:
     return '3';
    case 52:
     return '4';
    case 53:
     return '5';
    case 54:
     return '6';
    case 55:
     return '7';
    case 56:
     return '8';
    case 57:
     return '9';
    case 65:
     return 'A';
    case 66:
     return 'B';
    case 67:
     return 'C';
    case 68:
     return 'D';
    case 69:
     return 'E';
    case 70:
     return 'F';
    case 71:
     return 'G';
    case 72:
     return 'H';
    case 73:
     return 'I';
    case 74:
     return 'J';
    case 75:
     return 'K';
    case 76:
     return 'L';
    case 77:
     return 'M';
    case 78:
     return 'N';
    case 79:
     return 'O';
    case 80:
     return 'P';
    case 81:
     return 'Q';
    case 82:
     return 'R';
    case 83:
     return 'S';
    case 84:
     return 'T';
    case 85:
     return 'U';
    case 86:
     return 'V';
    case 87:
     return 'W';
    case 88:
     return 'X';
    case 89:
     return 'Y';
    case 90:
     return 'Z';
    case 112:
     return 'F1';
    case 113:
     return 'F2';
    case 114:
     return 'F3';
    case 115:
     return 'F4';
    case 116:
     return 'F5';
    case 117:
     return 'F6';
    case 118:
     return 'F7';
    case 119:
     return 'F8';
    case 120:
     return 'F9';
    case 121:
     return 'F10';
    case 122:
     return 'F11';
    case 123:
     return 'F12';
    case 96:
     return 'Number Pad 0';
    case 97:
     return 'Number Pad 1';
    case 98:
     return 'Number Pad 2';
    case 99:
     return 'Number Pad 3';
    case 100:
     return 'Number Pad 4';
    case 101:
     return 'Number Pad 5';
    case 102:
     return 'Number Pad 6';
    case 103:
     return 'Number Pad 7';
    case 104:
     return 'Number Pad 8';
    case 105:
     return 'Number Pad 9';
    case 8:
     return 'BACKSPACE';
    case 9:
     return 'TAB';
    case 13:
     return 'ENTER';
    case 16:
     return 'SHIFT';
    case 17:
     return 'CTRL';
    case 18:
     return 'ALT';
    case 19:
     return 'PAUSE/BREAK';
    case 20:
     return 'CAPS LOCK';
    case 27:
     return 'ESCAPE';
    case 33:
     return 'PAGE UP';
    case 34:
     return 'PAGE DOWN';
    case 35:
     return 'END';
    case 36:
     return 'HOME';
    case 37:
     return 'LEFT ARROW';
    case 38:
     return 'UP ARROW';
    case 39:
     return 'RIGHT ARROW';
    case 40:
     return 'DOWN ARROW';
    case 45:
     return 'INSERT';
    case 46:
     return 'DELETE';
    case 91:
     return 'LEFT WINDOW KEY';
    case 92:
     return 'RIGHT WINDOW KEY';
    case 93:
     return 'SELECT KEY';
    case 106:
     return 'MULTIPLY';
    case 107:
     return 'ADD';
    case 109:
     return 'SUBTRACT';
    case 110:
     return 'DECIMAL POINT';
    case 111:
     return 'DIVIDE';
    case 144:
     return 'NUM LOCK';
    case 145:
     return 'SCROLL LOCK';
    case 186:
     return 'SEMI-COLON';
    case 187:
     return 'EQUAL SIGN';
    case 188:
     return 'COMMA';
    case 189:
     return 'DASH';
    case 190:
     return 'PERIOD';
    case 191:
     return 'FORWARD SLASH';
    case 192:
     return 'GRAVE ACCENT';
    case 219:
     return 'OPEN BRACKET';
    case 220:
     return 'BACK SLASH';
    case 221:
     return 'CLOSE BRACKET';
    case 222:
     return 'SINGLE QUOTE';
    default:
      return false;
  }
}