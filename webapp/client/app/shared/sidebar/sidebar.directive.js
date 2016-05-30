'use strict';

angular.module('codebriefcaseApp')
  .directive('sidebar', function () {
    return {
      templateUrl: 'app/shared/sidebar/sidebar.html',
      restrict: 'EA'
    };
  });
