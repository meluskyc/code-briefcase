'use strict';

var express = require('express');
var controller = require('./tag.controller');

var router = express.Router();

var all = [];
var a = {name:'Java'};
var b = {name:'C'};
var c = {name:'C++'};
var d = {name:'C#'};
var e = {name:'Python'};
var f = {name:'Ruby'};
var g = {name:'Perl'};
var h = {name:'JavaScript'};
var i = {name:'VB'};
var j = {name:'ActionScript'};

all.push(a);
all.push(b);
all.push(c);
all.push(d);
all.push(e);
all.push(f);
all.push(g);
all.push(h);
all.push(i);
all.push(j);

/* GET all tags  */
router.get('/', function(req, res) {
  res.json(all);
});

module.exports = router;
