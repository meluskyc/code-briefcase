'use strict';

angular.module('codebriefcaseApp')
  .factory('Tag', function ($resource) {
    return $resource('/api/tags');
  });
