// Compress list of all Unicode letters to less than 2kB
// Credit goes to gagern, https://gist.github.com/gagern/89db1179766a702c564d
var _ = require('lodash');

var categories = [
"Cased_Letter","Close_Punctuation","Connector_Punctuation","Control",
"Currency_Symbol","Dash_Punctuation","Decimal_Number","Enclosing_Mark",
"Final_Punctuation","Format","Initial_Punctuation","Letter","Letter_Number",
"Line_Separator","Lowercase_Letter","Mark","Math_Symbol","Modifier_Letter",
"Modifier_Symbol","Nonspacing_Mark","Number","Open_Punctuation","Other",
"Other_Letter","Other_Number","Other_Punctuation","Other_Symbol",
"Paragraph_Separator","Private_Use","Punctuation","Separator",
"Space_Separator","Spacing_Mark","Surrogate","Symbol","Titlecase_Letter",
"Unassigned","Uppercase_Letter"
/*
"C","Co","Cn","Cc","Cf","Cs",
"L","Lu","Ll","Lt","Lm","Lo",
"M","Mn","Me","Mc",
"N","Nd","Nl","No",
"Z","Zs","Zl","Zp",
"P","Pd","Ps","Pi","Pe","Pf","Pc","Po",
"S","Sm","Sc","Sk","So"
*/
];
var cats = function() {
var ct = "C";
for (ct of categories)
{
    var cps = require("unicode-11.0.0/General_Category/"+ ct + "/code-points");

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
        //console.log(JSON.stringify(deltas));
        var hist = {};
        for (i of deltas)
        {
            hist[i] = (hist[i] || 0) + 1;
        }
        dict = Object.keys(hist).map(Number);
        dict.sort((a, b) => hist[a] !== hist[b] ? hist[b] - hist[a] : a - b);

        dictSrc = JSON.stringify(dict).replace(/\]/, '},').replace(/\[/, '{');
        //dictSrc = dictSrc.replace(/.{70,78},/g, '$&\n');
        //console.log(dictSrc);
        var bytes = deltas.map(d => dict.indexOf(d) + 0x20);
        strSrc = bytes.map(
            b => b === 0x22 ? '\\"' : b === 0x5c ? '\\\\' : b < 0x7f ?
                String.fromCharCode(b) : "\\x" + b.toString(16)
        ).join("");
        //strSrc = strSrc.replace(/.{70,73}[^\\]{3}/g, '$&"+\n"');
        strSrc = '"' + strSrc + '"';
        //console.log(strSrc);
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
    //var cps2 = decompress(dict, str);
    //console.log(ct + " official length = " + cps.length);
    //console.log(ct + " our length = " + cps2.length);
    //if (cps.join() !== cps2.join())
    //    throw error("Mismatch!");

    //var res = 'letters=(' +
    //    decompress.toString().replace(/(\W)\s+|\s+(?=\W)/g, '$1') +
    //    ')(' + dictSrc + ',' + strSrc + ');';
    //console.log(res);

    console.log("public static final Category " + ct + "=new Category(new int[]" + dictSrc + strSrc + ");\n");
}
}
//cats();
var cpses = [["IdentifierStart", require("unicode-11.0.0/General_Category/Letter/code-points").concat(
                                     require("unicode-11.0.0/General_Category/Letter_Number/code-points"),
                                     require("unicode-11.0.0/General_Category/Connector_Punctuation/code-points"),
                                     require("unicode-11.0.0/General_Category/Currency_Symbol/code-points")
                                     ).sort(function(a,b){return a - b;})],
             ["IdentifierPart", require("unicode-11.0.0/General_Category/Letter/code-points").concat(
                                    require("unicode-11.0.0/General_Category/Number/code-points"),
                                    require("unicode-11.0.0/General_Category/Nonspacing_Mark/code-points"),
                                    require("unicode-11.0.0/General_Category/Spacing_Mark/code-points"),
                                    require("unicode-11.0.0/General_Category/Connector_Punctuation/code-points"),
                                    require("unicode-11.0.0/General_Category/Currency_Symbol/code-points"),
                                    _.range(0, 9),_.range(0xE, 0x1C),_.range(0x7F, 0xA0)).sort(function(a,b){return a - b;})]];
var word = function (cpsGroup){
    /*
    var cps = require("unicode-11.0.0/General_Category/Number/code-points").concat(
    require("unicode-11.0.0/General_Category/Letter/code-points"),
    require("unicode-11.0.0/General_Category/Nonspacing_Mark/code-points"),
    require("unicode-11.0.0/General_Category/Spacing_Mark/code-points"),
    ["_".charCodeAt(0)]).sort(function(a,b){return a - b;});
    */

    //Identifier Start
    /*
    var cps = require("unicode-11.0.0/General_Category/Letter/code-points").concat(
    require("unicode-11.0.0/General_Category/Letter_Number/code-points"),
    require("unicode-11.0.0/General_Category/Connector_Punctuation/code-points"),
    require("unicode-11.0.0/General_Category/Currency_Symbol/code-points")
    ).sort(function(a,b){return a - b;});
    */
    //Identifier Part
    /*
    var cps = require("unicode-11.0.0/General_Category/Letter/code-points").concat(
    require("unicode-11.0.0/General_Category/Number/code-points"),
    require("unicode-11.0.0/General_Category/Nonspacing_Mark/code-points"),
    require("unicode-11.0.0/General_Category/Spacing_Mark/code-points"),
    require("unicode-11.0.0/General_Category/Connector_Punctuation/code-points"),
    require("unicode-11.0.0/General_Category/Currency_Symbol/code-points"),
    _.range(0, 9),_.range(0xE, 0x1C),_.range(0x7F, 0xA0)).sort(function(a,b){return a - b;});
    */
    //Vertical Space
    //var cps = [10, 11, 12, 13, 133, 8232, 8233];
    //var cps = require("unicode-11.0.0/General_Category/Space_Separator/code-points").concat(
          //["\t".charCodeAt(0)]).sort(function(a,b){return a - b;});
    //var cps = require("unicode-11.0.0/General_Category/Space_Separator/code-points").concat(
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
            //console.log(deltas[pushed - 1]);
            i = j;
            while (cps1[0] === j + 1)
                j = cps1.shift();
            if(j > 65535) { deltas.push(65535 - i); break; }
            pushed = deltas.push(j - i);
            //console.log(deltas[pushed - 1]);
            i = j;
        }
        //console.log(JSON.stringify(deltas));
        var hist = {};
        for (i of deltas)
        {
            hist[i] = (hist[i] || 0) + 1;
        }
        dict = Object.keys(hist).map(Number);
        dict.sort((a, b) => hist[a] !== hist[b] ? hist[b] - hist[a] : a - b);
/*        var cr = 0, dl = dict.length - 1;
        console.log(dict[0x5e]);
        if(dl > 0x5e) dl = 0x5e;

        if(dict[0x02] != null)
        {
            dict.splice(0x02, 0, dict[dl]); // "
            ++cr;
        }
        if(dict[0x3c] != null)
        {
            dict.splice(0x3c, 0, dict[dl]); // \
            ++cr;
        }

        dict.splice(dl+1, cr);
*/
        dictSrc = JSON.stringify(dict).replace(/\]/, '},').replace(/\[/, '{');
        //dictSrc = dictSrc.replace(/.{70,78},/g, '$&\n');
        //console.log(dictSrc);
        var bytes = deltas.map(d => dict.indexOf(d) + 0x20);
        //console.log(bytes);
        strSrc = bytes.map(
            b => b === 0x22 ? '\\"' : b === 0x5c ? '\\\\' : b < 0x7f ?
                String.fromCharCode(b) : "\\" + _.padStart(b.toString(8), 3, '0')
        ).join("");
        //strSrc = strSrc.replace(/.{70,73}[^\\]{3}/g, '$&"+\n"');
        strSrc = '"' + strSrc + '"';
        //console.log(strSrc);
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
    //console.log(ct + " official length = " + cps.length);
    //console.log(ct + " our length = " + cps2.length);
    //if (cps.join() !== cps2.join())
    //    throw error("Mismatch!");

    //var res = 'letters=(' +
    //    decompress.toString().replace(/(\W)\s+|\s+(?=\W)/g, '$1') +
    //    ')(' + dictSrc + ',' + strSrc + ');';
    //console.log(res);

    //console.log("public static final Category IdentifierStart=new Category(new int[]" + dictSrc + strSrc + ");\n");
    console.log("public static final Category "+cpsGroup[0]+"=new Category(new int[]" + dictSrc + strSrc + ");\n");
    //console.log("public static final Category Space=new Category(new int[]" + dictSrc + strSrc + ");\n");
    //console.log("public static final Category Space=new Category(new int[]" + dictSrc + strSrc + ");\n");
}
word(["Word", require("unicode-11.0.0/General_Category/Number/code-points").concat(
                  require("unicode-11.0.0/General_Category/Letter/code-points"),
                  require("unicode-11.0.0/General_Category/Nonspacing_Mark/code-points"),
                  require("unicode-11.0.0/General_Category/Spacing_Mark/code-points"),
                  ["_".charCodeAt(0)]).sort(function(a,b){return a - b;})])
for(cps of cpses)
{
//    word(cps);
}
var cases = function()
{
    //console.log(cps);
    //console.log(typeof cps);
    var keySrc = JSON.stringify(_.union(Array.from(require('unicode-11.0.0/Case_Folding/C/symbols.js').keys()), Array.from(require('unicode-11.0.0/Case_Folding/S/symbols.js').keys())));
    //console.log(_.keys(cps));
    keySrc = keySrc.replace(/["]/g, "'").replace(/\]/, '},').replace(/\[/, '{');
    var valSrc = JSON.stringify(Array.from(require('unicode-11.0.0/Case_Folding/C/symbols.js').values()).concat(Array.from(require('unicode-11.0.0/Case_Folding/S/symbols.js').values())));
    //console.log(valSrc.length);
    valSrc = valSrc.replace(/["]/g, "'").replace(/\]/, '}').replace(/\[/, '{');

    console.log('static final CharCharMap cases = new CharCharMap(new int[]'+keySrc+'new int[]'+valSrc+');\n');
}
//cases();

var brackets = function()
{
    var starts = require('unicode-11.0.0/General_Category/Open_Punctuation/symbols');
    var ends   = require('unicode-11.0.0/General_Category/Close_Punctuation/symbols');
    var startStr = JSON.stringify(starts).replace(/["]/g, "'").replace(/[\r\n]+/g, '');
    var endStr   = JSON.stringify(ends).replace(/["]/g, "'").replace(/[\r\n]+/g, '');

    console.log(startStr);
    console.log(endStr);
}
//brackets();