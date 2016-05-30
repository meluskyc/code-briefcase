'use strict';

angular.module('codebriefcaseApp')
  .controller('AddCtrl', function ($scope, $mdToast, $state, Tag, Fab, Item, Sidebar) {
    Fab.visible = false;
    $scope.loaded = false;
    $scope.tags = [];

    $scope.item = new Item();
    $scope.item.description = '';
    $scope.item.tag_primary = '';
    $scope.item.tag_secondary = '';
    $scope.item.content = '*******\nENTER YOUR CODE HERE\n*******';

    $scope.tags = Tag.query(function() {
      $scope.loaded = true;
    },
    function() {
      $mdToast.show(
        $mdToast.simple()
          .textContent('Unable to retrieve tags. Check connection.')
          .hideDelay(10000)
      );
    });

    $scope.goBack = function () {
      $state.go('List');
    };

    $scope.submitItem = function () {
      $scope.item.$save(function() {
        $state.go('List');
        $mdToast.show($mdToast.simple().textContent('Added').hideDelay(3000));
        Item.tagsDistinct(function(result) {
          angular.copy(result, Sidebar.tags);
        });
      }, function() {
        $mdToast.show($mdToast.simple()
          .textContent('Unable to add. Check connection')
          .hideDelay(3000));
      });
    };

    $scope.uiAceConfig = {
      useWrapMode : true,
      showGutter: true,
      advanced: {
        fontSize: '16px'
      },
      onLoad: function (_editor) {
        // This is to remove following warning message on console:
        // Automatically scrolling cursor into view after selection change this will be disabled in the next version
        // set editor.$blockScrolling = Infinity to disable this message
        _editor.$blockScrolling = Infinity;
      }
    };

  });
