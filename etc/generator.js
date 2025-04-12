// Compress list of all Unicode letters to less than 2kB
// Credit goes to gagern, https://gist.github.com/gagern/89db1179766a702c564d

// install instructions:
/*
npm install lodash
npm install @unicode/unicode-16.0.0
node generator.js out16.txt
*/

var lo = require('lodash');

//var categories = [
//"Cased_Letter","Close_Punctuation","Connector_Punctuation","Control",
//"Currency_Symbol","Dash_Punctuation","Decimal_Number","Enclosing_Mark",
//"Final_Punctuation","Format","Initial_Punctuation","Letter","Letter_Number",
//"Line_Separator","Lowercase_Letter","Mark","Math_Symbol","Modifier_Letter",
//"Modifier_Symbol","Nonspacing_Mark","Number","Open_Punctuation","Other",
//"Other_Letter","Other_Number","Other_Punctuation","Other_Symbol",
//"Paragraph_Separator","Private_Use","Punctuation","Separator",
//"Space_Separator","Spacing_Mark","Surrogate","Symbol","Titlecase_Letter",
//"Unassigned","Uppercase_Letter"
//];
var categories = [
"Other","Private_Use","Unassigned","Control","Format","Surrogate",
"Letter","Uppercase_Letter","Lowercase_Letter","Titlecase_Letter","Modifier_Letter","Other_Letter","Cased_Letter",
"Mark","Nonspacing_Mark","Enclosing_Mark","Spacing_Mark",
"Number","Decimal_Number","Letter_Number","Other_Number",
"Separator","Space_Separator","Line_Separator","Paragraph_Separator",
"Punctuation","Dash_Punctuation","Open_Punctuation","Initial_Punctuation","Close_Punctuation","Final_Punctuation","Connector_Punctuation","Other_Punctuation",
"Symbol","Math_Symbol","Currency_Symbol","Modifier_Symbol","Other_Symbol",
];
var abbr = [
"C","Co","Cn","Cc","Cf","Cs",
"L","Lu","Ll","Lt","Lm","Lo","Lc",
"M","Mn","Me","Mc",
"N","Nd","Nl","No",
"Z","Zs","Zl","Zp",
"P","Pd","Ps","Pi","Pe","Pf","Pc","Po",
"S","Sm","Sc","Sk","So"
];
var cats = function() {
var ct = "C";
var aidx = 0;
for (ct of categories)
{
    var cps = require("@unicode/unicode-16.0.0/General_Category/"+ ct + "/code-points");

    var dict, str;
    var dictSrc, strSrc;

    function compress() {
        var cps1 = cps.slice();
        var i = 0;
        var deltas = [];
        while (cps1.length) {
            var j = cps1.shift();
            if(j > 65535) { deltas.push(65535 - i); break; }
            deltas.push(j - i);
            i = j;
            while (cps1[0] === j + 1)
                j = cps1.shift();
            if(j > 65535) { deltas.push(65535 - i); break; }
            deltas.push(j - i);
            i = j;
        }
        var hist = {};
        for (i of deltas)
        {
            hist[i] = (hist[i] || 0) + 1;
        }
        dict = Object.keys(hist).map(Number);
        dict.sort((a, b) => hist[a] !== hist[b] ? hist[b] - hist[a] : a - b);

        dictSrc = JSON.stringify(dict).replace(/\]/, '},').replace(/\[/, '{');
        var bytes = deltas.map(d => dict.indexOf(d) + 0x20);
        strSrc = bytes.map(
            b => b === 0x22 ? '\\"' : b === 0x5c ? '\\\\' : b < 0x7f ?
                String.fromCharCode(b) : "\\x" + b.toString(16)
        ).join("");
        strSrc = '"' + strSrc + '"';
        str = new Function('return ' + strSrc)();
    }

    var decompress = function(d, s) {
        var i, j = 0, c = [], p = [], n = s.length;
        for (i = 0; i < n; ++i)
            c[i] = j = d[s.charCodeAt(i) - 32] + j;
        for (i = 0; i < n; i += 2)
        {
            for (j = c[i]; j <= c[i + 1]; ++j)
                p.push(j);
        }
        return p;
    }

    compress();

}
}
cats();
var cpses = [["IdentifierStart", require("@unicode/unicode-16.0.0/General_Category/Letter/code-points").concat(
                                     require("@unicode/unicode-16.0.0/General_Category/Letter_Number/code-points"),
                                     require("@unicode/unicode-16.0.0/General_Category/Connector_Punctuation/code-points"),
                                     require("@unicode/unicode-16.0.0/General_Category/Currency_Symbol/code-points")
                                     ).sort(function(a,b){return a - b;})],
             ["IdentifierPart", require("@unicode/unicode-16.0.0/General_Category/Letter/code-points").concat(
                                    require("@unicode/unicode-16.0.0/General_Category/Number/code-points"),
                                    require("@unicode/unicode-16.0.0/General_Category/Nonspacing_Mark/code-points"),
                                    require("@unicode/unicode-16.0.0/General_Category/Spacing_Mark/code-points"),
                                    require("@unicode/unicode-16.0.0/General_Category/Connector_Punctuation/code-points"),
                                    require("@unicode/unicode-16.0.0/General_Category/Currency_Symbol/code-points"),
                                    lo.range(0, 9),lo.range(0xE, 0x1C),lo.range(0x7F, 0xA0)).sort(function(a,b){return a - b;})]];
var word = function (cpsGroup){
    /*
    var cps = require("@unicode/unicode-16.0.0/General_Category/Number/code-points").concat(
    require("@unicode/unicode-16.0.0/General_Category/Letter/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Nonspacing_Mark/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Spacing_Mark/code-points"),
    ["_".charCodeAt(0)]).sort(function(a,b){return a - b;});
    */

    //Identifier Start
    /*
    var cps = require("@unicode/unicode-16.0.0/General_Category/Letter/code-points").concat(
    require("@unicode/unicode-16.0.0/General_Category/Letter_Number/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Connector_Punctuation/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Currency_Symbol/code-points")
    ).sort(function(a,b){return a - b;});
    */
    //Identifier Part
    /*
    var cps = require("@unicode/unicode-16.0.0/General_Category/Letter/code-points").concat(
    require("@unicode/unicode-16.0.0/General_Category/Number/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Nonspacing_Mark/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Spacing_Mark/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Connector_Punctuation/code-points"),
    require("@unicode/unicode-16.0.0/General_Category/Currency_Symbol/code-points"),
    lo.range(0, 9),lo.range(0xE, 0x1C),lo.range(0x7F, 0xA0)).sort(function(a,b){return a - b;});
    */
    //Vertical Space
    //var cps = [10, 11, 12, 13, 133, 8232, 8233];
    //var cps = require("@unicode/unicode-16.0.0/General_Category/Space_Separator/code-points").concat(
          //["\t".charCodeAt(0)]).sort(function(a,b){return a - b;});
    //var cps = require("@unicode/unicode-16.0.0/General_Category/Space_Separator/code-points").concat(
    //   ["\t".charCodeAt(0), 10, 11, 12, 13, 133, 8232, 8233]).sort(function(a,b){return a - b;});

    var cps = cpsGroup[1];
    var dict, str;
    var dictSrc, strSrc;

    function compress() {
        var cps1 = cps.slice();
        var i = 0, pushed = 0;
        var deltas = [];
        while (cps1.length) {
            var j = cps1.shift();
            if(j > 65535) { deltas.push(65535 - i); break; } //deltas.push(65535 - i);
            pushed = deltas.push(j - i);
            i = j;
            while (cps1[0] === j + 1)
                j = cps1.shift();
            if(j > 65535) { deltas.push(65535 - i); break; }
            pushed = deltas.push(j - i);
            i = j;
        }
        var hist = {};
        for (i of deltas)
        {
            hist[i] = (hist[i] || 0) + 1;
        }
        dict = Object.keys(hist).map(Number);
        dict.sort((a, b) => hist[a] !== hist[b] ? hist[b] - hist[a] : a - b);
        dictSrc = JSON.stringify(dict).replace(/\]/, '},').replace(/\[/, '{');
        var bytes = deltas.map(d => dict.indexOf(d) + 0x20);
        strSrc = bytes.map(
            b => b === 0x22 ? '\\"' : b === 0x5c ? '\\\\' : b < 0x7f ?
                String.fromCharCode(b) : "\\" + lo.padStart(b.toString(8), 3, '0')
        ).join("");
        strSrc = '"' + strSrc + '"';
        str = new Function('return ' + strSrc)();
    }

    var decompress = function(d, s) {
        var i, j = 0, c = [], p = [], n = s.length;
        for (i = 0; i < n; ++i)
            c[i] = j = d[s.charCodeAt(i) - 32] + j;
        for (i = 0; i < n; i += 2)
        {
            for (j = c[i]; j <= c[i + 1]; ++j)
                p.push(j);
        }
        return p;
    }

    compress();
    var cps2 = decompress(dict, str);

    //console.log("public static final Category IdentifierStart=new Category(new int[]" + dictSrc + strSrc + ");\n");
    console.log("public static final Category "+cpsGroup[0]+"=new Category(new int[]" + dictSrc + strSrc + ");\n");
    //console.log("public static final Category Space=new Category(new int[]" + dictSrc + strSrc + ");\n");
    //console.log("public static final Category Space=new Category(new int[]" + dictSrc + strSrc + ");\n");
}
word(["Word", require("@unicode/unicode-16.0.0/General_Category/Number/code-points").concat(
                  require("@unicode/unicode-16.0.0/General_Category/Letter/code-points"),
                  require("@unicode/unicode-16.0.0/General_Category/Nonspacing_Mark/code-points"),
                  require("@unicode/unicode-16.0.0/General_Category/Spacing_Mark/code-points"),
                  ["_".charCodeAt(0)]).sort(function(a,b){return a - b;})])
for(cps of cpses)
{
    word(cps);
}
var cases = function()
{
    //console.log(cps);
    //console.log(typeof cps);
    var keySrc = JSON.stringify(lo.union(Array.from(require('@unicode/unicode-16.0.0/Case_Folding/C/symbols.js').keys()), Array.from(require('@unicode/unicode-16.0.0/Case_Folding/S/symbols.js').keys())));
    //console.log(lo.keys(cps));
    keySrc = keySrc.replace(/["]/g, "'").replace(/\]/, '},').replace(/\[/, '{');
    var valSrc = JSON.stringify(Array.from(require('@unicode/unicode-16.0.0/Case_Folding/C/symbols.js').values()).concat(Array.from(require('@unicode/unicode-16.0.0/Case_Folding/S/symbols.js').values())));
    //console.log(valSrc.length);
    valSrc = valSrc.replace(/["]/g, "'").replace(/\]/, '}').replace(/\[/, '{');

    console.log('static final CharCharMap cases = new CharCharMap(new int[]'+keySrc+'new int[]'+valSrc+');\n');
}
cases();

var brackets = function()
{
    var starts = require('@unicode/unicode-16.0.0/General_Category/Open_Punctuation/symbols');
    var ends   = require('@unicode/unicode-16.0.0/General_Category/Close_Punctuation/symbols');
    var startStr = JSON.stringify(starts).replace(/["]/g, "'").replace(/[\r\n]+/g, '');
    var endStr   = JSON.stringify(ends).replace(/["]/g, "'").replace(/[\r\n]+/g, '');

    console.log(startStr);
    console.log(endStr);
}
brackets();