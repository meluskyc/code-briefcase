'use strict';

angular.module('codebriefcaseApp')
  .controller('FabCtrl', function ($scope, $state, Fab) {
    $scope.fab = Fab;
  });
