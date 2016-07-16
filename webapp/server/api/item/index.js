'use strict';

var express = require('express');
var controller = require('./item.controller');
var router = express.Router();

var tags = [];
var one = {tag_primary:'Java'};
var two = {tag_primary:'C'};
var three = {tag_primary:'C++'};
var four = {tag_primary:'C#'};
var five = {tag_primary:'Python'};
tags.push(one);
tags.push(two);
tags.push(three);
tags.push(four);
tags.push(five);

var items = [];
var one = {_id:1, description:'aaaaaa', content:'111111', tag_primary:'Java', date_updated:1458518971173, color:'#FF7043',
  ace_mode:'java', starred:'0'};
var two = {_id:2, description:'bbbbbb', content:'2222222', tag_primary:'C++', date_updated:1458518968173, color:'#C62828',
  ace_mode:'c_cpp', starred:'1'};
var three = {_id:3, description:'cccccc', content:'333333', tag_primary:'C', date_updated:1458518921173, color:'#FF5722',
  ace_mode:'c_cpp', starred:'0'};
var four = {_id:4, description:'dddddddd', content:'444444', tag_primary:'C#', date_updated:1458518920173, color:'#A4B42B',
  ace_mode:'csharp', starred:'1'};
var five = {_id:5, description:'eeeeeeeee', content:'5555555', tag_primary:'Java', date_updated:1458518955173, tag_secondary:'abc',
  color:'#FFCA28',ace_mode:'java', starred:'0'};
var six = {_id:6, description:'fffffff', content:'6666666', tag_primary:'Python', date_updated:1458518844173, color: '#C62828',
  ace_mode:'python', starred:'1'};
var seven = {_id:7, description:'aaaaaa', content:'111111', tag_primary:'Java', date_updated:1458518932173, color: '#283593',
  ace_mode:'java', starred:'0'};
var eight = {_id:8, description:'bbbbbb', content:'2222222', tag_primary:'C++', date_updated:1458518911173, color: '#00838F',
  ace_mode:'c_cpp',  starred:'1'};
var nine = {_id:9, description:'cccccc', content:'333333', tag_primary:'C', date_updated:1458518909173, color: '#616161',
  ace_mode:'c_cpp', starred:'0'};
var ten = {_id:10, description:'dddddddd', content:'444444', tag_primary:'C#', date_updated:1458518992173, color:'#F4511E',
  ace_mode:'csharp', starred:'0'};
var eleven = {_id:11, description:'eeeeeeeee', content:'5555555', tag_primary:'Java', date_updated:1458518932173, color: '#26C6DA',
  ace_mode:'java', starred:'0'};
var twelve = {_id:12, description:'fffffff', content:'6666666', tag_primary:'Python', date_updated:1458518932173, color: '#212121',
  ace_mode:'python', starred:'0'};
items.push(one);
items.push(two);
items.push(three);
items.push(four);
items.push(five);
items.push(six);
items.push(seven);
items.push(eight);
items.push(nine);
items.push(ten);
items.push(eleven);

router.route('/')
  .get(function (req, res) {
    res.json(items);
  })
  .post(function (req, res) {
    var item = {};
    item._id = items[items.length - 1]._id + 1;
    item.description = req.body.description;
    item.tag_primary = req.body.tag_primary;
    item.tag_secondary = req.body.tag_secondary;
    item.content = req.body.content;
    item.color = '#0D47A1';
    item.ace_mode = 'text';
    item.starred = 0;
    items.push(item);

    res.json(item);
  });

router.route('/tags')
  .get(function (req, res) {
    res.json(tags);
  });

router.route('/:_id')
  .get(function (req, res) {
    for (var i = 0; i < items.length; i++) {
      if (items[i]._id === Number(req.params._id)) {
        res.json(items[i]);
        break;
      }
    }
  })
  .put(function (req, res) {
    for (var i = 0; i < items.length; i++) {
      if (items[i]._id === Number(req.params._id)) {
        if (req.body.hasOwnProperty("description")) {
          items[i].description = req.body.description;
        }
        if (req.body.hasOwnProperty("tag_primary")) {
          items[i].tag_primary = req.body.tag_primary;
        }
        if (req.body.hasOwnProperty("tag_secondary")) {
          items[i].tag_secondary = req.body.tag_secondary;
        }
        if (req.body.hasOwnProperty("content")) {
          items[i].content = req.body.content;
        }
        if (req.body.hasOwnProperty("starred")) {
          items[i].starred = req.body.starred;
        }
        res.json(items[i]);
        break;
      }
    }
  })
  .delete(function (req, res) {
    for (var i = 0; i < items.length; i++) {
      if (items[i]._id === Number(req.params._id)) {
        items.splice(i, 1);
        res.json(items[i]);
        break;
      }
    }
  });

module.exports = router;
