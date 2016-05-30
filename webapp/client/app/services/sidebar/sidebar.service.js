'use strict';

angular.module('codebriefcaseApp')
  .factory('Sidebar', function (Item) {
    var Sidebar = {};
    var tags = {list:[]};

    Sidebar.filter = {
      tag_primary: '',
      starred: 0
    };

    Sidebar.refreshTags = function() {
      Item.tagsDistinct(function(result) {
        tags.list = result;
        console.log(result);
        console.log(tags.list);
        return result;
      });
    };

    Sidebar.tags = tags.list;

    return Sidebar;
  });
