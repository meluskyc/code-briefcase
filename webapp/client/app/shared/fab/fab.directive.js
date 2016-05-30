'use strict';

angular.module('codebriefcaseApp')
  .directive('fab', function () {
    return {
      templateUrl: 'app/shared/fab/fab.html',
      restrict: 'EA'
    };
  });
