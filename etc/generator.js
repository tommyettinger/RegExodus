// Compress list of all Unicode letters to less than 2kB
// Credit goes to gagern, https://gist.github.com/gagern/89db1179766a702c564d
var _ = require('lodash');

var categories = [
"C","Co","Cn","Cc","Cf","Cs",
"L","Lu","Ll","Lt","Lm","Lo",
"M","Mn","Me","Mc",
"N","Nd","Nl","No",
"Z","Zs","Zl","Zp",
"P","Pd","Ps","Pi","Pe","Pf","Pc","Po",
"S","Sm","Sc","Sk","So"
];
/*
var ct = "C";
for (ct of categories)
{
    var cps = require("unicode-8.0.0/categories/"+ ct + "/code-points");

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
    var cps2 = decompress(dict, str);
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
*/
var word = function (){
    var cps = require("unicode-8.0.0/categories/N/code-points").concat(
    require("unicode-8.0.0/categories/L/code-points"),
    require("unicode-8.0.0/categories/Mn/code-points"),
    require("unicode-8.0.0/categories/Mc/code-points"),
    ["_".charCodeAt(0)]).sort(function(a,b){return a - b;});
    /*
    //Identifier
    var cps = require("unicode-8.0.0/categories/L/code-points").concat(
    require("unicode-8.0.0/categories/N/code-points"),
    require("unicode-8.0.0/categories/Mn/code-points"),
    require("unicode-8.0.0/categories/Mc/code-points"),
    require("unicode-8.0.0/categories/Pc/code-points"),
    require("unicode-8.0.0/categories/Sc/code-points")
    ).sort(function(a,b){return a - b;});
    */

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
    var cps2 = decompress(dict, str);
    //console.log(ct + " official length = " + cps.length);
    //console.log(ct + " our length = " + cps2.length);
    //if (cps.join() !== cps2.join())
    //    throw error("Mismatch!");

    //var res = 'letters=(' +
    //    decompress.toString().replace(/(\W)\s+|\s+(?=\W)/g, '$1') +
    //    ')(' + dictSrc + ',' + strSrc + ');';
    //console.log(res);

    console.log("public static final Category Word=new Category(new int[]" + dictSrc + strSrc + ");\n");
}
//word();
var cases = function()
{
    var cps = _.merge(
    require('unicode-8.0.0/case-folding/C/symbols'),
    require('unicode-8.0.0/case-folding/S/symbols'));
    var keySrc = JSON.stringify(_.keys(cps)).replace(/["]/g, "'").replace(/\]/, '},').replace(/\[/, '{');
    var valSrc = JSON.stringify(_.values(cps)).replace(/["]/g, "'").replace(/\]/, '}').replace(/\[/, '{');

    console.log('static final CharCharMap cases=newCharCharMap(new int[]'+keySrc+'new int[]'+valSrc+');\n');
}
cases();