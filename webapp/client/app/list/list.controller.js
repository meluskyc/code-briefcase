'use strict';

angular.module('codebriefcaseApp')
  .controller('ListCtrl', function ($scope, $mdToast, $state, $mdSidenav, Item, Sidebar, Fab) {
    $scope.state = $state;
    $scope.filters = Sidebar.filter;
    $scope.items = [];

    Fab.visible = true;
    Fab.action = function () {
      $state.go('Add');
    };

    $scope.filter = function(item) {
        /* jshint laxbreak: true */
        return (item.starred === '1' && $scope.filters.starred)
          || (item.tag_primary === $scope.filters.tag_primary)
          || (!$scope.filters.tag_primary && !$scope.filters.starred);
    };

    $scope.items = Item.query({}, function() {}, function() {
      $mdToast.show(
        $mdToast.simple()
          .textContent('Check connection')
          .hideDelay(10000)
      );
    });

    $scope.openMenu = function () {
      $mdSidenav('left').toggle();
    };

    $scope.goToDetail = function () {
      $state.go('Detail', { _id: this.item._id});
    };

    $scope.convertTime = function (time) {
      return moment(time, 'x').fromNow();
    };

    $scope.star = function (item) {
      item.starred = (item.starred === '1') ? '0' : '1';

      Item.update({_id : item._id}, item, function() {}, function() {
        $mdToast.show(
          $mdToast.simple()
            .textContent('Unable to update. Check connection.')
            .hideDelay(10000)
        );
      });
    };
  });
