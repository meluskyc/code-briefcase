'use strict';

angular.module('codebriefcaseApp')
  .controller('DetailCtrl', function ($scope, $mdToast, $state, $stateParams, Item, Fab, Tag, Sidebar) {
    Fab.visible = false;
    $scope.loaded = false;
    $scope.tags = [];

    $scope.item = Item.get({_id : $stateParams._id},
      function() {
        $scope.uiAceConfig.mode = $scope.item.ace_mode;
        $scope.tags = Tag.query(function() {
          $scope.loaded = true;
        }, function() {
          $mdToast.show(
            $mdToast.simple()
              .textContent('Unable to retrieve tags. Check connection')
              .hideDelay(10000)
          );
        });
      });

    $scope.goBack = function () {
      $state.go('List');
    };

    $scope.updateItem = function () {
      Item.update({_id:$stateParams._id}, $scope.item, function() {
        $state.go('List');
        $mdToast.show(
          $mdToast.simple()
            .textContent('Updated')
            .hideDelay(3000)
        );
        Item.tagsDistinct(function(result) {
          angular.copy(result, Sidebar.tags);
        });
      }, function() {
        $mdToast.show(
          $mdToast.simple()
            .textContent('Unable to update. Check connection.')
            .hideDelay(10000)
        );
      });
    };

    $scope.deleteItem = function (item) {
      Item.delete({_id : item._id}, function() {
        $state.go('List');
        $mdToast.show(
          $mdToast.simple()
            .textContent('Deleted')
            .hideDelay(3000)
        );
        Item.tagsDistinct(function(result) {
          angular.copy(result, Sidebar.tags);
        });
      }, function() {
        $mdToast.show(
          $mdToast.simple()
            .textContent('Unable to delete. Check connection.')
            .hideDelay(10000)
        );
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
