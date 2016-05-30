'use strict';

angular.module('codebriefcaseApp')
  .controller('SidebarCtrl', function ($scope, Item, Sidebar) {
    $scope.tags = Sidebar.tags;
    Item.tagsDistinct(function(result) {
      Sidebar.tags = result;
      $scope.tags = Sidebar.tags;
    });
    $scope.filter = Sidebar.filter;
    $scope.filter.tag_primary = undefined;
  });
